package cambio.simulator.entities.patterns;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;

import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.nparsing.TypeNameAssociatedConfigurationData;
import cambio.simulator.nparsing.adapter.JsonTypeName;
import cambio.simulator.nparsing.adapter.JsonTypeNameResolver;
import cambio.simulator.parsing.GsonHelper;
import com.google.gson.Gson;
import com.google.gson.InstanceCreator;
import desmoj.core.simulator.Model;
import org.jetbrains.annotations.NotNull;

/**
 * Can store the configuration of an {@link InstanceOwnedPattern}.
 * Has the ability to create an instance of this pattern based on the stored configuration.
 *
 * @author Lion Wagner
 * @see InstanceOwnedPatternConfiguration#getPatternInstance(MicroserviceInstance)
 */
public class InstanceOwnedPatternConfiguration extends TypeNameAssociatedConfigurationData {

    public InstanceOwnedPatternConfiguration(String type, Map<String, Object> config) {
        super(type, config);
    }

    /**
     * Transposes the stored configuration into an {@link InstanceOwnedPattern}. For this, all subtypes of
     * {@link InstanceOwnedPattern} are scanned for their @{@link JsonTypeName} annotation. If any of the given names
     * match with the stored {@link InstanceOwnedPatternConfiguration#type} string a new instance is created.
     * {@link Gson} is used to inject the stored {@link InstanceOwnedPatternConfiguration#config} into the new
     * instance after the initialization.
     *
     * @param owner {@link MicroserviceInstance} that shall own the newly created pattern.
     * @return an {@link InstanceOwnedPattern} based on the stored configuration.
     */
    public InstanceOwnedPattern getPatternInstance(@NotNull MicroserviceInstance owner) {
        Objects.requireNonNull(owner);

        Class<? extends InstanceOwnedPattern> targetClass =
            JsonTypeNameResolver.resolveFromJsonTypeName(type, InstanceOwnedPattern.class);
        String name = targetClass.getSimpleName() + " of " + owner.getName();
        Model model = owner.getModel();

        Gson gson = new GsonHelper().getGsonBuilder()
            .registerTypeAdapter(InstanceOwnedPattern.class,
                new InstanceOwnedPatternCreator(owner, model, name, targetClass))
            .create();


        InstanceOwnedPattern instanceOwnedPattern = gson.fromJson(getConfigAsJsonString(), InstanceOwnedPattern.class);
        instanceOwnedPattern.onInitializedCompleted();
        return instanceOwnedPattern;

    }

    private static class InstanceOwnedPatternCreator implements InstanceCreator<InstanceOwnedPattern> {
        private final MicroserviceInstance owner;
        private final Model model;
        private final String name;
        private final Class<? extends InstanceOwnedPattern> targetClass;

        private InstanceOwnedPatternCreator(MicroserviceInstance owner, Model model, String name,
                                            Class<? extends InstanceOwnedPattern> targetClass) {
            this.owner = owner;
            this.model = model;
            this.name = name;
            this.targetClass = targetClass;
        }

        @Override
        public InstanceOwnedPattern createInstance(Type type) {
            try {
                Constructor<? extends InstanceOwnedPattern> c =
                    targetClass.getDeclaredConstructor(Model.class, String.class, Boolean.class,
                        MicroserviceInstance.class);
                return c.newInstance(model, name, true, owner);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(
                    String.format(
                        "Cannot create an instance of %s. Missing constructor of with parameters (%s, %s, %s, %s)",
                        targetClass.getName(), Model.class, String.class, Boolean.class, MicroserviceInstance.class),
                    e);
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(
                    String.format(
                        "Cannot create an instance of %s. Executing the constructor failed",
                        targetClass.getName()), e);
            }
        }
    }


}
