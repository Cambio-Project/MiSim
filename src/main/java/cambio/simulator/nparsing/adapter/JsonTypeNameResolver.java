package cambio.simulator.nparsing.adapter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

public class JsonTypeNameResolver {

    private static final HashMap<Class<?>, Map<String, Class<?>>> resolvedNamesCache = new HashMap<>();

    public static <U> Class<? extends U> resolveFromJsonTypeName(final String jsonTypeName,
                                                                 final Class<U> targetClass) {
        Map<String, Class<?>> resolvedNames =
            JsonTypeNameResolver.resolvedNamesCache.computeIfAbsent(targetClass, aClass -> {
                Class<U> actualClass = (Class<U>) aClass;

                Reflections reflections = new Reflections(
                    new ConfigurationBuilder()
                        .setUrls(ClasspathHelper.forPackage(
                            actualClass.getPackage().getName()))
                        .setScanners(new SubTypesScanner())
                );

                Set<Class<? extends U>> subtypes = reflections.getSubTypesOf(actualClass);

                Map<String, Class<?>> resolvedNamesInner = new HashMap<String, Class<?>>();
                for (Class<? extends U> subtype : subtypes) {
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
                            System.out.println("Warning: Types " + other.getTypeName() + " and " + subtype.getTypeName()
                                + " do have the same JsonTypeName. Ignoring the second entry.");

                        } else {
                            resolvedNamesInner.put(potentialName, subtype);
                        }
                    }
                }
                return resolvedNamesInner;
            });


        return (Class<? extends U>) resolvedNames.get(jsonTypeName);
    }
}