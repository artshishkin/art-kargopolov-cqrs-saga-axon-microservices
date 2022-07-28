package net.shyshkin.study.cqrs.estore.productservice.core.data;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProductLookupRepository extends JpaRepository<ProductLookupEntity, UUID> {

    Optional<ProductLookupEntity> findByProductIdOrTitle(UUID productId, String title);

}
