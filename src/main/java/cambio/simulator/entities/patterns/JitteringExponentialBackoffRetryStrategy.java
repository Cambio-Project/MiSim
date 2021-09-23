package cambio.simulator.entities.patterns;

import java.util.Random;

import cambio.simulator.parsing.adapter.JsonTypeName;

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

    //TODO: inject random seed
    private final transient Random rng = new Random();

    @Override
    public double getNextDelay(int tries) {
        return rng.nextDouble() * super.getNextDelay(tries);
    }
}
