package net.shyshkin.study.cqrs.estore.orderservice.core.model;

import lombok.Value;
import net.shyshkin.study.cqrs.estore.orderservice.core.OrderStatus;

import java.util.UUID;

@Value
public class OrderSummary {

    private final UUID orderId;
    private final OrderStatus orderStatus;

}
