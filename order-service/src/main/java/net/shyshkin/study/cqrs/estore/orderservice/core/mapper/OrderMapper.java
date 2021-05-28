package net.shyshkin.study.cqrs.estore.orderservice.core.mapper;

import net.shyshkin.study.cqrs.estore.core.commands.ReserveProductCommand;
import net.shyshkin.study.cqrs.estore.orderservice.command.CreateOrderCommand;
import net.shyshkin.study.cqrs.estore.orderservice.command.rest.CreateOrderRestModel;
import net.shyshkin.study.cqrs.estore.orderservice.core.data.OrderEntity;
import net.shyshkin.study.cqrs.estore.orderservice.core.events.OrderCreatedEvent;
import net.shyshkin.study.cqrs.estore.orderservice.core.model.OrderSummary;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface OrderMapper {

    OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

    CreateOrderCommand toCommand(CreateOrderRestModel restModel);

    OrderCreatedEvent toOrderCreatedEvent(CreateOrderCommand createOrderCommand);

    OrderEntity toEntity(OrderCreatedEvent event);

    ReserveProductCommand toReserveProductCommand(OrderCreatedEvent event);

    OrderSummary toOrderSummary(OrderEntity orderEntity);

}
