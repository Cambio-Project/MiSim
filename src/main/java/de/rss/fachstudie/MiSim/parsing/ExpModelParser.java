package de.rss.fachstudie.MiSim.parsing;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import de.rss.fachstudie.MiSim.events.InitialChaosMonkeyEvent;
import de.rss.fachstudie.MiSim.events.InitialEvent;
import de.rss.fachstudie.MiSim.events.InitialLatencyMonkeyEvent;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;

/**
 * The ExpModelParser class reads a json file that contains the experiment model.
 */
public class ExpModelParser {
    public static HashMap<String, String> simulation_meta_data;
    public static InitialEvent[] request_generators;
    public static GeneratorPOJO[] generators;
    public static InitialChaosMonkeyEvent[] chaosmonkeys;
    public static InitialLatencyMonkeyEvent[] latencymonkeys;

    public ExpModelParser(String filename) {
        try {
            Gson gson = new Gson();
            JsonObject root = gson.fromJson(new JsonReader(new FileReader(filename)), JsonObject.class);
            simulation_meta_data = gson.fromJson(root.get("simulation_meta_data"), new TypeToken<HashMap<String, String>>() {
            }.getType());
            generators = gson.fromJson(root.get("request_generators"), GeneratorPOJO[].class);
            //request_generators = gson.fromJson(root.get("request_generators"), InitialEvent[].class);
            chaosmonkeys = gson.fromJson(root.get("chaosmonkeys"), InitialChaosMonkeyEvent[].class);
            latencymonkeys = gson.fromJson(root.get("latencymonkeys"), InitialLatencyMonkeyEvent[].class);
//            System.out.println(request_generators[0].getMicroservice());
//            System.out.println(request_generators[0].getOperation());
//            System.out.println(request_generators[0].getInterval());
            if (chaosmonkeys == null) {
                chaosmonkeys = new InitialChaosMonkeyEvent[0];
            }
            if (latencymonkeys == null) {
                latencymonkeys = new InitialLatencyMonkeyEvent[0];
            }

        } catch (FileNotFoundException ex) {
            System.out.println("File " + " not found");
        }
    }

}
