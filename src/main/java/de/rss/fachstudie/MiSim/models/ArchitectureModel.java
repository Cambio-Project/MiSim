package de.rss.fachstudie.MiSim.models;

import de.rss.fachstudie.MiSim.entities.microservice.Microservice;
import de.rss.fachstudie.MiSim.parsing.ArchModelParser;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * Class that contains the architectural information provided by the architecture file.
 *
 * @author Lion Wagner
 */
public class ArchitectureModel {
    private static ArchitectureModel instance = null;

    public static ArchitectureModel get() {
        if (instance == null) {
            throw new IllegalStateException("Architecture Model was not initialized yet.");
        }
        return instance;
    }

    public static ArchitectureModel initialize(Path archFileLocation) {
        if (instance != null) {
            throw new IllegalStateException("Architecture Model was already initialized.");
        }
        instance = new ArchitectureModel(archFileLocation);
        ArchModelParser.initializeOperations();
        return get();
    }

    private final Set<Microservice> microservices;


    private ArchitectureModel(Path archFile) {
        microservices = ArchModelParser.parseMicroservicesArchModelFile(archFile);
    }


    public Set<Microservice> getMicroservices() {
        return new HashSet<>(microservices);
    }
}
