package net.shyshkin.study.cqrs.estore.productservice.command.interceptors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.estore.productservice.command.CreateProductCommand;
import net.shyshkin.study.cqrs.estore.productservice.core.data.ProductLookupEntity;
import net.shyshkin.study.cqrs.estore.productservice.core.data.ProductLookupRepository;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.messaging.MessageDispatchInterceptor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

@Component
@Slf4j
@RequiredArgsConstructor
public class CreateProductCommandInterceptor implements MessageDispatchInterceptor<CommandMessage<?>> {

    static final String PRODUCT_EXISTS_PATTERN = "Product with title `%s` or product ID `%s` already exists";
    private final ProductLookupRepository lookupRepository;

    @Override
    public BiFunction<Integer, CommandMessage<?>, CommandMessage<?>> handle(List<? extends CommandMessage<?>> messages) {
        return (index, command) -> {

            log.debug("Intercepted command type: {}", command.getPayloadType());

            if (CreateProductCommand.class.equals(command.getPayloadType())) {

                CreateProductCommand createProductCommand = (CreateProductCommand) command.getPayload();

                Optional<ProductLookupEntity> productLookupEntity = lookupRepository.findByProductIdOrTitle(createProductCommand.getProductId(), createProductCommand.getTitle());

                if (productLookupEntity.isPresent()) {
                    throw new IllegalArgumentException(
                            String.format(PRODUCT_EXISTS_PATTERN,
                                    createProductCommand.getTitle(),
                                    createProductCommand.getProductId()
                            )
                    );
                }

                if (createProductCommand.getTitle().contains("`")) {
                    throw new IllegalArgumentException("Title must not contain ` symbol (fake validation)");
                }
            }


            return command;
        };
    }
}
