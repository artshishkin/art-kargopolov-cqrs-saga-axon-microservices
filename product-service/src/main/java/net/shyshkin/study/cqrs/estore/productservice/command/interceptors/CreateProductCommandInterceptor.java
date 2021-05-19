package net.shyshkin.study.cqrs.estore.productservice.command.interceptors;

import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.estore.productservice.command.CreateProductCommand;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.messaging.MessageDispatchInterceptor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.BiFunction;

@Component
@Slf4j
public class CreateProductCommandInterceptor implements MessageDispatchInterceptor<CommandMessage<?>> {

    @Override
    public BiFunction<Integer, CommandMessage<?>, CommandMessage<?>> handle(List<? extends CommandMessage<?>> messages) {
        return (index, command) -> {

            log.debug("Intercepted command type: {}", command.getPayloadType());

            if (CreateProductCommand.class.equals(command.getPayloadType())) {

                CreateProductCommand createProductCommand = (CreateProductCommand) command.getPayload();

                if (createProductCommand.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("Price can not be less or equal to zero");
                }

                if (createProductCommand.getTitle() == null || createProductCommand.getTitle().isEmpty()) {
                    throw new IllegalArgumentException("Title can not be empty");
                }

                if (createProductCommand.getTitle().contains("`")) {
                    throw new IllegalArgumentException("Title must not contain ` symbol (fake validation)");
                }
            }


            return command;
        };
    }
}
