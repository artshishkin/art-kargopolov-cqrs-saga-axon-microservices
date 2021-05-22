package net.shyshkin.study.cqrs.estore.orderservice.core.events;

import lombok.Data;
import net.shyshkin.study.cqrs.estore.orderservice.core.OrderStatus;

import java.util.UUID;

@Data
public class OrderCreatedEvent {

    private UUID orderId;
    private UUID productId;
    private UUID userId;
    private int quantity;
    private UUID addressId;
    private OrderStatus orderStatus;

}
