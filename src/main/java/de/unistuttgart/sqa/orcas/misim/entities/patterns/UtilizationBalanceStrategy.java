package de.unistuttgart.sqa.orcas.misim.entities.patterns;

import java.util.Collection;
import java.util.Comparator;

import de.unistuttgart.sqa.orcas.misim.entities.microservice.MicroserviceInstance;

/**
 * Strategy that chooses the least utilized Microservice Instance by current relative Queue demand.
 */
class UtilizationBalanceStrategy implements LoadBalancingStrategy {

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
