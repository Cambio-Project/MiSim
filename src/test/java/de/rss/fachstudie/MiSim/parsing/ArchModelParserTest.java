package de.rss.fachstudie.MiSim.parsing;

import org.junit.jupiter.api.BeforeAll;

import java.io.File;
import java.nio.file.Paths;

class ArchModelParserTest {

    @BeforeAll
    static void beforeAll() {
        File f = new File("./Examples/example_architecture_model.json");
        ArchModelParser.parseMicroservicesArchModelFile(Paths.get(f.getAbsolutePath()));
    }



}