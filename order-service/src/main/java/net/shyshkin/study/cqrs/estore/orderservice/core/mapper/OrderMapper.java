package net.shyshkin.study.cqrs.estore.orderservice.core.mapper;

import net.shyshkin.study.cqrs.estore.orderservice.command.CreateOrderCommand;
import net.shyshkin.study.cqrs.estore.orderservice.command.rest.CreateOrderRestModel;
import net.shyshkin.study.cqrs.estore.orderservice.core.events.OrderCreatedEvent;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface OrderMapper {

    OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

    CreateOrderCommand toCommand(CreateOrderRestModel restModel);

    OrderCreatedEvent toOrderCreatedEvent(CreateOrderCommand createOrderCommand);

}
