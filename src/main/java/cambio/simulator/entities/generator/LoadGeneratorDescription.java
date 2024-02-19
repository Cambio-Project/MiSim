package cambio.simulator.entities.generator;

import java.util.concurrent.TimeUnit;

import cambio.simulator.entities.microservice.Operation;
import cambio.simulator.events.ISelfScheduled;
import com.google.gson.annotations.SerializedName;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeOperations;
import desmoj.core.simulator.TimeSpan;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * This class represents the description of a load generator.
 *
 * @author Lion Wagner
 */
public abstract class LoadGeneratorDescription implements ISelfScheduled {

    @SerializedName(value = "start", alternate = {"initial_arrival_time", "arrival_time"})
    protected double initialArrivalTime = 0;

    @SerializedName(value = "stop", alternate = {"final_time", "stop_time", "end", "end_time"})
    protected double stopTime = Double.POSITIVE_INFINITY;

    @SerializedName(value = "repeating")
    protected boolean repeating = false;

    @SerializedName(value = "repetitions", alternate = {"max_repetitions"})
    protected double maxRepetitions = Double.POSITIVE_INFINITY;

    @SerializedName(value = "skip", alternate = {"repetition_skip", "repetition"})
    protected double repetitionSkip = 0.0d;

    @SerializedName(value = "operation", alternate = {"target_operation", "target"})
    protected Operation targetOperation = null;

    @SerializedName(value = "name", alternate = {"generator_name"})
    private String name = null;

    private transient ArrivalRateModel arrivalRateModel = null;

    private transient int repetitions = 0;

    public LoadGeneratorDescription() {
    }

    /**
     * Should create an {@link ArrivalRateModel} to describe when the load generator generates new requests.
     *
     * @return the arrival rate description in form of an {@link ArrivalRateModel}
     */
    protected abstract ArrivalRateModel createArrivalRateModel();

    /**
     * Tries to initialize the underlying {@link ArrivalRateModel}.
     *
     * <p>
     * This method has to be called before an {@link LoadGeneratorDescriptionExecutor} can execute this description.
     *
     * @throws IllegalStateException if the arrival rate model was initialized already.
     */
    public final void initializeArrivalRateModel() {
        if (arrivalRateModel != null) {
            throw new IllegalStateException("Arrival rate model was already initialized");
        }
        arrivalRateModel = createArrivalRateModel();
    }


    /**
     * Returns the initial arrival time of the load generator, alias the "start" to which the arrival rate profile is
     * shifted to.
     *
     * @return The initial arrival time of the load generator.
     * @throws LoadGeneratorStopException when the arrival rate model has no defined arrivals.
     */
    public final TimeInstant getInitialArrivalTime() throws LoadGeneratorStopException {
        if (!arrivalRateModel.hasNext()) {
            throw new LoadGeneratorStopException("Load generator has no defined arrivals.");
        }
        return getNextTimeInstant(new TimeInstant(initialArrivalTime, TimeUnit.SECONDS));
    }

    /**
     * Grabs the next target time for when a new request should be sent.
     *
     * @return the target time when the next request should be sent.
     * @throws LoadGeneratorStopException if max repetitions are reached, or the underlying {@link ArrivalRateModel}
     *                                    does not describe any further arrival times.
     */
    @NotNull
    @Contract("_->!null")
    public final TimeInstant getNextTimeInstant(TimeInstant presentTime) throws LoadGeneratorStopException {
        if (presentTime.getTimeAsDouble() >= stopTime) {
            throw new LoadGeneratorStopException("Load generator has finished (stop time reached).");
        }

        if (arrivalRateModel.hasNext()) {
            long nextTarget = arrivalRateModel.getNextTimeInstant();
            if (arrivalRateModel.getDuration() < Long.MAX_VALUE) {
                nextTarget =
                    repetitions * (arrivalRateModel.getDuration() + (new TimeInstant(
                        repetitionSkip)).getTimeInEpsilon()) + nextTarget;
            }
            return TimeOperations.add(new TimeInstant(initialArrivalTime, TimeUnit.SECONDS),
                new TimeSpan(nextTarget, TimeOperations.getEpsilon()));
        } else if (repeating) {
            repetitions++;
            if (repetitions == maxRepetitions) {
                throw new LoadGeneratorStopException(String.format("Max Repetitions Reached (%s)", maxRepetitions));
            }
            arrivalRateModel.reset();
            return getNextTimeInstant(presentTime);
        } else {
            throw new LoadGeneratorStopException("No more Arrival Rate definitions available.");
        }
    }

    public void scaleLoad(final ScaleFactor scaleFactor) {
        arrivalRateModel.scaleLoad(scaleFactor);
    }

    @Override
    public final void doInitialSelfSchedule() {

    }

    public Operation getTargetOperation() {
        return targetOperation;
    }

    public final String getName() {
        return name;
    }


    @Override
    public String toString() {
        return "LoadGeneratorDesc. target= %s [%d, %d] %s ".format(this.targetOperation.getFullyQualifiedPlainName(),
            initialArrivalTime, stopTime, this.repeating ? "repeating" : "single");
    }
}
