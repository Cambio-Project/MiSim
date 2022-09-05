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
import cambio.simulator.parsing.adapter.MiSimModelReferencingTypeAdapter;
import cambio.simulator.parsing.adapter.NormalDistributionAdapter;
import com.google.gson.*;
import com.google.gson.internal.UnsafeAllocator;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import desmoj.core.dist.ContDistNormal;

/**
 * Adapter for parsing {@link Operation}s from JSON.
 *
 * @author Lion Wagner
 */
public class OperationAdapter extends MiSimModelReferencingTypeAdapter<Operation> {

    private final String parentMicroserviceName;
    private final List<DependencyDescription> dependencies;

    /**
     * Constructor creating an adapter for parsing Operations. This adapter will not parse the underlying {@link
     * DependencyDescription}s, since they can only be resolved once all {@link Operation}s have been initialized.
     * Instead, it will provide a list of dependencies, that can be resolved later.
     *
     * @param baseModel    Base model of the simulation.
     * @param name         Name of the parent {@link cambio.simulator.entities.microservice.Microservice}.
     * @param dependencies A mutable {@link List} to which all dependencies of the operation will be added.
     */
    public OperationAdapter(MiSimModel baseModel, String name,
                            List<DependencyDescription> dependencies) {
        super(baseModel);
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

        if (name.startsWith(parentMicroserviceName)) {
            name = name.substring(parentMicroserviceName.length() + 1);
        }

        Gson gson = GsonHelper.getGsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .registerTypeAdapter(Operation.class, new OperationInstanceCreator(model, name))
            .registerTypeAdapter(ContDistNormal.class, new NormalDistributionAdapter(model))
            .registerTypeAdapter(DependencyDescription.class, new DependencyDescriptionCreator(model))
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
