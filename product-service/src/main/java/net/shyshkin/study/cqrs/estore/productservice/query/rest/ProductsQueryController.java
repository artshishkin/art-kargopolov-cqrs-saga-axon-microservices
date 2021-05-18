package net.shyshkin.study.cqrs.estore.productservice.query.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductsQueryController {

    @GetMapping
    public List<ProductRestModel> getProducts() {
        throw new RuntimeException("Not implemented yet");
    }

}
