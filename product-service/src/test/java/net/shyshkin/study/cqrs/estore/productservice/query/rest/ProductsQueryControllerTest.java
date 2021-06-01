package net.shyshkin.study.cqrs.estore.productservice.query.rest;

import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.estore.productservice.command.CreateProductCommand;
import net.shyshkin.study.cqrs.estore.productservice.commontest.AbstractGlobalAxonServerTest;
import net.shyshkin.study.cqrs.estore.productservice.core.data.ProductRepository;
import net.shyshkin.study.cqrs.estore.productservice.core.errorhandling.ErrorMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Slf4j
class ProductsQueryControllerTest extends AbstractGlobalAxonServerTest {

    @Autowired
    CommandGateway commandGateway;

    @Autowired
    ProductRepository repository;

    private CreateProductCommand createProductCommand;

    @BeforeEach
    void setUp() {
        createProductCommand = createProductCommandStub();
    }

    @Test
    void getProducts() {

        //given
        UUID productId = createProductCommand.getProductId();

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

    private CreateProductCommand createProductCommandStub() {
        CreateProductCommand createProductCommand = CreateProductCommand
                .builder()
                .productId(UUID.randomUUID())
                .price(new BigDecimal("123.00"))
                .title(Faker.instance().commerce().productName())
                .quantity(Faker.instance().random().nextInt(1, 10))
                .build();

        UUID productId = commandGateway.sendAndWait(createProductCommand, 1, TimeUnit.SECONDS);

        log.debug("Product Id: {}", productId);

        await()
                .timeout(2, TimeUnit.SECONDS)
                .untilAsserted(
                        () -> assertThat(repository.findByProductId(productId))
                                .hasValueSatisfying(entity -> assertThat(entity.getProductId()).isEqualTo(productId))
                );

        return createProductCommand;
    }

    @Nested
    class GetProductByIdTests {

        @Test
        void getProductById_present() {

            //given
            UUID productId = createProductCommand.getProductId();

            //when
            ResponseEntity<ProductRestModel> responseEntity = restTemplate
                    .getForEntity("/products/{id}", ProductRestModel.class, productId);

            //then
            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(responseEntity.getBody())
                    .satisfies(productRestModel -> assertThat(productRestModel)
                            .hasFieldOrPropertyWithValue("productId", productId)
                            .isEqualToComparingFieldByField(createProductCommand));
        }

        @Test
        void getProductById_absent() {

            //given
            UUID productId = UUID.randomUUID();

            //when
            ResponseEntity<ErrorMessage> responseEntity = restTemplate
                    .getForEntity("/products/{id}", ErrorMessage.class, productId);

            //then
            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(responseEntity.getBody())
                    .satisfies(errorMessage -> assertThat(errorMessage.getMessage())
                            .contains("org.axonframework.queryhandling.QueryExecutionException: Product with id ")
                            .contains("`" + productId + "`")
                            .contains(" not found")
                    )
                    .satisfies(errorMessage -> assertThat(errorMessage.getTimestamp()).isNotNull());
        }
    }
}