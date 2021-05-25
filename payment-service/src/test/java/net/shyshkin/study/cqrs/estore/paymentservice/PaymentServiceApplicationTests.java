package net.shyshkin.study.cqrs.estore.paymentservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "axon.axonserver.enabled=false"
})
class PaymentServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
