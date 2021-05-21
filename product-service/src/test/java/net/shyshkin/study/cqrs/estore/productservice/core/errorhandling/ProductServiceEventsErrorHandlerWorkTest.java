package net.shyshkin.study.cqrs.estore.productservice.core.errorhandling;

import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.estore.productservice.command.rest.CreateProductRestModel;
import net.shyshkin.study.cqrs.estore.productservice.core.data.ProductLookupRepository;
import net.shyshkin.study.cqrs.estore.productservice.testcontainers.AxonServerContainer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static java.time.LocalDateTime.now;
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
class ProductServiceEventsErrorHandlerWorkTest {

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    ProductLookupRepository lookupRepository;

    @Container
    public static AxonServerContainer axonServer = AxonServerContainer.getInstance();

    @ParameterizedTest(name = "[{index}] {0}")
    @DisplayName("Handling exception in ListenerInvocationErrorHandler method")
    @CsvSource({
            "using ListenerInvocationErrorHandler,throwInEventHandler Exception (ExceptionHandler),Exception occurred while processing events",
    })
    void handleExceptionInEventHandler(String testName, String title, String errorMessage) {
        //given
        CreateProductRestModel createProductRestModel = CreateProductRestModel.builder()
                .title(title + UUID.randomUUID())
                .price(new BigDecimal("321.00"))
                .quantity(2)
                .build();
        long productCountBefore = lookupRepository.count();

        //when
        ResponseEntity<ErrorMessage> responseEntity = restTemplate
                .postForEntity("/products",
                        createProductRestModel,
                        ErrorMessage.class);

        //then
        ErrorMessage body = responseEntity.getBody();
        log.debug("Response body: {}", body);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(body)
                .hasNoNullFieldsOrProperties()
                .satisfies(errorMess -> assertThat(errorMess.getTimestamp()).isEqualToIgnoringNanos(now()))
                .satisfies(errorMess -> assertThat(errorMess.getMessage()).contains(errorMessage));
        //then
        long productCountAfter = lookupRepository.count();
        assertThat(productCountAfter).isEqualTo(productCountBefore);

        await().timeout(100, TimeUnit.MILLISECONDS);

        List<String> expectedLogMessagesOrderedByExecution = List.of(
                "Exception in EventHandler (ExceptionHandler)",
                "Exception in EventHandler (ListenerInvocationErrorHandler)"
        );

        AtomicInteger idx = new AtomicInteger(0);
        expectedLogMessagesOrderedByExecution
                .forEach(message -> log.debug("View logs for message {} `{}`", idx.getAndIncrement(), message));
    }
}