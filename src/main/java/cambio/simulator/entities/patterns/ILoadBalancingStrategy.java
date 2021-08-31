package cambio.simulator.entities.patterns;

import java.util.Collection;

import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.entities.microservice.NoInstanceAvailableException;

/**
 * Interface for a load balancing strategy.
 *
 * <p>
 * Implementations will be provided a collection of running instances and should be able to select one of these as next
 * target.
 */
public interface ILoadBalancingStrategy extends IStrategy {

    /**
     * Searches through the list of available instances to find the most suitable to receive the next request.
     *
     * @param runningInstances collection of all currently running instances.
     * @return the {@link MicroserviceInstance} to which the next request should be sent.
     */
    MicroserviceInstance getNextInstance(Collection<MicroserviceInstance> runningInstances) throws
        NoInstanceAvailableException;

}

