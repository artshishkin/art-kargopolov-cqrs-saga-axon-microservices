package net.shyshkin.study.cqrs.estore.orderservice.testcontainers;

import lombok.Getter;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;

@Getter
public class TestComposeContainer extends DockerComposeContainer<TestComposeContainer> {

    private static final String COMPOSE_FILE_PATH = "src/test/resources/compose-test.yml";
    private static TestComposeContainer container;

    private String productServiceHost;
    private Integer productServicePort;

    public TestComposeContainer() {
        super(new File(COMPOSE_FILE_PATH));
    }

    public static TestComposeContainer getInstance() {
        if (container == null) {
            container = new TestComposeContainer()
                    .withExposedService("axon-server_1", 8124,
                            Wait.forLogMessage(".*Started AxonServer in.*\\n", 1))
                    .withExposedService("product-service_1", 8080,
                            Wait.forHealthcheck()
                    )
                    .waitingFor("payment-service_1", Wait.forHealthcheck());

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
