package cambio.simulator.nparsing.adapter.experiement;

import java.io.IOException;

import cambio.simulator.models.ExperimentModel;
import cambio.simulator.models.MiSimModel;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.apache.commons.lang3.NotImplementedException;

/**
 * @author Lion Wagner
 */
public class ExperimentModelAdapter extends TypeAdapter<ExperimentModel> {
    private final MiSimModel baseModel;

    public ExperimentModelAdapter(MiSimModel baseModel) {
        this.baseModel = baseModel;
    }

    @Override
    public void write(JsonWriter out, ExperimentModel value) throws IOException {
        throw new NotImplementedException();
    }

    @Override
    public ExperimentModel read(JsonReader in) throws IOException {
        throw new NotImplementedException();
    }
}
