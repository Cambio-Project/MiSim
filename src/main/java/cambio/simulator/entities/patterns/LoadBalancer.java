package cambio.simulator.entities.patterns;

import java.util.*;
import java.util.stream.Collectors;

import cambio.simulator.entities.microservice.*;
import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Model;

/**
 * Wrapper Class for {@link ILoadBalancingStrategy} to encapsulate common behavior around it. E.g. capture last chosen
 * MicroserviceInstance or distribution of messages
 *
 * @author Lion Wagner
 */
public final class LoadBalancer extends Entity {
    private final Map<MicroserviceInstance, Integer> distribution = new HashMap<>();
    private final ILoadBalancingStrategy loadBalancingStrategy;
    private MicroserviceInstance lastChosenInstance = null;

    /**
     * Creates a new instance of a loadbalancer that will use the given strategy.
     */
    public LoadBalancer(Model model, String name, boolean showInTrace, ILoadBalancingStrategy loadBalancingStrategy) {
        super(model, name, showInTrace);
        if (loadBalancingStrategy != null) {
            this.loadBalancingStrategy = loadBalancingStrategy;
        } else {
            System.out.println("[Warning] " + name + ": No load balancing strategy given, defaulting to randomized "
                + "load balancing.");
            this.loadBalancingStrategy = new RandomLoadBalanceStrategy();
        }
        this.loadBalancingStrategy.onInitializedCompleted(model);
    }

    /**
     * Retrieves the next candidate for receiving a request, consulting its {@link ILoadBalancingStrategy}.
     *
     * @return a potentially reachable {@link MicroserviceInstance}
     * @throws NoInstanceAvailableException if no {@link MicroserviceInstance} is available to send requests to.
     */
    public MicroserviceInstance getNextInstance(Collection<MicroserviceInstance> instances)
        throws NoInstanceAvailableException {
        //filter for all running Instances
        Collection<MicroserviceInstance> runningInstances = instances
            .stream()
            .filter(microserviceInstance -> microserviceInstance.getState() == InstanceState.RUNNING)
            .collect(Collectors.toList());
        final MicroserviceInstance next = loadBalancingStrategy.getNextInstance(runningInstances);
        lastChosenInstance = next;

        if (next == null) {
            throw new NoInstanceAvailableException();
        }

        distribution.merge(next, 1, Integer::sum);
        return next;
    }

    public MicroserviceInstance getLastChosenInstance() {
        return lastChosenInstance;
    }

    public ILoadBalancingStrategy getLoadBalancingStrategy() {
        return loadBalancingStrategy;
    }
}
