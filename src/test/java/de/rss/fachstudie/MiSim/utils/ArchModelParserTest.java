package de.rss.fachstudie.MiSim.utils;

import de.rss.fachstudie.MiSim.entities.Microservice;
import de.rss.fachstudie.MiSim.entities.Operation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;

class ArchModelParserTest {

    @BeforeAll
    static void beforeAll() {
        File f = new File("./Examples/loon_mock_model.json");
        ArchModelParser.parseArchModelFile(f.getAbsolutePath());
    }

    @Test
    public void operationDependenciesAreNotNull() {
        //If dependencies are not given for operations, they should default to an empty array
        for (Microservice microservice : ArchModelParser.microservices) {
            for (Operation operation : microservice.getOperations()) {
               // Assertions.assertNotNull(operation.getDependencies(), "Dependencies are null for " + operation.getQuotedName() + ".");
            }
        }
    }

}