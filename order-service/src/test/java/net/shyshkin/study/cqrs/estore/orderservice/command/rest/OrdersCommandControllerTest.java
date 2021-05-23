package net.shyshkin.study.cqrs.estore.orderservice.command.rest;

import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.estore.orderservice.command.CreateOrderCommand;
import net.shyshkin.study.cqrs.estore.orderservice.commontest.AbstractAxonServerTest;
import net.shyshkin.study.cqrs.estore.orderservice.core.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Slf4j
class OrdersCommandControllerTest extends AbstractAxonServerTest {

    @Test
    void createOrder_correct() {
        //given
        CreateOrderRestModel createOrderRestModel = CreateOrderRestModel.builder()
                .productId(UUID.randomUUID())
                .addressId(UUID.randomUUID())
                .quantity(3)
                .build();

        //when
        ResponseEntity<CreateOrderCommand> responseEntity = restTemplate
                .postForEntity("/orders", createOrderRestModel, CreateOrderCommand.class);

        //then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        URI location = responseEntity.getHeaders().getLocation();
        assertThat(location)
                .isNotNull();

        log.debug("Response entity: {}", responseEntity);

        String locationString = location.toString();
        String orderIdString = locationString.substring(locationString.lastIndexOf("/") + 1);
        UUID orderId = UUID.fromString(orderIdString);

        await()
                .timeout(1, TimeUnit.SECONDS)
                .untilAsserted(() ->
                        assertThat(ordersRepository.findByOrderId(orderId))
                                .hasValueSatisfying(orderEntity ->
                                        assertThat(orderEntity)
                                                .hasNoNullFieldsOrProperties()
                                                .satisfies(entity -> log.debug("Entity: {}", entity))
                                                .hasFieldOrPropertyWithValue("orderId", orderId)
                                                .hasFieldOrPropertyWithValue("productId", createOrderRestModel.getProductId())
                                                .hasFieldOrPropertyWithValue("addressId", createOrderRestModel.getAddressId())
                                                .hasFieldOrPropertyWithValue("quantity", createOrderRestModel.getQuantity())
                                                .hasFieldOrPropertyWithValue("orderStatus", OrderStatus.CREATED)
                                )
                );
    }
}