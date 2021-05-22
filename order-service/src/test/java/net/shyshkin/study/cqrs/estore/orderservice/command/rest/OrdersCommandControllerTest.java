package net.shyshkin.study.cqrs.estore.orderservice.command.rest;

import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.estore.orderservice.command.CreateOrderCommand;
import net.shyshkin.study.cqrs.estore.orderservice.commontest.AbstractAxonServerTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class OrdersCommandControllerTest extends AbstractAxonServerTest {

    @Test
    void createOrder_correct() {
        //given
        CreateOrderRestModel createOrderRestModel = CreateOrderRestModel.builder()
                .productId(UUID.randomUUID())
                .addressId(UUID.randomUUID())
                .quantity(3)
                .build();

        //when
        ResponseEntity<CreateOrderCommand> responseEntity = restTemplate
                .postForEntity("/orders", createOrderRestModel, CreateOrderCommand.class);

        //then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseEntity.getHeaders().getLocation())
                .isNotNull();

        log.debug("Response entity: {}", responseEntity);

    }
}