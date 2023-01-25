package cambio.simulator.export;

import java.io.File;
import java.nio.file.Path;
import java.util.Random;

import cambio.simulator.test.TestBase;
import org.junit.jupiter.api.BeforeEach;

/**
 * @author Lion Wagner
 */
public class MiSimReporterTest<T extends MiSimReporter<?>> extends TestBase {
    protected final static Random RNG = new Random(157);
    protected T reporter;
    protected File outputFile;
    private Path outputDir;

    @BeforeEach
    void setUp() {
        outputDir = reporter.model.getExperimentMetaData().getReportLocation();
        outputFile = outputDir.resolve("raw").resolve("test.csv").toFile();
    }

}
