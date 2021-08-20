package cambio.simulator.nparsing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import cambio.simulator.models.ArchitectureModel;
import cambio.simulator.models.ExperimentMetaData;
import cambio.simulator.models.ExperimentModel;
import cambio.simulator.models.MiSimModel;
import cambio.simulator.nparsing.adapter.architecture.ArchitectureModelAdapter;
import cambio.simulator.nparsing.adapter.experiement.ExperimentMetaDataAdapter;
import cambio.simulator.nparsing.adapter.experiement.ExperimentModelAdapter;
import cambio.simulator.parsing.GsonHelper;
import cambio.simulator.parsing.ParsingException;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Contract;

/**
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

    public static ExperimentMetaData loadExperimentMetaData(File experimentOrScenarioFileLocation,
                                                            File architectureModelLocation) {
        return loadModel(experimentOrScenarioFileLocation,
            ExperimentMetaData.class,
            new ExperimentMetaDataAdapter(experimentOrScenarioFileLocation, architectureModelLocation));

    }


    public static ArchitectureModel loadArchitectureModel(MiSimModel baseModel) {
        return loadModel(
            baseModel.getExperimentMetaData().getArchFileLocation(),
            ArchitectureModel.class,
            new ArchitectureModelAdapter(baseModel)
        );
    }

    public static ExperimentModel loadExperimentModel(MiSimModel baseModel) {
        if (true) {
            throw new NotImplementedException();
        }
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
                String
                    .format("[Error]  Cannot start the simulation. Model file %s was not found!",
                        targetFile.getAbsolutePath()),e);
        }
    }
}
