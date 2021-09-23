package cambio.simulator.models;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.google.gson.annotations.SerializedName;

/**
 * Class that contains the architectural information provided by the architecture file.
 *
 * @author Lion Wagner
 */
@SuppressWarnings("FieldMayBeFinal")
public class ExperimentMetaData {
    private static ExperimentMetaData instance = null;


    private int seed = new Random().nextInt();

    private String reportType = "default";

    private double duration = -1;

    @SerializedName(value = "experimentName", alternate = {"experiment_name", "name"})
    private String experimentName;

    @SerializedName(value = "modelName", alternate = {"model_name"})
    private String modelName;

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
    private transient long durationOfSetupMS = -1;
    private transient long durationOfMetaDataLoading;


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

    public void setDurationOfMetaDataLoading(long durationOfMetaDataLoading) {
        this.durationOfMetaDataLoading = durationOfMetaDataLoading;
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
        return durationOfMetaDataLoading + durationOfSetupMS;
    }

    public LocalDateTime getStartTimestamp() {
        return startTimestamp;
    }

    public File getReportLocation() {
        return reportLocation;
    }
}
