package net.shyshkin.study.cqrs.estore.core.commands;

import lombok.Builder;
import lombok.Data;
import net.shyshkin.study.cqrs.estore.core.model.PaymentDetails;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.UUID;

@Data
@Builder
public class ProcessPaymentCommand {

    @TargetAggregateIdentifier
    private final UUID paymentId;
    private final UUID orderId;
    private final PaymentDetails paymentDetails;

}
