package cambio.simulator.entities.generator;

import java.util.Iterator;
import java.util.function.Consumer;

import org.jetbrains.annotations.Contract;

/**
 * @author Lion Wagner
 */
public abstract class ArrivalRateModel implements Iterator<Double> {

    protected Double lastTimeInstant = null;

    protected abstract double getDuration();

    protected abstract void resetModelIteration();

    public final void reset() {
        resetModelIteration();
        lastTimeInstant = null;
    }

    @Contract("->!null")
    public final Double getNextTimeInstant() throws LoadGeneratorStopException {
        if (this.hasNext()) {
            Double next = this.next();
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

    @Override
    public final void forEachRemaining(Consumer<? super Double> action) {
        throw new UnsupportedOperationException("forEachRemaining");
    }
}
