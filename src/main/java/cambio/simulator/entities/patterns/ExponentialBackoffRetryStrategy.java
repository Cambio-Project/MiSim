package cambio.simulator.entities.patterns;

import cambio.simulator.parsing.JsonTypeName;

/**
 * Represents an exponential retry backoff strategy. Generates doubles based on the formula  {@code baseBackoff *
 * base^(tries) }. The value is capped between 0 and {@code maxBackoff} (both inclusive).
 *
 * @author Lion Wagner
 */
@JsonTypeName(value = "exponential", alternativeNames = "exp")
public class ExponentialBackoffRetryStrategy implements IRetryStrategy {

    @SuppressWarnings({"FieldMayBeFinal", "CanBeFinal", "FieldCanBeLocal"})
    private double baseBackoff = 0.010;
    @SuppressWarnings({"FieldMayBeFinal", "CanBeFinal", "FieldCanBeLocal"})
    private double maxBackoff = 1;
    @SuppressWarnings({"FieldMayBeFinal", "CanBeFinal", "FieldCanBeLocal"})
    private int base = 3;


    @Override
    public double getNextDelay(int tries) {
        return Math.max(0, Math.min(baseBackoff * Math.pow(base, tries - 1), maxBackoff));
    }
}
