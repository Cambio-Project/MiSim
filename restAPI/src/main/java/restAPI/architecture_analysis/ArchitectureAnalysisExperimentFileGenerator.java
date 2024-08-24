package restAPI.architecture_analysis;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ArchitectureAnalysisExperimentFileGenerator {
    private static final String experimentJSON = """
            {
              "simulation_metadata": {
                "experiment_name": "Architecture-Only-Experiment",
                "model_name": "Architecture-Only-Model",
                "duration": 0
              }
            }""";

    public File generateExperimentFile() throws IOException {
        File file = File.createTempFile("architecture-only-experiment", ".json");
        FileWriter writer = new FileWriter(file);
        writer.write(experimentJSON);
        writer.close();
        return file;
    }

}
