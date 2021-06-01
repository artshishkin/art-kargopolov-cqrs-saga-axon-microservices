package net.shyshkin.study.cqrs.estore.productservice.command;

import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.estore.productservice.command.rest.CreateProductRestModel;
import net.shyshkin.study.cqrs.estore.productservice.commontest.AbstractGlobalAxonServerTest;
import net.shyshkin.study.cqrs.estore.productservice.core.errorhandling.ErrorMessage;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class ProductAggregateErrorHandlingTest extends AbstractGlobalAxonServerTest {

    @Test
    void createProduct_errorInProductAggregate() {

        //given
        String title = "testing - throw IllegalStateException";
        String price = "666.00";
        String errorMessage = "An error took place in CreateProductCommand @CommandHandler method";

        CreateProductRestModel createProductRestModel = CreateProductRestModel.builder()
                .title(title)
                .price(new BigDecimal(price))
                .quantity(2)
                .build();

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
    }


}