package cambio.simulator.entities.patterns;

import java.util.Random;

import cambio.simulator.parsing.adapter.JsonTypeName;

/**
 * @author Lion Wagner
 */
@JsonTypeName("jittering")
public class JitteringExponentialBackoffRetryStrategy extends ExponentialBackoffRetryStrategy {

    //TODO: inject random seed
    private final transient Random rng = new Random();

    @Override
    public double getNextDelay(int tries) {
        return rng.nextDouble() * super.getNextDelay(tries);
    }
}
