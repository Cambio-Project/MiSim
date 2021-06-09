package de.unistuttgart.sqa.orcas.misim.parsing;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import de.unistuttgart.sqa.orcas.misim.entities.microservice.Microservice;
import de.unistuttgart.sqa.orcas.misim.entities.microservice.Operation;
import de.unistuttgart.sqa.orcas.misim.models.ArchitectureModel;
import de.unistuttgart.sqa.orcas.misim.models.MainModel;

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
            MicroserviceParserData[]
                microservicesData = gson.fromJson(root.get("microservices"), MicroserviceParserData[].class);

            return Arrays.stream(microservicesData).map(microserviceParserData ->
                microserviceParserData.convertToMicroservice(MainModel.get(), MainModel.get().traceIsOn()))
                .collect(Collectors.toSet());
        } catch (FileNotFoundException e) {
            throw new ParsingException(String.format("Could not find architecture file '%s'", path.toAbsolutePath()),
                e);
        }
    }

    /**
     * Creates the actual Operation objects for each microservice. (Building concrete dependencies)
     */
    public static void initializeOperations() {
        for (Microservice service : ArchitectureModel.get().getMicroservices()) {
            for (Operation operation : service.getOperations()) {
                operation.initializeDependencies();
            }
        }
    }

}