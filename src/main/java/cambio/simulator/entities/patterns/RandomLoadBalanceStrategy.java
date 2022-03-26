package cambio.simulator.entities.patterns;

import java.util.*;

import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.entities.microservice.NoInstanceAvailableException;
import cambio.simulator.models.MiSimModel;
import cambio.simulator.parsing.JsonTypeName;

@JsonTypeName("random")
final class RandomLoadBalanceStrategy implements ILoadBalancingStrategy {

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

        if (rng == null) {
            createRNG(runningInstances); //this will only be called once, branch prediction should take care of it later
        }

        int targetIndex = (int) (rng.nextDouble() * runningInstances.size());


        //use (hopefully) optimized implementation of get
        if (runningInstances instanceof ArrayList || runningInstances instanceof ArrayDeque) {
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

    private void createRNG(Collection<MicroserviceInstance> runningInstances) {
        runningInstances.stream().findFirst().ifPresent(instance -> {
            MiSimModel model = (MiSimModel) instance.getModel();
            rng = new Random(model.getExperimentMetaData().getSeed());
        });
    }
}
