package cambio.simulator.models;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import cambio.simulator.entities.generator.LoadGeneratorDescriptionExecutor;
import cambio.simulator.events.ExperimentAction;
import cambio.simulator.events.ISelfScheduled;
import com.google.gson.annotations.SerializedName;

/**
 * Singleton class that represents the experiment model.
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
}
