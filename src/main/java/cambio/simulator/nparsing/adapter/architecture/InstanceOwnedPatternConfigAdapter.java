package cambio.simulator.nparsing.adapter.architecture;

import java.io.IOException;

import cambio.simulator.entities.patterns.InstanceOwnedPatternConfiguration;
import cambio.simulator.parsing.GsonHelper;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * @author Lion Wagner
 */
public class InstanceOwnedPatternConfigAdapter extends TypeAdapter<InstanceOwnedPatternConfiguration> {
    @Override
    public void write(JsonWriter out, InstanceOwnedPatternConfiguration value) throws IOException {
        throw new RuntimeException("");
    }

    @Override
    public InstanceOwnedPatternConfiguration read(JsonReader in) throws IOException {
        Gson gson = new GsonHelper().getGson();
        return gson.fromJson(in,InstanceOwnedPatternConfiguration.class);
    }
}
