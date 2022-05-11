package cambio.simulator.events;

import java.util.Arrays;
import java.util.Objects;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.entities.microservice.Operation;
import cambio.simulator.entities.networking.InternalRequest;
import cambio.simulator.parsing.JsonTypeName;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import desmoj.core.dist.ContDistNormal;
import desmoj.core.simulator.*;

/**
 * Event that triggers a latency injection. The injection can be applied on different levels:<br> Either all outgoing
 * requests of a {@code Microservice} are delayed.<br> Or all outgoing dependency requests of a single {@code Operation}
 * can be delayed.<br> Or the connection between two specific {@code Operation}s can also be delayed.
 *
 * @author Lion Wagner
 */
@JsonTypeName(value = "delay", alternativeNames = "delay_injection")
public class DelayInjection extends SelfScheduledExperimentAction {
    @Expose
    @SerializedName(value = "delay", alternate = {"delay_distribution", "distribution"})
    private ContDistNormal delayDistribution;
    @Expose
    @SerializedName(value = "microservice", alternate = {"target"})
    private Microservice microservice;
    @Expose
    private Operation operationSrc;
    @Expose
    private Operation operationTrg;
    @Expose
    private double duration;

    public DelayInjection(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
    }

    /**
     * Constructs a new {@link DelayInjection}.
     *
     * @param delay        mean delay that should be added to a connection
     * @param stdDeviation standard deviation of this delay
     * @param microservice target {@link Microservice}
     * @param operationSrc {@link Operation} of the microservice that should be affected, can be set to {@code null} to
     *                     affect all {@link Operation}s
     * @param operationTrg target {@link Operation} of the operationSrc that should be affected, can be set to {@code
     *                     null} to affect all outgoing {@link InternalRequest}s
     */
    public DelayInjection(Model model, String name, boolean showInTrace, double delay, double stdDeviation,
                          Microservice microservice, Operation operationSrc, Operation operationTrg) {
        super(model, name, showInTrace);
        Objects.requireNonNull(microservice);

        this.delayDistribution = new ContDistNormal(model, null, delay, stdDeviation, false, false);
        this.microservice = microservice;
        this.operationSrc = operationSrc;
        this.operationTrg = operationTrg;

        validateArguments();
    }

    private void validateArguments() {
        Objects.requireNonNull(microservice);

        if (operationSrc != null) {
            if (Arrays.stream(microservice.getOperations()).noneMatch(operation -> operation == operationSrc)) {
                throw new IllegalArgumentException(
                    String.format("Operation %s is not an operation of microservice %s", operationSrc, microservice));
            }
            if (operationTrg != null
                && Arrays.stream(operationSrc.getDependencyDescriptions())
                .noneMatch(dependency -> dependency.getAllTargetOperations().contains(operationTrg))) {
                throw new IllegalArgumentException(
                    String.format("Operation %s is not a dependency of operation %s", operationTrg, operationSrc));
            }
        }
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    @Override
    public void onRoutineExecution() {
        microservice.applyDelay(delayDistribution, operationSrc, operationTrg);
        if (duration > 0) {
            new ExternalEvent(getModel(), "LatencyMonkeyDeactivator", this.traceIsOn()) {
                @Override
                public void eventRoutine() {
                    microservice.applyDelay(null, operationSrc, operationTrg);
                }
            }.schedule(new TimeSpan(duration));
        }

    }
}
