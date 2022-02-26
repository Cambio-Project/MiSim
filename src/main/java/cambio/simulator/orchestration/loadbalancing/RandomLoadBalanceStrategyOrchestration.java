package cambio.simulator.orchestration.loadbalancing;

import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.entities.microservice.NoInstanceAvailableException;
import cambio.simulator.entities.patterns.ILoadBalancingStrategy;
import cambio.simulator.models.MiSimModel;
import cambio.simulator.orchestration.environment.Container;
import cambio.simulator.orchestration.environment.ContainerState;
import cambio.simulator.orchestration.environment.Pod;
import cambio.simulator.orchestration.environment.PodState;
import cambio.simulator.orchestration.MicroserviceOrchestration;
import cambio.simulator.orchestration.management.ManagementPlane;
import cambio.simulator.parsing.JsonTypeName;

import java.util.*;

@JsonTypeName("random_orchestration")
public class RandomLoadBalanceStrategyOrchestration implements ILoadBalancingStrategy {
    @Override
    public MicroserviceInstance getNextInstance(Collection<MicroserviceInstance> runningInstances) throws NoInstanceAvailableException {
        throw new UnsupportedOperationException("Not supposed to be called in orchestration mode");
    }

    @Override
    public MicroserviceInstance getNextInstance(MicroserviceOrchestration microserviceOrchestration) throws NoInstanceAvailableException {
        final Set<Pod> replicaSet = microserviceOrchestration.getDeployment().getRunningReplicas();

        List<Pod> pods = new ArrayList<>(replicaSet);
        Collections.shuffle(pods, new Random(ManagementPlane.getInstance().getExperimentSeed()));

        for (Pod pod : pods) {
            final Set<Container> containers = pod.getContainers();
            for (Container container : containers) {
                if (container.getMicroserviceInstance().getOwner().equals(microserviceOrchestration)) {
                    if (container.getContainerState() == ContainerState.RUNNING) {
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
