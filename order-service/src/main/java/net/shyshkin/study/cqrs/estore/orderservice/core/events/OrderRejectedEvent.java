package net.shyshkin.study.cqrs.estore.orderservice.core.events;

import lombok.Builder;
import lombok.Data;
import net.shyshkin.study.cqrs.estore.orderservice.core.OrderStatus;

import java.util.UUID;

@Data
@Builder
public class OrderRejectedEvent {

    private final UUID orderId;
    private final OrderStatus orderStatus = OrderStatus.REJECTED;
    private final String reason;

}
