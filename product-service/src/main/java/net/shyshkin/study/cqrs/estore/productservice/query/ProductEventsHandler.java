package net.shyshkin.study.cqrs.estore.productservice.query;

import net.shyshkin.study.cqrs.estore.productservice.core.events.ProductCreatedEvent;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

@Component
public class ProductEventsHandler {

    @EventHandler
    public void on(ProductCreatedEvent event){

    }

}
