package net.shyshkin.study.cqrs.estore.productservice.query.rest;

import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.estore.productservice.command.CreateProductCommand;
import net.shyshkin.study.cqrs.estore.productservice.core.data.ProductRepository;
import net.shyshkin.study.cqrs.estore.productservice.testcontainers.AxonServerContainer;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false",
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "axon.axonserver.servers=${AXON_SERVERS}"
})
@Testcontainers
class ProductsQueryControllerTest {

    @Container
    public static AxonServerContainer axonServer = AxonServerContainer.getInstance();

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    CommandGateway commandGateway;

    @Autowired
    ProductRepository repository;

    @Test
    void getProducts() {

        //given
        CreateProductCommand createProductCommand = CreateProductCommand
                .builder()
                .productId(UUID.randomUUID().toString())
                .price(new BigDecimal("123.00"))
                .title(Faker.instance().commerce().productName())
                .quantity(Faker.instance().random().nextInt(1, 10))
                .build();

        String productId = commandGateway.sendAndWait(createProductCommand, 1, TimeUnit.SECONDS);
        log.debug("Product Id: {}", productId);

        await()
                .timeout(2, TimeUnit.SECONDS)
                .untilAsserted(
                        () -> assertThat(repository.findByProductId(productId))
                                .hasValueSatisfying(entity -> assertThat(entity.getProductId()).isEqualTo(productId))
                );

        ParameterizedTypeReference<List<ProductRestModel>> responseType = new ParameterizedTypeReference<>() {
        };

        //when
        ResponseEntity<List<ProductRestModel>> responseEntity = restTemplate.exchange("/products", HttpMethod.GET, null, responseType);

        //then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody())
                .hasSizeGreaterThanOrEqualTo(1)
                .anySatisfy(productRestModel -> assertThat(productRestModel)
                        .hasFieldOrPropertyWithValue("productId", productId)
                        .isEqualToComparingFieldByField(createProductCommand));
    }

}