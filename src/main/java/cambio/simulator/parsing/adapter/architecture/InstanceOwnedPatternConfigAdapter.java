package cambio.simulator.parsing.adapter.architecture;

import java.io.IOException;

import cambio.simulator.entities.patterns.InstanceOwnedPatternConfiguration;
import cambio.simulator.parsing.GsonHelper;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Adapter for parsing pattern configurations written in json into {@link InstanceOwnedPatternConfiguration}s. These
 * will later be used by the {@link cambio.simulator.entities.microservice.Microservice} class to instantiate a new
 * instance of the target pattern.
 *
 * @author Lion Wagner
 */
class InstanceOwnedPatternConfigAdapter extends TypeAdapter<InstanceOwnedPatternConfiguration> {
    @Override
    public void write(JsonWriter out, InstanceOwnedPatternConfiguration value) throws IOException {
        throw new RuntimeException("");
    }

    @Override
    public InstanceOwnedPatternConfiguration read(JsonReader in) throws IOException {
        Gson gson = new GsonHelper().getGson();
        return gson.fromJson(in, InstanceOwnedPatternConfiguration.class);
    }
}
