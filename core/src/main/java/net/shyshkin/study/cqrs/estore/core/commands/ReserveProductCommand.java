package net.shyshkin.study.cqrs.estore.core.commands;

import lombok.Builder;
import lombok.Data;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.UUID;

@Data
@Builder
public class ReserveProductCommand {

    @TargetAggregateIdentifier
    private final UUID productId;
    private final int quantity;
    private final UUID orderId;
    private final UUID userId;

}
