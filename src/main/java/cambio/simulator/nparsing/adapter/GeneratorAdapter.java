package cambio.simulator.nparsing.adapter;

import java.io.IOException;

import cambio.simulator.entities.generator.Generator;
import cambio.simulator.nparsing.TypeNameAssociatedConfigurationData;
import cambio.simulator.parsing.GsonParser;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * @author Lion Wagner
 */
public class GeneratorAdapter extends TypeAdapter<Generator> {
    @Override
    public void write(JsonWriter out, Generator value) throws IOException {

    }

    @Override
    public Generator read(JsonReader in) throws IOException {

        Gson gson = new GsonParser().getGson();

        TypeNameAssociatedConfigurationData generatorData =
            gson.fromJson(in, TypeNameAssociatedConfigurationData.class);

        if (generatorData.type == null) {
            System.out.println();
            in.skipValue();
        }

        Class<? extends LoadGeneratorDescription> targetType =
            GeneratorDescriptionResolver.getDescriptors().get(generatorData.type);

        LoadGeneratorDescription resultDescriptor = gson.fromJson(generatorData.getConfigAsJsonString(), targetType);


        return null;
    }
}
