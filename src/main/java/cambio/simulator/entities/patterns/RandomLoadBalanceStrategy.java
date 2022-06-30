package cambio.simulator.entities.patterns;

import java.util.*;

import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.entities.microservice.NoInstanceAvailableException;
import cambio.simulator.models.MiSimModel;
import cambio.simulator.parsing.JsonTypeName;
import desmoj.core.simulator.Model;

/**
 * A randomized load balancing strategy. This strategy always picks a random instance from the list of available
 * instances. Hence, it is not guaranteed to be fair, but it is rather fast (specifically if the given collection of
 * running instances allows for O(1) reads).
 */
@JsonTypeName("random")
public final class RandomLoadBalanceStrategy implements ILoadBalancingStrategy {

    private Random rng = null;

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

        int targetIndex = (int) (rng.nextDouble() * runningInstances.size());

        //use (hopefully) optimized implementation of get
        if (runningInstances instanceof List) {
            return ((List<MicroserviceInstance>) runningInstances).get(targetIndex);
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

    @Override
    public void onInitializedCompleted(Model model) {
        try {
            rng = new Random(((MiSimModel) model).getExperimentMetaData().getSeed());
        } catch (ClassCastException e) {
            rng = new Random();
        }
    }
}
