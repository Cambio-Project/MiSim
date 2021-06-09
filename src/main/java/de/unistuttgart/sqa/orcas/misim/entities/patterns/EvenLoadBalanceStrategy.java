package de.unistuttgart.sqa.orcas.misim.entities.patterns;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import de.unistuttgart.sqa.orcas.misim.entities.microservice.MicroserviceInstance;

/**
 * Strategy that balances the amount of requests evenly between instances. Does not care about internal demand of the
 * requested operation.
 */
class EvenLoadBalanceStrategy implements LoadBalancingStrategy {
    private Map<MicroserviceInstance, Integer> distribution = new HashMap<>();

    /**
     * Returns the Microservice Instance the handeled the least amount of requests since the last scaling operation.
     */
    @Override
    public MicroserviceInstance getNextInstance(Collection<MicroserviceInstance> runningInstances) {
        //TODO: may need optimization to cope with scaling
        //if instance count changed, reset the distribution
        if (distribution.keySet().size() != runningInstances.size()) {
            distribution = new HashMap<>(runningInstances.size());
            for (MicroserviceInstance instance : runningInstances) {
                distribution.put(instance, 0);
            }
        }

        MicroserviceInstance instance = distribution.entrySet()
            .stream()
            .min(Comparator.comparingInt(Map.Entry::getValue))
            .map(Map.Entry::getKey)
            .orElse(null);
        if (instance != null) {
            distribution.merge(instance, 1, Integer::sum);
        }

        return instance;
    }
}
