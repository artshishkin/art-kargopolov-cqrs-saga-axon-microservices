package net.shyshkin.study.cqrs.estore.orderservice.command;

import lombok.Data;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.UUID;

@Data
public class ApproveOrderCommand {

    @TargetAggregateIdentifier
    private final UUID orderId;

}
