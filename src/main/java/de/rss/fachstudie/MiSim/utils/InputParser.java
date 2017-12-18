package de.rss.fachstudie.MiSim.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import de.rss.fachstudie.MiSim.entities.Microservice;
import de.rss.fachstudie.MiSim.events.InitialChaosMonkeyEvent;
import de.rss.fachstudie.MiSim.events.InitialEvent;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;

/**
 * The InputParser reads a valid json file and converts the contents into microservices and operations.
 */
public class InputParser {
    public static Microservice[] microservices;
    public static InitialEvent[] generators;
    public static InitialChaosMonkeyEvent[] monkeys;
    public static HashMap<String, String> simulation;

    public InputParser(String filename) {
        try {
            Gson gson = new Gson();
            JsonObject root = gson.fromJson(new JsonReader(new FileReader(filename)), JsonObject.class);
            simulation = gson.fromJson(root.get("simulation"), new TypeToken<HashMap<String, String>>(){}.getType());
            microservices = gson.fromJson(root.get("microservices"), Microservice[].class);
            generators = gson.fromJson(root.get("generators"), InitialEvent[].class);
            monkeys = gson.fromJson(root.get("chaosmonkeys"), InitialChaosMonkeyEvent[].class);
        } catch(FileNotFoundException ex) {
            System.out.println("File " + filename + " not found");
        }
    }
}