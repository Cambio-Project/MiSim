package cambio.simulator.orchestration.loadbalancing;

import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.entities.microservice.NoInstanceAvailableException;
import cambio.simulator.entities.patterns.ILoadBalancingStrategy;
import cambio.simulator.orchestration.environment.Container;
import cambio.simulator.orchestration.environment.ContainerState;
import cambio.simulator.orchestration.environment.Pod;
import cambio.simulator.orchestration.environment.PodState;
import cambio.simulator.orchestration.MicroserviceOrchestration;
import cambio.simulator.parsing.JsonTypeName;

import java.util.*;

@JsonTypeName("leastUtil_orchestration")
public class LeastUtilizationLoadBalanceStrategyOrchestration implements ILoadBalancingStrategy {
    @Override
    public MicroserviceInstance getNextInstance(Collection<MicroserviceInstance> runningInstances) throws NoInstanceAvailableException {
        throw new UnsupportedOperationException("Not supposed to be called in orchestration mode");
    }

    @Override
    public MicroserviceInstance getNextInstance(MicroserviceOrchestration microserviceOrchestration) {
        final Set<Pod> replicaSet = microserviceOrchestration.getDeployment().getRunningReplicas();
        MicroserviceInstance leastUtilized = null;
        for (Pod pod : replicaSet) {
            final Set<Container> containers = pod.getContainers();
            for (Container container : containers) {
                if (container.getMicroserviceInstance().getOwner().equals(microserviceOrchestration)) {
                    if (container.getContainerState() == ContainerState.RUNNING) {
                        if (leastUtilized == null || container.getMicroserviceInstance().getRelativeWorkDemand() < leastUtilized.getRelativeWorkDemand()) {
                            leastUtilized = container.getMicroserviceInstance();
                        }
                    }
                    break;
                }
            }
        }
        return leastUtilized;
    }

    public static String getName() {
        return "LeastUtilBalancer";
    }
}
