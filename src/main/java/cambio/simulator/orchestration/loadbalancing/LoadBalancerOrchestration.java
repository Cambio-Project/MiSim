package cambio.simulator.orchestration.loadbalancing;

import cambio.simulator.entities.NamedEntity;
import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.entities.patterns.ILoadBalancingStrategy;
import cambio.simulator.orchestration.MicroserviceOrchestration;
import desmoj.core.simulator.Model;

public class LoadBalancerOrchestration extends NamedEntity {
    private final ILoadBalancingStrategy loadBalancingStrategy;
    private final MicroserviceOrchestration microserviceOrchestration;

    public LoadBalancerOrchestration(Model model, String name, boolean showInTrace, ILoadBalancingStrategy loadBalancingStrategy, MicroserviceOrchestration microserviceOrchestration) {
        super(model, name, showInTrace);
        this.loadBalancingStrategy = loadBalancingStrategy;
        this.microserviceOrchestration = microserviceOrchestration;
    }

    public MicroserviceInstance getNextServiceInstance() {
        return loadBalancingStrategy.getNextInstance(this.microserviceOrchestration);
    }
}
