package cambio.simulator.entities.patterns;

import cambio.simulator.nparsing.adapter.JsonTypeName;
import cambio.simulator.parsing.FromJson;

/**
 * @author Lion Wagner
 */
@JsonTypeName(value = "exponential", alternativeNames = "exp")
public class ExponentialBackoffRetryStrategy implements IRetryStrategy {

    @FromJson
    @SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
    private double baseBackoff = 0.010;
    @FromJson
    @SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
    private double maxBackoff = 1;
    @FromJson
    @SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
    private int base = 3;


    @Override
    public double getNextDelay(int tries) {
        return Math.max(0, Math.min(baseBackoff * Math.pow(base, tries - 1), maxBackoff));
    }
}
