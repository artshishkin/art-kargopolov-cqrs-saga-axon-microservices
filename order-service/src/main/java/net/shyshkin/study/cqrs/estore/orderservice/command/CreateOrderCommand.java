package net.shyshkin.study.cqrs.estore.orderservice.command;

import lombok.Builder;
import lombok.Data;
import net.shyshkin.study.cqrs.estore.orderservice.core.OrderStatus;

import java.util.UUID;

@Data
@Builder
public class CreateOrderCommand {

    @Builder.Default
    private final UUID orderId = UUID.randomUUID();

    @Builder.Default
    private final UUID userId =UUID.fromString("27b95829-4f3f-4ddf-8983-151ba010e35b");

    private final UUID productId;
    private final int quantity;
    private final UUID addressId;
    private final OrderStatus orderStatus;

}
