package net.shyshkin.study.cqrs.estore.core.model;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class User {

    private final String firstName;
    private final String lastName;
    private final UUID userId;
    private final PaymentDetails paymentDetails;

}
