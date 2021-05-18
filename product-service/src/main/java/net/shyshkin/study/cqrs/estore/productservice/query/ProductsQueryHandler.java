package net.shyshkin.study.cqrs.estore.productservice.query;

import lombok.RequiredArgsConstructor;
import net.shyshkin.study.cqrs.estore.productservice.core.data.ProductRepository;
import net.shyshkin.study.cqrs.estore.productservice.mapper.ProductMapper;
import net.shyshkin.study.cqrs.estore.productservice.query.rest.ProductRestModel;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductsQueryHandler {

    private final ProductRepository repository;
    private final ProductMapper mapper;

    @QueryHandler
    public List<ProductRestModel> findProducts(FindProductsQuery findProductsQuery) {
        return repository.findAll()
                .stream()
                .map(mapper::toProductRestModel)
                .collect(Collectors.toList());
    }
}
