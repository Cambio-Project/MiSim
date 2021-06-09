package de.rss.fachstudie.MiSim.models;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import de.rss.fachstudie.MiSim.parsing.GsonParser;
import de.rss.fachstudie.MiSim.parsing.ParsingException;
import de.rss.fachstudie.MiSim.parsing.ScenarioDescription;

/**
 * Class that contains the architectural information provided by the architecture file.
 *
 * @author Lion Wagner
 */
public class ExperimentMetaData {
    private static ExperimentMetaData instance = null;

    /**
     * Gets the experiment meta data singleton.
     * @return the experiment meta data.
     */
    public static ExperimentMetaData get() {
        if (instance == null) {
            throw new IllegalStateException("Experiment Model was not initialized yet.");
        }
        return instance;
    }

    /**
     * Initializes the experiment meta data object based on a experiment or scenario file.
     * @return the experiment meta data object.
     */
    public static ExperimentMetaData initialize(File archFileLocation, File expFileLocation, File scenarioPath) {
        if (instance != null) {
            throw new IllegalStateException("Architecture Model was already initialized.");
        }
        Gson gson = new GsonParser().getGson();

        try {
            if (expFileLocation != null) {
                JsonObject root = gson.fromJson(new JsonReader(new FileReader(expFileLocation)), JsonObject.class);
                instance = gson.fromJson(root.get("simulation_meta_data"), ExperimentMetaData.class);
                instance.setExpFileLocation(expFileLocation);
            } else if (scenarioPath != null) {
                ScenarioDescription description =
                    gson.fromJson(new JsonReader(new FileReader(scenarioPath)), ScenarioDescription.class);
                instance = new ExperimentMetaData();
                instance.experiment_name = description.name;
                instance.duration = description.duration;
                instance.model_name = "UnnamedModel";
                instance.setExpFileLocation(scenarioPath);
            }
            instance.setArchFileLocation(archFileLocation);

            if (instance.duration <= 0) {
                throw new ParsingException("Experiment 'duration' should be greater than 0.");
            }
            if (instance.experiment_name == null) {
                throw new ParsingException("'experiment_name' has to be set.");
            }
            if (instance.model_name == null) {
                throw new ParsingException("'model_name' has to be set.");
            }


        } catch (FileNotFoundException e) {
            throw new ParsingException(
                String.format("Could not find experiment file '%s'",
                    expFileLocation != null ? expFileLocation.getAbsolutePath() : null), e);
        }
        return get();
    }


    @SuppressWarnings("FieldMayBeFinal")
    private Integer seed = null;
    @SuppressWarnings("FieldMayBeFinal")
    private String report = "default";
    @SuppressWarnings("FieldMayBeFinal")
    private double duration = -1;
    @SuppressWarnings("FieldMayBeFinal")
    private String experiment_name;
    @SuppressWarnings("FieldMayBeFinal")
    private String model_name;
    @SuppressWarnings("FieldMayBeFinal")
    private TimeUnit time_unit = TimeUnit.SECONDS;

    /*
     * These are of type File, since java.nio.Path is not directly parsable by gson
     */
    private File expFileLocation;
    private File archFileLocation;

    public String getReportType() {
        return report;
    }

    public String getExperimentName() {
        return experiment_name;
    }

    public String getModelName() {
        return model_name;
    }

    public double getDuration() {
        return duration;
    }

    public int getSeed() {
        return seed;
    }

    public TimeUnit getTimeUnit() {
        return time_unit;
    }

    public File getExpFileLocation() {
        return expFileLocation;
    }

    private void setExpFileLocation(File expFileLocation) {
        this.expFileLocation = expFileLocation;
    }

    public File getArchFileLocation() {
        return archFileLocation;
    }

    private void setArchFileLocation(File archFileLocation) {
        this.archFileLocation = archFileLocation;
    }
}
