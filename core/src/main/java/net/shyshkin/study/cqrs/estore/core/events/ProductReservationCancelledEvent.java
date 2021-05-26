package net.shyshkin.study.cqrs.estore.core.events;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ProductReservationCancelledEvent {

    private final UUID productId;
    private final int quantity;
    private final UUID orderId;
    private final UUID userId;
    private final String reason;

}
