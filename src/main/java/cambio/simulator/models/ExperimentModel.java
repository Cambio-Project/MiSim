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
    private final LoadGeneratorDescriptionExecutor[] generators;

    private final Set<ExperimentAction> otherExperimentActions;


    public ExperimentModel(LoadGeneratorDescriptionExecutor[] generators,
                           Set<ExperimentAction> experimentActions) {
        this.generators = generators;
        this.otherExperimentActions = experimentActions;
    }

    /**
     * Creates a new Experimentmodel based on the given set of ISelfScheduled objects.
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
                                                                @NotNull Iterable<ISelfScheduled> selfSchedulers) {
        Set<LoadGeneratorDescriptionExecutor> executors = new HashSet<>();
        Set<ExperimentAction> actions = new HashSet<>();
        selfSchedulers.forEach(iSelfScheduled -> {
            if (iSelfScheduled instanceof LoadGeneratorDescription) {
                executors.add(
                    new LoadGeneratorDescriptionExecutor(baseModel, (LoadGeneratorDescription) iSelfScheduled));
            } else if (iSelfScheduled instanceof ExperimentAction) {
                actions.add((ExperimentAction) iSelfScheduled);
            }
        });
        return new ExperimentModel(executors.toArray(new LoadGeneratorDescriptionExecutor[0]), actions);
    }

    /**
     * Collects all self scheduling entities from the experiment, so they can be scheduled during the inital scheduling
     * of the model.
     *
     * @return all self scheduling entities from the experiment
     * @see MiSimModel#doInitialSchedules()
     */
    public Set<ISelfScheduled> getAllSelfSchedulesEntities() {
        Set<ISelfScheduled> allSelfScheduledEntities = getAllObjectsOfType(ISelfScheduled.class);
        Collections.addAll(allSelfScheduledEntities, generators);
        return allSelfScheduledEntities;

    }

    public <T> Set<T> getAllObjectsOfType(Class<T> clazz) {
        return otherExperimentActions.stream().filter(o -> clazz.isAssignableFrom(o.getClass())).map(clazz::cast)
            .collect(Collectors.toSet());
    }

}
