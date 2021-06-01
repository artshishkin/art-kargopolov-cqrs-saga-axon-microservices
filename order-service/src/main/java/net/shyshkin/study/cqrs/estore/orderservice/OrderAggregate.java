package net.shyshkin.study.cqrs.estore.orderservice;

import lombok.NoArgsConstructor;
import net.shyshkin.study.cqrs.estore.orderservice.command.ApproveOrderCommand;
import net.shyshkin.study.cqrs.estore.orderservice.command.CreateOrderCommand;
import net.shyshkin.study.cqrs.estore.orderservice.command.RejectOrderCommand;
import net.shyshkin.study.cqrs.estore.orderservice.core.OrderStatus;
import net.shyshkin.study.cqrs.estore.orderservice.core.events.OrderApprovedEvent;
import net.shyshkin.study.cqrs.estore.orderservice.core.events.OrderCreatedEvent;
import net.shyshkin.study.cqrs.estore.orderservice.core.events.OrderRejectedEvent;
import net.shyshkin.study.cqrs.estore.orderservice.core.mapper.OrderMapper;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.factory.annotation.Autowired;

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

    private transient OrderMapper mapper;

    @CommandHandler
    public OrderAggregate(CreateOrderCommand createOrderCommand) {

        OrderCreatedEvent orderCreatedEvent = OrderMapper.INSTANCE.toOrderCreatedEvent(createOrderCommand);
        AggregateLifecycle.apply(orderCreatedEvent);

    }

    @Autowired
    public void setMapper(OrderMapper mapper) {
        this.mapper = mapper;
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
        OrderApprovedEvent orderApprovedEvent = mapper.toEvent(approveOrderCommand);
        AggregateLifecycle.apply(orderApprovedEvent);
    }

    @EventSourcingHandler
    protected void on(OrderApprovedEvent orderApprovedEvent) {
        this.orderStatus = orderApprovedEvent.getOrderStatus();
    }

    @CommandHandler
    public void handle(RejectOrderCommand rejectOrderCommand) {
        OrderRejectedEvent orderRejectedEvent = mapper.toEvent(rejectOrderCommand);
        AggregateLifecycle.apply(orderRejectedEvent);
    }

    @EventSourcingHandler
    protected void on(OrderRejectedEvent orderRejectedEvent) {
        this.orderStatus = orderRejectedEvent.getOrderStatus();
    }
}
