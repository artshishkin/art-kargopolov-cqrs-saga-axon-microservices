package net.shyshkin.study.cqrs.estore.productservice.mapper;

import net.shyshkin.study.cqrs.estore.productservice.command.CreateProductCommand;
import net.shyshkin.study.cqrs.estore.productservice.rest.CreateProductRestModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(imports = UUID.class)
public interface ProductMapper {

    @Mapping(target = "productId", expression = "java(UUID.randomUUID().toString())")
    CreateProductCommand toCreateCommand(CreateProductRestModel model);

}
