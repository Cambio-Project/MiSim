package cambio.simulator.entities.patterns;

import java.util.Collection;
import java.util.Comparator;

import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.parsing.JsonTypeName;

/**
 * Strategy that chooses the least utilized Microservice Instance by current relative Queue demand.
 */
@JsonTypeName("util")
class UtilizationBalanceStrategy implements ILoadBalancingStrategy {

    /**
     * Returns a the instance of the list, which currently has the lowest demand left.
     */
    @Override
    public MicroserviceInstance getNextInstance(Collection<MicroserviceInstance> runningInstances) {
        return runningInstances.stream()
            .min(Comparator.comparingDouble(MicroserviceInstance::getRelativeWorkDemand))
            .orElse(null);
    }
}
