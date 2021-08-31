package cambio.simulator.parsing.adapter.experiement;

import java.io.File;
import java.io.IOException;

import cambio.simulator.models.ExperimentMetaData;
import cambio.simulator.parsing.GsonHelper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * @author Lion Wagner
 */
public class ExperimentMetaDataAdapter extends TypeAdapter<ExperimentMetaData> {
    public static final String SIMULATION_METADATA_KEY = "simulation_metadata";
    private final File experimentOrScenarioFileLocation;
    private final File architectureModelLocation;

    public ExperimentMetaDataAdapter(File experimentOrScenarioFileLocation, File architectureModelLocation) {
        this.experimentOrScenarioFileLocation = experimentOrScenarioFileLocation;
        this.architectureModelLocation = architectureModelLocation;
    }

    @Override
    public void write(JsonWriter out, ExperimentMetaData value) throws IOException {

    }

    @Override
    public ExperimentMetaData read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.BEGIN_OBJECT) {
            Gson gson = new GsonHelper().getGson();
            JsonObject root = JsonParser.parseReader(in).getAsJsonObject();

            // if contains "simulation_metadata" -> parse the value of this key into ExperimentMetaData
            if (root.has(SIMULATION_METADATA_KEY)) {
                root = root.get(SIMULATION_METADATA_KEY).getAsJsonObject();
            }

            if (!root.has("exp_file_location")) {
                root.add("exp_file_location", new JsonPrimitive(gson.toJson(experimentOrScenarioFileLocation)));
            }
            if (!root.has("arch_file_location")) {
                root.add("arch_file_location", new JsonPrimitive(gson.toJson(architectureModelLocation)));
            }
            return gson.fromJson(root, ExperimentMetaData.class);
        } else {
            in.skipValue();
            return null;
        }
    }
}
