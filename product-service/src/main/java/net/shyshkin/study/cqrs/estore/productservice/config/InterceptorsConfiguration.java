package net.shyshkin.study.cqrs.estore.productservice.config;

import net.shyshkin.study.cqrs.estore.productservice.command.interceptors.CreateProductCommandInterceptor;
import org.axonframework.commandhandling.CommandBus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InterceptorsConfiguration {

    @Autowired
    public void registerCreateProductCommandInterceptor(CreateProductCommandInterceptor createProductCommandInterceptor, CommandBus commandBus) {
        commandBus.registerDispatchInterceptor(createProductCommandInterceptor);
    }

}
