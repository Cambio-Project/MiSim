package cambio.simulator.nparsing;

import java.util.Map;

import cambio.simulator.parsing.GsonParser;
import com.google.gson.Gson;

/**
 * @author Lion Wagner
 */
public class TypeNameAssociatedConfigurationData {
    public String type;
    public Map<String, Object> config;

    public String getConfigAsJsonString() {
        Gson gson = new GsonParser().getGson();
        return gson.toJson(config);
    }

}
