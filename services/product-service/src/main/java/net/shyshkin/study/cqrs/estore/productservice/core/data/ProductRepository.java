package net.shyshkin.study.cqrs.estore.productservice.core.data;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<ProductEntity, UUID> {

    Optional<ProductEntity> findByProductId(UUID productId);

    Optional<ProductEntity> findByProductIdOrTitle(UUID productId, String title);
}
