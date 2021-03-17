package de.rss.fachstudie.MiSim.parsing;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import de.rss.fachstudie.MiSim.entities.Operation;
import de.rss.fachstudie.MiSim.entities.microservice.Microservice;
import desmoj.core.simulator.Model;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The ArchModelParser reads a valid json file and converts the contents into microservices and operations.
 */
public class ArchModelParser {
    public static Microservice[] microservices;
    public static MicroservicePOJO[] microservicePojos = new MicroservicePOJO[0];

    public static Set<Microservice> parseArchModelFile(Path path, Model model) {
        try {
            Gson gson = new Gson();
            JsonObject root = gson.fromJson(new JsonReader(new FileReader(path.toFile())), JsonObject.class);
            microservicePojos = gson.fromJson(root.get("microservices"), MicroservicePOJO[].class);

            List<Microservice> services = Arrays.stream(microservicePojos).map(microservicePOJO -> microservicePOJO.convertToMicroservice(model, model.traceIsOn())).collect(Collectors.toList());

            for (Microservice service : services) {
                for (Operation operation : service.getOperations()) {
                    operation.initializeDependencies(services);
                }
            }

            return new HashSet<>(services);
        } catch (FileNotFoundException e) {
            throw new ParsingException(String.format("Could not find architecture file '%s'", path.toAbsolutePath()), e);
        }

    }
}