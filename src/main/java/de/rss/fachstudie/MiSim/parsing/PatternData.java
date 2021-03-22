package de.rss.fachstudie.MiSim.parsing;

import de.rss.fachstudie.MiSim.entities.microservice.Microservice;
import de.rss.fachstudie.MiSim.entities.microservice.MicroserviceInstance;
import de.rss.fachstudie.MiSim.entities.patterns.*;
import desmoj.core.simulator.Entity;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lion Wagner
 */
public class PatternData {
    private String type = "";
    private Map<String, Object> config = new HashMap<>();

    public InstanceOwnedPattern tryGetOwnedInstanceOrNull(MicroserviceInstance owner_instance) {
        try {
            return (InstanceOwnedPattern) tryGetPattern(owner_instance);
        } catch (ClassCastException e) {
            return null;
        }
    }

    public ServiceOwnedPattern tryGetServiceOwnedInstanceOrNull(Microservice owner_service) {
        try {
            return (ServiceOwnedPattern) tryGetPattern(owner_service);
        } catch (ClassCastException e) {
            return null;
        }
    }

    private Pattern tryGetPattern(Entity owner) {
        String typename = type.toLowerCase().trim();
        Pattern output = null;
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
