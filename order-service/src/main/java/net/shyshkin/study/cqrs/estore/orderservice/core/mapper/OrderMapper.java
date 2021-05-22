package net.shyshkin.study.cqrs.estore.orderservice.core.mapper;

import net.shyshkin.study.cqrs.estore.orderservice.command.CreateOrderCommand;
import net.shyshkin.study.cqrs.estore.orderservice.command.rest.CreateOrderRestModel;
import org.mapstruct.Mapper;

@Mapper
public interface OrderMapper {

    CreateOrderCommand toCommand(CreateOrderRestModel restModel);
}
