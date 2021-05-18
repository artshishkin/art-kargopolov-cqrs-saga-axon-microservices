package net.shyshkin.study.cqrs.estore.productservice.query;

import lombok.RequiredArgsConstructor;
import net.shyshkin.study.cqrs.estore.productservice.core.data.ProductEntity;
import net.shyshkin.study.cqrs.estore.productservice.core.data.ProductRepository;
import net.shyshkin.study.cqrs.estore.productservice.core.events.ProductCreatedEvent;
import net.shyshkin.study.cqrs.estore.productservice.mapper.ProductMapper;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductEventsHandler {

    private final ProductRepository repository;
    private final ProductMapper mapper;

    @EventHandler
    public void on(ProductCreatedEvent event) {

        ProductEntity productEntity = mapper.toProductEntity(event);
        repository.save(productEntity);
    }

}
