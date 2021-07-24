package cambio.simulator.nparsing;

import java.util.Map;

import cambio.simulator.parsing.GsonHelper;
import com.google.gson.Gson;

/**
 * @author Lion Wagner
 */
public class TypeNameAssociatedConfigurationData {
    public String type;
    public Map<String, Object> config;

    public String getConfigAsJsonString() {
        Gson gson = new GsonHelper().getGson();
        return gson.toJson(config);
    }

}
