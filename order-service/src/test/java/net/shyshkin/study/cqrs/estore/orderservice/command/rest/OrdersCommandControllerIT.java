package net.shyshkin.study.cqrs.estore.orderservice.command.rest;

import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.estore.orderservice.command.CreateOrderCommand;
import net.shyshkin.study.cqrs.estore.orderservice.core.OrderStatus;
import net.shyshkin.study.cqrs.estore.orderservice.core.data.OrdersRepository;
import net.shyshkin.study.cqrs.estore.orderservice.core.model.OrderSummary;
import net.shyshkin.study.cqrs.estore.orderservice.testcontainers.TestComposeContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "eureka.client.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:testOrdersCommandControllerIT;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "axon.axonserver.servers=${AXON_SERVERS}"
})
@DirtiesContext
@Testcontainers
class OrdersCommandControllerIT {

    @Container
    public static TestComposeContainer testComposeContainer = TestComposeContainer.getInstance();

    @Autowired
    TestRestTemplate testRestTemplate;

    @Autowired
    RestTemplateBuilder restTemplateBuilder;

    RestTemplate restTemplate;

    @Autowired
    OrdersRepository ordersRepository;

    @BeforeEach
    void setUp() {
        String rootUri = "http://" + testComposeContainer.getProductServiceHost() + ":" + testComposeContainer.getProductServicePort();
        log.debug("Root URI: {}", rootUri);
        restTemplate = restTemplateBuilder.rootUri(rootUri).build();
    }

    @Test
    void createOrder_correct() throws InterruptedException {
        //given
        UUID productId = createProduct(5);
        CreateOrderRestModel createOrderRestModel = CreateOrderRestModel.builder()
                .productId(productId)
                .addressId(UUID.fromString("afbb5881-a872-4d13-993c-faeb8350eea5"))
                .quantity(1)
                .build();

        //when
        ResponseEntity<CreateOrderCommand> responseEntity = testRestTemplate
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

        // Must have Status CREATED
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

        Thread.sleep(1000);

        log.debug("View in logs (current or another instance of `order-service`): `OrderCreatedEvent is handled`" +
                " followed by `ProductReservedEvent is handled` " +
                " followed by `Successfully fetched payment details` " +
                " followed by `PaymentProcessedEvent is handled` " +
                " followed by `OrderSaga is completed for order with Id` "
        );

        // Must have Status APPROVED
        await()
                .timeout(3, TimeUnit.SECONDS)
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
                                                .hasFieldOrPropertyWithValue("orderStatus", OrderStatus.APPROVED)
                                )
                );
    }

    @Test
    void createOrderSubscribing_approved() {
        //given
        UUID productId = createProduct(1);
        CreateOrderRestModel createOrderRestModel = CreateOrderRestModel.builder()
                .productId(productId)
                .addressId(UUID.fromString("afbb5881-a872-4d13-993c-faeb8350eea5"))
                .quantity(1)
                .build();

        //when
        ResponseEntity<OrderSummary> responseEntity = testRestTemplate
                .postForEntity("/orders/with_subscription", createOrderRestModel, OrderSummary.class);

        //then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        URI location = responseEntity.getHeaders().getLocation();
        assertThat(location).isNotNull();

        OrderSummary orderSummary = responseEntity.getBody();
        assertThat(orderSummary.getMessage()).isEmpty();
        assertThat(orderSummary.getOrderStatus()).isEqualTo(OrderStatus.APPROVED);

        log.debug("Response entity: {}", responseEntity);

        String locationString = location.toString();
        String orderIdString = locationString.substring(locationString.lastIndexOf("/") + 1);
        UUID orderId = UUID.fromString(orderIdString);

        assertThat(orderSummary.getOrderId()).isEqualTo(orderId);


        log.debug("View in logs (current or another instance of `order-service`): `OrderCreatedEvent is handled`" +
                " followed by `ProductReservedEvent is handled` " +
                " followed by `Successfully fetched payment details` " +
                " followed by `PaymentProcessedEvent is handled` " +
                " followed by `OrderSaga is completed for order with Id` "
        );

        // Must have Status APPROVED
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
                                                .hasFieldOrPropertyWithValue("orderStatus", OrderStatus.APPROVED)
                                )
                );
    }

    @Test
    void createOrderSubscribing_notEnough_rejected() {
        //given
        UUID productId = createProduct(1);
        CreateOrderRestModel createOrderRestModel = CreateOrderRestModel.builder()
                .productId(productId)
                .addressId(UUID.fromString("afbb5881-a872-4d13-993c-faeb8350eea5"))
                .quantity(2)
                .build();

        //when
        ResponseEntity<OrderSummary> responseEntity = testRestTemplate
                .postForEntity("/orders/with_subscription", createOrderRestModel, OrderSummary.class);

        //then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        URI location = responseEntity.getHeaders().getLocation();
        assertThat(location).isNull();

        OrderSummary orderSummary = responseEntity.getBody();
        assertThat(orderSummary.getMessage()).isEqualTo("Insufficient number of items in stock");
        assertThat(orderSummary.getOrderStatus()).isEqualTo(OrderStatus.REJECTED);

        log.debug("Response entity: {}", responseEntity);

        UUID orderId = orderSummary.getOrderId();

        log.debug("View in logs (current or another instance of `order-service`): `OrderCreatedEvent is handled`" +
                " followed by `ProductReservedEvent is handled` " +
                " followed by `There was an Exception: org.axonframework.commandhandling.CommandExecutionException:Insufficient number of items in stock` " +
                " followed by `Order has been rejected` " +
                " followed by `OrderRejectedEvent is handled` " +
                " followed by `OrderSaga is rejected for order with Id` "
        );

        // Must have Status REJECTED
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
                                                .hasFieldOrPropertyWithValue("orderStatus", OrderStatus.REJECTED)
                                )
                );
    }

    private UUID createProduct(int count) {
        String productName = Faker.instance().commerce().productName();
        String createProductRestModelJson =
                "{" +
                        "    \"title\":\"" + productName + "\"," +
                        "    \"price\":12.00," +
                        "    \"quantity\":" + count +
                        "}";

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<String> requestEntity = new RequestEntity<>(createProductRestModelJson, headers, HttpMethod.POST, null, null);
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                "/products",
                HttpMethod.POST,
                requestEntity,
                String.class);
        String response = responseEntity.getBody();
        response = response.replace("Http POST: ", "");
        return UUID.fromString(response);
    }
}