package net.shyshkin.study.cqrs.estore.productservice.rest;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
public class ProductController {

    @PostMapping
    public String createProduct() {
        return "Http POST is triggered";
    }

    @GetMapping
    public String getProduct() {
        return "Http GET is handled";
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
