package de.rss.fachstudie.MiSim.parsing;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import de.rss.fachstudie.MiSim.models.MainModel;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Array;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The ExpModelParser class reads a json file that contains the experiment model.
 */
public class ExpModelParser {
    public static Set<Object> parsedExperimentObjects = Collections.unmodifiableSet(new HashSet<>());


    public static final List<Class<? extends Parser<?>>> parserClasses;

    /*
     * Classes of objects that should be loaded by the {@code ExpModelParser} can be registered here.
     * TODO: this may be automatable via a compile-level injection.
     * TODO: Maybe move to separate class for cleanup
     */
    static {
        List<Class<? extends Parser<?>>> temp = Arrays.asList(
                ChaosMonkeyParser.class,
                SummonerMonkeyParser.class,
                LatencyMonkeyParser.class,
                GeneratorParser.class);
        parserClasses = Collections.unmodifiableList(temp);
    }

    public static Set<Object> parseExperimentData(Path path) {
        try {
            Gson gson = new GsonParser().getGson();
            JsonObject root = gson.fromJson(new JsonReader(new FileReader(path.toFile())), JsonObject.class);

            Set<? extends Parser<?>> parserInstances = parserClasses.stream().map(aClass -> {
                try {
                    return aClass.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                    throw new ParsingException(String.format("Parser %s could not be instantiated.", aClass.getTypeName()));
                }
            }).collect(Collectors.toSet());

            Set<Parser<?>> parsedObjects = new HashSet<>();

            for (Parser<?> parserInstance : parserInstances) {
                Class<?> clazz = Array.newInstance(parserInstance.getClass(), 0).getClass();
                Object result = gson.fromJson(root.get(parserInstance.getDescriptionKey()), clazz);

                if (result == null) continue;

                int count = Array.getLength(result);
                for (int i = 0; i < count; i++) {
                    parsedObjects.add((Parser<?>) Array.get(result, i));
                }
            }

            parsedExperimentObjects = parsedObjects.stream().map(o -> o.convertToObject(MainModel.get())).collect(Collectors.toSet());

            return parsedExperimentObjects;


        } catch (FileNotFoundException ex) {
            throw new ParsingException(String.format("Experiment file '%s' not found", path.toUri()));
        }
    }
}

