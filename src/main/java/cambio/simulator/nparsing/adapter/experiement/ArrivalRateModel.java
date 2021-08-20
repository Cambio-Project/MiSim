package cambio.simulator.nparsing.adapter.experiement;

import java.util.Iterator;

import cambio.simulator.entities.generator.GeneratorStopException;
import desmoj.core.simulator.TimeInstant;

/**
 * @author Lion Wagner
 */
public abstract class ArrivalRateModel implements Iterator<TimeInstant> {

    protected ArrivalRateModelRepetitionDescription repetitionDescription;

    public void setRepetitionDescription(
        ArrivalRateModelRepetitionDescription repetitionDescription) {
        this.repetitionDescription = repetitionDescription;
    }

    protected abstract void reset();

    public final TimeInstant getNextTimeInstant() throws GeneratorStopException {
        if (this.hasNext()) {
            TimeInstant next = this.next();

            if (next == null) {
                throw new GeneratorStopException("No more Arrival Rate definitions available.");
            }

            return new TimeInstant(next.getTimeAsDouble() +
                repetitionDescription.getRepetitions() * repetitionDescription.getRepetitionSkip());

        } else {
            if (repetitionDescription.isRepeating()) {
                this.reset();
                repetitionDescription.increment();
                return this.getNextTimeInstant();
            } else {
                throw new GeneratorStopException("No more Arrival Rate definitions available.");
            }
        }
    }

    public final TimeInstant getFirstTimeInstant() throws GeneratorStopException {
        if (this.hasNext()) {
            return getNextTimeInstant();
        } else {
            throw new GeneratorStopException("No Arrival Rate defined for this Generator.");
        }


    }


}
