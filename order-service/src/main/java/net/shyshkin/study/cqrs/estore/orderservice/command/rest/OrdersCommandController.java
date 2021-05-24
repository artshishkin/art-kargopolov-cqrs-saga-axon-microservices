package net.shyshkin.study.cqrs.estore.orderservice.command.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.estore.orderservice.command.CreateOrderCommand;
import net.shyshkin.study.cqrs.estore.orderservice.core.mapper.OrderMapper;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrdersCommandController {

    private final CommandGateway commandGateway;
    private final OrderMapper mapper;

    @PostMapping
    public ResponseEntity<CreateOrderCommand> createOrder(@Valid @RequestBody CreateOrderRestModel createOrderRestModel) {

        CreateOrderCommand createOrderCommand = mapper.toCommand(createOrderRestModel);
        UUID result = commandGateway.sendAndWait(createOrderCommand);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .pathSegment("{id}")
                .buildAndExpand(result)
                .toUri();
        return ResponseEntity.created(location)
                .body(createOrderCommand);
    }
}