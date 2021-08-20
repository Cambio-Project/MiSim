package cambio.simulator.nparsing.adapter.experiement;

/**
 * Immutable data class that contains information  about the repetition behavior of a {@link ArrivalRateModel}.
 *
 * @author Lion Wagner
 */
public final class ArrivalRateModelRepetitionDescription {
    private final double repetitionSkip;
    private final boolean repeating;
    private int repetitions = 0;

    public ArrivalRateModelRepetitionDescription(boolean repeating, double repetitionSkip) {
        this.repeating = repeating;
        this.repetitionSkip = repetitionSkip;
    }

    public double getRepetitionSkip() {
        return repetitionSkip;
    }

    public boolean isRepeating() {
        return repeating;
    }

    public int getRepetitions() {
        return repetitions;
    }

    public void increment() {
        repetitions++;
    }
}
