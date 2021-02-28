package de.rss.fachstudie.MiSim.entities.patterns;

import de.rss.fachstudie.MiSim.entities.microservice.MicroserviceInstance;

import java.util.*;

/**
 * Strategy that balances the amount of requests evenly between instances. Does not care about internal demand of the
 * requested operation.
 */
class EvenLoadBalanceStrategy implements LoadBalancingStrategy {
    private Map<MicroserviceInstance, Integer> distribution = new HashMap<>();

    /**
     * Returns the Microservice Instance the handeled the least amount of requests since
     * the last scaling operation.
     */
    @Override
    public MicroserviceInstance getNextInstance(Collection<MicroserviceInstance> runningInstances) {
        //TODO: may need optimization to cope with scaling
        if (distribution.keySet().size() != runningInstances.size()) {//if instance count changed, reset the distribution
            distribution = new HashMap<>(runningInstances.size());
            for (MicroserviceInstance instance : runningInstances) distribution.put(instance, 0);
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
