package cambio.simulator.orchestration.loadbalancing;

import cambio.simulator.entities.NamedEntity;
import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.entities.patterns.ILoadBalancingStrategy;
import cambio.simulator.orchestration.k8objects.Service;
import desmoj.core.simulator.Model;

public class LoadBalancerOrchestration extends NamedEntity {
    private ILoadBalancingStrategy loadBalancingStrategy;
    private Service service;

    public LoadBalancerOrchestration(Model model, String name, boolean showInTrace, ILoadBalancingStrategy loadBalancingStrategy, Service service) {
        super(model, name, showInTrace);
        this.loadBalancingStrategy = loadBalancingStrategy;
        this.service = service;
    }

    public MicroserviceInstance getNextServiceInstance() {
        return loadBalancingStrategy.getNextInstance(this.service);
    }
}
