package cambio.simulator.parsing.adapter.scenario;

import java.io.IOException;

import cambio.simulator.models.ExperimentModel;
import cambio.simulator.models.MiSimModel;
import cambio.simulator.parsing.GsonHelper;
import cambio.simulator.parsing.ParsingException;
import cambio.simulator.parsing.adapter.MiSimModelReferencingTypeAdapter;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * @author Lion Wagner
 */
public class ScenarioDescriptionAdapter extends MiSimModelReferencingTypeAdapter<ExperimentModel> {


    public ScenarioDescriptionAdapter(MiSimModel baseModel) {
        super(baseModel);
    }

    @Override
    public void write(JsonWriter out, ExperimentModel value) throws IOException {

    }

    @Override
    public ExperimentModel read(JsonReader in) throws IOException {
        Gson gson = new GsonHelper().getGsonBuilder().create();
        ScenarioDescription scenarioDescription = gson.fromJson(in, ScenarioDescription.class);

        ExperimentModel parse;
        try {
            parse = scenarioDescription.parse(baseModel);
        } catch (Exception e) {
            throw new ParsingException("Could not parse into Scenario.");
        }
        return parse;
    }
}
