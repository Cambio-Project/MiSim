package cambio.simulator.parsing;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

/**
 * Utility class for resloving {@link JsonTypeName}s into actual types using Reflection.
 */
public class JsonTypeNameResolver {

    private static final HashMap<Class<?>, Map<String, Class<?>>> resolvedNamesCache = new HashMap<>();

    /**
     * Resolves the given {@code jsonTypeName} into an actual type that is marked with {@code @JsonTypeName
     * (jsonTypeName)}.
     *
     * <p>
     * Depending on the search space, this method can be quite slow. However, results for each supertype will be
     * cached.
     *
     * @param jsonTypeName name that should be resolved into a type
     * @param baseClass    a class instance of {@code <U>}
     * @param <U>          super type of the expected target type. All subtypes of this type will be searched for a
     *                     fitting {@link JsonTypeName}.
     * @return the resolved type of the given jsonTypeName or null if none is found.
     */
    public static <U> Class<? extends U> resolveFromJsonTypeName(final String jsonTypeName,
                                                                 final Class<U> baseClass) {
        Map<String, Class<?>> resolvedNames =
            JsonTypeNameResolver.resolvedNamesCache.computeIfAbsent(baseClass, aClass -> {
                @SuppressWarnings("unchecked")
                Class<U> actualBaseClass = (Class<U>) aClass;

                Reflections reflections = new Reflections(
                    new ConfigurationBuilder()
                        .setUrls(ClasspathHelper.forPackage(
                            actualBaseClass.getPackage().getName()))
                        .setScanners(new SubTypesScanner())
                );

                Set<Class<? extends U>> subtypesOfBaseClass = reflections.getSubTypesOf(actualBaseClass);

                Map<String, Class<?>> resolvedNamesInner = new HashMap<>();
                for (Class<? extends U> subtype : subtypesOfBaseClass) {
                    if (!subtype.isAnnotationPresent(JsonTypeName.class)) {
                        continue;
                    }

                    JsonTypeName typeNameValues1 = subtype.getAnnotation(JsonTypeName.class);
                    Set<String> potentialNames =
                        Arrays.stream(typeNameValues1.alternativeNames()).collect(Collectors.toSet());
                    potentialNames.add(typeNameValues1.value());

                    for (String potentialName : potentialNames) {
                        if (resolvedNamesInner.containsKey(potentialName)) {
                            Class<?> other = resolvedNamesInner.get(potentialName);
                            System.out.printf(
                                "Warning: Types %s and %s do have the same JsonTypeName. "
                                    + "Ignoring the second entry (%s).%n",
                                other.getTypeName(), subtype.getTypeName(), subtype.getSimpleName());

                        } else {
                            resolvedNamesInner.put(potentialName, subtype);
                        }
                    }
                }
                return resolvedNamesInner;
            });


        //noinspection unchecked
        return (Class<? extends U>) resolvedNames.get(jsonTypeName);
    }
}