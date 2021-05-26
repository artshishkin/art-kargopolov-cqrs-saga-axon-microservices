package net.shyshkin.study.cqrs.estore.orderservice;

import lombok.NoArgsConstructor;
import net.shyshkin.study.cqrs.estore.orderservice.command.ApproveOrderCommand;
import net.shyshkin.study.cqrs.estore.orderservice.command.CreateOrderCommand;
import net.shyshkin.study.cqrs.estore.orderservice.core.OrderStatus;
import net.shyshkin.study.cqrs.estore.orderservice.core.events.OrderCreatedEvent;
import net.shyshkin.study.cqrs.estore.orderservice.core.mapper.OrderMapper;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

import java.util.UUID;

@Aggregate
@NoArgsConstructor
public class OrderAggregate {

    @AggregateIdentifier
    private UUID orderId;
    private UUID productId;
    private UUID userId;
    private int quantity;
    private UUID addressId;
    private OrderStatus orderStatus;

    @CommandHandler
    public OrderAggregate(CreateOrderCommand createOrderCommand) {

        OrderCreatedEvent orderCreatedEvent = OrderMapper.INSTANCE.toOrderCreatedEvent(createOrderCommand);
        AggregateLifecycle.apply(orderCreatedEvent);

    }

    @EventSourcingHandler
    public void on(OrderCreatedEvent orderCreatedEvent) {
        this.orderId = orderCreatedEvent.getOrderId();
        this.productId = orderCreatedEvent.getProductId();
        this.userId = orderCreatedEvent.getUserId();
        this.quantity = orderCreatedEvent.getQuantity();
        this.addressId = orderCreatedEvent.getAddressId();
        this.orderStatus = orderCreatedEvent.getOrderStatus();

    }

    @CommandHandler
    public void handle(ApproveOrderCommand approveOrderCommand) {

        // TODO: 26.05.2021 Create and publish OrderApprovedEvent

    }

}
