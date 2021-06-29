package cambio.simulator.models;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.parsing.ArchModelParser;

/**
 * Class that contains the architectural information provided by the architecture file.
 *
 * @author Lion Wagner
 */
public class ArchitectureModel {
    private static ArchitectureModel instance = null;
    private final Set<Microservice> microservices;

    private ArchitectureModel(Path archFile) {
        microservices = ArchModelParser.parseMicroservicesArchModelFile(archFile);
    }

    /**
     * Gets the ArchitectureModel singletons.
     *
     * @return the ArchitectureModel singleton.
     */
    public static ArchitectureModel get() {
        if (instance == null) {
            throw new IllegalStateException("Architecture Model was not initialized yet.");
        }
        return instance;
    }

    /**
     * Creates the ArchitectureModel, based on an architecture file.
     *
     * @param archFileLocation Path to the architecture File
     * @return the ArchitectureModel singleton.
     */
    public static ArchitectureModel initialize(Path archFileLocation) {
        if (instance != null) {
            throw new IllegalStateException("Architecture Model was already initialized.");
        }
        instance = new ArchitectureModel(archFileLocation);
        ArchModelParser.initializeOperations();
        return get();
    }

    /**
     * Gets all available microservices.
     *
     * @return all microservices
     */
    public Set<Microservice> getMicroservices() {
        return new HashSet<>(microservices);
    }
}
