package de.unistuttgart.sqa.orcas.misim.models;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import de.unistuttgart.sqa.orcas.misim.events.ISelfScheduled;
import de.unistuttgart.sqa.orcas.misim.parsing.ExpModelParser;
import de.unistuttgart.sqa.orcas.misim.parsing.ScenarioDescription;

/**
 * Singleton class that represents the experiment model.
 *
 * @author Lion Wagner
 */
public class ExperimentModel {
    private static ExperimentModel instance = null;

    /**
     * Gets the experiment model singleton.
     *
     * @return the experiment model singleton.
     */
    public static ExperimentModel get() {
        if (instance == null) {
            throw new IllegalStateException("Experiment Model was not initialized yet.");
        }
        return instance;
    }

    /**
     * Initializes the experiment model singleton.
     * @param expFileLocation path of the experiment file
     * @return the experiment model singleton.
     * @throws IllegalStateException if model was already initialized
     */
    public static ExperimentModel initialize(Path expFileLocation) {
        if (instance != null) {
            throw new IllegalStateException("Experiment Model was already initialized.");
        }
        instance = new ExperimentModel(expFileLocation);

        return get();
    }

    /**
     * Initializes the experiment model singleton.
     * @param scenario description the scenario that should be executed
     * @return the experiment model singleton.
     * @throws IllegalStateException if model was already initialized
     */
    public static ExperimentModel initialize(ScenarioDescription scenario) {
        if (instance != null) {
            throw new IllegalStateException("Experiment Model was already initialized.");
        }
        instance = new ExperimentModel(scenario);
        return get();
    }


    private final Set<Object> experimentObjects;

    private ExperimentModel(Path expFileLocation) {
        experimentObjects = Collections.unmodifiableSet(ExpModelParser.parseExperimentData(expFileLocation));
    }

    private ExperimentModel(ScenarioDescription scenarioDescription) {
        experimentObjects = scenarioDescription.parse();
    }

    public Set<ISelfScheduled> getAllSelfSchedulesEvents() {
        return getAllObjectsOfType(ISelfScheduled.class);

    }

    public <T> Set<T> getAllObjectsOfType(Class<T> clazz) {
        return experimentObjects.stream().filter(o -> clazz.isAssignableFrom(o.getClass())).map(clazz::cast)
            .collect(Collectors.toSet());
    }
}
