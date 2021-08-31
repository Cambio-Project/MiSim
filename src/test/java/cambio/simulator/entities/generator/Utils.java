package cambio.simulator.entities.generator;

import java.io.IOException;

import cambio.simulator.entities.microservice.Operation;
import cambio.simulator.parsing.GsonHelper;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * @author Lion Wagner
 */
public class Utils {

    public static <T extends LoadGeneratorDescription> T getLoadGeneratorDescription(String configJson,
                                                                                     Class<T> clazz) {
        Gson gson = new GsonHelper()
            .getGsonBuilder().registerTypeAdapter(Operation.class, new TypeAdapter<Operation>() {
                @Override
                public void write(JsonWriter out, Operation value) throws IOException {
                }

                @Override
                public Operation read(JsonReader in) throws IOException {
                    return null;
                }
            }).create();

        T descriptionInstance = gson.fromJson(configJson, clazz);
        descriptionInstance.initializeArrivalRateModel();
        return descriptionInstance;

    }
}
