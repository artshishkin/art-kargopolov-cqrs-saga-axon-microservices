package net.shyshkin.study.cqrs.estore.orderservice.core.data;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.shyshkin.study.cqrs.estore.orderservice.core.OrderStatus;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class OrderEntity {

    @Id
    @Column(unique = true)
    @EqualsAndHashCode.Include
    @Type(type = "uuid-char") //VARCHAR(255) instead of default binary
    private UUID orderId;

    private UUID productId;
    private UUID userId;
    private int quantity;
    private UUID addressId;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

}
