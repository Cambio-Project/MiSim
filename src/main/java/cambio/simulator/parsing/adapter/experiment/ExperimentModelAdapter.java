package cambio.simulator.parsing.adapter.experiment;

import static cambio.simulator.parsing.adapter.experiment.ExperimentMetaDataAdapter.SIMULATION_METADATA_KEYS;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import cambio.simulator.entities.generator.LoadGeneratorDescriptionExecutor;
import cambio.simulator.events.ExperimentAction;
import cambio.simulator.models.ExperimentModel;
import cambio.simulator.models.MiSimModel;
import cambio.simulator.parsing.GsonHelper;
import cambio.simulator.parsing.ParsingException;
import cambio.simulator.parsing.adapter.MiSimModelReferencingTypeAdapter;
import cambio.simulator.parsing.adapter.NormalDistributionAdapter;
import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.*;
import desmoj.core.dist.ContDistNormal;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;

/**
 * Adapter for parsing a json input into the experiment model. An experiment model contains generator definitions und
 * experiment action definitions.
 *
 * <p>
 * Actions can be written directly into the root json. These are named actions:
 * <pre>
 *         {
 *             ...
 *             "named_chaos_monkey": {
 *                 "type": "chaosmonkey",
 *                 "config":{
 *                     ...
 *                 }
 *             },
 *             ...
 *         }
 * </pre>
 * Actions can also be named by providing the {@code action_name} property. Also, actions can also be nested in an array
 * for grouping.
 *
 * <p>
 * This would look like the following:
 * <pre>
 *         {
 *             ...
 *             "monkeys_group":[
 *                 "unnamed_chaos_monkey": {
 *                     "type": "chaosmonkey",
 *                     "config":{
 *                         ...
 *                     }
 *                 },
 *                 "named_chaos_monkey": {
 *                     "type": "chaosmonkey",
 *                     "action_name": "ChaosMonkey2",
 *                     "config":{
 *                         ...
 *                     }
 *                 },
 *                 ...
 *             ]
 *             ...
 *         } *
 * </pre>
 *
 * @author Lion Wagner
 */
public class ExperimentModelAdapter extends MiSimModelReferencingTypeAdapter<ExperimentModel> {
    public static final String CURRENT_JSON_OBJECT_NAME_KEY = "action_name";

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

    public ExperimentModelAdapter(MiSimModel baseModel) {
        super(baseModel);
    }

    @Override
    public void write(JsonWriter out, ExperimentModel value) throws IOException {
        throw new NotImplementedException();
    }

    @Override
    public ExperimentModel read(@NotNull JsonReader in) throws IOException {
        LoadGeneratorExecutorAdapter generatorExecutorAdapter = new LoadGeneratorExecutorAdapter(model);
        ExperimentActionAdapter experimentActionAdapter = new ExperimentActionAdapter(model);
        Gson gson = GsonHelper.getGsonBuilder()
            .registerTypeAdapter(LoadGeneratorDescriptionExecutor.class, generatorExecutorAdapter)
            .registerTypeAdapter(ExperimentAction.class, experimentActionAdapter)
            .registerTypeAdapter(ContDistNormal.class, new NormalDistributionAdapter(model))
            .registerTypeAdapterFactory(new NameReferenceTypeAdapterFactory(model))
            .create();

        generatorExecutorAdapter.setParser(gson);
        experimentActionAdapter.setParser(gson);


        List<LoadGeneratorDescriptionExecutor> generators = new ArrayList<>();
        List<ExperimentAction> experimentActions = new ArrayList<>();

        if (in.peek() == JsonToken.NULL) {
            return null;
        }

        in.beginObject();
        while (in.hasNext()) {
            try {
                //order matters here!
                String elementName = in.nextName();
                JsonToken token = in.peek();
                JsonElement currentElement = JsonParser.parseReader(in);
                if (generatorNames.contains(elementName)) {
                    generators.addAll(Arrays.asList(parseToGeneratorArray(currentElement, token, gson)));
                } else if (token == JsonToken.BEGIN_ARRAY) {
                    //parsing a grouped list of (potentially unnamed) ExperimentActions
                    ExperimentAction[] parsedExperimentActions =
                        gson.fromJson(currentElement, ExperimentAction[].class);
                    Collections.addAll(experimentActions, parsedExperimentActions);
                } else if (token == JsonToken.BEGIN_OBJECT
                    && Arrays.stream(SIMULATION_METADATA_KEYS).noneMatch(key -> key.equals(elementName))) {
                    //parsing a named ExperimentAction
                    //the name "simulation_meta_data" is reserved for metadata
                    currentElement.getAsJsonObject().add(CURRENT_JSON_OBJECT_NAME_KEY, new JsonPrimitive(elementName));
                    ExperimentAction experimentAction = gson.fromJson(currentElement, ExperimentAction.class);
                    experimentActions.add(experimentAction);
                }

            } catch (ParsingException e) {
                Matcher lineMatcher = Pattern.compile(".*line (\\d+).*").matcher(in.toString());
                Matcher columnMatcher = Pattern.compile(".*column (\\d+).*").matcher(in.toString());
                boolean found = lineMatcher.find() && columnMatcher.find();

                if (found) {
                    int line = lineMatcher.groupCount() > 0 ? Integer.parseInt(lineMatcher.group(1)) : -1;
                    int column = columnMatcher.groupCount() > 0 ? Integer.parseInt(columnMatcher.group(1)) : -1;
                    throw new ParsingException(String.format("Failed parsing at line %s column %s.\n[Reason]: %s", line,
                        column,
                        e.getMessage()), e);
                } else {
                    throw e;
                }
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
