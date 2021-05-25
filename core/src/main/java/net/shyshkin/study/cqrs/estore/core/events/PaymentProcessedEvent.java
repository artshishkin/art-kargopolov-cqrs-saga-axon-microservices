package net.shyshkin.study.cqrs.estore.core.events;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class PaymentProcessedEvent {

    private final UUID orderId;
    private final UUID paymentId;

}
