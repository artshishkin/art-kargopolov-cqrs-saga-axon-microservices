package net.shyshkin.study.cqrs.estore.productservice.query.rest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRestModel {

    private UUID productId;
    private String title;
    private BigDecimal price;
    private Integer quantity;

}
