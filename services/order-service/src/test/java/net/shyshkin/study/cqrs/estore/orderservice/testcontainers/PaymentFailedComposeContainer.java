package net.shyshkin.study.cqrs.estore.orderservice.testcontainers;

import lombok.Getter;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;

@Getter
public class PaymentFailedComposeContainer extends DockerComposeContainer<PaymentFailedComposeContainer> {

    private static final String COMPOSE_FILE_PATH = "src/test/resources/compose-test-wo-payment.yml";
    private static PaymentFailedComposeContainer container;

    private String productServiceHost;
    private Integer productServicePort;

    public PaymentFailedComposeContainer() {
        super(new File(COMPOSE_FILE_PATH));
    }

    public static PaymentFailedComposeContainer getInstance() {
        if (container == null) {
            container = new PaymentFailedComposeContainer()
                    .withExposedService("axon-server_1", 8124,
                            Wait.forLogMessage(".*Started AxonServer in.*\\n", 1))
                    .withExposedService("product-service_1", 8080,
                            Wait.forHealthcheck()
                    );

        }
        return container;
    }

    @Override
    public void start() {

        super.start();

        String axonHost = container.getServiceHost("axon-server_1", 8124);
        Integer axonPort = container.getServicePort("axon-server_1", 8124);
        String servers = axonHost + ":" + axonPort;
        System.setProperty("AXON_SERVERS", servers);

        productServiceHost = container.getServiceHost("product-service_1", 8080);
        productServicePort = container.getServicePort("product-service_1", 8080);

    }

    @Override
    public void stop() {
        //do nothing, JVM handles shut down
    }
}
