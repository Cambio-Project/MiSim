package cambio.simulator.orchestration.loadbalancing;

import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.orchestration.k8objects.Service;

public interface ILoadBalancingStrategyOrchestration {

    MicroserviceInstance getNextServiceInstance(Service service);
}
