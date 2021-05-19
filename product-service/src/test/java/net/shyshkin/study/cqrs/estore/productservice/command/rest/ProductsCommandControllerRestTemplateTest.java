package net.shyshkin.study.cqrs.estore.productservice.command.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.estore.productservice.core.data.ProductEntity;
import net.shyshkin.study.cqrs.estore.productservice.core.data.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.Optional;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = {ProductsCommandControllerRestTemplateTest.Initializer.class})
@Testcontainers
class ProductsCommandControllerRestTemplateTest {

    @Autowired
    TestRestTemplate restTemplate;

    @Container
    public static GenericContainer axonServer = new GenericContainer(DockerImageName.parse("axoniq/axonserver"))
            .withExposedPorts(8024, 8124)
            .waitingFor(
                    Wait.forLogMessage(".*Started AxonServer in.*\\n", 1)
            );

    @Autowired
    ProductsCommandController controller;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ProductRepository repository;

    @Test
    void createProduct_correct() {

        //given
        CreateProductRestModel createProductRestModel = CreateProductRestModel.builder()
                .title(Faker.instance().commerce().productName())
                .price(new BigDecimal("125.00"))
                .quantity(2)
                .build();

        //when
        ResponseEntity<String> responseEntity = restTemplate
                .postForEntity("/products",
                        createProductRestModel,
                        String.class);

        //then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        String body = responseEntity.getBody();
        assertThat(body).startsWith("Http POST: ");

        String productId = body.replace("Http POST: ", "");

        log.debug("Product Id: {}", productId);

        await()
                .timeout(1L, SECONDS)
                .untilAsserted(() -> {
                    Optional<ProductEntity> productOptional = repository.findByProductId(productId);
                    assertThat(productOptional)
                            .hasValueSatisfying(productEntity -> assertThat(productEntity)
                                    .hasNoNullFieldsOrProperties()
                                    .isEqualToIgnoringGivenFields(createProductRestModel, "productId")
                                    .hasFieldOrPropertyWithValue("productId", productId)
                                    .satisfies(entity -> log.debug("Entity: {}", entity))
                            );
                });
    }

    @ParameterizedTest
    @CsvSource({
            ",125.0,\"NotBlank.title\",Product title is a required field",
            "iPhone 3,-125.0,\"Min.price\",Price can not be lower then 1"
    })
    void createProduct_validationFailed(String title, String price, String errorCode, String defaultMessage) throws Exception {

        //given
        CreateProductRestModel createProductRestModel = CreateProductRestModel.builder()
                .title(title)
                .price(new BigDecimal(price))
                .quantity(2)
                .build();
        String jsonPayload = objectMapper.writeValueAsString(createProductRestModel);
        log.debug("JSON request body: {}", jsonPayload);

        //when
        ResponseEntity<String> responseEntity = restTemplate
                .postForEntity("/products",
                        createProductRestModel,
                        String.class);

        //then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        String body = responseEntity.getBody();
        log.debug("Response body: {}", body);
        assertThat(body)
                .contains("\"status\":400,\"error\":\"Bad Request\"")
                .contains("\"message\":\"Validation failed for object='createProductRestModel'. Error count: 1\"")
                .contains(errorCode)
                .contains("\"defaultMessage\":\"" + defaultMessage);

    }

    static class Initializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            String host = axonServer.getHost();
            Integer port = axonServer.getMappedPort(8124);
            log.debug("axonServer {}:{}", host, port);

            String servers = host + ":" + port;
            TestPropertyValues.of(
                    "axon.axonserver.servers=" + servers,
                    "eureka.client.register-with-eureka=false",
                    "eureka.client.fetch-registry=false",
                    "spring.datasource.url=jdbc:h2:mem:testdb",
                    "spring.datasource.username=sa",
                    "spring.datasource.password="
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }
}