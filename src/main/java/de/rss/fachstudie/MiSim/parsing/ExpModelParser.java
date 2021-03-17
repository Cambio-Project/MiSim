package de.rss.fachstudie.MiSim.parsing;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import de.rss.fachstudie.MiSim.entities.generator.Generator;
import de.rss.fachstudie.MiSim.entities.microservice.Microservice;
import de.rss.fachstudie.MiSim.events.ChaosMonkeyEvent;
import de.rss.fachstudie.MiSim.events.LatencyMonkeyEvent;
import de.rss.fachstudie.MiSim.models.MainModel;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

/**
 * The ExpModelParser class reads a json file that contains the experiment model.
 */
public class ExpModelParser {
    public static HashMap<String, String> simulation_meta_data = new HashMap<>();
    public static Generator[] generators = new Generator[0];
    public static ChaosMonkeyEvent[] chaosmonkeys = new ChaosMonkeyEvent[0];
    public static LatencyMonkeyEvent[] latencymonkeys = new LatencyMonkeyEvent[0];


    public static void loadMetadata(Path path) {
        try {
            Gson gson = new Gson();
            JsonObject root = gson.fromJson(new JsonReader(new FileReader(path.toFile())), JsonObject.class);
            simulation_meta_data = gson.fromJson(root.get("simulation_meta_data"), new TypeToken<HashMap<String, String>>() {
            }.getType());

        } catch (FileNotFoundException e) {
            throw new ParsingException(String.format("Could not find architecture file '%s'", path.toAbsolutePath()), e);
        }
    }

    public static void parseExperimentData(Path path, MainModel model, Set<Microservice> microservices) {
        try {
            Gson gson = new Gson();
            JsonObject root = gson.fromJson(new JsonReader(new FileReader(path.toFile())), JsonObject.class);
            simulation_meta_data = gson.fromJson(root.get("simulation_meta_data"), new TypeToken<HashMap<String, String>>() {
            }.getType());
            GeneratorParser[] generatorData = gson.fromJson(root.get("request_generators"), GeneratorParser[].class);
            ChaosMonkeyParser[] chaosmonkeyData = gson.fromJson(root.get("chaosmonkeys"), ChaosMonkeyParser[].class);
            LatencyMonkeyParser[] latencymonkeyData = gson.fromJson(root.get("latencymonkeys"), LatencyMonkeyParser[].class);

            if (generatorData != null && generatorData.length > 0)
                generators = Arrays.stream(generatorData)
                        .map(generatorParser -> generatorParser.convertToObject(model, microservices))
                        .toArray(value -> new Generator[generatorData.length]);
            if (chaosmonkeyData != null && chaosmonkeyData.length > 0)
                chaosmonkeys = Arrays.stream(chaosmonkeyData)
                        .map(chaosMonkeyParser -> chaosMonkeyParser.convertToObject(model, microservices))
                        .toArray(value -> new ChaosMonkeyEvent[chaosmonkeyData.length]);
            if (latencymonkeyData != null && latencymonkeyData.length > 0)
                latencymonkeys = Arrays.stream(latencymonkeyData)
                        .map(latencyMonkeyParser -> latencyMonkeyParser.convertToObject(model, microservices))
                        .toArray(value -> new LatencyMonkeyEvent[latencymonkeyData.length]);
        } catch (FileNotFoundException ex) {
            System.out.println("File " + " not found");
        }
    }
}
