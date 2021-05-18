package net.shyshkin.study.cqrs.estore.productservice.core.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ProductEntity implements Serializable {

    private static final long serialVersionUID = -4720706880319964283L;

    @Id
    @Column(unique = true)
    @EqualsAndHashCode.Include
    private String productId;

    @Column(unique = true)
    private String title;
    private BigDecimal price;
    private Integer quantity;

}
