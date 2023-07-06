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
 * Base class for all MiSimReporters. Takes care of
 * <ul>
 * <li>registering and deregistering the reporter at the {@link MiSimReporters} class.</li>
 * <li>creation of output directories </li>
 * <li>creation of the actual output writers ({@link AsyncReportWriter}s)</li>
 * <li>setting default headers (and therefore column count) </li>
 * </ul>
 *
 * <p>
 *     A Reporter delegates writing to {@link AsyncReportWriter}s per dataset.
 *     However, there will always be exactly one writer type (e.g. {@link AsyncListReportWriter}).
 *     A dataset is identified by name. Datasets can be given a prefix to group them together or mark them as
 *     originating from a specific reporter.
 *     By default a dataset has two columns defined by
 *     {@link MiSimReporters#DEFAULT_TIME_COLUMN_NAME} and
 *     {@link MiSimReporters#DEFAULT_VALUE_COLUMN_NAME} which should be seperated by
 *     {@link MiSimReporters#csvSeperator}.
 *     The default headers for a specific dataset can be overwritten by calling
 *     {@link MiSimReporter#registerDefaultHeader(String, String...)}.
 *     This can be done only once and has to be done before the first write to the dataset occurs.
 * </p>
 *
 * @author Lion Wagner
 */
public abstract class MiSimReporter<R extends AsyncReportWriter<?>> {

    protected final MiSimModel model;

    protected final String datasetsPrefix;
    protected final Path reportBasePath;
    private final HashMap<String, R> writers = new HashMap<>();
    private final HashMap<String, String[]> customHeaders = new HashMap<>();

    /**
     * Creates a new MiSimReporter. Registers this reporter at the {@link MiSimReporters} class.
     *
     * @param model          model that provides the report location in its metadata.
     * @param datasetsPrefix name prefix for all datasets created by this reporter.
     */
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

    /**
     * Deregisters this reporter from {@link MiSimReporters}, which causes it not to be finalized at the end of the
     * simulation.
     *
     * @return true if the reporter was registered and was successfully deregistered.
     * @see MiSimReporters#deregister(MiSimReporter)
     */
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
    @SuppressWarnings("unchecked")
    public abstract <T> void addDatapoint(String dataSetName, TimeInstant when, T... data);

    /**
     * Finalizes the report. This method is called automatically by the
     * {@link cambio.simulator.events.SimulationEndEvent}. This will flush all writers and deregister this reporter.
     *
     * @see MiSimReporters#deregister(MiSimReporter)
     */
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

    /**
     * Changes the headers of the given dataset. This can only be done once and has to be done before the first write
     *
     * @param dataSetName name of the dataset for which the header should be changed
     * @param headers     new headers for the dataset
     */
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
