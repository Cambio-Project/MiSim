package cambio.simulator.models;

import java.util.*;
import java.util.stream.Collectors;

import cambio.simulator.entities.generator.LoadGeneratorDescription;
import cambio.simulator.entities.generator.LoadGeneratorDescriptionExecutor;
import cambio.simulator.events.ExperimentAction;
import cambio.simulator.events.ISelfScheduled;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the experiment model.
 *
 * @author Lion Wagner
 */
public class ExperimentModel {

    @SerializedName(value = "generators", alternate = {"request_generators"})
    private final TreeSet<LoadGeneratorDescriptionExecutor> generators;

    private final TreeSet<ExperimentAction> otherExperimentActions;


    public ExperimentModel(Collection<LoadGeneratorDescriptionExecutor> generators,
                           Collection<ExperimentAction> experimentActions) {
        this.generators = new TreeSet<>(Comparator.comparing(LoadGeneratorDescriptionExecutor::getIdentNumber));
        this.generators.addAll(generators);

        this.otherExperimentActions = new TreeSet<>(Comparator.comparing(ExperimentAction::getName));
        this.otherExperimentActions.addAll(experimentActions);
    }

    /**
     * Creates a new Experiment model based on the given set of ISelfScheduled objects.
     *
     * <p>
     * Looks for {@link LoadGeneratorDescription}s and {@link ExperimentAction}s within the set and adds them to a new
     * Experiment model.
     *
     * @param baseModel      parent model of the resulting ExperimentModel and potentially created {@link
     *                       ExperimentAction}s
     * @param selfSchedulers entities that should be part of the new ExperimentModel
     * @return a new experiment model always. It may be empty if no selfSchedulers or wrongly typed ones are given.
     */
    @Contract("_, _ -> new")
    public static @NotNull ExperimentModel fromScheduleEntities(MiSimModel baseModel,
                                                                @NotNull Collection<ISelfScheduled> selfSchedulers) {
        Set<LoadGeneratorDescriptionExecutor> executors = new HashSet<>();
        Set<ExperimentAction> actions = new HashSet<>();
        selfSchedulers.forEach(iSelfScheduled -> {
            if (iSelfScheduled instanceof LoadGeneratorDescription) {
                executors.add(
                    new LoadGeneratorDescriptionExecutor(baseModel, (LoadGeneratorDescription) iSelfScheduled));
            } else if (iSelfScheduled instanceof ExperimentAction) {
                actions.add((ExperimentAction) iSelfScheduled);
            } else {
                throw new IllegalArgumentException(String.format(
                    "Unknown Type '%s' consider implementing the ISelfScheduled interface to allow for usage in the "
                        + "simulation.", iSelfScheduled.getClass()));
            }
        });
        return new ExperimentModel(executors, actions);
    }

    /**
     * Collects all self scheduling entities from the experiment, so they can be scheduled during the initial scheduling
     * of the model.
     *
     * @return all self scheduling entities from the experiment
     * @see MiSimModel#doInitialSchedules()
     */
    public Set<ISelfScheduled> getAllSelfSchedulesEntities() {
        TreeSet<ISelfScheduled> selfSchedulers = new TreeSet<>(Comparator.comparing(iSelfScheduled -> {
            if (iSelfScheduled instanceof LoadGeneratorDescriptionExecutor) {
                return ((LoadGeneratorDescriptionExecutor) iSelfScheduled).getIdentNumber();
            } else if (iSelfScheduled instanceof ExperimentAction) {
                return (long) iSelfScheduled.hashCode();
            }
            return null;
        }));
        selfSchedulers.addAll(generators);
        selfSchedulers.addAll(otherExperimentActions);
        return selfSchedulers;
    }


    /**
     * Retrieves all {@link ExperimentAction}s that extend the given class. E.g. {@link ISelfScheduled}.
     *
     * <p>
     * <b>This may be removed in future update, use {@link #getAllSelfSchedulesEntities()} instead.</b>
     */
    @Deprecated
    public <T> Set<T> getAllObjectsOfType(Class<T> clazz) {
        return otherExperimentActions.stream().filter(o -> clazz.isAssignableFrom(o.getClass())).map(clazz::cast)
            .collect(Collectors.toSet());
    }
}
