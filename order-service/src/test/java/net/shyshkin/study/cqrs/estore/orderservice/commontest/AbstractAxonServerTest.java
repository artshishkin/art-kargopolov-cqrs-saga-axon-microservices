package net.shyshkin.study.cqrs.estore.orderservice.commontest;

import net.shyshkin.study.cqrs.estore.orderservice.core.data.OrdersRepository;
import net.shyshkin.study.cqrs.estore.orderservice.testcontainers.AxonServerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "eureka.client.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "axon.axonserver.servers=${AXON_SERVERS}"
})
@Testcontainers
public abstract class AbstractAxonServerTest {

    @Autowired
    protected TestRestTemplate restTemplate;

    @Container
    public static AxonServerContainer axonServer = AxonServerContainer.getInstance();

    @Autowired
    protected OrdersRepository ordersRepository;

}