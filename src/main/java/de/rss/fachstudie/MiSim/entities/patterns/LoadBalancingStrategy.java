package de.rss.fachstudie.MiSim.entities.patterns;

import java.util.Collection;
import java.util.Locale;

import de.rss.fachstudie.MiSim.entities.microservice.MicroserviceInstance;
import desmoj.core.simulator.Model;

/**
 * Interface for a load balancing strategy.
 *
 * <p>
 * Implementations will be provided a collection of running instances and should be able to select one of these as next
 * target.
 */
public interface LoadBalancingStrategy {

    /**
     * Searches through the list of available instances to find the most suitable to receive the next request.
     *
     * @param runningInstances collection of all currently running instances.
     *
     * @return the {@link MicroserviceInstance} to which the next request should be send.
     */
    MicroserviceInstance getNextInstance(Collection<MicroserviceInstance> runningInstances);

    static LoadBalancingStrategy fromName(Model model, String name) {
        switch (String.valueOf(name).toLowerCase(Locale.ROOT)) {
            case "even":
                return new EvenLoadBalanceStrategy();
            case "random":
                return new RandomLoadBalanceStrategy();
            case "util":
                return new UtilizationBalanceStrategy();
            default:
                model.sendWarning("Defaulting to randomized load balancing.",
                                  LoadBalancingStrategy.class.getTypeName(),
                                  String.format("Could not find load balancing strategy \"%s\"", name),
                                  "Use one of the existing Load balancing strategies.");
                return new RandomLoadBalanceStrategy();
        }
    }

}

