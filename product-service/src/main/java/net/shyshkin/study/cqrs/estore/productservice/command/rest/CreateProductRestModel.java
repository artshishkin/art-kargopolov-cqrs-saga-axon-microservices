package net.shyshkin.study.cqrs.estore.productservice.command.rest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProductRestModel {

    @NotBlank(message = "Product title is a required field")
    private String title;

    @Min(value = 1, message = "Price can not be lower then 1")
    private BigDecimal price;

    @Min(value = 1, message = "Quantity can not lower then 1")
    @Max(value = 5, message = "Quantity can not larger then 5")
    private Integer quantity;

}
