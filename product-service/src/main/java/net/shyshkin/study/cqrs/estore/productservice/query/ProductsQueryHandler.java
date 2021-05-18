package net.shyshkin.study.cqrs.estore.productservice.query;

import lombok.RequiredArgsConstructor;
import net.shyshkin.study.cqrs.estore.productservice.core.data.ProductRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductsQueryHandler {

    private final ProductRepository repository;
}
