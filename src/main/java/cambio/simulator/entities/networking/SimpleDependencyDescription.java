package cambio.simulator.entities.networking;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.entities.microservice.Operation;
import cambio.simulator.misc.NameResolver;
import cambio.simulator.misc.Util;
import cambio.simulator.models.ArchitectureModel;
import cambio.simulator.parsing.adapter.architecture.ArchitectureModelAdapter;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import desmoj.core.dist.ContDistNormal;
import desmoj.core.dist.NumericalDist;

/**
 * Represents the leaf dependencies in a hierarchy of {@link DependencyDescription}s. Describes a
 * dependency of an {@code Operation} to another {@code Operation}.
 *
 * @author Lion Wagner, Sebastian Frank
 */
public class SimpleDependencyDescription extends AbstractDependencyDescription {

    @Expose
    @SerializedName(value = "service", alternate = {"target_service", "trg_service"})
    private final String targetServiceName = null;
    @Expose
    @SerializedName(value = "operation", alternate = {"target_operation", "trg_operation"})
    private final String targetOperationName = null;

    @Expose
    private final ContDistNormal customDelay;

    private final transient Operation targetOperation;

    private final transient Operation parentOperation;

    private transient NumericalDist<Double> extraDelay;

    private transient boolean hasResolvedNames = false;

    /**
     * Constructs a new Dependency object to represent the need of one operation to call another.
     * The target operation will be always be required for the parent operation to complete.
     * Messages do not use a custom delay.
     *
     * @param parent parent operation that requires this dependency
     * @param targetOperation child operation that may be need to complete the parent operation
     */
    public SimpleDependencyDescription(Operation parent, Operation targetOperation) {
        this(parent, targetOperation, 1);
    }

    /**
     * Constructs a new Dependency object to represent the need of one operation to call another.
     * Messages do not use a custom delay.
     *
     * @param parent parent operation that potentially requires this dependency
     * @param targetOperation child operation that may be need to complete the parent operation
     * @param probability probability with which the parent operation needs to call the
     *        {@code targetOperation}
     */
    public SimpleDependencyDescription(Operation parent, Operation targetOperation,
            double probability) {
        this(parent, targetOperation, probability, null);
    }

    /**
     * Constructs a new Dependency object to represent the need of one operation to call another.
     * Messages can use a custom delay.
     *
     * @param parent parent operation that potentially requires this dependency
     * @param targetOperation child operation that may be need to complete the parent operation
     * @param probability probability with which the parent operation needs to call the
     *        {@code targetOperation}
     * @param customDelay delay of this dependency that overrides the default network delay, null
     *        values will be ignored
     */
    public SimpleDependencyDescription(Operation parent, Operation targetOperation,
            double probability, Double customDelay) {
        this(targetOperation, targetOperation, probability, customDelay, 1);
    }

    /**
     * Constructs a new Dependency object to represent the need of one operation to call another.
     * Messages can use a custom delay.
     *
     * @param parent parent operation that potentially requires this dependency
     * @param targetOperation child operation that may be need to complete the parent operation
     * @param probability probability with which the parent operation needs to call the
     *        {@code targetOperation}
     * @param customDelay delay of this dependency that overrides the default network delay, null
     *        values will be ignored
     * @param alternativeProbability probability at which the dependency - and its descendants - is
     *        selected from a group of dependencies for execution when inside of an
     *        {@link AlternativeDependencyDescription}
     */
    public SimpleDependencyDescription(Operation parent, Operation targetOperation,
            double probability, Double customDelay, double alternativeProbability) {
        super(parent.getModel(), probability, alternativeProbability);
        Objects.requireNonNull(parent);
        Objects.requireNonNull(targetOperation);
        if (customDelay != null) {
            Util.requireNonNegative(customDelay, "Delay has to be positive.");
        }

        this.parentOperation = parent;
        this.targetOperation = targetOperation;
        this.customDelay = customDelay == null ? null
                : new ContDistNormal(parent.getModel(), null, customDelay, 0, false, false);
        this.extraDelay = null;
    }

    @Override
    public void applyExtraDelay(NumericalDist<Double> dist, Operation operationTrg) {
        if (this.getTargetOperation().equals(operationTrg)) {
            this.setExtraDelay(dist);
        }
    }

    @Override
    public void applyExtraDelay(NumericalDist<Double> dist) {
        this.setExtraDelay(dist);
    }

    @Override
    public List<DependencyDescription> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public List<ServiceDependencyInstance> generateDependenciesForExecutions(final Request request,
            final Random random) {
        List<ServiceDependencyInstance> result = new ArrayList<>();
        if (isExecuted(random)) {
            Operation nextOperationEntity = this.getTargetOperation();
            ServiceDependencyInstance dep = new ServiceDependencyInstance(request.getModel(),
                    request, nextOperationEntity, this);
            result.add(dep);
        }
        return result;
    }

    @Override
    public Set<Operation> getAllTargetOperations() {
        return Collections.singleton(this.targetOperation);
    }

    @Override
    public List<SimpleDependencyDescription> getLeafDescendants() {
        return Collections.singletonList(this);
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
     * Gets whether this dependency has a custom delay. This delay can be used to override the
     * default network delay.
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
     * Turns the {@link SimpleDependencyDescription#targetServiceName} and
     * {@link SimpleDependencyDescription#targetOperationName} into the actual target Operation
     * reference.
     *
     * <p>
     * This method is used during parsing with a {@link ArchitectureModelAdapter} and may only be
     * called once per {@link SimpleDependencyDescription} object.
     *
     * @param model {@link ArchitectureModel} that this {@link SimpleDependencyDescription} belongs
     *        to.
     */
    public void resolveNames(final ArchitectureModel model) {
        if (hasResolvedNames) {
            return;
        }

        if (targetOperationName == null) {
            throw new IllegalStateException("Target operation was not defined.");
        }

        String fullyQualifiedName =
                NameResolver.combineToFullyQualifiedName(targetServiceName, targetOperationName);

        Operation targetOperation = NameResolver.resolveOperationName(model, fullyQualifiedName);

        // injecting field via reflection to keep "final" modifier
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
