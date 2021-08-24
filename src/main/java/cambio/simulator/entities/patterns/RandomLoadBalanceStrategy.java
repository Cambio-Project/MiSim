package cambio.simulator.entities.patterns;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.entities.microservice.NoInstanceAvailableException;
import cambio.simulator.nparsing.adapter.JsonTypeName;

@JsonTypeName("random")
public final class RandomLoadBalanceStrategy implements ILoadBalancingStrategy {

    //TODO: inject random seed

    /**
     * Returns a random Microservice Instance of given Collection.
     *
     * @throws NoInstanceAvailableException if the provided collection is empty or null
     */
    @Override
    public MicroserviceInstance getNextInstance(Collection<MicroserviceInstance> runningInstances)
        throws NoInstanceAvailableException {

        if (runningInstances == null || runningInstances.size() == 0) {
            throw new NoInstanceAvailableException();
        }

        int targetIndex = (int) (Math.random() * runningInstances.size());


        //use (hopefully) optimized implementation of get
        if (runningInstances instanceof List) {
            return ((ArrayList<MicroserviceInstance>) runningInstances).get(targetIndex);
        }

        //otherwise, we iterate to the searched index
        for (MicroserviceInstance existingInstance : runningInstances) {
            if (--targetIndex < 0) {
                return existingInstance;
            }
        }

        //this case can never be reached
        throw new AssertionError();
    }
}
