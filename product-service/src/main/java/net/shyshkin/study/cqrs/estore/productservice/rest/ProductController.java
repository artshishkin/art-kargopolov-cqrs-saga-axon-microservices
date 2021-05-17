package net.shyshkin.study.cqrs.estore.productservice.rest;

import lombok.RequiredArgsConstructor;
import net.shyshkin.study.cqrs.estore.productservice.command.CreateProductCommand;
import net.shyshkin.study.cqrs.estore.productservice.mapper.ProductMapper;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final Environment environment;
    private final ProductMapper mapper;
    private final CommandGateway commandGateway;

    @PostMapping
    public String createProduct(@RequestBody CreateProductRestModel createProductRestModel) {
        CreateProductCommand createProductCommand = mapper.toCreateCommand(createProductRestModel);

        String returnValue;
        try {
            returnValue = commandGateway.sendAndWait(createProductCommand);
        } catch (Exception exception) {
            returnValue = exception.getLocalizedMessage();
        }

        return "Http POST: " + returnValue;
    }

    @GetMapping
    public String getProduct() {
        Integer serverPort = environment.getProperty("local.server.port", Integer.class);
        String appName = environment.getProperty("spring.application.name");

        return "Http GET on " + appName + ":" + serverPort;
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
