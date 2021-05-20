package net.shyshkin.study.cqrs.estore.productservice.command.interceptors;

import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.estore.productservice.command.rest.CreateProductRestModel;
import net.shyshkin.study.cqrs.estore.productservice.testcontainers.AxonServerContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

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
class CreateProductCommandInterceptorTest {

    @Autowired
    TestRestTemplate restTemplate;

    @Container
    public static AxonServerContainer axonServer = AxonServerContainer.getInstance();

    @Test
    void createProduct_validationFailedInInterceptor() throws Exception {

        //given
        String title = "Title with incorrect ` symbol";
        String price = "666.00";
        String errorMessage = "Title must not contain ` symbol (fake validation)";

        CreateProductRestModel createProductRestModel = CreateProductRestModel.builder()
                .title(title)
                .price(new BigDecimal(price))
                .quantity(2)
                .build();

        //when
        ResponseEntity<String> responseEntity = restTemplate
                .postForEntity("/products",
                        createProductRestModel,
                        String.class);

        //then
        String body = responseEntity.getBody();
        log.debug("Response body: {}", body);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(body).contains(errorMessage);
    }
}