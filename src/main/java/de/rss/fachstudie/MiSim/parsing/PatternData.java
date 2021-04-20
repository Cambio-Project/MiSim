package de.rss.fachstudie.MiSim.parsing;

import de.rss.fachstudie.MiSim.entities.microservice.Microservice;
import de.rss.fachstudie.MiSim.entities.microservice.MicroserviceInstance;
import de.rss.fachstudie.MiSim.entities.patterns.*;
import desmoj.core.simulator.Entity;

import java.util.HashMap;
import java.util.Map;

/**
 * Generic structure of a pattern. Containing a string based name of the pattern and a map with its configuration.
 *
 * @author Lion Wagner
 */
public class PatternData {

    @SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
    private String type = "";
    @SuppressWarnings("FieldMayBeFinal")
    private Map<String, Object> config = new HashMap<>();

    /**
     * Tries to parse the pattern into an {@code InstanceOwnedPattern}. Returns null otherwise.
     */
    public InstanceOwnedPattern tryGetInstanceOwnedPatternOrNull(MicroserviceInstance owner_instance) {
        try {
            return (InstanceOwnedPattern) tryGetPattern(owner_instance);
        } catch (ClassCastException e) {
            return null;
        }
    }

    /**
     * Tries to parse the pattern into an {@code ServiceOwnedPattern}. Returns null otherwise.
     */
    public ServiceOwnedPattern tryGetServiceOwnedPatternOrNull(Microservice owner_service) {
        try {
            return (ServiceOwnedPattern) tryGetPattern(owner_service);
        } catch (ClassCastException e) {
            return null;
        }
    }

    /**
     * Resolves the given string encoded name pattern name to a new pattern object.
     * <p>
     * TODO: Resolve name on class loader stage during compilation in a generic manner. This could be a respectively large task.
     *
     * @param owner owning entity, e.g. a {@code MicroserviceInstance} or {@code Microservice}
     * @return the parsed pattern
     * @throws ParsingException if the string encoded type is unknown.
     */
    private Pattern tryGetPattern(Entity owner) {
        String typename = type.toLowerCase().trim();
        Pattern output;
        switch (typename) {//TODO: this can be further automized with reflection
            case "retry":
                output = new RetryManager(owner.getModel(), String.format("RetryManager_of_%s", owner.getName()), true, (MicroserviceInstance) owner);
                break;
            case "circuitbreaker":
                output = new CircuitBreaker(owner.getModel(), String.format("CircuitBreaker_of_%s", owner.getName()), true, (MicroserviceInstance) owner);
                break;
            case "autoscale":
                output = new PreemptiveAutoScaler(owner.getModel(), String.format("AutoScaler_of_%s", owner.getName()), true, (Microservice) owner);
                break;
            default:
                throw new ParsingException(String.format("Could not find pattern of type %s", typename));
        }
        output.initFields(config);
        return output;
    }
}
