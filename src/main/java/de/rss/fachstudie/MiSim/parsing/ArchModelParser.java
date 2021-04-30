package de.rss.fachstudie.MiSim.parsing;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import de.rss.fachstudie.MiSim.entities.microservice.Microservice;
import de.rss.fachstudie.MiSim.entities.microservice.Operation;
import de.rss.fachstudie.MiSim.models.ArchitectureModel;
import de.rss.fachstudie.MiSim.models.MainModel;

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

    /**
     * Step one of parsing. Translate the textural information into Microservices. Does not initialize dependencies
     * yet.
     *
     * @param path File path to the architecture file
     * @return all parsed microservices, dependencies are not initialized yet.
     * @see ArchModelParser#initializeOperations()
     */
    public static Set<Microservice> parseMicroservicesArchModelFile(Path path) {
        try {
            Gson gson = new Gson();
            JsonObject root = gson.fromJson(new JsonReader(new FileReader(path.toFile())), JsonObject.class);
            MicroservicePOJO[] microservicePojos = gson.fromJson(root.get("microservices"), MicroservicePOJO[].class);

            List<Microservice> services = Arrays.stream(microservicePojos).map(microservicePOJO -> microservicePOJO.convertToMicroservice(MainModel.get(), MainModel.get().traceIsOn())).collect(Collectors.toList());

            return new HashSet<>(services);
        } catch (FileNotFoundException e) {
            throw new ParsingException(String.format("Could not find architecture file '%s'", path.toAbsolutePath()), e);
        }
    }

    public static void initializeOperations() {
        for (Microservice service : ArchitectureModel.get().getMicroservices()) {
            for (Operation operation : service.getOperations()) {
                operation.initializeDependencies();
            }
        }
    }

}