package cambio.simulator.parsing;

import java.lang.reflect.*;
import java.util.Arrays;

import cambio.simulator.entities.patterns.*;
import cambio.simulator.models.MiSimModel;
import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class for parsing {@link TypeNameAssociatedConfigurationData} into a pattern.
 *
 * @author Lion Wagner
 */
public class PatternConfigurationParser {

    /**
     * Resolves a {@link TypeNameAssociatedConfigurationData} into a pattern instance. If a strategy configuration is
     * given, it will also be resolved into an {@link IStrategy} object and assigned using the {@link
     * IStrategyAcceptor#setStrategy(IStrategy)} method.
     *
     * @param model             underlying model
     * @param ownerName         Name of the owner, will be used for naming the pattern
     * @param configurationData data that is used to configure (to parse from) the pattern
     * @param patternBaseType   an instance of  {@code <T>}
     * @param <T>               super type of the expected pattern, usually {@link InstanceOwnedPattern} or {@link
     *                          ServiceOwnedPattern}
     * @return an instance of the pattern described by the {@code configurationData}
     * @throws ClassNotFoundException    if the type given by the configuration is not a sub-type of {@code <T>}.
     * @throws ClassCastException        if the evaluated class of the resulting pattern implements {@link
     *                                   IStrategyAcceptor} but does not have a generic parameter extending {@link
     *                                   IStrategy}.
     * @throws StrategyNotFoundException if a base {@link IStrategy} type was found, but no extending type that
     *                                   correlates to the type name given in the strategy configuration given in the
     *                                   {@code configurationData} was found.
     * @see JsonTypeName
     */
    public static <T extends IPatternLifeCycleHooks> T getPatternInstance(
        MiSimModel model,
        String ownerName,
        @NotNull TypeNameAssociatedConfigurationData configurationData,
        Class<T> patternBaseType) throws ClassNotFoundException {

        Class<? extends T> concreteTargetClass =
            JsonTypeNameResolver.resolveFromJsonTypeName(configurationData.type, patternBaseType);

        if (concreteTargetClass == null) {
            String msg =
                String.format("Could not find class that inherits from '%s' and is annotated with type '%s'.%n",
                    patternBaseType.getName(), configurationData.type);
            System.out.println(msg);
            throw new ClassNotFoundException(msg);
        }

        String name = concreteTargetClass.getSimpleName() + " of " + ownerName;

        Gson gson = GsonHelper.getGsonBuilder()
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
                throw new ClassCastException(
                    String.format("[Error] Could not find %s generic type parameter. Could not determine concrete "
                        + "type of strategy.", IStrategy.class));
            }


            if (configurationData.hasStrategyConfiguration()) {
                String strategyTypeName = configurationData.strategyConfiguration.type;
                Class<? extends IStrategy> strategyConcreteType =
                    JsonTypeNameResolver.resolveFromJsonTypeName(strategyTypeName, strategyBaseType);
                if (strategyConcreteType == null) {
                    throw new StrategyNotFoundException(
                        String.format("[Error] Could not find '%s' as a JsonTypeName inheriting from %s",
                            strategyTypeName,
                            strategyBaseType.getName()));
                }

                Gson strategyGson = GsonHelper.getGsonBuilder().create();
                IStrategy strategyObject =
                    strategyGson.fromJson(configurationData.getStrategyConfigurationAsJsonString(),
                        strategyConcreteType);

                // noinspection  unchecked
                ((IStrategyAcceptor<IStrategy>) patternInstance).setStrategy(strategyObject);
                strategyObject.onInitializedCompleted(model);

            } else {
                System.out.printf("[Warning] No strategy information was given for a %s configuration. If no default"
                        + " strategy is specified this run will most likely fail.%n",
                    concreteTargetClass.getName());
            }
        }

        patternInstance.onInitializedCompleted(model);
        return patternInstance;
    }

    /**
     * Tries to inject the owner object in the 'owner' field of the child object.
     *
     * @param child injection target
     * @param owner injection value
     * @throws NoSuchFieldException   if {@code  child} does not have an 'owner' field
     * @throws IllegalAccessException if the 'owner' field of {@code child} is not accessible via reflection
     */
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
