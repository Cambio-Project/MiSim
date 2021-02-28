package de.rss.fachstudie.MiSim.parsing;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import de.rss.fachstudie.MiSim.entities.microservice.Microservice;

import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * The ArchModelParser reads a valid json file and converts the contents into microservices and operations.
 */
public class ArchModelParser {
    public static Microservice[] microservices;
    public static MicroservicePOJO[] microservicePojos = new MicroservicePOJO[0];

    public static MicroservicePOJO[] parseArchModelFile(String filename) {
        try {
            Gson gson = new Gson();
            JsonObject root = gson.fromJson(new JsonReader(new FileReader(filename)), JsonObject.class);
            microservicePojos = gson.fromJson(root.get("microservices"), MicroservicePOJO[].class);

            microservices = null;// gson.fromJson(root.get("microservices"), Microservice[].class);
            if (microservices == null) microservices = new Microservice[0];//
        } catch (FileNotFoundException ex) {
            System.out.println("File " + filename + " not found");
        }
        return microservicePojos;
    }
}