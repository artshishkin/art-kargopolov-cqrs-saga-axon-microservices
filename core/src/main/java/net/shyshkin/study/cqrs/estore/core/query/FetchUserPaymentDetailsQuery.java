package net.shyshkin.study.cqrs.estore.core.query;

import lombok.Data;

import java.util.UUID;

@Data
public class FetchUserPaymentDetailsQuery {

    private final UUID userId;

}
