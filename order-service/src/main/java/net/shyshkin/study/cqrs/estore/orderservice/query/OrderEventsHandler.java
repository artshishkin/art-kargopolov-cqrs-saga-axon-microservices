package net.shyshkin.study.cqrs.estore.orderservice.query;

import lombok.RequiredArgsConstructor;
import net.shyshkin.study.cqrs.estore.orderservice.core.data.OrderEntity;
import net.shyshkin.study.cqrs.estore.orderservice.core.data.OrdersRepository;
import net.shyshkin.study.cqrs.estore.orderservice.core.events.OrderCreatedEvent;
import net.shyshkin.study.cqrs.estore.orderservice.core.mapper.OrderMapper;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventsHandler {

    private final OrdersRepository repository;
    private final OrderMapper mapper;

    @EventHandler
    public void on(OrderCreatedEvent orderCreatedEvent) {
        OrderEntity orderEntity = mapper.toEntity(orderCreatedEvent);
        repository.save(orderEntity);
    }

}
