package net.shyshkin.study.cqrs.estore.productservice.command;

import lombok.Builder;
import lombok.Data;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class CreateProductCommand {

    @TargetAggregateIdentifier
    private final UUID productId;
    private final String title;
    private final BigDecimal price;
    private final Integer quantity;

}
