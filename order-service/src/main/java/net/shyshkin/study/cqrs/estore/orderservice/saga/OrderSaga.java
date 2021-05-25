package net.shyshkin.study.cqrs.estore.orderservice.saga;

import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.estore.core.commands.ReserveProductCommand;
import net.shyshkin.study.cqrs.estore.core.events.ProductReservedEvent;
import net.shyshkin.study.cqrs.estore.core.model.User;
import net.shyshkin.study.cqrs.estore.core.query.FetchUserPaymentDetailsQuery;
import net.shyshkin.study.cqrs.estore.orderservice.core.events.OrderCreatedEvent;
import net.shyshkin.study.cqrs.estore.orderservice.core.mapper.OrderMapper;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.CommandResultMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

@Saga
@Slf4j
public class OrderSaga {

    private transient CommandGateway commandGateway;
    private transient QueryGateway queryGateway;
    private transient OrderMapper mapper;

    @Autowired
    public void setCommandGateway(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    @Autowired
    public void setQueryGateway(QueryGateway queryGateway) {
        this.queryGateway = queryGateway;
    }

    @Autowired
    public void setMapper(OrderMapper mapper) {
        this.mapper = mapper;
    }

    @StartSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderCreatedEvent orderCreatedEvent) {

        ReserveProductCommand reserveProductCommand = mapper.toReserveProductCommand(orderCreatedEvent);

        log.debug("OrderCreatedEvent is handled for orderId: {} and productId: {}",
                orderCreatedEvent.getOrderId(), orderCreatedEvent.getProductId());

        commandGateway.send(reserveProductCommand, new CommandCallback<ReserveProductCommand, Object>() {
            @Override
            public void onResult(CommandMessage<? extends ReserveProductCommand> commandMessage, CommandResultMessage<?> commandResultMessage) {
                log.debug("command message: `{}`, result: `{}`", commandMessage, commandResultMessage);
                if (commandResultMessage.isExceptional()) {
                    //start compensating transaction
                    Throwable throwable = commandResultMessage.exceptionResult();
                    log.debug("There was an Exception: {}:{}", throwable.getClass().getName(), throwable.getMessage());
                }
            }
        });
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(ProductReservedEvent productReservedEvent) {

        log.debug("ProductReservedEvent is handled: {}", productReservedEvent);

        // Process user's payment
        FetchUserPaymentDetailsQuery query = new FetchUserPaymentDetailsQuery(productReservedEvent.getUserId());
        User user;
        try {
            user = queryGateway.query(query, User.class).join();
        } catch (Exception ex) {
            log.error("Exception during fetching user details {}:{}", ex.getClass().getName(), ex.getMessage());
            // TODO: 25.05.2021 Start compensating transaction
            return;
        }
        if (user == null) {
            // TODO: 25.05.2021 Start compensating transaction
            return;
        }
        log.debug("Successfully fetched payment details: {}", user);
    }
}
