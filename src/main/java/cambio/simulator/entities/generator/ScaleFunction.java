package cambio.simulator.entities.generator;

import java.util.Objects;

/**
 * A function that defines the shape of the time-dependent {@link ScaleFactor}.
 */
@FunctionalInterface
public interface ScaleFunction {

    /**
     * Maps a textual type description to the default functions.
     *
     * @param type the type as String
     * @return the function, constant by default
     */
    static ScaleFunction detect(final String type) {
        Objects.requireNonNull(type);
        return switch (type.toLowerCase().trim()) {
            case "linear" -> linear();
            case "exponential" -> exponential();
            default -> constant();
        };
    }

    /**
     * Creates a new function that reverts the provided function. This is just a wrapper, so if the original function
     * is changed, also the reverted function will change.
     *
     * @param scaleFunction the function that should be reverted.
     * @return the reverted function.
     */
    static ScaleFunction revert(ScaleFunction scaleFunction) {
        return progressRelative -> scaleFunction.map(1 - progressRelative);
    }

    static ScaleFunction constant() {
        return progressRelative -> 1;
    }

    static ScaleFunction linear() {
        return progressRelative -> progressRelative;
    }

    static ScaleFunction exponential() {
        return progressRelative -> 0.01 * Math.pow(1 + 99, progressRelative);
    }

    /**
     * Maps the progress (time) to a value describing how much scaling should be applied.
     *
     * @param progressRelative Value between 0 and 1.
     * @return Value between 0 and 1.
     */
    double map(final double progressRelative);
}
