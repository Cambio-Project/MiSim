package de.rss.fachstudie.MiSim.parsing;

import java.io.File;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeAll;

class ArchModelParserTest {

    @BeforeAll
    static void beforeAll() {
        File f = new File("./Examples/example_architecture_model.json");
        ArchModelParser.parseMicroservicesArchModelFile(Paths.get(f.getAbsolutePath()));
    }


}