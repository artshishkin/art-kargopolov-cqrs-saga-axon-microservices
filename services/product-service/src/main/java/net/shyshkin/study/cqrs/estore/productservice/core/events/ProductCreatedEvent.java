package net.shyshkin.study.cqrs.estore.productservice.core.events;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ProductCreatedEvent {
    private UUID productId;
    private String title;
    private BigDecimal price;
    private Integer quantity;
}
