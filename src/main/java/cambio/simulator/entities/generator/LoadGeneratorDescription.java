package cambio.simulator.entities.generator;

import cambio.simulator.entities.microservice.Operation;
import cambio.simulator.events.ISelfScheduled;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import desmoj.core.simulator.TimeInstant;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * @author Lion Wagner
 */
public abstract class LoadGeneratorDescription implements ISelfScheduled {

//    potential later addition
//    @SerializedName(value = "stop", alternate = "stop_timestamp")
//    protected double stop = Double.POSITIVE_INFINITY;

    @Expose
    @SerializedName(value = "start", alternate = {"initial_arrival_time", "arrival_time"})
    protected double initialArrivalTime = 0;

    @SerializedName(value = "repeating")
    protected boolean repeating = false;

    @SerializedName(value = "repetitions", alternate = {"max_repetitions"})
    protected double maxRepetitions = Double.POSITIVE_INFINITY;

    @SerializedName(value = "skip", alternate = {"repetition_skip", "repetition"})
    protected double repetitionSkip = 0.0d;

    @SerializedName(value = "operation", alternate = {"target_operation"})
    protected Operation targetOperation = null;

    @SerializedName(value = "name", alternate = {"generator_name"})
    private String name = null;

    private transient ArrivalRateModel arrivalRateModel = null;

    private transient int repetitions = 0;

    public LoadGeneratorDescription() {
    }

    protected abstract ArrivalRateModel createArrivalRateModel();

    public final void initializeArrivalRateModel() {
        if (arrivalRateModel != null) {
            throw new IllegalStateException("Arrival rate model was already initialized");
        }
        arrivalRateModel = createArrivalRateModel();
    }

    @NotNull
    @Contract("->!null")
    public final TimeInstant getNextTimeInstant() throws LoadGeneratorStopException {

        if (arrivalRateModel.hasNext()) {
            double nextTarget = arrivalRateModel.getNextTimeInstant();
            if (arrivalRateModel.getDuration() < Double.POSITIVE_INFINITY) {
                nextTarget = repetitions * (arrivalRateModel.getDuration() + repetitionSkip)
                    + nextTarget;
            }
            return new TimeInstant(initialArrivalTime + nextTarget);

        } else if (repeating) {
            repetitions++;
            if (repetitions == maxRepetitions) {
                throw new LoadGeneratorStopException(String.format("Max Repitions Reached (%s)", maxRepetitions));
            }
            arrivalRateModel.reset();
            return getNextTimeInstant();
        } else {
            throw new LoadGeneratorStopException("No more Arrival Rate definitions available.");
        }
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
}
