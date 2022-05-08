package cambio.simulator.entities.patterns;

import java.util.Random;

import cambio.simulator.models.MiSimModel;
import cambio.simulator.parsing.JsonTypeName;

/**
 * Represents a jittering exponential retry backoff strategy. Generates doubles based on the formula  {@code
 * random(0,baseBackoff * base^(tries) }). The value is capped between 0 (inclusive) and {@code maxBackoff}
 * (exclusive).
 *
 * @author Lion Wagner
 * @see <a href= "https://aws.amazon.com/de/blogs/architecture/exponential-backoff-and-jitter/">
 *     AWS article about backoff strategies</a>
 */
@JsonTypeName("jittering")
public class JitteringExponentialBackoffRetryStrategy extends ExponentialBackoffRetryStrategy {

    private transient Random rng;

    @Override
    public double getNextDelay(int tries) {
        return rng.nextDouble() * super.getNextDelay(tries);
    }

    @Override
    public void onInitializedCompleted(MiSimModel model) {
        super.onInitializedCompleted(model);
        rng = new Random(model.getExperimentMetaData().getSeed());
    }
}
