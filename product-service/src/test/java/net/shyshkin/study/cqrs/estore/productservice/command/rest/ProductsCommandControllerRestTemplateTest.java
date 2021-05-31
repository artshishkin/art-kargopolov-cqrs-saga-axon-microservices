package net.shyshkin.study.cqrs.estore.productservice.command.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.estore.core.model.ProductIdDto;
import net.shyshkin.study.cqrs.estore.productservice.commontest.AbstractAxonServerTest;
import net.shyshkin.study.cqrs.estore.productservice.core.data.ProductEntity;
import net.shyshkin.study.cqrs.estore.productservice.core.data.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Slf4j
class ProductsCommandControllerRestTemplateTest extends AbstractAxonServerTest {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ProductRepository repository;

    @Test
    void createProduct_correct() {

        //given
        String title = Faker.instance().commerce().productName();
        CreateProductRestModel createProductRestModel = CreateProductRestModel.builder()
                .title(title)
                .price(new BigDecimal("125.00"))
                .quantity(2)
                .build();

        //when
        ResponseEntity<ProductIdDto> responseEntity = restTemplate
                .postForEntity("/products",
                        createProductRestModel,
                        ProductIdDto.class);

        //then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        ProductIdDto body = responseEntity.getBody();
        URI location = responseEntity.getHeaders().getLocation();

        assertThat(location).isNotNull();
        UUID productId = body.getProductId();
        assertThat(location.toString()).endsWith(productId.toString());

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

        assertThat(lookupRepository.findByProductIdOrTitle(productId, title))
                .hasValueSatisfying(lookupEntity -> assertThat(lookupEntity)
                        .hasNoNullFieldsOrProperties()
                        .hasFieldOrPropertyWithValue("productId", productId)
                        .hasFieldOrPropertyWithValue("title", title)
                );
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

}