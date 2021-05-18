package net.shyshkin.study.cqrs.estore.productservice.command.rest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProductRestModel {

    private String title;
    private BigDecimal price;
    private Integer quantity;

}
