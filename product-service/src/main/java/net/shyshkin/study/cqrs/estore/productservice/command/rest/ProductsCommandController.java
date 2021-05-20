package net.shyshkin.study.cqrs.estore.productservice.command.rest;

import lombok.RequiredArgsConstructor;
import net.shyshkin.study.cqrs.estore.productservice.command.CreateProductCommand;
import net.shyshkin.study.cqrs.estore.productservice.mapper.ProductMapper;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductsCommandController {

    private final Environment environment;
    private final ProductMapper mapper;
    private final CommandGateway commandGateway;

    @PostMapping
    public String createProduct(@Valid @RequestBody CreateProductRestModel createProductRestModel) {
        CreateProductCommand createProductCommand = mapper.toCreateCommand(createProductRestModel);

        String returnValue = commandGateway.sendAndWait(createProductCommand);
        return "Http POST: " + returnValue;
    }

    @PutMapping
    public String updateProduct() {
        return "Http PUT is handled";
    }

    @DeleteMapping
    public String deleteProduct() {
        return "Http DELETE is handled";
    }

}
