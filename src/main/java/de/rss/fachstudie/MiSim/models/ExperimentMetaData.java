package de.rss.fachstudie.MiSim.models;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import de.rss.fachstudie.MiSim.parsing.ParsingException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * Class that contains the architectural information provided by the architecture file.
 *
 * @author Lion Wagner
 */
public class ExperimentMetaData {
    private static ExperimentMetaData instance = null;

    public static ExperimentMetaData get() {
        if (instance == null) {
            throw new IllegalStateException("Architecture Model was not initialized yet.");
        }
        return instance;
    }

    public static ExperimentMetaData initialize(Path expFileLocation, Path archFileLocation) {
        if (instance != null) {
            throw new IllegalStateException("Architecture Model was already initialized.");
        }
        try {
            Gson gson = new Gson();
            JsonObject root = gson.fromJson(new JsonReader(new FileReader(expFileLocation.toFile())), JsonObject.class);
            instance = gson.fromJson(root.get("simulation_meta_data"), ExperimentMetaData.class);
            instance.setExpFileLocation(expFileLocation.toFile());
            instance.setArchFileLocation(archFileLocation.toFile());

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
            throw new ParsingException(String.format("Could not find architecture file '%s'", expFileLocation.toAbsolutePath()), e);
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
