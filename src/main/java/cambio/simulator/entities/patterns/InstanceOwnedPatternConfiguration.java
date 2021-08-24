package cambio.simulator.entities.patterns;

import java.util.Map;

import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.nparsing.TypeNameAssociatedConfigurationData;
import cambio.simulator.nparsing.adapter.PatternConfigurationParser;
import org.jetbrains.annotations.NotNull;

/**
 * Can store the configuration of an {@link InstanceOwnedPattern}.
 * Has the ability to create an instance of this pattern based on the stored configuration.
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

    public InstanceOwnedPattern getPatternInstance(@NotNull MicroserviceInstance owner) {
        InstanceOwnedPattern patternInstance =
            PatternConfigurationParser.getPatternInstance(owner.getModel(), owner.getName(), this,
                InstanceOwnedPattern.class);

        try {
            PatternConfigurationParser.injectOwnerProperty(patternInstance, owner);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return patternInstance;
    }


}
