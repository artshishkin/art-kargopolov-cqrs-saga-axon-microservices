package net.shyshkin.study.cqrs.estore.orderservice.query;

import lombok.Value;

import java.util.UUID;

@Value
public class FindOrderQuery {

    private final UUID orderId;

}
