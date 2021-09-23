package cambio.simulator.parsing.adapter.architecture;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import cambio.simulator.entities.microservice.Operation;
import cambio.simulator.entities.networking.DependencyDescription;
import cambio.simulator.models.MiSimModel;
import cambio.simulator.parsing.GsonHelper;
import cambio.simulator.parsing.ParsingException;
import cambio.simulator.parsing.adapter.NormalDistributionAdapter;
import com.google.gson.Gson;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.UnsafeAllocator;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import desmoj.core.dist.ContDistNormal;

/**
 * Adapter for parsing {@link Operation}s from JSON.
 *
 * @author Lion Wagner
 */
class OperationAdapter extends TypeAdapter<Operation> {

    private final MiSimModel baseModel;
    private final String parentMicroserviceName;
    private final List<DependencyDescription> dependencies;

    public OperationAdapter(MiSimModel baseModel, String name,
                            List<DependencyDescription> dependencies) {
        this.baseModel = baseModel;
        parentMicroserviceName = name;
        this.dependencies = dependencies;
    }

    @Override
    public void write(JsonWriter out, Operation value) throws IOException {

    }

    @Override
    public Operation read(JsonReader in) throws IOException {
        JsonObject root = JsonParser.parseReader(in).getAsJsonObject();
        String name = root.get("name").getAsString().trim();
        if (name.contains(".")) {
            String[] names = name.split("\\.");
            String msName = names[0];
            String opName = names[1];
            if (!msName.equals(parentMicroserviceName)) {
                throw new ParsingException(
                    String.format("Fully qualified name \"%s\" does not match name of the parent \"%s\"", name,
                        parentMicroserviceName));
            }
            name = opName;
        }

        Gson gson = new GsonHelper().getGsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .registerTypeAdapter(Operation.class, new OperationInstanceCreator(baseModel, name))
            .registerTypeAdapter(ContDistNormal.class, new NormalDistributionAdapter(baseModel))
            .registerTypeAdapter(DependencyDescription.class, new DependencyDescriptionCreator(baseModel))
            .create();

        Operation operation = gson.fromJson(root, Operation.class);

        Collections.addAll(this.dependencies, operation.getDependencyDescriptions());
        try {
            Field parentOperationField = DependencyDescription.class.getDeclaredField("parentOperation");
            parentOperationField.setAccessible(true);
            for (DependencyDescription dependency : operation.getDependencyDescriptions()) {
                parentOperationField.set(dependency, operation);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ParsingException("Failed to parse a dependency.", e);
        }

        return operation;
    }


    private static final class OperationInstanceCreator implements InstanceCreator<Operation> {

        private final MiSimModel model;
        private final String name;

        public OperationInstanceCreator(MiSimModel model, String name) {
            this.model = model;
            this.name = name;
        }

        @Override
        public Operation createInstance(Type type) {
            return new Operation(model, name, true, null, 0);
        }
    }

    private static final class DependencyDescriptionCreator implements InstanceCreator<DependencyDescription> {
        private final MiSimModel baseModel;

        public DependencyDescriptionCreator(MiSimModel baseModel) {

            this.baseModel = baseModel;
        }

        @Override
        public DependencyDescription createInstance(Type type) {

            try {
                Field defaultProbability = DependencyDescription.class.getDeclaredField("probability");
                defaultProbability.setAccessible(true);

                //using Gson's UnsafeAllocator to work around having constructors that are only used for parsing.
                DependencyDescription dependencyDescription =
                    UnsafeAllocator.create().newInstance(DependencyDescription.class);
                defaultProbability.set(dependencyDescription,
                    new ContDistNormal(baseModel, "DependencyDistribution", 1, 0, false, false));
                return dependencyDescription;
            } catch (Exception e) {
                throw new ParsingException("Parsing failed", e);
            }
        }
    }
}
