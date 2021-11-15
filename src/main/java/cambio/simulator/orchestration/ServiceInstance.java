package cambio.simulator.orchestration;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.entities.microservice.MicroserviceInstance;
import desmoj.core.simulator.Model;

public class ServiceInstance extends MicroserviceInstance {
    private Container container = null;


    public ServiceInstance(Model model, String name, boolean showInTrace, Microservice microservice, int instanceID) {
        super(model, name, showInTrace, microservice, instanceID);
    }

    public Container getContainer() {
        return container;
    }

    public void setContainer(Container container) {
        this.container = container;
    }
}
