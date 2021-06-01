package net.shyshkin.study.cqrs.estore.productservice.mapper;

import net.shyshkin.study.cqrs.estore.core.commands.CancelProductReservationCommand;
import net.shyshkin.study.cqrs.estore.core.commands.ReserveProductCommand;
import net.shyshkin.study.cqrs.estore.core.events.ProductReservationCancelledEvent;
import net.shyshkin.study.cqrs.estore.core.events.ProductReservedEvent;
import net.shyshkin.study.cqrs.estore.productservice.command.CreateProductCommand;
import net.shyshkin.study.cqrs.estore.productservice.command.rest.CreateProductRestModel;
import net.shyshkin.study.cqrs.estore.productservice.core.data.ProductEntity;
import net.shyshkin.study.cqrs.estore.productservice.core.data.ProductLookupEntity;
import net.shyshkin.study.cqrs.estore.productservice.core.events.ProductCreatedEvent;
import net.shyshkin.study.cqrs.estore.productservice.query.rest.ProductRestModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

@Mapper(imports = UUID.class)
public interface ProductMapper {

    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

    @Mapping(target = "productId", expression = "java(UUID.randomUUID())")
    CreateProductCommand toCreateCommand(CreateProductRestModel model);

    ProductCreatedEvent toCreatedEvent(CreateProductCommand createProductCommand);

    ProductEntity toProductEntity(ProductCreatedEvent event);

    ProductRestModel toProductRestModel(ProductEntity entity);

    ProductLookupEntity toProductLookupEntity(ProductCreatedEvent event);

    ProductReservedEvent toEvent(ReserveProductCommand command);

    ProductReservationCancelledEvent toEvent(CancelProductReservationCommand command);

}
