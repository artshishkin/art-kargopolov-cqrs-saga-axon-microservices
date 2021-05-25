package net.shyshkin.study.cqrs.estore.paymentservice.query;

import lombok.RequiredArgsConstructor;
import net.shyshkin.study.cqrs.estore.core.events.PaymentProcessedEvent;
import net.shyshkin.study.cqrs.estore.paymentservice.core.data.PaymentEntity;
import net.shyshkin.study.cqrs.estore.paymentservice.core.data.PaymentsRepository;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventsHandler {

    private final PaymentsRepository repository;

    @EventHandler
    public void on(PaymentProcessedEvent paymentProcessedEvent) {

        PaymentEntity paymentEntity = PaymentEntity.builder()
                .paymentId(paymentProcessedEvent.getPaymentId())
                .orderId(paymentProcessedEvent.getOrderId())
                .build();

        repository.save(paymentEntity);
    }
}
