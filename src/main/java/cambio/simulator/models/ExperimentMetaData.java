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

    private double duration = -1;

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

    private long setupDuration = -1;
    private long experimentDuration = -1;
    private long reportDuration = -1;

    private transient long startOfSetup;
    private transient long startOfExperiment;
    private transient long startOfReport;
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

    public Path getReportBaseFolder() {
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
        this.startOfExperiment = experimentStartTime;
        this.setupDuration = startOfExperiment - startOfSetup;
    }

    public void markStartOfReport(long startOfReport) {
        this.startOfReport = startOfReport;
        this.experimentDuration = startOfReport - startOfExperiment;
    }

    public void markEndOfExecution(long endOfExecution) {
        this.endOfExecution = endOfExecution;
        this.reportDuration = endOfExecution - startOfReport;
    }

    public long getSetupDuration() {
        return setupDuration;
    }

    public long getExperimentDuration() {
        return experimentDuration;
    }

    public long getReportDuration() {
        return reportDuration;
    }

    public long getExecutionDuration() {
        return endOfExecution - startOfSetup;
    }
}
