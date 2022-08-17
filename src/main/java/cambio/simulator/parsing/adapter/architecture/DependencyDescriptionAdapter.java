package cambio.simulator.parsing.adapter.architecture;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

import cambio.simulator.entities.networking.AbstractDependencyDescription;
import cambio.simulator.entities.networking.AlternativeDependencyDescription;
import cambio.simulator.entities.networking.DependencyDescription;
import cambio.simulator.entities.networking.LoopDependencyDescription;
import cambio.simulator.entities.networking.SimpleDependencyDescription;
import cambio.simulator.models.MiSimModel;
import cambio.simulator.parsing.GsonHelper;
import cambio.simulator.parsing.ParsingException;
import cambio.simulator.parsing.adapter.DiscreteDistributionAdapter;
import cambio.simulator.parsing.adapter.MiSimModelReferencingTypeAdapter;
import cambio.simulator.parsing.adapter.NormalDistributionAdapter;
import com.google.gson.Gson;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.internal.UnsafeAllocator;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import desmoj.core.dist.ContDistNormal;
import desmoj.core.dist.DiscreteDist;
import desmoj.core.dist.DiscreteDistConstant;

/**
 * Adapter for parsing {@link DependencyDescription}s from JSON.
 * 
 * @author Sebastian Frank, Lion Wagner
 *
 */
public class DependencyDescriptionAdapter
        extends MiSimModelReferencingTypeAdapter<DependencyDescription> {

    private final String parentMicroserviceName;

    public DependencyDescriptionAdapter(MiSimModel model, String parentMicroserviceName) {
        super(model);
        this.parentMicroserviceName = parentMicroserviceName;
    }

    @Override
    public void write(JsonWriter out, DependencyDescription value) throws IOException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public DependencyDescription read(JsonReader in) throws IOException {
        if (in == null) {
            throw new IllegalArgumentException("JsonReader must not be null");
        }

        JsonObject root = JsonParser.parseReader(in).getAsJsonObject();
        JsonElement jsonTypeElement = root.get("type");
        Type instanceType = SimpleDependencyDescription.class; // default type
        if (jsonTypeElement != null) {
            instanceType = detectDependencyTypeFrom(jsonTypeElement);
        }

        return createDependencyFrom(root, instanceType);
    }

    private Type detectDependencyTypeFrom(final JsonElement typeElement) {
        assert typeElement != null;

        final String lowerCaseType = typeElement.getAsString().trim().toLowerCase();
        switch (lowerCaseType) {
            case "basic":
                return SimpleDependencyDescription.class;
            case "alternative":
                return AlternativeDependencyDescription.class;
            case "loop":
                return LoopDependencyDescription.class;
            default:
                throw new JsonParseException(
                        "Could not recognize dependency type: " + lowerCaseType);
        }
    }

    private DependencyDescription createDependencyFrom(final JsonObject root,
            final Type instanceType) {
        assert root != null;
        assert instanceType != null;

        Gson gson = GsonHelper.getGsonBuilder().excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(instanceType, new DependencyDescriptionCreator(model))
                .registerTypeAdapter(DependencyDescription.class,
                        new DependencyDescriptionAdapter(model, parentMicroserviceName))
                .registerTypeHierarchyAdapter(DiscreteDist.class,
                        new DiscreteDistributionAdapter(model))
                .create();
        return gson.fromJson(root, instanceType);
    }

    private static final class DependencyDescriptionCreator
            implements InstanceCreator<DependencyDescription> {
        private final MiSimModel baseModel;

        public DependencyDescriptionCreator(MiSimModel baseModel) {
            this.baseModel = baseModel;
        }

        @Override
        public DependencyDescription createInstance(Type type) {
            if (!(type instanceof Class)) {
                throw new IllegalArgumentException("Type must be instantiable.");
            }

            try {
                Class<DependencyDescription> clazz = (Class<DependencyDescription>) type;

                // using Gson's UnsafeAllocator to work around having constructors that are
                // only used for parsing.
                // TODO: Should be changed since this kind of 'black magic' can cause a lot of
                // trouble ;)
                DependencyDescription dependencyDescription =
                        UnsafeAllocator.create().newInstance(clazz);
                setDefaultProbability(dependencyDescription);
                setDefaultAlternativeProbability(dependencyDescription);
                setDefaultIterations(dependencyDescription);

                return dependencyDescription;
            } catch (Exception e) {
                throw new ParsingException("Parsing of dependency failed", e);
            }
        }

        private void setDefaultProbability(DependencyDescription dependencyDescription)
                throws ReflectiveOperationException {
            if (dependencyDescription instanceof AbstractDependencyDescription) {
                Field defaultProbability =
                        AbstractDependencyDescription.class.getDeclaredField("probability");
                defaultProbability.setAccessible(true);
                defaultProbability.set(dependencyDescription, new ContDistNormal(baseModel,
                        "DependencyDistribution", 1, 0, false, false));
            }
        }

        private void setDefaultAlternativeProbability(DependencyDescription dependencyDescription)
                throws ReflectiveOperationException {
            if (dependencyDescription instanceof AbstractDependencyDescription) {
                Field defaultAlternativeProbability = AbstractDependencyDescription.class
                        .getDeclaredField("alternativeProbability");
                defaultAlternativeProbability.setAccessible(true);
                defaultAlternativeProbability.set(dependencyDescription, new ContDistNormal(
                        baseModel, "DependencyAlternativeDistribution", 1, 0, false, false));
            }
        }

        private void setDefaultIterations(DependencyDescription dependencyDescription)
                throws ReflectiveOperationException {
            if (dependencyDescription instanceof LoopDependencyDescription) {
                Field iterations = LoopDependencyDescription.class.getDeclaredField("iterations");
                iterations.setAccessible(true);
                iterations.set(dependencyDescription, new DiscreteDistConstant<Integer>(baseModel,
                        "DependencyIterationsDistribution", 1, false, false));
            }
        }

    }

}
