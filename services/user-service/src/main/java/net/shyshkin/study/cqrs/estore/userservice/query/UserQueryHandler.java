package net.shyshkin.study.cqrs.estore.userservice.query;

import net.shyshkin.study.cqrs.estore.core.model.PaymentDetails;
import net.shyshkin.study.cqrs.estore.core.model.User;
import net.shyshkin.study.cqrs.estore.core.query.FetchUserPaymentDetailsQuery;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class UserQueryHandler {

    @QueryHandler
    public User findUserPaymentDetails(FetchUserPaymentDetailsQuery query) {

        PaymentDetails paymentDetails = PaymentDetails.builder()
                .cardNumber("123Card")
                .cvv("123")
                .name("ARTEM SHYSHKIN")
                .validUntilMonth(12)
                .validUntilYear(2030)
                .build();

        return User.builder()
                .firstName("Artem")
                .lastName("Shyshkin")
                .userId(query.getUserId())
                .paymentDetails(paymentDetails)
                .build();
    }

}
