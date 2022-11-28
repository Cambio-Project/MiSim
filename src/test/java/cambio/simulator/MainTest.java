package cambio.simulator;

import static cambio.simulator.test.FileLoaderUtil.loadFromTestResources;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Arrays;

import cambio.simulator.test.TestBase;
import org.junit.jupiter.api.Test;

class MainTest extends TestBase {

    @Test
    void producesCorrectOutputIncludingTraces() {

        File test_architecture = loadFromTestResources("SSPExample","ssp_architecture.json");
        File test_experiment = loadFromTestResources("SSPExample", "ssp_experiment.json");

        File output = this.runSimulationCheckExitTempOutput(0, test_architecture, test_experiment);

        assertTrue(output.exists());
        assertTrue(output.isDirectory());

        //output should contain exactly:
        // - a "graph" folder
        // - a "raw" folder
        // - a requirements.txt file
        // - 4 html files
        // - 3 json file
        // - 7 python files
        File[] resultFiles = output.listFiles()[0].listFiles();
        assertNotNull(resultFiles);
        //print result files
        System.out.println("Files in output folder:");
        Arrays.stream(resultFiles).forEach(System.out::println);
        
        checkFileWithNameExists(resultFiles, "graph");
        checkFileWithNameExists(resultFiles, "raw");
        checkFileWithNameExists(resultFiles, "requirements.txt");
        checkFileTypeCount(4, resultFiles, ".html");
        checkFileTypeCount(3, resultFiles, ".json");
        checkFileTypeCount(7, resultFiles, ".py");
        assertEquals(17, resultFiles.length);
    }

    private static void checkFileWithNameExists(File[] resultFiles, String raw) {
        assertEquals(1, Arrays.stream(resultFiles).filter(f -> f.getName().equals(raw)).count());
    }

    private static void checkFileTypeCount(int expected, File[] resultFiles, String suffix) {
        assertEquals(expected, Arrays.stream(resultFiles).filter(f -> f.getName().endsWith(suffix)).count());
    }

}