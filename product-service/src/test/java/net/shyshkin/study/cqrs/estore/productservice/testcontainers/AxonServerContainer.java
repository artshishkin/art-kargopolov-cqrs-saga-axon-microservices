package net.shyshkin.study.cqrs.estore.productservice.testcontainers;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

public class AxonServerContainer extends GenericContainer<AxonServerContainer> {

    private static final String IMAGE_VERSION = "axoniq/axonserver:4.5.1";
    private static AxonServerContainer container;

    public AxonServerContainer() {
        super(IMAGE_VERSION);
    }

    public static AxonServerContainer createNewInstance() {
        return new AxonServerContainer()
                .withExposedPorts(8024, 8124)
                .waitingFor(
                        Wait.forLogMessage(".*Started AxonServer in.*\\n", 1)
                );
    }

    public static AxonServerContainer getGlobalInstance() {
        if (container == null) {
            container = createNewInstance();
        }
        return container;
    }

    @Override
    public void start() {

        super.start();

        String host = this.getHost();
        Integer port = this.getMappedPort(8124);
        String servers = host + ":" + port;
        System.setProperty("AXON_SERVERS", servers);

    }

    @Override
    public void stop() {
        //do nothing, JVM handles shut down
    }
}
