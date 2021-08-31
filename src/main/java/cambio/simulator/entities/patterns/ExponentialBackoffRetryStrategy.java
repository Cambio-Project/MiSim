package cambio.simulator.entities.patterns;

import cambio.simulator.parsing.adapter.JsonTypeName;

/**
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
