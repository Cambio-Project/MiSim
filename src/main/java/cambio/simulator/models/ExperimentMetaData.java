package cambio.simulator.models;

import static cambio.simulator.parsing.adapter.experiement.ExperimentMetaDataAdapter.SIMULATION_METADATA_KEY;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import cambio.simulator.parsing.GsonHelper;
import cambio.simulator.parsing.ParsingException;
import cambio.simulator.parsing.ScenarioDescription;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;

/**
 * Class that contains the architectural information provided by the architecture file.
 *
 * @author Lion Wagner
 */
public class ExperimentMetaData {
    private static ExperimentMetaData instance = null;
    @SuppressWarnings("FieldMayBeFinal")
    private int seed = new Random().nextInt();

    @SuppressWarnings("FieldMayBeFinal")
    private String reportType = "default";
    private double duration = -1;
    @SerializedName(value = "experimentName", alternate = {"experiment_name", "name"})
    private String experimentName;
    @SerializedName(value = "modelName", alternate = {"model_name"})
    private String modelName;
    @SuppressWarnings("FieldMayBeFinal")
    private TimeUnit timeUnit = TimeUnit.SECONDS;
    /*
     * These are of type File, since java.nio.Path is not directly parsable by gson
     */
    private File expFileLocation;
    private File archFileLocation;
    private File reportLocation;
    private String description;

    private LocalDateTime startTimestamp;

    private transient long startOfSetup;
    private long durationOfSetupMS = -1;

    /**
     * Gets the experiment meta data singleton.
     *
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
     *
     * @return the experiment meta data object.
     */
    public static ExperimentMetaData initialize(File archFileLocation, File expFileLocation, File scenarioPath) {
        if (instance != null) {
            throw new IllegalStateException("Architecture Model was already initialized.");
        }
        Gson gson = new GsonHelper().getGson();

        try {
            if (expFileLocation != null) {
                JsonObject root = gson.fromJson(new JsonReader(new FileReader(expFileLocation)), JsonObject.class);
                instance = gson.fromJson(root.get(SIMULATION_METADATA_KEY), ExperimentMetaData.class);
                instance.setExpFileLocation(expFileLocation);
            } else if (scenarioPath != null) {
                ScenarioDescription description =
                    gson.fromJson(new JsonReader(new FileReader(scenarioPath)), ScenarioDescription.class);
                instance = new ExperimentMetaData();
                instance.experimentName = description.name;
                instance.duration = description.duration;
                instance.modelName = "UnnamedModel";
                instance.setExpFileLocation(scenarioPath);
            }
            instance.setArchFileLocation(archFileLocation);

            if (instance.duration <= 0) {
                throw new ParsingException("Experiment 'duration' should be greater than 0.");
            }
            if (instance.experimentName == null) {
                throw new ParsingException("'experiment_name' has to be set.");
            }
            if (instance.modelName == null) {
                throw new ParsingException("'model_name' has to be set.");
            }


        } catch (FileNotFoundException e) {
            throw new ParsingException(
                String.format("Could not find experiment file '%s'",
                    expFileLocation != null ? expFileLocation.getAbsolutePath() : null), e);
        }
        return get();
    }


    public String getReportType() {
        return reportType;
    }

    public String getExperimentName() {
        return experimentName;
    }

    public String getModelName() {
        return modelName;
    }

    public double getDuration() {
        return duration;
    }

    public int getSeed() {
        return seed;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
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


    public void markStartOfSetup(long startTime) {
        this.startOfSetup = startTime;
    }

    public void markEndOfSetup(long endTime) {
        this.durationOfSetupMS = endTime - startOfSetup;
    }

    public void setStartDate(LocalDateTime startTime) {
        this.startTimestamp = startTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getDurationOfSetupMS() {
        return durationOfSetupMS;
    }

    public LocalDateTime getStartTimestamp() {
        return startTimestamp;
    }

    public File getReportLocation() {
        return reportLocation;
    }
}
