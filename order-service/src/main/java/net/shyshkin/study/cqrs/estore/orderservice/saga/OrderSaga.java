package net.shyshkin.study.cqrs.estore.orderservice.saga;

import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.estore.core.commands.ProcessPaymentCommand;
import net.shyshkin.study.cqrs.estore.core.commands.ReserveProductCommand;
import net.shyshkin.study.cqrs.estore.core.events.PaymentProcessedEvent;
import net.shyshkin.study.cqrs.estore.core.events.ProductReservedEvent;
import net.shyshkin.study.cqrs.estore.core.model.User;
import net.shyshkin.study.cqrs.estore.core.query.FetchUserPaymentDetailsQuery;
import net.shyshkin.study.cqrs.estore.orderservice.command.ApproveOrderCommand;
import net.shyshkin.study.cqrs.estore.orderservice.core.events.OrderApprovedEvent;
import net.shyshkin.study.cqrs.estore.orderservice.core.events.OrderCreatedEvent;
import net.shyshkin.study.cqrs.estore.orderservice.core.mapper.OrderMapper;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.CommandResultMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

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

        ProcessPaymentCommand processPaymentCommand = ProcessPaymentCommand
                .builder()
                .paymentId(UUID.randomUUID())
                .orderId(productReservedEvent.getOrderId())
                .paymentDetails(user.getPaymentDetails())
                .build();

        Object result = null;
        try {
            result = commandGateway.sendAndWait(processPaymentCommand, 10, TimeUnit.SECONDS);
        } catch (Exception ex) {
            log.error("There was an Exception: {}:{}", ex.getClass().getName(), ex.getMessage());
            // TODO: 25.05.2021 Start compensating transaction
            return;
        }

        if (result == null) {
            // TODO: 25.05.2021 Start compensating transaction
            log.debug("Result of Process Payment Command is NULL. Starting compensating transaction");
        }
        log.debug("Process Payment Command result: {}", result);
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(PaymentProcessedEvent paymentProcessedEvent) {

        log.debug("PaymentProcessedEvent is handled: {}", paymentProcessedEvent);

        ApproveOrderCommand approveOrderCommand = new ApproveOrderCommand(paymentProcessedEvent.getOrderId());
        commandGateway.send(approveOrderCommand);

    }

    @EndSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderApprovedEvent orderApprovedEvent) {

        log.debug("OrderApprovedEvent is handled: {}", orderApprovedEvent);
        log.debug("OrderSaga if competed for order with Id: {}", orderApprovedEvent.getOrderId());

//        SagaLifecycle.end(); //for programmatically end Saga instead of @EndSaga annotation

    }

}
