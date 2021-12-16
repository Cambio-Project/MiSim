package cambio.simulator.entities.patterns;

import java.util.Collection;

import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.entities.microservice.NoInstanceAvailableException;
import cambio.simulator.orchestration.MicroserviceOrchestration;

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

    /**
     * In case we use the orchestration plugin we dont have a collection of microservice instances, we have a MicroserviceOrchestration
     * (like we have in Kubernetes for abstracting network access to a group of instances). default method just for
     * compatability with old version. Inside the orchestration plugin, only this method is used. Other method throws
     * UnsupportedOperationException if used in orchestration mode.
     * @param microserviceOrchestration
     * @return
     * @throws NoInstanceAvailableException
     */
    default MicroserviceInstance getNextInstance(MicroserviceOrchestration microserviceOrchestration) throws
            NoInstanceAvailableException {
        return null;
    }

}

