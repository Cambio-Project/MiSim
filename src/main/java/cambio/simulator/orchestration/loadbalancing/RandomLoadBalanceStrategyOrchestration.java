package cambio.simulator.orchestration.loadbalancing;

import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.entities.microservice.NoInstanceAvailableException;
import cambio.simulator.entities.patterns.ILoadBalancingStrategy;
import cambio.simulator.orchestration.environment.Container;
import cambio.simulator.orchestration.environment.ContainerState;
import cambio.simulator.orchestration.environment.Pod;
import cambio.simulator.orchestration.environment.PodState;
import cambio.simulator.orchestration.k8objects.Service;
import cambio.simulator.parsing.JsonTypeName;

import java.util.*;
@JsonTypeName("random_orchestration")
public class RandomLoadBalanceStrategyOrchestration implements ILoadBalancingStrategy {
    @Override
    public MicroserviceInstance getNextInstance(Collection<MicroserviceInstance> runningInstances) throws NoInstanceAvailableException {
        throw new UnsupportedOperationException("Not supposed to be called in orchestration mode");
    }

    @Override
    public MicroserviceInstance getNextInstance(Service service) throws NoInstanceAvailableException {
        final Set<Pod> replicaSet = service.getDeployment().getReplicaSet();

        List<Pod> pods = new ArrayList<>(replicaSet);
        Collections.shuffle(pods);

        for (Pod pod : pods) {
            if (pod.getPodState() == PodState.RUNNING) {
                final Set<Container> containers = pod.getContainers();
                for (Container container : containers) {
                    if (container.getMicroserviceInstance().getOwner().equals(service)) {
                        if (container.getContainerState() == ContainerState.RUNNING) {
                            return container.getMicroserviceInstance();
                        }
                        //If container is not running, then try another pod
                        break;
                    }
                }
            }
        }
        return null;
    }

    public static String getName(){
        return "RandomLoadBalancer";
    }
}
