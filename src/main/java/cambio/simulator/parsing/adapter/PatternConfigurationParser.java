package cambio.simulator.parsing.adapter;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

import cambio.simulator.entities.NamedEntity;
import cambio.simulator.entities.patterns.IPatternLifeCycleHooks;
import cambio.simulator.entities.patterns.IStrategy;
import cambio.simulator.entities.patterns.IStrategyAcceptor;
import cambio.simulator.parsing.EntityCreator;
import cambio.simulator.parsing.GsonHelper;
import cambio.simulator.parsing.TypeNameAssociatedConfigurationData;
import com.google.gson.Gson;
import desmoj.core.simulator.Model;

/**
 * @author Lion Wagner
 */
public class PatternConfigurationParser {


    public static <T extends IPatternLifeCycleHooks, O extends NamedEntity> T
    getPatternInstance(Model model,
                       String ownerName,
                       TypeNameAssociatedConfigurationData configurationData,
                       Class<T> patternBaseType) {

        Class<? extends T> concreteTargetClass =
            JsonTypeNameResolver.resolveFromJsonTypeName(configurationData.type, patternBaseType);

        if (concreteTargetClass == null) {
            System.out.printf("Could not find class that inherits from '%s' and is annotated with type '%s'.%n",
                patternBaseType.getName(), configurationData.type);
            return null;
        }

        String name = concreteTargetClass.getSimpleName() + " of " + ownerName;

        Gson gson = new GsonHelper().getGsonBuilder()
            .registerTypeAdapter(concreteTargetClass,
                EntityCreator.getCreator(model, name, concreteTargetClass))
            .excludeFieldsWithoutExposeAnnotation()
            .create();
        T patternInstance = gson.fromJson(configurationData.getConfigAsJsonString(), concreteTargetClass);

        if (patternInstance instanceof IStrategyAcceptor) {
            ParameterizedType genericSuperclass = (ParameterizedType) concreteTargetClass.getGenericSuperclass();
            Class<? extends IStrategy> strategyBaseType = null;
            //find type argument that defines the Strategy, this may need some improvements for more complex class
            //structures
            for (Type actualTypeArgument : genericSuperclass.getActualTypeArguments()) {
                if (actualTypeArgument instanceof Class
                    && IStrategy.class.isAssignableFrom((Class<?>) actualTypeArgument)) {
                    //noinspection unchecked
                    strategyBaseType = (Class<? extends IStrategy>) actualTypeArgument;
                }
            }

            if (strategyBaseType == null) {
                throw new RuntimeException(
                    String.format("[Error] Could not find %s type parameter. Could not determine concrete "
                        + "type of strategy.", IStrategy.class));
            }


            if (configurationData.hasStrategyConfiguration()) {
                String strategyTypeName = configurationData.strategyConfiguration.type;
                Class<? extends IStrategy> strategyConcreteType =
                    JsonTypeNameResolver.resolveFromJsonTypeName(strategyTypeName, strategyBaseType);
                if (strategyConcreteType == null) {
                    throw new RuntimeException(
                        String.format("[Error] Could not find '%s' as a JsonTypeName inheriting from %s",
                            strategyTypeName,
                            strategyBaseType.getName()));
                }

                Gson strategyGson = new GsonHelper().getGsonBuilder().create();
                IStrategy strategyObject =
                    strategyGson.fromJson(configurationData.getStrategyConfigurationAsJsonString(),
                        strategyConcreteType);

                // noinspection  unchecked
                ((IStrategyAcceptor<IStrategy>) patternInstance).setStrategy(strategyObject);

            } else {
                System.out.printf("[Warning] No strategy information was given for a %s configuration. If no default"
                        + " strategy is specified this run will most likely run into errors.%n",
                    concreteTargetClass.getName());
            }
        }

        patternInstance.onInitializedCompleted();
        return patternInstance;
    }


    public static void injectOwnerProperty(Object child, Object owner)
        throws NoSuchFieldException, IllegalAccessException {
        Class<?> clazz = child.getClass();
        while (Arrays.stream(clazz.getDeclaredFields()).noneMatch(field -> field.getName().equals("owner"))) {
            clazz = clazz.getSuperclass();
        }

        Field ownerField = clazz.getDeclaredField("owner");
        ownerField.setAccessible(true);
        ownerField.set(child, owner);
    }
}
