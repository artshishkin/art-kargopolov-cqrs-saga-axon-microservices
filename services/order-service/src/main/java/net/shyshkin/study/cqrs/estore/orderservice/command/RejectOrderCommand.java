package net.shyshkin.study.cqrs.estore.orderservice.command;

import lombok.Builder;
import lombok.Data;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.UUID;

@Data
@Builder
public class RejectOrderCommand {

    @TargetAggregateIdentifier
    private final UUID orderId;
    private final String reason;
}
