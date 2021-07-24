package cambio.simulator.nparsing;

import java.io.FileNotFoundException;
import java.io.FileReader;

import cambio.simulator.entities.generator.Generator;
import cambio.simulator.nparsing.adapter.GeneratorAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
class GeneratorParserTest {

    @Test
    void name() throws FileNotFoundException {
        Gson gson = new GsonBuilder().registerTypeAdapter(Generator.class, new GeneratorAdapter()).create();

        JsonReader reader = new JsonReader(new FileReader("Examples/new_example.json"));
        Experiment exp = gson.fromJson(reader, Experiment.class);
    }
}