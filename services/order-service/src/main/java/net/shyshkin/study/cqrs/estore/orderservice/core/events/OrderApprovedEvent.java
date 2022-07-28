package net.shyshkin.study.cqrs.estore.orderservice.core.events;

import lombok.Data;
import net.shyshkin.study.cqrs.estore.orderservice.core.OrderStatus;

import java.util.UUID;

@Data
public class OrderApprovedEvent {

    private final UUID orderId;
    private final OrderStatus orderStatus = OrderStatus.APPROVED;

}
