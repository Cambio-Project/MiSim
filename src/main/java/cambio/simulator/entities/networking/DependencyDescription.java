package cambio.simulator.entities.networking;

import java.lang.reflect.Field;
import java.util.Objects;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.entities.microservice.Operation;
import cambio.simulator.misc.NameResolver;
import cambio.simulator.misc.Util;
import cambio.simulator.models.ArchitectureModel;
import cambio.simulator.nparsing.adapter.architecture.ArchitectureModelAdapter;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import desmoj.core.dist.ContDistNormal;
import desmoj.core.dist.NumericalDist;

/**
 * Represents a dependency of an {@code Operation} to another {@code Operation}.
 *
 * @author Lion Wagner
 */
public class DependencyDescription {

    @Expose
    @SerializedName(value = "service", alternate = {"target_service", "trg_service"})
    private final String targetServiceName = null;
    @Expose
    @SerializedName(value = "operation", alternate = {"target_operation", "trg_operation"})
    private final String targetOperationName = null;

    @Expose
    private final ContDistNormal probability;

    @Expose
    private final ContDistNormal customDelay;

    private final transient Operation targetOperation;

    private final transient Operation parentOperation;

    private transient NumericalDist<Double> extraDelay;

    private transient boolean hasResolvedNames = false;

    /**
     * Constructs a new Dependency object to represent the need of one operation to call another. The target operation
     * will be always be required guaranteed. Messages do not use a custom delay.
     *
     * @param parent          parent operation that requires this dependency
     * @param targetOperation child operation that may be need to complete the parent operation
     */
    public DependencyDescription(Operation parent, Operation targetOperation) {
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
    public DependencyDescription(Operation parent, Operation targetOperation, double probability) {
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
    public DependencyDescription(Operation parent, Operation targetOperation, double probability, Double customDelay) {
        Objects.requireNonNull(parent);
        Objects.requireNonNull(targetOperation);
        Util.requirePercentage(probability, "Probability hast to be a value between 0 and 1 (inclusive)");
        if (customDelay != null) {
            Util.requireNonNegative(customDelay, "Delay has to be positive.");
        }

        this.parentOperation = parent;
        this.targetOperation = targetOperation;
        this.probability =
            new ContDistNormal(parent.getModel(), "DependencyDistribution", probability, 0, false, false);
        this.customDelay =
            customDelay == null ? null : new ContDistNormal(parent.getModel(), null, customDelay, 0, false, false);
        this.extraDelay = null;
    }


    public double getProbability() {
        return probability.sample();
    }

    public Microservice getTargetMicroservice() {
        return targetOperation.getOwnerMS();
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
     * This delay can be used to override the default network delay.
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
        if (customDelay == null) {
            throw new IllegalStateException("No Custom Delay was set for this Dependency.");
        }

        double nextlatency;
        do {
            nextlatency = customDelay.sample();
        } while (nextlatency < 0);
        return nextlatency;
    }

    /**
     * Turns the {@link DependencyDescription#targetServiceName} and  {@link DependencyDescription#targetOperationName}
     * into the actual target Operation reference.
     *
     * <p>
     * This method is used during parsing with a {@link ArchitectureModelAdapter} and
     * may only be called once per {@link DependencyDescription} object.
     *
     * @param model {@link ArchitectureModel} that this {@link DependencyDescription} belongs to.
     */
    public void resolveNames(ArchitectureModel model) {
        if (hasResolvedNames) {
            return;
        }

        if (targetOperationName == null) {
            throw new IllegalStateException("Target operation was not defined.");
        }


        String fullyQualifiedName = NameResolver.resolveFullyQualifiedName(targetServiceName, targetOperationName);

        Operation targetOperation = NameResolver.resolveOperationName(model, fullyQualifiedName);

        //injecting field via reflection to keep "final" modifier
        try {
            Field f = this.getClass().getDeclaredField("targetOperation");
            f.setAccessible(true);
            f.set(this, targetOperation);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        hasResolvedNames = true;
    }
}
