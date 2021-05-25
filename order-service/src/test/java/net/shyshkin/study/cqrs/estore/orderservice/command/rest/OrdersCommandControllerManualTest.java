package net.shyshkin.study.cqrs.estore.orderservice.command.rest;

import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.estore.orderservice.command.CreateOrderCommand;
import net.shyshkin.study.cqrs.estore.orderservice.core.OrderStatus;
import net.shyshkin.study.cqrs.estore.orderservice.core.data.OrdersRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Disabled("Only for manual testing")
class OrdersCommandControllerManualTest {

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    OrdersRepository ordersRepository;

    @Test
    void createOrder_correct() {
        //given
        String productId = "753f6a14-df36-4743-ba67-ad879b8c6172";
        CreateOrderRestModel createOrderRestModel = CreateOrderRestModel.builder()
                .productId(UUID.fromString(productId))
                .addressId(UUID.fromString("afbb5881-a872-4d13-993c-faeb8350eea5"))
                .quantity(1)
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

        await()
                .timeout(100, TimeUnit.MILLISECONDS);
        log.debug("View in logs (current or another instance of `order-service`): `OrderCreatedEvent is handled`" +
                " followed by `ProductReservedEvent is handled` " +
                " followed by `Successfully fetched payment details` "
        );
    }
}