package cambio.simulator.parsing;

import java.util.HashMap;
import java.util.Map;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.entities.patterns.CircuitBreaker;
import cambio.simulator.entities.patterns.InstanceOwnedPattern;
import cambio.simulator.entities.patterns.PreemptiveAutoScaler;
import cambio.simulator.entities.patterns.RetryManager;
import cambio.simulator.entities.patterns.ServiceOwnedPattern;
import desmoj.core.simulator.Entity;

/**
 * Generic structure of a pattern. Containing a string based name of the pattern and a map with its configuration.
 *
 * @author Lion Wagner
 */
public class PatternData {

    @SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
    public String type = "";
    @SuppressWarnings("FieldMayBeFinal")
    public Map<String, Object> config = new HashMap<>();

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
     * @throws IlleagalStateException if the string encoded type is unknown.
     */
    private Object tryGetPattern(Entity owner) {

        String typename = type.toLowerCase().trim();
        Object output;
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
        Map<String, Object> nameMap = new HashMap<>();
        config.forEach((s, o) -> nameMap.put(s.toLowerCase().replace("_", ""), o));
//        output.initFields(nameMap);
        return output;
    }
}
