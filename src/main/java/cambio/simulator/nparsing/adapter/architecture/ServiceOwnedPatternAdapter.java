package cambio.simulator.nparsing.adapter.architecture;

import java.io.IOException;

import cambio.simulator.entities.patterns.ServiceOwnedPattern;
import cambio.simulator.nparsing.TypeNameAssociatedConfigurationData;
import cambio.simulator.nparsing.adapter.PatternConfigurationParser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import desmoj.core.simulator.Model;

/**
 * @author Lion Wagner
 */
public class ServiceOwnedPatternAdapter extends TypeAdapter<ServiceOwnedPattern> {


    private final String msName;
    private final Model model;

    public ServiceOwnedPatternAdapter(Model model, String msName) {
        this.msName = msName;
        this.model = model;
    }

    @Override
    public void write(JsonWriter out, ServiceOwnedPattern value) throws IOException {

    }

    @Override
    public ServiceOwnedPattern read(JsonReader in) throws IOException {
        Gson gson = new GsonBuilder().create();

        if (in.peek() == JsonToken.BEGIN_OBJECT) {
            TypeNameAssociatedConfigurationData configData = gson.fromJson(in,
                TypeNameAssociatedConfigurationData.class);
            ServiceOwnedPattern pattern = PatternConfigurationParser.getPatternInstance(model, msName, configData,
                ServiceOwnedPattern.class);
            return pattern;
        }
        return null;
    }
}
