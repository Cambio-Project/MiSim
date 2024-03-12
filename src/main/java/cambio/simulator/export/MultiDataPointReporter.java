package cambio.simulator.export;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import org.jetbrains.annotations.NotNull;

/**
 * Dynamically-typed data point collector.
 *
 * @author Lion Wagner
 */
public class MultiDataPointReporter extends MiSimReporter<AsyncMultiColumnReportWriter> {

    public MultiDataPointReporter(@NotNull Model model) {
        this("", model);
    }

    public MultiDataPointReporter(@NotNull String datasetsPrefix, @NotNull Model model) {
        super(model, datasetsPrefix);
    }

    /**
     * Adds a new datapoint to the given dataset.
     *
     * @param dataSetName name of the dataset to which the datapoint should be added
     * @param when        point in simulation time to which the datapoint is associated to
     * @param data        data that should be logged
     * @param <T>         type of the data that should be logged.
     */
    @Override
    public <T> void addDatapoint(final String dataSetName, final TimeInstant when, final T... data) {
        checkArgumentsAreNotNull(dataSetName, when, data);
        AsyncMultiColumnReportWriter writerThread = getWriter(dataSetName);
        writerThread.addDataPoint(when.getTimeAsDouble(), data);
    }

    @Override
    protected AsyncMultiColumnReportWriter createWriter(Path datasetPath, String[] headers) throws IOException {
        Objects.requireNonNull(headers);
        return new AsyncMultiColumnReportWriter(datasetPath, headers);
    }
}
