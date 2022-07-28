package net.shyshkin.study.cqrs.estore.productservice.command;

import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.estore.core.commands.CancelProductReservationCommand;
import net.shyshkin.study.cqrs.estore.core.commands.ReserveProductCommand;
import net.shyshkin.study.cqrs.estore.productservice.commontest.AbstractGlobalAxonServerTest;
import net.shyshkin.study.cqrs.estore.productservice.core.data.ProductRepository;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Slf4j
class ProductSnapshotTest extends AbstractGlobalAxonServerTest {

    @Autowired
    CommandGateway commandGateway;

    @Autowired
    ProductRepository productRepository;

    @Test
    void snapshotCreation() {
        //given
        UUID productId = UUID.randomUUID();

        String price = Faker.instance().commerce().price().replace(",", ".");
        log.debug("price: {}", price);
        CreateProductCommand createProductCommand = CreateProductCommand.builder()
                .productId(productId)
                .quantity(5)
                .title(Faker.instance().commerce().productName())
                .price(new BigDecimal(price))
                .build();

        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ReserveProductCommand reserveProductCommand = ReserveProductCommand.builder()
                .orderId(orderId)
                .userId(userId)
                .quantity(1)
                .productId(productId)
                .build();

        CancelProductReservationCommand cancelProductReservationCommand = CancelProductReservationCommand.builder()
                .productId(productId)
                .orderId(orderId)
                .userId(userId)
                .quantity(1)
                .reason("Some fake reason")
                .build();

        //when
        commandGateway.sendAndWait(createProductCommand);
        expectQuantityForOrder(5, productId);

        commandGateway.sendAndWait(reserveProductCommand);
        expectQuantityForOrder(4, productId);

        commandGateway.sendAndWait(cancelProductReservationCommand);
        expectQuantityForOrder(5, productId);

        //then
        log.debug("View logs for \n`Reading events for aggregate id` - 3 times\n" +
                "`ProductReservedEvent is called`\n" +
                "`ProductReservationCancelledEvent is called`\n" +
                "`Snapshot created for aggregate type ProductAggregate`");
    }

    private void expectQuantityForOrder(int quantity, UUID productId) {
        await()
                .timeout(1, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(productRepository.findByProductId(productId))
                        .hasValueSatisfying(entity -> assertThat(entity.getQuantity()).isEqualTo(quantity)));
    }
}