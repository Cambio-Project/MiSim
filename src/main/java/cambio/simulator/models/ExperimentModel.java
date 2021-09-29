package cambio.simulator.models;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
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

}
