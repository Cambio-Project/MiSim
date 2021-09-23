package cambio.simulator.entities.patterns;

import java.util.Map;

import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.parsing.TypeNameAssociatedConfigurationData;
import cambio.simulator.parsing.adapter.JsonTypeNameResolver;
import cambio.simulator.parsing.adapter.PatternConfigurationParser;
import org.jetbrains.annotations.NotNull;

/**
 * Can store the configuration of an {@link InstanceOwnedPattern}. Has the ability to create an instance of this pattern
 * based on the stored configuration.
 *
 * @author Lion Wagner
 * @see InstanceOwnedPatternConfiguration#getPatternInstance(MicroserviceInstance)
 */
public class InstanceOwnedPatternConfiguration extends TypeNameAssociatedConfigurationData {

    public InstanceOwnedPatternConfiguration(String type, Map<String, Object> config) {
        super(type, config, null);
    }

    public InstanceOwnedPatternConfiguration(String type, Map<String, Object> config,
                                             TypeNameAssociatedConfigurationData strategyConfiguration) {
        super(type, config, strategyConfiguration);
    }

    /**
     * Parses this configuration into a pattern instance.
     *
     * @param owner {@link MicroserviceInstance} that will own the created pattern.
     * @return an instance of pattern that is defined in the given config.
     */
    public InstanceOwnedPattern getPatternInstance(@NotNull MicroserviceInstance owner) {
        InstanceOwnedPattern patternInstance =
            PatternConfigurationParser.getPatternInstance(owner.getModel(), owner.getName(), this,
                InstanceOwnedPattern.class);

        try {
            if (patternInstance != null) {
                PatternConfigurationParser.injectOwnerProperty(patternInstance, owner);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return patternInstance;
    }

    /**
     * Triggers the {@link JsonTypeNameResolver} to build a cache for all subtypes of {@link InstanceOwnedPattern}.
     * Calling this method speeds up simulation time, but slows down parsing by an equal amount.
     */
    public void preCacheData() {
        JsonTypeNameResolver.resolveFromJsonTypeName(type, InstanceOwnedPattern.class);
    }
}
