package de.rss.fachstudie.MiSim.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import de.rss.fachstudie.MiSim.entities.Microservice;

import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * The ArchModelParser reads a valid json file and converts the contents into microservices and operations.
 */
public class ArchModelParser {
    public static Microservice[] microservices;

    public ArchModelParser(String filename) {
        try {
            Gson gson = new Gson();
            JsonObject root = gson.fromJson(new JsonReader(new FileReader(filename)), JsonObject.class);
            microservices = gson.fromJson(root.get("microservices"), Microservice[].class);
        } catch(FileNotFoundException ex) {
            System.out.println("File " + filename + " not found");
        }
    }
}