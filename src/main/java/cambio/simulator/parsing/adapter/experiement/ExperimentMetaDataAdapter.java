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
 * Adapter for parsing the metadata within an experiment description into a {@link ExperimentMetaData } object.
 *
 * <p>
 * Looks for the keys found in {@link cambio.simulator.parsing.adapter.experiement.ExperimentMetaDataAdapter#SIMULATION_METADATA_KEYS}
 * within the description. If found, the {@link JsonObject} with an equal name will be parsed, otherwise the description
 * will be directly parsed.
 *
 * @author Lion Wagner
 */
public class ExperimentMetaDataAdapter extends TypeAdapter<ExperimentMetaData> {
    public static final String[] SIMULATION_METADATA_KEYS = {"simulation_metadata", "simulation_meta_data"};
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

            // if contains a member name from "SIMULATION_METADATA_KEYS" ->
            // parse the value of this member into ExperimentMetaData
            for (String key : SIMULATION_METADATA_KEYS) {
                if (root.has(key)) {
                    root = root.get(key).getAsJsonObject();
                    break;
                }
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
