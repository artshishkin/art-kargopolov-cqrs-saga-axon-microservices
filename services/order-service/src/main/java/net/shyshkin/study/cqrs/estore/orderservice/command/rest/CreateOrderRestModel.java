package net.shyshkin.study.cqrs.estore.orderservice.command.rest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRestModel {

    @NotNull
    private UUID productId;

    @Min(value = 1, message = "Quantity should not be less then 1")
    @Max(value = 100, message = "Quantity should not be more then 100")
    private int quantity;

    @NotNull
    private UUID addressId;

}
