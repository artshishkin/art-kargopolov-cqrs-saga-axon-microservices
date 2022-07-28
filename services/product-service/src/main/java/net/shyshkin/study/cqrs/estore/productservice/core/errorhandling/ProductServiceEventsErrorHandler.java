package net.shyshkin.study.cqrs.estore.productservice.core.errorhandling;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.EventMessageHandler;
import org.axonframework.eventhandling.ListenerInvocationErrorHandler;

@Slf4j
public class ProductServiceEventsErrorHandler implements ListenerInvocationErrorHandler {

    @Override
    public void onError(Exception exception, EventMessage<?> event, EventMessageHandler eventHandler) throws Exception {
        log.error("Exception in EventHandler (ListenerInvocationErrorHandler): {}:{}",
                exception.getClass().getName(), exception.getMessage());
        throw exception;
    }
}
