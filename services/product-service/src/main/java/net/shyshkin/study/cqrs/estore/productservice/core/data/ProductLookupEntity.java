package net.shyshkin.study.cqrs.estore.productservice.core.data;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "product_lookup")
public class ProductLookupEntity implements Serializable {

    private static final long serialVersionUID = -203523508911497034L;

    @Id
    @EqualsAndHashCode.Include
    @Type(type = "uuid-char") //VARCHAR(255) instead of default binary
    private UUID productId;

    @Column(unique = true)
    private String title;

}
