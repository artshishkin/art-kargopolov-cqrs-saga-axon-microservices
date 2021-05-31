package net.shyshkin.study.cqrs.estore.productservice.command.rest;

import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.estore.productservice.command.CreateProductCommand;
import net.shyshkin.study.cqrs.estore.productservice.commontest.AbstractAxonServerTest;
import net.shyshkin.study.cqrs.estore.productservice.core.data.ProductRepository;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Slf4j
@TestPropertySource(properties = {
        "axon.eventhandling.processors.product-group.mode=tracking",
        "logging.level.net.shyshkin.study.cqrs.estore.productservice.command.interceptors=info"
})
@DirtiesContext
class EventsReplayControllerTest extends AbstractAxonServerTest {

    @Autowired
    ProductRepository repository;

    @Autowired
    CommandGateway commandGateway;

    @Test
    void resetEvents() {
        //given
        long countInit = repository.count();
        long countLookupInit = lookupRepository.count();

        int createdProducts = createStubProducts();

        long initialCount = createdProducts + countInit;
        long initialLookupCount = createdProducts + countLookupInit;

        await()
                .timeout(2, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    assertThat(repository.count()).isEqualTo(initialCount);
                    assertThat(lookupRepository.count()).isEqualTo(initialLookupCount);
                });

        //when
        ResponseEntity<String> responseEntity = restTemplate
                .postForEntity("/management/eventProcessor/{processorName}/reset", null, String.class, "product-group");

        //then
        String responseBody = responseEntity.getBody();
        log.debug("Response body: {}", responseBody);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        await().
                timeout(3, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    long count = repository.count();
                    log.debug("Product repository contains {} products", count);
                    assertThat(count).isEqualTo(initialCount);
                    assertThat(lookupRepository.count()).isEqualTo(initialLookupCount);
                });
    }

    private int createStubProducts() {
        List<CompletableFuture<Object>> completableFutureList = IntStream
                .rangeClosed(1, 100)
                .mapToObj(i -> createOneProduct())
                .map(createProductCommand -> commandGateway.send(createProductCommand))
                .collect(Collectors.toList());
        List<Object> objectList = completableFutureList
                .stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        log.debug("Created {} products", objectList.size());

        return objectList.size();
    }

    private CreateProductCommand createOneProduct() {
        return CreateProductCommand.builder()
                .price(randomPrice())
                .title(Faker.instance().commerce().productName() + UUID.randomUUID())
                .productId(UUID.randomUUID())
                .quantity(Faker.instance().random().nextInt(1, 10))
                .build();
    }

    private BigDecimal randomPrice() {
        return new BigDecimal(Faker.instance().commerce().price().replace(",", "."));
    }


}