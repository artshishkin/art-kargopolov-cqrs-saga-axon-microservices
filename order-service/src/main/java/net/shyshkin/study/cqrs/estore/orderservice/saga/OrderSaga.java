package net.shyshkin.study.cqrs.estore.orderservice.saga;

import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.estore.core.commands.CancelProductReservationCommand;
import net.shyshkin.study.cqrs.estore.core.commands.ProcessPaymentCommand;
import net.shyshkin.study.cqrs.estore.core.commands.ReserveProductCommand;
import net.shyshkin.study.cqrs.estore.core.events.PaymentProcessedEvent;
import net.shyshkin.study.cqrs.estore.core.events.ProductReservationCancelledEvent;
import net.shyshkin.study.cqrs.estore.core.events.ProductReservedEvent;
import net.shyshkin.study.cqrs.estore.core.model.User;
import net.shyshkin.study.cqrs.estore.core.query.FetchUserPaymentDetailsQuery;
import net.shyshkin.study.cqrs.estore.orderservice.command.ApproveOrderCommand;
import net.shyshkin.study.cqrs.estore.orderservice.command.RejectOrderCommand;
import net.shyshkin.study.cqrs.estore.orderservice.core.events.OrderApprovedEvent;
import net.shyshkin.study.cqrs.estore.orderservice.core.events.OrderCreatedEvent;
import net.shyshkin.study.cqrs.estore.orderservice.core.events.OrderRejectedEvent;
import net.shyshkin.study.cqrs.estore.orderservice.core.mapper.OrderMapper;
import net.shyshkin.study.cqrs.estore.orderservice.core.model.OrderSummary;
import net.shyshkin.study.cqrs.estore.orderservice.query.FindOrderQuery;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.CommandResultMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.deadline.annotation.DeadlineHandler;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;
import java.util.UUID;

@Saga
@Slf4j
public class OrderSaga {

    static final String PAYMENT_PROCESSING_DEADLINE = "payment-processing-deadline";

    private transient CommandGateway commandGateway;
    private transient QueryGateway queryGateway;
    private transient OrderMapper mapper;
    private transient DeadlineManager deadlineManager;
    private transient QueryUpdateEmitter queryUpdateEmitter;

    @Value("${app.testing.deadline:false}")
    private boolean isDeadlineTesting;
    private String scheduleId;

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

    @Autowired
    public void setDeadlineManager(DeadlineManager deadlineManager) {
        this.deadlineManager = deadlineManager;
    }

    @Autowired
    public void setQueryUpdateEmitter(QueryUpdateEmitter queryUpdateEmitter) {
        this.queryUpdateEmitter = queryUpdateEmitter;
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

                    RejectOrderCommand rejectOrderCommand = RejectOrderCommand.builder()
                            .orderId(reserveProductCommand.getOrderId())
                            .reason(throwable.getMessage())
                            .build();

                    commandGateway.send(rejectOrderCommand);
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
            // Start compensating transaction
            cancelProductReservation(productReservedEvent, ex.getMessage());
            return;
        }
        if (user == null) {
            // Start compensating transaction
            cancelProductReservation(productReservedEvent, "Can not fetch payment details for user " + productReservedEvent.getUserId());
            return;
        }
        log.debug("Successfully fetched payment details: {}", user);

        scheduleId = deadlineManager.schedule(
                Duration.ofSeconds(4),
                PAYMENT_PROCESSING_DEADLINE,
                productReservedEvent);

        // FAKE condition for Deadline testing
        if (isDeadlineTesting) return;

        ProcessPaymentCommand processPaymentCommand = ProcessPaymentCommand
                .builder()
                .paymentId(UUID.randomUUID())
                .orderId(productReservedEvent.getOrderId())
                .paymentDetails(user.getPaymentDetails())
                .build();

        Object result = null;
        try {
            result = commandGateway.sendAndWait(processPaymentCommand);
        } catch (Exception ex) {
            log.error("There was an Exception: {}:{}", ex.getClass().getName(), ex.getMessage());
            // Start compensating transaction
            cancelProductReservation(productReservedEvent, "Can not process payment: " + ex.getLocalizedMessage());
            return;
        }

        if (result == null) {
            log.debug("Result of Process Payment Command is NULL. Starting compensating transaction");
            // Start compensating transaction
            cancelProductReservation(productReservedEvent, "Could not process user payment with provided user details");
            return;
        }
        log.debug("Process Payment Command result: {}", result);
    }

    private void cancelProductReservation(ProductReservedEvent productReservedEvent, String reason) {

        cancelDeadline();

        CancelProductReservationCommand command = CancelProductReservationCommand.builder()
                .orderId(productReservedEvent.getOrderId())
                .productId(productReservedEvent.getProductId())
                .userId(productReservedEvent.getUserId())
                .quantity(productReservedEvent.getQuantity())
                .reason(reason)
                .build();

        commandGateway.send(command);
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(PaymentProcessedEvent paymentProcessedEvent) {

        cancelDeadline();

        log.debug("PaymentProcessedEvent is handled: {}", paymentProcessedEvent);

        ApproveOrderCommand approveOrderCommand = new ApproveOrderCommand(paymentProcessedEvent.getOrderId());
        commandGateway.send(approveOrderCommand);

    }

    private void cancelDeadline() {
//        deadlineManager.cancelAll(PAYMENT_PROCESSING_DEADLINE);
        if (scheduleId != null) {
            deadlineManager.cancelSchedule(PAYMENT_PROCESSING_DEADLINE, scheduleId);
            scheduleId = null;
        }
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderApprovedEvent orderApprovedEvent) {

        log.debug("OrderApprovedEvent is handled: {}", orderApprovedEvent);
        log.debug("OrderSaga is competed for order with Id: {}", orderApprovedEvent.getOrderId());

        OrderSummary orderSummary = mapper.toOrderSummary(orderApprovedEvent);
        queryUpdateEmitter.emit(FindOrderQuery.class, query -> true, orderSummary);

        //        SagaLifecycle.end(); //for programmatically end Saga instead of @EndSaga annotation

    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(ProductReservationCancelledEvent productReservationCancelledEvent) {

        log.debug("ProductReservationCancelledEvent is handled: {}", productReservationCancelledEvent);

        RejectOrderCommand rejectOrderCommand = RejectOrderCommand.builder()
                .orderId(productReservationCancelledEvent.getOrderId())
                .reason(productReservationCancelledEvent.getReason())
                .build();

        commandGateway.send(rejectOrderCommand);
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderRejectedEvent orderRejectedEvent) {

        log.debug("OrderRejectedEvent is handled: {}", orderRejectedEvent);
        log.debug("OrderSaga is rejected for order with Id: {}", orderRejectedEvent.getOrderId());

        OrderSummary orderSummary = mapper.toOrderSummary(orderRejectedEvent);
        queryUpdateEmitter.emit(FindOrderQuery.class, query -> true, orderSummary);

    }

    @DeadlineHandler(deadlineName = PAYMENT_PROCESSING_DEADLINE)
    public void handlePaymentDeadline(ProductReservedEvent productReservedEvent) {

        log.debug("Payment processing deadline took place. Sending a compensating transaction to cancel the product reservation");
        cancelProductReservation(productReservedEvent, "Payment timeout");
    }

}
