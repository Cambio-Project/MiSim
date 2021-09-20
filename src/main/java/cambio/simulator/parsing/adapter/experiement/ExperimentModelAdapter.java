package cambio.simulator.parsing.adapter.experiement;

import static cambio.simulator.parsing.adapter.experiement.ExperimentMetaDataAdapter.SIMULATION_METADATA_KEY;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import cambio.simulator.entities.generator.LoadGeneratorDescriptionExecutor;
import cambio.simulator.events.ExperimentAction;
import cambio.simulator.models.ExperimentModel;
import cambio.simulator.models.MiSimModel;
import cambio.simulator.parsing.GsonHelper;
import cambio.simulator.parsing.adapter.NormalDistributionAdapter;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import desmoj.core.dist.ContDistNormal;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;

/**
 * @author Lion Wagner
 */
public class ExperimentModelAdapter extends TypeAdapter<ExperimentModel> {
    public static final String CURRENT_JSON_OBJECT_NAME_KEY = "event_name";

    /**
     * Names that the array containing the generators inside the experiment file can have.
     */
    private static final List<String> generatorNames;

    static {
        List<String> names = new LinkedList<>();
        try {
            Field field = ExperimentModel.class.getDeclaredField("generators");
            field.setAccessible(true);
            SerializedName annotation = field.getAnnotation(SerializedName.class);

            if (annotation != null) {
                names.add(annotation.value());
                Collections.addAll(names, annotation.alternate());
            } else {
                names.add(field.getName());
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        generatorNames = Collections.unmodifiableList(names);
    }

    private final MiSimModel baseModel;

    public ExperimentModelAdapter(MiSimModel baseModel) {
        this.baseModel = baseModel;
    }

    @Override
    public void write(JsonWriter out, ExperimentModel value) throws IOException {
        throw new NotImplementedException();
    }

    @Override
    public ExperimentModel read(@NotNull JsonReader in) throws IOException {
        GeneratorExecutorAdapter generatorExecutorAdapter = new GeneratorExecutorAdapter(baseModel);
        ExperimentActionAdapter experimentActionAdapter = new ExperimentActionAdapter(baseModel);
        Gson gson = new GsonHelper().getGsonBuilder()
            .registerTypeAdapter(LoadGeneratorDescriptionExecutor.class, generatorExecutorAdapter)
            .registerTypeAdapter(ExperimentAction.class, experimentActionAdapter)
            .registerTypeAdapter(ContDistNormal.class, new NormalDistributionAdapter(baseModel))
            .registerTypeAdapterFactory(new NameReferenceTypeAdapterFactory(baseModel))
            .create();

        generatorExecutorAdapter.setParser(gson);
        experimentActionAdapter.setParser(gson);


        LoadGeneratorDescriptionExecutor[] generators = new LoadGeneratorDescriptionExecutor[0];
        Set<ExperimentAction> experimentActions = new HashSet<>();

        if (in.peek() == JsonToken.NULL) {
            return null;
        }

        in.beginObject();
        while (in.hasNext()) {
            //order matters here!
            String elementName = in.nextName();
            JsonToken token = in.peek();
            JsonElement currentElement = JsonParser.parseReader(in);
            if (generatorNames.contains(elementName)) {
                generators = parseToGeneratorArray(currentElement, token, gson);
            } else if (token == JsonToken.BEGIN_ARRAY) {
                //parsing a grouped list of (potentially unnamed) ExperimentActions
                ExperimentAction[] parsedExperimentActions = gson.fromJson(currentElement, ExperimentAction[].class);
                Collections.addAll(experimentActions, parsedExperimentActions);
            } else if (token == JsonToken.BEGIN_OBJECT && !elementName.equals(SIMULATION_METADATA_KEY)) {
                //parsing a named ExperimentAction
                //the name "simulation_meta_data" is reserved for metadata
                currentElement.getAsJsonObject().add(CURRENT_JSON_OBJECT_NAME_KEY, new JsonPrimitive(elementName));
                ExperimentAction experimentAction = gson.fromJson(currentElement, ExperimentAction.class);
                experimentActions.add(experimentAction);
            }
        }
        in.endObject();

        return new ExperimentModel(generators, experimentActions);
    }

    private LoadGeneratorDescriptionExecutor[] parseToGeneratorArray(JsonElement currentElement, JsonToken token,
                                                                     Gson gson) {
        LoadGeneratorDescriptionExecutor[] generators = new LoadGeneratorDescriptionExecutor[0];
        if (token == JsonToken.BEGIN_ARRAY) {
            //expecting array of generators
            LoadGeneratorDescriptionExecutor[] generatorsRaw = gson.fromJson(currentElement,
                LoadGeneratorDescriptionExecutor[].class);
            generators = Arrays.stream(generatorsRaw)
                .filter(Objects::nonNull)
                .toArray(LoadGeneratorDescriptionExecutor[]::new);

        } else if (token == JsonToken.BEGIN_OBJECT) {
            //expecting a single
            LoadGeneratorDescriptionExecutor generatorRaw = gson.fromJson(currentElement,
                LoadGeneratorDescriptionExecutor.class);
            if (generatorRaw != null) {
                generators = new LoadGeneratorDescriptionExecutor[] {generatorRaw};
            }
        }
        return generators;
    }

}
