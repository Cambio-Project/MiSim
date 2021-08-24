package cambio.simulator.entities.patterns;

import java.util.Random;

import cambio.simulator.nparsing.adapter.JsonTypeName;

/**
 * @author Lion Wagner
 */
@JsonTypeName("jittering_linear")
public class JitteringLinearBackoffRetryStrategy extends LinearBackoffRetryStrategy {

    //TODO: inject random seed
    private final transient Random rng = new Random();

    @Override
    public double getNextDelay(int tries) {
        return rng.nextDouble() * super.getNextDelay(tries);
    }
}
