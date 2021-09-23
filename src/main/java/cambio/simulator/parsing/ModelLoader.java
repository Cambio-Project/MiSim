package cambio.simulator.parsing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import cambio.simulator.models.ArchitectureModel;
import cambio.simulator.models.ExperimentMetaData;
import cambio.simulator.models.ExperimentModel;
import cambio.simulator.models.MiSimModel;
import cambio.simulator.parsing.adapter.architecture.ArchitectureModelAdapter;
import cambio.simulator.parsing.adapter.experiement.ExperimentMetaDataAdapter;
import cambio.simulator.parsing.adapter.experiement.ExperimentModelAdapter;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import org.jetbrains.annotations.Contract;

/**
 * Utility class for loading the MiSim architecture and experiment descriptions from JSON-files.
 *
 * @author Lion Wagner
 */
public final class ModelLoader {

    @Contract("null->fail")
    private static void checkFileExistence(File file) {
        if (file == null) {
            throw new ParsingException(
                "[Error]  Cannot start the simulation. Model file was not found. Check your Paths and parameters.");
        } else if (!file.exists()) {
            throw new ParsingException(
                String
                    .format("[Error]  Cannot start the simulation. Model file %s was not found!",
                        file.getAbsolutePath()));
        }
    }

    /**
     * Parses the given information into a new {@link ExperimentMetaData} object.
     *
     * @param experimentOrScenarioFileLocation location of the experiment file, that should be used
     * @param architectureModelLocation        location of the architecture file, that should be used for the
     *                                         experiment
     * @return a new instance of an {@link ExperimentMetaData} object that has been parsed using an {@link
     *     ExperimentMetaDataAdapter}.
     */
    public static ExperimentMetaData loadExperimentMetaData(File experimentOrScenarioFileLocation,
                                                            File architectureModelLocation) {
        return loadModel(experimentOrScenarioFileLocation,
            ExperimentMetaData.class,
            new ExperimentMetaDataAdapter(experimentOrScenarioFileLocation, architectureModelLocation));

    }

    /**
     * Parses the given information into a new {@link ArchitectureModel} object.
     *
     * @param baseModel parent {@link MiSimModel}, architecture description file location will be extracted from the
     *                  metadata of this object.
     * @return a new instance of an {@link ArchitectureModel} object that has been parsed using an {@link
     *     ArchitectureModelAdapter}.
     */
    public static ArchitectureModel loadArchitectureModel(MiSimModel baseModel) {
        return loadModel(
            baseModel.getExperimentMetaData().getArchFileLocation(),
            ArchitectureModel.class,
            new ArchitectureModelAdapter(baseModel)
        );
    }

    /**
     * Parses the given information into a new {@link ExperimentModel} object.
     *
     * @param baseModel parent {@link MiSimModel}, experiment description file location will be extracted from the
     *                  metadata of this object.
     * @return a new instance of an {@link ExperimentModel} object that has been parsed using an {@link
     *     ExperimentModelAdapter}.
     */
    public static ExperimentModel loadExperimentModel(MiSimModel baseModel) {
        return loadModel(
            baseModel.getExperimentMetaData().getExpFileLocation(),
            ExperimentModel.class,
            new ExperimentModelAdapter(baseModel)
        );
    }

    private static <T> T loadModel(File targetFile, Class<T> targetType, TypeAdapter<T> adapter) {
        checkFileExistence(targetFile);
        try {
            Gson gson = new GsonHelper()
                .getGsonBuilder()
                .registerTypeAdapter(targetType, adapter)
                .create();
            JsonReader reader = new JsonReader(new FileReader(targetFile));
            return gson.fromJson(reader, targetType);

        } catch (FileNotFoundException e) {
            throw new ParsingException(
                String.format("[Error]  Cannot start the simulation. Model file %s was not found!",
                    targetFile.getAbsolutePath()), e);
        }
    }
}
