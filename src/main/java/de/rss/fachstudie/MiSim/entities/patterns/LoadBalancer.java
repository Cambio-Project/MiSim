package de.rss.fachstudie.MiSim.entities.patterns;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import de.rss.fachstudie.MiSim.entities.microservice.InstanceState;
import de.rss.fachstudie.MiSim.entities.microservice.MicroserviceInstance;
import de.rss.fachstudie.MiSim.entities.microservice.NoInstanceAvailableException;
import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Model;

/**
 * Wrapper Class for {@link LoadBalancingStrategy} to encapsulate common behavior around it. E.g. capture last chosen
 * MicroserviceInstance or distribution of messages
 *
 * @author Lion Wagner
 */
public final class LoadBalancer extends Entity {
    private LoadBalancingStrategy loadBalancingStrategy;
    private MicroserviceInstance lastChosenInstance = null;
    private final Collection<MicroserviceInstance> instances;
    private final Map<MicroserviceInstance, Integer> distribution = new HashMap<>();

    public LoadBalancer(Model model, String name, boolean showInTrace, Collection<MicroserviceInstance> instances) {
        super(model, name, showInTrace);
        this.instances = instances;
    }

    /**
     * Retrieves the next candidate for receiving a request, consulting its {@link LoadBalancingStrategy}.
     *
     * @return a potentially reachable {@link MicroserviceInstance}
     * @throws NoInstanceAvailableException if no {@link MicroserviceInstance} is available to send requests to.
     */
    public MicroserviceInstance getNextInstance() throws NoInstanceAvailableException {
        //filter for all running Instances
        Collection<MicroserviceInstance> runningInstances = this.instances
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


    public void setLoadBalancingStrategy(LoadBalancingStrategy loadBalancingStrategy) {
        this.loadBalancingStrategy = loadBalancingStrategy;
    }

    public MicroserviceInstance getLastChosenInstance() {
        return lastChosenInstance;
    }

}
