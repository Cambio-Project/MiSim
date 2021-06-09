package de.unistuttgart.sqa.orcas.misim.entities.patterns;

import java.util.Collection;

import de.unistuttgart.sqa.orcas.misim.entities.microservice.MicroserviceInstance;

class RandomLoadBalanceStrategy implements LoadBalancingStrategy {

    //TODO: inject random seed

    /**
     * Returns a random Microservice Instance of given Collection.
     */
    @Override
    public MicroserviceInstance getNextInstance(Collection<MicroserviceInstance> runningInstances) {
        if (runningInstances.size() < 1) {
            return null;
        }

        int size = (int) (Math.random() * runningInstances.size());
        for (MicroserviceInstance existingInstance : runningInstances) {
            if (--size < 0) {
                return existingInstance;
            }
        }
        throw new AssertionError();
    }
}
