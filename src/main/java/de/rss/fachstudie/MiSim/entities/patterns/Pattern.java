package de.rss.fachstudie.MiSim.entities.patterns;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.rss.fachstudie.MiSim.parsing.FromJson;
import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Model;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Represents a resilience parsable entity. Extending this class allows for automatic field initialization based on a
 * value map.
 *
 * @author Lion Wagner
 */
public abstract class Pattern extends Entity {

    public Pattern(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
    }

    /**
     * Reflectively initializes all fields based on name and arguments.
     * <br>
     * Fields marked as {@code final} will not be set properly. (In IntelliJ debugging they appear to be correct, but
     * they are not!)
     * <br>
     * However, they can be set to {@code private}.
     *
     * <p>
     * Mark fields that should be injected from with the {@code FromJson} annotation.
     *
     * @param arguments Map of arguments of name and value key-value pairs.
     * @see FromJson
     */
    public final void initFields(Map<String, Object> arguments) {
        List<String> missingProperties = new ArrayList<>();

        for (Field field : this.getClass().getDeclaredFields()) {
            if (!field.isAnnotationPresent(FromJson.class)) {
                continue; //ignore fields that are not loaded by json
            }

            try {
                field.setAccessible(true);
                Object argumentValue = arguments != null ? arguments.get(field.getName()) : null;
                if (argumentValue == null) {
                    String missingInfo;
                    Object defaultValue = field.get(this);
                    missingInfo =
                        String.format("%s was not defined. Defaulting to value %s", field.getName(), defaultValue);
                    missingProperties.add(missingInfo);
                } else {
                    if (ClassUtils.isAssignable(field.getType(), int.class, true)
                        && ClassUtils.isAssignable(argumentValue.getClass(), Number.class, true)) {
                        argumentValue = ((Number) argumentValue).intValue();
                    }
                    field.set(this, argumentValue);
                }
            } catch (IllegalAccessException e) {
                System.out.println(this.getClass().getSimpleName() + ": Could not initialize field " + field.getName());
                e.printStackTrace();
            }
        }

        if (!missingProperties.isEmpty()) {
            sendWarning(String.format("Using default values:\n%s", StringUtils.join(missingProperties, "\n")), "", "",
                "Check your experiment arguments for this pattern.");
        }

        onFieldInitCompleted();
    }

    /**
     * Can be implemented to do some custom initialization after the fields were injected.
     */
    protected void onFieldInitCompleted() {
    }

    /**
     * Will be called by the owning instance upon an unexpected shutdown (kill).
     * TODO: distinguish killed and shutdown better
     */
    public void shutdown() {
    }
}
