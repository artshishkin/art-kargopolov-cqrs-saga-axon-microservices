package net.shyshkin.study.cqrs.estore.orderservice.core.data;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrdersRepository extends JpaRepository<OrderEntity, UUID> {

    Optional<OrderEntity> findByOrderId(UUID orderId);

}
