package net.shyshkin.study.cqrs.estore.productservice.mapper;

import net.shyshkin.study.cqrs.estore.productservice.command.CreateProductCommand;
import net.shyshkin.study.cqrs.estore.productservice.core.events.ProductCreatedEvent;
import net.shyshkin.study.cqrs.estore.productservice.rest.CreateProductRestModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

@Mapper(imports = UUID.class)
public interface ProductMapper {

    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

    @Mapping(target = "productId", expression = "java(UUID.randomUUID().toString())")
    CreateProductCommand toCreateCommand(CreateProductRestModel model);

    ProductCreatedEvent toCreatedEvent(CreateProductCommand createProductCommand);

}
