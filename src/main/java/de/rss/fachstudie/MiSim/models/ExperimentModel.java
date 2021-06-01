package de.rss.fachstudie.MiSim.models;

import de.rss.fachstudie.MiSim.events.ISelfScheduled;
import de.rss.fachstudie.MiSim.parsing.ExpModelParser;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Lion Wagner
 */
public class ExperimentModel {
    private static ExperimentModel instance = null;

    public static ExperimentModel get() {
        if (instance == null) {
            throw new IllegalStateException("Architecture Model was not initialized yet.");
        }
        return instance;
    }

    public static ExperimentModel initialize(Path expFileLocation) {
        if (instance != null) {
            throw new IllegalStateException("Architecture Model was already initialized.");
        }
        instance = new ExperimentModel(expFileLocation);

        return get();
    }

    private final Set<Object> experimentObjects;

    private ExperimentModel(Path expFileLocation) {
        experimentObjects = Collections.unmodifiableSet(ExpModelParser.parseExperimentData(expFileLocation));
    }

    public Set<ISelfScheduled> getAllSelfSchedulesEvents() {
        return getAllObjectsOfType(ISelfScheduled.class);

    }

    public <T> Set<T> getAllObjectsOfType(Class<T> clazz) {
        return experimentObjects.stream().filter(o -> clazz.isAssignableFrom(o.getClass())).map(clazz::cast).collect(Collectors.toSet());
    }
}
