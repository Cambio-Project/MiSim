package cambio.simulator.parsing.adapter.experiement;

import java.io.IOException;

import cambio.simulator.entities.generator.LoadGeneratorDescription;
import cambio.simulator.entities.generator.LoadGeneratorDescriptionExecutor;
import cambio.simulator.models.MiSimModel;
import cambio.simulator.parsing.adapter.ConfigurableNamedTypeAdapter;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Adapter for creating {@link LoadGeneratorDescriptionExecutor}s  from JSON.
 *
 * @author Lion Wagner
 */
public class LoadGeneratorExecutorAdapter extends TypeAdapter<LoadGeneratorDescriptionExecutor> {

    private final MiSimModel model;

    private Gson parser;

    public LoadGeneratorExecutorAdapter(MiSimModel model) {
        this.model = model;
    }

    public void setParser(Gson parser) {
        this.parser = parser;
    }

    @Override
    public void write(JsonWriter out, LoadGeneratorDescriptionExecutor value) throws IOException {

    }

    @Override
    public LoadGeneratorDescriptionExecutor read(JsonReader in) throws IOException {
        if (parser == null) {
            throw new IllegalStateException("Parser was not initialized.");
        }

        ConfigurableNamedTypeAdapter<LoadGeneratorDescription> adapter =
            new ConfigurableNamedTypeAdapter<>(LoadGeneratorDescription.class, parser);
        LoadGeneratorDescription description = adapter.read(in);
        if (description.getTargetOperation() == null) {
            return null;
        }
        description.initializeArrivalRateModel();
        return new LoadGeneratorDescriptionExecutor(model, description);
    }
}
