package cambio.simulator.export;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import cambio.simulator.models.ExperimentMetaData;
import cambio.simulator.models.MiSimModel;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import org.jetbrains.annotations.NotNull;

/**
 * @author Lion Wagner
 */
public abstract class MiSimReporter<R extends AsyncReportWriter<?>> {

    protected final MiSimModel model;

    protected final String datasetsPrefix;
    protected final Path reportBasePath;
    private final HashMap<String, R> writers = new HashMap<>();
    private final HashMap<String, String[]> customHeaders = new HashMap<>();

    public MiSimReporter(Model model, @NotNull String datasetsPrefix) {
        Objects.requireNonNull(model);
        this.model = (MiSimModel) model;
        MiSimReporters.registerReporter(this);

        this.datasetsPrefix = datasetsPrefix;
        ExperimentMetaData experimentMetaData = this.model.getExperimentMetaData();
        this.reportBasePath = experimentMetaData.getReportLocation().resolve("raw");
    }


    protected final void checkArgumentsAreNotNull(String dataSetName, TimeInstant when, Object data) {
        Objects.requireNonNull(dataSetName);
        Objects.requireNonNull(when);
        Objects.requireNonNull(data);
    }

    protected final boolean deregister() {
        return MiSimReporters.deregister(this);
    }

    /**
     * Adds a new datapoint to the given dataset.
     *
     * @param dataSetName name of the dataset to which the datapoint should be added
     * @param when        point in simulation time to which the datapoint is associated to
     * @param data        data that should be logged
     * @param <T>         type of the data that should be logged.
     */
    public abstract <T> void addDatapoint(String dataSetName, TimeInstant when, T... data);


    public void finalizeReport() {
        writers.values().forEach(AsyncReportWriter::finalizeWriteout);
        deregister();
        customHeaders.clear();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()
            + (datasetsPrefix.equals("") ? "" : "{"
            + "datasetsPrefix='" + datasetsPrefix + '\''
            + '}');
    }

    public void registerDefaultHeader(String dataSetName, String... headers) {
        Objects.requireNonNull(dataSetName);
        Objects.requireNonNull(headers);

        if (customHeaders.putIfAbsent(dataSetName, headers) != null) {
            throw new IllegalArgumentException(
                "Header for dataset " + dataSetName + " already registered as "
                    + Arrays.toString(customHeaders.get(dataSetName)));
        }
    }

    protected abstract R createWriter(Path datasetPath, String[] headers) throws IOException;

    protected final R getWriter(final String datasetID) {
        return writers.computeIfAbsent(datasetID, (s) -> {
            Path outputFilePath = null;
            try {
                Files.createDirectories(reportBasePath);
                outputFilePath = reportBasePath.resolve(datasetsPrefix + datasetID + ".csv");
                return createWriter(outputFilePath,
                    customHeaders.getOrDefault(datasetID, new String[] {MiSimReporters.DEFAULT_VALUE_COLUMN_NAME}));
            } catch (IOException e) {
                throw new RuntimeException(String.format("Could not open file '%s'.", outputFilePath), e);
            }
        });
    }
}
