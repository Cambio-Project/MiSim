package de.rss.fachstudie.MiSim.events;

import de.rss.fachstudie.MiSim.entities.microservice.Microservice;
import de.rss.fachstudie.MiSim.entities.microservice.Operation;
import de.rss.fachstudie.MiSim.misc.Util;
import de.rss.fachstudie.MiSim.parsing.LatencyMonkeyParser;
import de.rss.fachstudie.MiSim.parsing.Parser;
import desmoj.core.dist.ContDistNormal;
import desmoj.core.dist.NumericalDist;
import desmoj.core.simulator.Model;

import java.util.Arrays;
import java.util.Objects;

/**
 * Event that triggers a latency injection.
 * <p>
 * The injection can be applied on different levels:
 * <p>
 * Either all outgoing requests of a {@code Microservice} are delayed.<p>
 * <p>
 * Or all outgoing dependency requests of a single {@code Operation} can be delayed.<p>
 * <p>
 * Or the connection between two specific  {@code Operation}s can also be delayed.
 *
 * @author Lion Wagner
 */
public class LatencyMonkeyEvent extends SelfScheduledEvent {
    private final double delay;
    private final double std_deviation;
    private final Microservice microservice;
    private final Operation operation_src;
    private final Operation operation_trg;

    public LatencyMonkeyEvent(Model model, String name, boolean showInTrace, double delay, Microservice microservice) {
        this(model, name, showInTrace, delay, 0, microservice, null, null);
    }

    public LatencyMonkeyEvent(Model model, String name, boolean showInTrace, double delay, Operation operation_src) {
        this(model, name, showInTrace, delay, 0, operation_src.getOwnerMS(), operation_src, null);
    }

    public LatencyMonkeyEvent(Model model, String name, boolean showInTrace, double delay, Operation operation_src, Operation operation_trg) {
        this(model, name, showInTrace, delay, 0, operation_src.getOwnerMS(), operation_src, operation_trg);
    }

    public LatencyMonkeyEvent(Model model, String name, boolean showInTrace, double delay, double std_deviation, Microservice microservice) {
        this(model, name, showInTrace, delay, std_deviation, microservice, null, null);
    }

    public LatencyMonkeyEvent(Model model, String name, boolean showInTrace, double delay, double std_deviation, Operation operation_src) {
        this(model, name, showInTrace, delay, std_deviation, operation_src.getOwnerMS(), operation_src, null);
    }

    public LatencyMonkeyEvent(Model model, String name, boolean showInTrace, double delay, double std_deviation, Microservice microservice, Operation operation_src, Operation operation_trg) {
        super(model, name, showInTrace);
        Objects.requireNonNull(microservice);

        this.delay = delay;
        this.std_deviation = std_deviation;
        this.microservice = microservice;
        this.operation_src = operation_src;
        this.operation_trg = operation_trg;

        validateArguments();
    }

    private void validateArguments() {
        Objects.requireNonNull(microservice);

        Util.requireNonNegative(delay, "The injected delay cannot be negative");
        Util.requireNonNegative(std_deviation, "The standard deviation of the injected delay cannot be negative");

        if (operation_src != null) {
            if (Arrays.stream(microservice.getOperations()).noneMatch(operation -> operation == operation_src)) {
                throw new IllegalArgumentException(String.format("Operation %s is not an operation of microservice %s", operation_src, microservice));
            }
            if (operation_trg != null
                    && Arrays.stream(operation_src.getDependencies()).noneMatch(dependency -> dependency.getTargetOperation() == operation_trg)) {
                throw new IllegalArgumentException(String.format("Operation %s is not a dependency of operation %s", operation_trg, operation_src));
            }
        }
    }

    @Override
    public void eventRoutine() {
        NumericalDist<Double> dist = new ContDistNormal(getModel(), String.format("DelayDistribution_of_%s", this.getName()), delay, std_deviation, false, false);
        microservice.applyDelay(dist, operation_src, operation_trg);
    }
}
