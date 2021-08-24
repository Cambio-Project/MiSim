package cambio.simulator.nparsing;

import java.util.HashMap;
import java.util.Map;

import cambio.simulator.parsing.GsonHelper;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * @author Lion Wagner
 */
public class TypeNameAssociatedConfigurationData {
    //these fields will be injected by Gson
    public final String type;
    public final Map<String, Object> config;

    @SerializedName(value = "strategy")
    public final TypeNameAssociatedConfigurationData strategyConfiguration;

    public TypeNameAssociatedConfigurationData(String type, Map<String, Object> config,
                                               TypeNameAssociatedConfigurationData strategyConfiguration) {
        this.type = type;
        this.config = config;
        this.strategyConfiguration = strategyConfiguration;
    }

    public String getConfigAsJsonString() {
        Gson gson = new GsonHelper().getGson();
        if (config == null) {
            return gson.toJson(new HashMap<String, Object>());
        }
        return gson.toJson(config);
    }

    public String getStrategyConfigurationAsJsonString() {
        if (strategyConfiguration != null) {
            return strategyConfiguration.getConfigAsJsonString();
        } else {
            return null;
        }
    }

    public boolean hasStrategyConfiguration() {
        return strategyConfiguration != null;
    }

}
