package cambio.simulator.orchestration.loadbalancing;

import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.orchestration.environment.Container;
import cambio.simulator.orchestration.environment.ContainerState;
import cambio.simulator.orchestration.environment.Pod;
import cambio.simulator.orchestration.environment.PodState;
import cambio.simulator.orchestration.k8objects.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class LeastUtilizationLoadBalanceStrategyOrchestration implements ILoadBalancingStrategyOrchestration{
    @Override
    public MicroserviceInstance getNextServiceInstance(Service service) {



        final Set<Pod> replicaSet = service.getDeployment().getReplicaSet();
        List<Pod> pods = new ArrayList<>(replicaSet);

        MicroserviceInstance leastUtilized = null;
        for (Pod pod : pods) {
            if(pod.getPodState()== PodState.RUNNING){
                final Set<Container> containers = pod.getContainers();
                for (Container container : containers) {
                    if (container.getMicroserviceInstance().getOwner().equals(service)) {
                        if(container.getContainerState()== ContainerState.RUNNING){
                            if(leastUtilized == null || container.getMicroserviceInstance().getRelativeWorkDemand() < leastUtilized.getRelativeWorkDemand()){
                                leastUtilized = container.getMicroserviceInstance();
                            }
                        }
                        break;
                    }
                }
            }

        }

        return leastUtilized;
    }
}
