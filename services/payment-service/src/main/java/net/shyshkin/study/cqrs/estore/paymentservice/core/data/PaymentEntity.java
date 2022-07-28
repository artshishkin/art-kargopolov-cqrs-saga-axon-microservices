package net.shyshkin.study.cqrs.estore.paymentservice.core.data;

import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PaymentEntity {

    @Id
    @EqualsAndHashCode.Include
    @Type(type = "uuid-char") //VARCHAR(255) instead of default binary
    private UUID paymentId;

    private UUID orderId;

}
