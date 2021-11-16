package cambio.simulator.orchestration;

import cambio.simulator.entities.NamedEntity;
import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.orchestration.deprecated.Service;
import desmoj.core.simulator.Model;

public class LoadBalancerOrchestration extends NamedEntity {
    private ILoadBalancingStrategyOrchestration loadBalancingStrategy;
    private Service service;

    public LoadBalancerOrchestration(Model model, String name, boolean showInTrace, ILoadBalancingStrategyOrchestration loadBalancingStrategy, Service service) {
        super(model, name, showInTrace);
        this.loadBalancingStrategy = loadBalancingStrategy;
        this.service = service;
    }

    public MicroserviceInstance getNextServiceInstance() {
        return loadBalancingStrategy.getNextServiceInstance(this.service);
    }
}
