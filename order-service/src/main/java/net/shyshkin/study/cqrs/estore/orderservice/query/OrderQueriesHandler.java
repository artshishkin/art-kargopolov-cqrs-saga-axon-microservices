package net.shyshkin.study.cqrs.estore.orderservice.query;

import lombok.RequiredArgsConstructor;
import net.shyshkin.study.cqrs.estore.orderservice.core.data.OrdersRepository;
import net.shyshkin.study.cqrs.estore.orderservice.core.mapper.OrderMapper;
import net.shyshkin.study.cqrs.estore.orderservice.core.model.OrderSummary;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

import javax.persistence.EntityNotFoundException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderQueriesHandler {

    private final OrdersRepository repository;
    private final OrderMapper mapper;

    @QueryHandler
    public OrderSummary findOrder(FindOrderQuery query) {
        UUID orderId = query.getOrderId();
        return repository
                .findByOrderId(orderId)
                .map(mapper::toOrderSummary)
                .orElseThrow(() -> new EntityNotFoundException("Order with id `" + orderId + "` not found"));
    }
}
