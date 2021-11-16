package cambio.simulator.orchestration;

import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.orchestration.deprecated.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RandomLoadBalanceStrategyOrchestration implements ILoadBalancingStrategyOrchestration {
    @Override
    public MicroserviceInstance getNextServiceInstance(Service service) {

        final Set<Pod> replicaSet = service.getDeployment().getReplicaSet();

        int targetIndex = (int) (Math.random() * replicaSet.size());

        for (Pod pod : replicaSet) {
            if (--targetIndex < 0) {
                final Set<Container> containers = pod.getContainers();
                for (Container container : containers) {
                    if (container.getMicroserviceInstance().getOwner().equals(service)) {
                        return container.getMicroserviceInstance();
                    }
                }
            }
        }
        return null;
    }
}
