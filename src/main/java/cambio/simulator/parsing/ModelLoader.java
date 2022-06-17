package cambio.simulator.parsing;

import java.io.*;
import java.util.function.Function;

import cambio.simulator.models.*;
import cambio.simulator.parsing.adapter.architecture.ArchitectureModelAdapter;
import cambio.simulator.parsing.adapter.experiment.ExperimentMetaDataAdapter;
import cambio.simulator.parsing.adapter.experiment.ExperimentModelAdapter;
import cambio.simulator.parsing.adapter.scenario.ScenarioDescriptionAdapter;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import org.jetbrains.annotations.Contract;

/**
 * Utility class for loading the MiSim architecture and experiment descriptions from JSON-files.
 *
 * @author Lion Wagner
 */
public final class ModelLoader {


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
        ExperimentMetaData experimentMetaData = loadModel(experimentOrScenarioFileLocation,
            ExperimentMetaData.class,
            new ExperimentMetaDataAdapter(experimentOrScenarioFileLocation, architectureModelLocation));

        if (experimentMetaData.getDuration() < 0 || Double.isInfinite(experimentMetaData.getDuration())) {
            System.out.println(
                "[Warning] Simulation duration is not set or infinite. The simulation may runs infinitely.");
        }

        return experimentMetaData;

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
            baseModel.getExperimentMetaData().getArchitectureDescriptionLocation(),
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
        File modelLocation = baseModel.getExperimentMetaData().getExperimentDescriptionLocation();
        Function<TypeAdapter<ExperimentModel>, ExperimentModel> loadFunction = adapter ->
            loadModel(modelLocation, ExperimentModel.class, adapter);

        try {
            //try parsing from scenario description
            return loadFunction.apply(new ScenarioDescriptionAdapter(baseModel));
        } catch (ParsingException e) {
            System.out.printf("[Info] Could not detect %s as scenario. Trying to detect an experiment.%n",
                modelLocation);

            //try to parse into experiment
            return loadFunction.apply(new ExperimentModelAdapter(baseModel));
        }
    }

    public static <T> T loadModel(File targetFile, Class<T> targetType, TypeAdapter<T> adapter) {
        checkFileExistence(targetFile);
        try {
            Gson gson = GsonHelper
                .getGsonBuilder()
                .registerTypeAdapter(targetType, adapter)
                .create();
            JsonReader reader = new JsonReader(new FileReader(targetFile));
            return gson.fromJson(reader, targetType);

        } catch (FileNotFoundException e) {
            throw new ParsingException(
                String.format("Cannot start the simulation. Model file %s was not found!",
                    targetFile.getAbsolutePath()), e);
        } catch (JsonSyntaxException e) {
            throw new ParsingException(
                String.format("Cannot start the simulation. Model file %s contains Json Syntax errors!",
                    targetFile.getAbsolutePath()), e);
        } catch (ParsingException e) {
            throw new ParsingException(String.format("Error parsing model file %s\n%s", targetFile.getAbsolutePath(),
                e.getMessage()), e);
        }
    }

    @Contract("null->fail")
    private static void checkFileExistence(File file) {
        if (file == null) {
            throw new ParsingException(
                "[Error] Cannot start the simulation. Model file was not found. Check your Paths and parameters.");
        } else if (!file.exists()) {
            throw new ParsingException(
                String
                    .format("[Error]  Cannot start the simulation. Model file %s was not found!",
                        file.getAbsolutePath()));
        }
    }
}
