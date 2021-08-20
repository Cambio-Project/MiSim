package cambio.simulator.nparsing;

import java.util.Map;

import cambio.simulator.parsing.GsonHelper;
import com.google.gson.Gson;

/**
 * @author Lion Wagner
 */
public class TypeNameAssociatedConfigurationData {
    //these fields will be injected by Gson
    public final String type;
    public final Map<String, Object> config;

    public TypeNameAssociatedConfigurationData(String type, Map<String, Object> config) {
        this.type = type;
        this.config = config;
    }

    public String getConfigAsJsonString() {
        Gson gson = new GsonHelper().getGson();
        return gson.toJson(config);
    }

}
