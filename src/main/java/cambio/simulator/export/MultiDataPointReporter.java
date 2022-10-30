package cambio.simulator.export;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import org.jetbrains.annotations.NotNull;

/**
 * Dynamically-typed data point collector.
 *
 * @author Lion Wagner
 */
public class MultiDataPointReporter extends MiSimReporter {

    protected final String datasetsPrefix;
    protected final Path reportBasePath;
    protected final HashMap<String, AsyncReportWriter<?>> writerThreads = new HashMap<>();

    public MultiDataPointReporter(@NotNull Model model) {
        this("", model);
    }

    public MultiDataPointReporter(@NotNull String datasetsPrefix, @NotNull Model model) {
        super(model);
        Objects.requireNonNull(datasetsPrefix);

        this.datasetsPrefix = datasetsPrefix;
        this.reportBasePath = this.model.getExperimentMetaData().getReportLocation().resolve("raw");
    }

    /**
     * Adds a new datapoint to the given dataset.
     *
     * @param dataSetName name of the dataset to which the datapoint should be added
     * @param when        point in simulation time to which the datapoint is associated to
     * @param data        data that should be logged
     * @param <T>         type of the data that should be logged.
     */
    public <T> void addDatapoint(final String dataSetName, final TimeInstant when, final T data) {
        checkArgumentsAreNotNull(dataSetName, when, data);

        if (!writerThreads.containsKey(dataSetName)) {
            try {
                Files.createDirectories(reportBasePath);
                AsyncReportWriter<?> writerThread =
                    new AsyncSimpleReportWriter(reportBasePath.resolve(datasetsPrefix + dataSetName
                        + ".csv"));
                writerThreads.put(dataSetName, writerThread);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        AsyncReportWriter<?> writerThread = writerThreads.get(dataSetName);
        writerThread.addDataPoint(when.getTimeAsDouble(), data);
    }


    @Override
    public void finalizeReport() {
        writerThreads.values().forEach(AsyncReportWriter::finalizeWriteout);
        super.deregister();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()
            + (datasetsPrefix.equals("") ? "" : "{"
            + "datasetsPrefix='" + datasetsPrefix + '\''
            + '}');
    }
}
