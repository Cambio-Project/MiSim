package cambio.simulator.entities.networking;

import java.util.Objects;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.entities.microservice.Operation;
import cambio.simulator.misc.Util;
import desmoj.core.dist.ContDistNormal;
import desmoj.core.dist.NumericalDist;

/**
 * Represents a dependency of an {@code Operation} to another {@code Operation}.
 *
 * @author Lion Wagner
 */
public class Dependency {
    private final Operation parent;
    private final Microservice targetMicroservice;
    private final Operation targetOperation;
    private final double probability;
    private final NumericalDist<Double> customDelay;
    private NumericalDist<Double> extraDelay;

    /**
     * Constructs a new Dependency object to represent the need of one operation to call another. The target operation
     * will be always be required guaranteed. Messages do not use a custom delay.
     *
     * @param parent          parent operation that requires this dependency
     * @param targetOperation child operation that may be need to complete the parent operation
     */
    public Dependency(Operation parent, Operation targetOperation) {
        this(parent, targetOperation, 1);
    }


    /**
     * Constructs a new Dependency object to represent the need of one operation to call another. Messages do not use a
     * custom delay.
     *
     * @param parent          parent operation that potentially requires this dependency
     * @param targetOperation child operation that may be need to complete the parent operation
     * @param probability     probability with which the parent operation needs to call the {@code targetOperation}
     */
    public Dependency(Operation parent, Operation targetOperation, double probability) {
        this(parent, targetOperation, probability, null);

    }

    /**
     * Constructs a new Dependency object to represent the need of one operation to call another. Messages can use a
     * custom delay.
     *
     * @param parent          parent operation that potentially requires this dependency
     * @param targetOperation child operation that may be need to complete the parent operation
     * @param probability     probability with which the parent operation needs to call the {@code targetOperation}
     * @param customDelay     delay of this dependency that overrides the default network delay, null values will be
     *                        ignored
     */
    public Dependency(Operation parent, Operation targetOperation, double probability, Double customDelay) {
        Objects.requireNonNull(parent);
        Objects.requireNonNull(targetOperation);
        Util.requirePercentage(probability, "Probability hast to be a value between 0 and 1 (inclusive)");
        if (customDelay != null) {
            Util.requireNonNegative(customDelay, "Delay has to be positive.");
        }

        this.parent = parent;
        this.targetMicroservice = targetOperation.getOwnerMS();
        this.targetOperation = targetOperation;
        this.probability = probability;
        this.customDelay =
            customDelay == null ? null : new ContDistNormal(parent.getModel(), null, customDelay, 0, false, false);
        this.extraDelay = null;
    }

    public double getProbability() {
        return probability;
    }

    public Microservice getTargetMicroservice() {
        return targetMicroservice;
    }

    public Operation getTargetOperation() {
        return targetOperation;
    }

    public void setExtraDelay(NumericalDist<Double> dist) {
        extraDelay = dist;
    }

    /**
     * Gets the extra delay assigned to this dependency.
     *
     * @return the extra delay assigned to this dependency.
     */
    public synchronized double getNextExtraDelay() {
        if (extraDelay == null) {
            return 0;
        }
        double nextlatency;
        do {
            nextlatency = extraDelay.sample();
        } while (nextlatency < 0);
        return nextlatency;
    }

    /**
     * Gets whether this dependency has a custom delay.
     * This delay can be used to override the the default network delay.
     *
     * @return whether this dependency has a custom delay.
     */
    public synchronized boolean hasCustomDelay() {
        return customDelay != null;
    }

    /**
     * Gets the custom delay assigned to this dependency.
     *
     * @return the custom delay assigned to this dependency.
     */
    public synchronized double getNextCustomDelay() {
        double nextlatency;
        do {
            nextlatency = customDelay.sample();
        } while (nextlatency < 0);
        return nextlatency;
    }
}
