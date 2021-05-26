package net.shyshkin.study.cqrs.estore.orderservice.query;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.estore.orderservice.core.data.OrderEntity;
import net.shyshkin.study.cqrs.estore.orderservice.core.data.OrdersRepository;
import net.shyshkin.study.cqrs.estore.orderservice.core.events.OrderApprovedEvent;
import net.shyshkin.study.cqrs.estore.orderservice.core.events.OrderCreatedEvent;
import net.shyshkin.study.cqrs.estore.orderservice.core.mapper.OrderMapper;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

import javax.persistence.EntityNotFoundException;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
@ProcessingGroup("order-group")
public class OrderEventsHandler {

    private final OrdersRepository repository;
    private final OrderMapper mapper;

    @EventHandler
    public void on(OrderCreatedEvent orderCreatedEvent) {
        OrderEntity orderEntity = mapper.toEntity(orderCreatedEvent);
        repository.save(orderEntity);
    }

    @EventHandler
    public void on(OrderApprovedEvent orderApprovedEvent) {
        UUID orderId = orderApprovedEvent.getOrderId();
        OrderEntity orderEntity = repository
                .findByOrderId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order with id `" + orderId + "` not found"));
        orderEntity.setOrderStatus(orderApprovedEvent.getOrderStatus());
        repository.save(orderEntity);
    }

}
