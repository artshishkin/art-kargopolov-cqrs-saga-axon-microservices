package net.shyshkin.study.cqrs.estore.productservice.query;

import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.estore.productservice.command.CreateProductCommand;
import net.shyshkin.study.cqrs.estore.productservice.testcontainers.AxonServerContainer;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
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
class ProductEventsHandlerExceptionHandlingTest {

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    CommandGateway commandGateway;

    @Container
    public static AxonServerContainer axonServer = AxonServerContainer.getInstance();

    @ParameterizedTest(name="[{index}] {0}")
    @DisplayName("Handling exception in @EventHandler method")
    @CsvSource({
            "using try-catch block,throwInEventHandler IllegalArgumentException (try-catch),Exception in EventHandler (try-catch block)",
            "using @ExceptionHandler method,throwInEventHandler IllegalArgumentException (ExceptionHandler),IllegalArgumentException in EventHandler (ExceptionHandler)",
    })
    void handleExceptionInEventHandler(String testName, String title, String expectedLogMessage) {
        //given
        String productId = UUID.randomUUID().toString();
        CreateProductCommand createProductCommand = CreateProductCommand.builder()
                .productId(productId)
                .title(title)
                .price(new BigDecimal("125.00"))
                .quantity(2)
                .build();

        //when
        String returnValue = commandGateway.sendAndWait(createProductCommand);

        //then
        assertThat(returnValue).isEqualTo(productId);
        await().timeout(100, TimeUnit.MILLISECONDS);
        log.debug("View logs for message `{}`", expectedLogMessage);
    }
}