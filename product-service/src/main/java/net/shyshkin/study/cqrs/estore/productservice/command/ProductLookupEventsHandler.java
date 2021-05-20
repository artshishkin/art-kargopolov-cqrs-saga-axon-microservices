package net.shyshkin.study.cqrs.estore.productservice.command;

import net.shyshkin.study.cqrs.estore.productservice.core.events.ProductCreatedEvent;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("product-group")
public class ProductLookupEventsHandler {

    @EventHandler
    public void on(ProductCreatedEvent event) {

    }

}
