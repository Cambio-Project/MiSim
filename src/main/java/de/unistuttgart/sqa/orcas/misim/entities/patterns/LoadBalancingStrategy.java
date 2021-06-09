package de.unistuttgart.sqa.orcas.misim.entities.patterns;

import java.util.Collection;
import java.util.Locale;

import de.unistuttgart.sqa.orcas.misim.entities.microservice.MicroserviceInstance;
import desmoj.core.simulator.ModelComponent;

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
     * @return the {@link MicroserviceInstance} to which the next request should be send.
     */
    MicroserviceInstance getNextInstance(Collection<MicroserviceInstance> runningInstances);

    /**
     * Converts the given type identifier into a {@link LoadBalancingStrategy} instance.
     *
     * @param someModelComponent A {@link ModelComponent} that can be used for potentially sending warning messages.
     * @param type               type identifier of the strategy (e.g. "even","random" or "util")
     * @return the parsed {@link LoadBalancingStrategy} or a {@link RandomLoadBalanceStrategy} if the type is unknown.
     */
    static LoadBalancingStrategy fromName(ModelComponent someModelComponent, String type) {
        switch (String.valueOf(type).toLowerCase(Locale.ROOT)) {
            case "even":
                return new EvenLoadBalanceStrategy();
            case "random":
                return new RandomLoadBalanceStrategy();
            case "util":
                return new UtilizationBalanceStrategy();
            default:
                someModelComponent.sendWarning("Defaulting to randomized load balancing.",
                    LoadBalancingStrategy.class.getTypeName(),
                    String.format("Could not find load balancing strategy \"%s\"", type),
                    "Use one of the existing Load balancing strategies.");
                return new RandomLoadBalanceStrategy();
        }
    }

}

