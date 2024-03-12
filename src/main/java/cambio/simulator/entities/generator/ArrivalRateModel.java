package cambio.simulator.entities.generator;

import java.util.Iterator;
import java.util.function.Consumer;

import org.jetbrains.annotations.Contract;

/**
 * Represents an iterable arrival rate model that provides.
 *
 * <p>
 * {@link ArrivalRateModel}s should provide long values that represent the next target time. Target times may not
 * advance if multiple requests should be sent at once. Also throws a {@link LoadGeneratorStopException} once no more
 * times are available.
 *
 * @author Lion Wagner
 */
public abstract class ArrivalRateModel implements Iterator<Long> {

    protected Long lastTimeInstant = 0L;

    protected abstract long getDuration();

    protected abstract void resetModelIteration();

    public abstract void scaleLoad(final ScaleFactor scaleFactor);

    public final void reset() {
        resetModelIteration();
        lastTimeInstant = null;
    }

    /**
     * Generates the next target time instance as long (in epsilon time units).
     *
     * @return a long containing the next target arrival time of a request (in epsilon time units)
     * @throws LoadGeneratorStopException when no next arrival time can be determined.
     */
    @Contract("->!null")
    public final Long getNextTimeInstant() throws LoadGeneratorStopException {
        if (this.hasNext()) {
            Long next = this.next();
            if (next == null) {
                throw new LoadGeneratorStopException("No more Arrival Rate definitions available.");
            }
            lastTimeInstant = next;
            return next;

        } else if (lastTimeInstant == null) {
            throw new LoadGeneratorStopException("No Arrival Rate defined for this Generator.");
        } else {
            throw new LoadGeneratorStopException("No more Arrival Rate definitions available.");
        }
    }

    /**
     * Unsupported Operation.
     */
    @Contract(value = "_ -> fail", pure = true)
    @Override
    public final void forEachRemaining(Consumer<? super Long> action) {
        throw new UnsupportedOperationException("forEachRemaining");
    }

}
