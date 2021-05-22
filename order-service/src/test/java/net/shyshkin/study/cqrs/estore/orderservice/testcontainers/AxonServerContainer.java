package net.shyshkin.study.cqrs.estore.orderservice.testcontainers;

import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

@TestPropertySource(properties = {
        "logging.level.org.axonframework=debug"
})
public class AxonServerContainer extends GenericContainer<AxonServerContainer> {

    private static final String IMAGE_VERSION = "axoniq/axonserver";
    private static AxonServerContainer container;

    public AxonServerContainer() {
        super(IMAGE_VERSION);
    }

    public static AxonServerContainer getInstance() {
        if (container == null) {
            container = new AxonServerContainer()
                    .withExposedPorts(8024, 8124)
                    .waitingFor(
                            Wait.forLogMessage(".*Started AxonServer in.*\\n", 1)
                    );
            ;
        }
        return container;
    }

    @Override
    public void start() {

        super.start();

        String host = container.getHost();
        Integer port = container.getMappedPort(8124);
        String servers = host + ":" + port;
        System.setProperty("AXON_SERVERS", servers);

    }

    @Override
    public void stop() {
        //do nothing, JVM handles shut down
    }
}
