package net.shyshkin.study.cqrs.estore.productservice.query;

import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.estore.productservice.command.CreateProductCommand;
import net.shyshkin.study.cqrs.estore.productservice.commontest.AbstractGlobalAxonServerTest;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Slf4j
class ProductEventsHandlerExceptionHandlingTest  extends AbstractGlobalAxonServerTest {

    @Autowired
    CommandGateway commandGateway;

    @ParameterizedTest(name="[{index}] {0}")
    @DisplayName("Handling exception in @EventHandler method")
    @CsvSource({
            "using try-catch block,throwInEventHandler IllegalArgumentException (try-catch),Exception in EventHandler (try-catch block)",
            "using @ExceptionHandler method,throwInEventHandler IllegalArgumentException (ExceptionHandler),IllegalArgumentException in EventHandler (ExceptionHandler)",
    })
    void handleExceptionInEventHandler(String testName, String title, String expectedLogMessage) {
        //given
        UUID productId = UUID.randomUUID();
        CreateProductCommand createProductCommand = CreateProductCommand.builder()
                .productId(productId)
                .title(title)
                .price(new BigDecimal("125.00"))
                .quantity(2)
                .build();

        //when
        UUID returnValue = commandGateway.sendAndWait(createProductCommand);

        //then
        assertThat(returnValue).isEqualTo(productId);
        await().timeout(100, TimeUnit.MILLISECONDS);
        log.debug("View logs for message `{}`", expectedLogMessage);
    }
}