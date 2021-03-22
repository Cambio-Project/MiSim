package de.rss.fachstudie.MiSim.parsing;

import de.rss.fachstudie.MiSim.entities.microservice.MicroserviceInstance;
import de.rss.fachstudie.MiSim.entities.patterns.CircuitBreaker;
import de.rss.fachstudie.MiSim.entities.patterns.Pattern;
import de.rss.fachstudie.MiSim.entities.patterns.RetryManager;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lion Wagner
 */
public class PatternData {
    private String type = "";
    private Map<String, Object> config = new HashMap<>();

    public Pattern getNewInstance(MicroserviceInstance owner) {
        String typename = type.toLowerCase().trim();
        Pattern output = null;
        switch (typename) {//TODO: this can be further automized with reflection
            case "retry":
                output = new RetryManager(owner.getModel(), String.format("RetryManager_of_%s", owner.getName()), true, owner);
                break;
            case "circuitbreaker":
                output = new CircuitBreaker(owner.getModel(), String.format("CircuitBreaker_of_%s", owner.getName()), true, owner);
                break;
            default:
                throw new ParsingException(String.format("Could not find pattern of type %s", typename));
        }
        output.initFields(config);
        return output;
    }
}
