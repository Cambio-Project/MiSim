package cambio.simulator.models;

import java.util.Set;
import java.util.stream.Collectors;

import cambio.simulator.entities.generator.LoadGeneratorDescriptionExecutor;
import cambio.simulator.events.ExperimentAction;
import cambio.simulator.events.ISelfScheduled;

/**
 * Singleton class that represents the experiment model.
 *
 * @author Lion Wagner
 */
public class ExperimentModel {
    private final LoadGeneratorDescriptionExecutor[] generators;

    private final Set<ExperimentAction> otherExperimentActions;


    public ExperimentModel(LoadGeneratorDescriptionExecutor[] generators,
                           Set<ExperimentAction> experimentActions) {
        this.generators = generators;
        this.otherExperimentActions = experimentActions;
    }


    public Set<ISelfScheduled> getAllSelfSchedulesEvents() {
        return getAllObjectsOfType(ISelfScheduled.class);

    }

    public <T> Set<T> getAllObjectsOfType(Class<T> clazz) {
        return otherExperimentActions.stream().filter(o -> clazz.isAssignableFrom(o.getClass())).map(clazz::cast)
            .collect(Collectors.toSet());
    }
}
