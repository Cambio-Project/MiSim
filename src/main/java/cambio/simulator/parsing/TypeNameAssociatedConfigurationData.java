package cambio.simulator.parsing;

import java.util.HashMap;
import java.util.Map;

import cambio.simulator.entities.patterns.IStrategyAcceptor;
import cambio.simulator.parsing.adapter.JsonTypeName;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * Represents the configuration data that relates to a specific type.
 *
 * @author Lion Wagner
 * @see JsonTypeName
 */
public class TypeNameAssociatedConfigurationData {
    //these fields will be injected by Gson
    public final String type;
    public final Map<String, Object> config;

    @SerializedName(value = "strategy")
    public final TypeNameAssociatedConfigurationData strategyConfiguration;

    /**
     * Creates a new {@link TypeNameAssociatedConfigurationData} based on the arguments.
     *
     * @param strategyConfiguration nested configuration for types that are {@link IStrategyAcceptor}s.
     */
    public TypeNameAssociatedConfigurationData(String type, Map<String, Object> config,
                                               TypeNameAssociatedConfigurationData strategyConfiguration) {
        this.type = type;
        this.config = config;
        this.strategyConfiguration = strategyConfiguration;
    }

    /**
     * Converts the nested configuration data into a JSON-string using Gson.
     *
     * @return the configuration data  as JSON
     */
    public String getConfigAsJsonString() {
        Gson gson = GsonHelper.getGson();
        if (config == null) {
            return gson.toJson(new HashMap<String, Object>());
        }
        return gson.toJson(config);
    }

    /**
     * Converts the nested strategy configuration data into a JSON-string using Gson.
     *
     * @return the strategy configuration data as JSON. Returns {@code null} if there is no strategy configuration.
     */
    public String getStrategyConfigurationAsJsonString() {
        if (strategyConfiguration != null) {
            return strategyConfiguration.getConfigAsJsonString();
        } else {
            return null;
        }
    }

    /**
     * Checks whether there is a nested strategy configuration.
     *
     * @return {@code true} when there is a nested strategy configuration, {@code false} otherwise.
     */
    public boolean hasStrategyConfiguration() {
        return strategyConfiguration != null;
    }

}
