package cambio.simulator.orchestration;

import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.orchestration.deprecated.Service;

public interface ILoadBalancingStrategyOrchestration {

    MicroserviceInstance getNextServiceInstance(Service service);
}
