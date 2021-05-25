package net.shyshkin.study.cqrs.estore.paymentservice.command;

import net.shyshkin.study.cqrs.estore.core.commands.ProcessPaymentCommand;
import net.shyshkin.study.cqrs.estore.core.events.PaymentProcessedEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

import java.util.UUID;

@Aggregate
public class PaymentAggregate {

    @AggregateIdentifier
    private UUID paymentId;
    private UUID orderId;

    public PaymentAggregate() {
    }

    @CommandHandler
    public PaymentAggregate(ProcessPaymentCommand command) {

        if(command.getPaymentDetails() == null) {
            throw new IllegalArgumentException("Missing payment details");
        }

        if(command.getOrderId() == null) {
            throw new IllegalArgumentException("Missing orderId");
        }

        if(command.getPaymentId() == null) {
            throw new IllegalArgumentException("Missing paymentId");
        }

        PaymentProcessedEvent paymentProcessedEvent = PaymentProcessedEvent.builder()
                .paymentId(command.getPaymentId())
                .orderId(command.getOrderId())
                .build();

        AggregateLifecycle.apply(paymentProcessedEvent);
    }

    @EventSourcingHandler
    public void on(PaymentProcessedEvent paymentProcessedEvent) {
        this.orderId = paymentProcessedEvent.getOrderId();
        this.paymentId = paymentProcessedEvent.getPaymentId();
    }
}
