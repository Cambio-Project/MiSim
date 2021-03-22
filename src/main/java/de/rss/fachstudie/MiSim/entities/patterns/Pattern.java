package de.rss.fachstudie.MiSim.entities.patterns;

import de.rss.fachstudie.MiSim.entities.microservice.MicroserviceInstance;
import de.rss.fachstudie.MiSim.parsing.FromJson;
import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Model;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class Pattern extends Entity {

    protected final MicroserviceInstance owner;

    public Pattern(Model model, String name, boolean showInTrace, MicroserviceInstance owner) {
        super(model, name, showInTrace);
        this.owner = owner;
    }

    /**
     * Reflectively initializes all fields based on name and arguments
     *
     * @param arguments Map of arguments of name and value key-value pairs.
     */
    public final void initFields(Map<String, Object> arguments) {
        List<String> missingProperties = new ArrayList<>();

        for (Field field : this.getClass().getDeclaredFields()) {
            if (!field.isAnnotationPresent(FromJson.class)) continue; //ignore fields that are not loaded by json

            try {
                field.setAccessible(true);
                Object argumentValue = arguments.get(field.getName());
                if (argumentValue == null) {
                    String missingInfo;
                    Object defaultValue = field.get(this);
                    missingInfo = String.format("%s was not defined. Defaulting to value %s", field.getName(), defaultValue);
                    missingProperties.add(missingInfo);
                } else {
                    if (ClassUtils.isAssignable(field.getType(), int.class, true) &&
                            ClassUtils.isAssignable(argumentValue.getClass(), Number.class, true)) {
                        argumentValue = ((Number) argumentValue).intValue();
                    }
                    field.set(this, argumentValue);
                }
            } catch (IllegalAccessException e) {
            }
        }

        if (!missingProperties.isEmpty()) {
            sendWarning(String.format("Using default values:\n%s", StringUtils.join(missingProperties, "\n")), "", "", "Check your experiment arguments for this pattern.");
        }
    }

    /**
     * Will be called by the owning instance upon an unexpected shutdown (kill)
     */
    public abstract void close();

}
