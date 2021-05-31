package net.shyshkin.study.cqrs.estore.productservice.command;

import net.shyshkin.study.cqrs.estore.core.commands.CancelProductReservationCommand;
import net.shyshkin.study.cqrs.estore.core.commands.ReserveProductCommand;
import net.shyshkin.study.cqrs.estore.core.events.ProductReservationCancelledEvent;
import net.shyshkin.study.cqrs.estore.core.events.ProductReservedEvent;
import net.shyshkin.study.cqrs.estore.productservice.core.events.ProductCreatedEvent;
import net.shyshkin.study.cqrs.estore.productservice.mapper.ProductMapper;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

import java.math.BigDecimal;
import java.util.UUID;

@Aggregate
public class ProductAggregate {

    @AggregateIdentifier
    private UUID productId;
    private String title;
    private BigDecimal price;
    private Integer quantity;


    public ProductAggregate() {
    }

    @CommandHandler
    public ProductAggregate(CreateProductCommand createProductCommand) {

        if (createProductCommand.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price can not be less or equal to zero");
        }

        if (createProductCommand.getTitle() == null || createProductCommand.getTitle().isEmpty()) {
            throw new IllegalArgumentException("Title can not be empty");
        }

        ProductCreatedEvent productCreatedEvent = ProductMapper.INSTANCE.toCreatedEvent(createProductCommand);

        AggregateLifecycle.apply(productCreatedEvent);

        if (productCreatedEvent.getTitle().contains("throw IllegalStateException"))
            throw new IllegalStateException("An error took place in CreateProductCommand @CommandHandler method");
    }

    @CommandHandler
    public void handle(ReserveProductCommand reserveProductCommand) {
        if (quantity < reserveProductCommand.getQuantity()) {
            throw new IllegalArgumentException("Insufficient number of items in stock");
        }

        ProductReservedEvent productReservedEvent = ProductReservedEvent.builder()
                .orderId(reserveProductCommand.getOrderId())
                .productId(reserveProductCommand.getProductId())
                .quantity(reserveProductCommand.getQuantity())
                .userId(reserveProductCommand.getUserId())
                .build();

        AggregateLifecycle.apply(productReservedEvent);
    }

    @CommandHandler
    public void handle(CancelProductReservationCommand cancelProductReservationCommand) {
        ProductReservationCancelledEvent productReservationCancelledEvent = ProductReservationCancelledEvent.builder()
                .orderId(cancelProductReservationCommand.getOrderId())
                .productId(cancelProductReservationCommand.getProductId())
                .quantity(cancelProductReservationCommand.getQuantity())
                .userId(cancelProductReservationCommand.getUserId())
                .reason(cancelProductReservationCommand.getReason())
                .build();

        AggregateLifecycle.apply(productReservationCancelledEvent);
    }

    @EventSourcingHandler
    public void on(ProductCreatedEvent productCreatedEvent) {
        this.productId = productCreatedEvent.getProductId();
        this.title = productCreatedEvent.getTitle();
        this.price = productCreatedEvent.getPrice();
        this.quantity = productCreatedEvent.getQuantity();
    }

    @EventSourcingHandler
    public void on(ProductReservedEvent productReservedEvent) {
        this.quantity -= productReservedEvent.getQuantity();
    }

    @EventSourcingHandler
    public void on(ProductReservationCancelledEvent productReservationCancelledEvent) {
        this.quantity += productReservationCancelledEvent.getQuantity();
    }

}
