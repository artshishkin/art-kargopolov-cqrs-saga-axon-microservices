package net.shyshkin.study.cqrs.estore.orderservice.saga;

import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.estore.core.commands.ReserveProductCommand;
import net.shyshkin.study.cqrs.estore.orderservice.core.events.OrderCreatedEvent;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.CommandResultMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

@Saga
@Slf4j
public class OrderSaga {

    @Autowired
    private transient CommandGateway commandGateway;

    @StartSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderCreatedEvent orderCreatedEvent) {

        ReserveProductCommand reserveProductCommand = ReserveProductCommand.builder()
                .productId(orderCreatedEvent.getProductId())
                .orderId(orderCreatedEvent.getOrderId())
                .quantity(orderCreatedEvent.getQuantity())
                .userId(orderCreatedEvent.getUserId())
                .build();

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
}
