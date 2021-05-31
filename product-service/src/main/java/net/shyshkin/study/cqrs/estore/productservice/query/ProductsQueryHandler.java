package net.shyshkin.study.cqrs.estore.productservice.query;

import lombok.RequiredArgsConstructor;
import net.shyshkin.study.cqrs.estore.productservice.core.data.ProductRepository;
import net.shyshkin.study.cqrs.estore.productservice.mapper.ProductMapper;
import net.shyshkin.study.cqrs.estore.productservice.query.rest.ProductRestModel;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
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

    @QueryHandler
    public ProductRestModel findProductById(FindProductByIdQuery findProductByIdQuery) {
        UUID productId = findProductByIdQuery.getProductId();
        return repository
                .findByProductId(productId)
                .map(mapper::toProductRestModel)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Product with id `%s` not found", productId)));
    }
}
