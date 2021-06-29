package cambio.simulator.nparsing.adapter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

/**
 * @author Lion Wagner
 */
public final class GeneratorDescriptionResolver {

    private static final Map<String, Class<? extends LoadGeneratorDescription>> descriptorMap;

    static {
        Reflections reflections = new Reflections(
            new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(
                    LoadGeneratorDescription.class.getPackage().getName()))
                .setScanners(new SubTypesScanner())
        );

        Set<Class<? extends LoadGeneratorDescription>> set = reflections.getSubTypesOf(LoadGeneratorDescription.class);

        Map<String, Class<? extends LoadGeneratorDescription>> tempDescriptorMap = new HashMap<>();
        for (Class<? extends LoadGeneratorDescription> clazz : set) {
            LoadGeneratorDescriptorTypeName name = clazz.getAnnotation(LoadGeneratorDescriptorTypeName.class);
            if (name == null) {
                System.out.println(String
                    .format("%s %s is missing its @%s annotation and can therefore not be parsed.",
                        LoadGeneratorDescription.class.getSimpleName(), clazz.getName(),
                        LoadGeneratorDescriptorTypeName.class.getName()));
                continue;
            }
            tempDescriptorMap.put(name.name(), clazz);
        }
        descriptorMap = Collections.unmodifiableMap(tempDescriptorMap);
    }

    public static Map<String, Class<? extends LoadGeneratorDescription>> getDescriptors() {
        return descriptorMap;
    }
}
