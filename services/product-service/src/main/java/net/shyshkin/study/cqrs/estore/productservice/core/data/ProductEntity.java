package net.shyshkin.study.cqrs.estore.productservice.core.data;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "products")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ProductEntity implements Serializable {

    private static final long serialVersionUID = 2268008426323359015L;

    @Id
    @Column(unique = true)
    @EqualsAndHashCode.Include
    @Type(type = "uuid-char") //VARCHAR(255) instead of default binary
    private UUID productId;

    @Column(unique = true)
    private String title;
    private BigDecimal price;
    private Integer quantity;

}
