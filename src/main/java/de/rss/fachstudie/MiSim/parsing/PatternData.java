package de.rss.fachstudie.MiSim.parsing;

import java.util.HashMap;
import java.util.Map;

import de.rss.fachstudie.MiSim.entities.microservice.Microservice;
import de.rss.fachstudie.MiSim.entities.microservice.MicroserviceInstance;
import de.rss.fachstudie.MiSim.entities.patterns.CircuitBreaker;
import de.rss.fachstudie.MiSim.entities.patterns.InstanceOwnedPattern;
import de.rss.fachstudie.MiSim.entities.patterns.Pattern;
import de.rss.fachstudie.MiSim.entities.patterns.PreemptiveAutoScaler;
import de.rss.fachstudie.MiSim.entities.patterns.RetryManager;
import de.rss.fachstudie.MiSim.entities.patterns.ServiceOwnedPattern;
import desmoj.core.simulator.Entity;

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
     * Tries to parse the pattern into an {@code InstanceOwnedPattern}. Returns {@code null} otherwise.
     *
     * @param ownerInstance microservice instance that owns this pattern
     * @return this pattern as {@code InstanceOwnedPattern} or {@code null} if it is not instance owned
     */
    public InstanceOwnedPattern tryGetInstanceOwnedPatternOrNull(MicroserviceInstance ownerInstance) {
        try {
            return (InstanceOwnedPattern) tryGetPattern(ownerInstance);
        } catch (ClassCastException e) {
            return null;
        }
    }

    /**
     * Tries to parse the pattern into an {@code ServiceOwnedPattern}. Returns {@code null} otherwise.
     *
     * @param ownerService microservice that owns this pattern
     * @return this pattern as {@code ServiceOwnedPattern} or {@code null} if it is not service owned
     */
    public ServiceOwnedPattern tryGetServiceOwnedPatternOrNull(Microservice ownerService) {
        try {
            return (ServiceOwnedPattern) tryGetPattern(ownerService);
        } catch (ClassCastException e) {
            return null;
        }
    }

    /**
     * Resolves the given string encoded name pattern name to a new pattern object.
     *
     * <p>
     * TODO: Resolve name on class loader stage during compilation in a generic manner. This could be a respectively
     * large task.
     *
     * @param owner owning entity, e.g. a {@code MicroserviceInstance} or {@code Microservice}
     * @return the parsed pattern
     * @throws ParsingException if the string encoded type is unknown.
     */
    private Pattern tryGetPattern(Entity owner) {
        // TODO: find pattern type from name
        // see  Thread.currentThread().getContextClassLoader().loadClass("java.lang.String");
        // or https://github.com/ronmamo/reflections

        String typename = type.toLowerCase().trim();
        Pattern output;
        switch (typename) {
            case "retry":
                output = new RetryManager(owner.getModel(), String.format("RetryManager_of_%s", owner.getName()), true,
                    (MicroserviceInstance) owner);
                break;
            case "circuitbreaker":
                output =
                    new CircuitBreaker(owner.getModel(), String.format("CircuitBreaker_of_%s", owner.getName()), true,
                        (MicroserviceInstance) owner);
                break;
            case "autoscale":
                output =
                    new PreemptiveAutoScaler(owner.getModel(), String.format("AutoScaler_of_%s", owner.getName()), true,
                        (Microservice) owner);
                break;
            default:
                throw new ParsingException(String.format("Could not find pattern of type %s", typename));
        }
        output.initFields(config);
        return output;
    }
}
