package net.shyshkin.study.cqrs.estore.orderservice.core.model;

import lombok.Builder;
import lombok.Data;
import net.shyshkin.study.cqrs.estore.orderservice.core.OrderStatus;

import java.util.UUID;

@Data
@Builder
public class OrderSummary {

    private final UUID orderId;
    private final OrderStatus orderStatus;

    @Builder.Default
    private final String message = "";

}
