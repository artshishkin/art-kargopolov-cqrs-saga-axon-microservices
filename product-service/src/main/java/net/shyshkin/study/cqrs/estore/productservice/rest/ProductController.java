package net.shyshkin.study.cqrs.estore.productservice.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final Environment environment;

    @PostMapping
    public String createProduct(@RequestBody CreateProductRestModel createProductRestModel) {
        return "Http POST: " + createProductRestModel;
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
