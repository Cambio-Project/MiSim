package cambio.simulator.orchestration.loadbalancing;

import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.orchestration.environment.Container;
import cambio.simulator.orchestration.environment.ContainerState;
import cambio.simulator.orchestration.environment.Pod;
import cambio.simulator.orchestration.k8objects.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class RandomLoadBalanceStrategyOrchestration implements ILoadBalancingStrategyOrchestration {
    @Override
    public MicroserviceInstance getNextServiceInstance(Service service) {

        final Set<Pod> replicaSet = service.getDeployment().getReplicaSet();

        List<Pod> pods = new ArrayList<>();
        pods.addAll(replicaSet);
        Collections.shuffle(pods);

        for (Pod pod : pods) {
            final Set<Container> containers = pod.getContainers();
            for (Container container : containers) {
                if (container.getMicroserviceInstance().getOwner().equals(service)) {
                    if(container.getContainerState()== ContainerState.RUNNING){
                        return container.getMicroserviceInstance();
                    }
                    //If container is not running, then try another pod
                    break;
                }
            }
        }


        return null;
    }
}
