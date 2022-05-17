package cambio.simulator.models;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private int seed = new Random().nextInt();

    private String reportType = "default";
    //TODO: report overwrite

    @SerializedName(value = "duration", alternate = {"experiment_duration"})
    private double duration = Double.POSITIVE_INFINITY;

    @SerializedName(value = "experimentName", alternate = {"experiment_name", "name"})
    private String experimentName;

    @SerializedName(value = "modelName", alternate = {"model_name"})
    private String modelName;

    private TimeUnit timeUnit = TimeUnit.SECONDS;

    @SerializedName(value = "report_directory", alternate = {"report_dir", "report_location", "report_folder"})
    private Path reportBaseFolder = Paths.get(".", "Report");

    private File expFileLocation;
    private File archFileLocation;

    private String description;

    private LocalDateTime startTimestamp;

    private long setupExecutionDuration = -1;
    private long experimentExecutionDuration = -1;

    private transient long startOfSetup;
    private transient long startOfExecution;
    private transient long endOfExecution;

    private transient Path reportLocation;

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

    public File getExperimentDescriptionLocation() {
        return expFileLocation;
    }

    public File getArchitectureDescriptionLocation() {
        return archFileLocation;
    }

    public Path getReportBaseDirectory() {
        return reportBaseFolder;
    }

    public Path getReportLocation() {
        return reportLocation;
    }

    public void setReportLocation(Path reportLocation) {
        this.reportLocation = reportLocation;
    }

    public void setStartDate(LocalDateTime startTime) {
        this.startTimestamp = startTime;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getStartTimestamp() {
        return startTimestamp;
    }

    public void markStartOfSetup(long setupStartTime) {
        this.startOfSetup = setupStartTime;
    }

    public void markStartOfExperiment(long experimentStartTime) {
        this.startOfExecution = experimentStartTime;
        this.setupExecutionDuration = startOfExecution - startOfSetup;
    }

    public void markEndOfExecution(long endOfExecution) {
        this.endOfExecution = endOfExecution;
        this.experimentExecutionDuration = endOfExecution - startOfExecution;
    }

    public long getSetupExecutionDuration() {
        return setupExecutionDuration;
    }

    public long getExperimentExecutionDuration() {
        return experimentExecutionDuration;
    }

    public long getExecutionDuration() {
        return endOfExecution - startOfSetup;
    }
}
