package net.shyshkin.study.cqrs.estore.paymentservice.core.data;

import lombok.*;

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
    private UUID paymentId;

    private UUID orderId;

}
