package cambio.simulator.export;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

/**
 * A {@link MiSimReporter} that collects uses an {@link AsyncListReportWriter} to collect data points.
 * Allows for Reporting a list of values for a single time point. The given values are converted to strings on
 * write.
 *
 * @author Lion Wagner
 * @see AsyncListReportWriter
 */
public class ListCollectingReporter extends MiSimReporter<AsyncListReportWriter> {


    public ListCollectingReporter(Model model) {
        this("", model);
    }

    public ListCollectingReporter(String datasetsPrefix, Model model) {
        super(model, datasetsPrefix);
    }

    @Override
    @SafeVarargs
    public final <T> void addDatapoint(String dataSetName, TimeInstant when, T... data) {
        AsyncListReportWriter writer = getWriter(dataSetName);
        for (T d : data) {
            writer.addDataPoint(when.getTimeAsDouble(), d);
        }
    }

    public <T> void addDatapoint(String dataSetName, TimeInstant when, Collection<T> data) {
        data.forEach(d -> this.addDatapoint(dataSetName, when, d));
    }

    @Override
    protected AsyncListReportWriter createWriter(Path datasetPath, String[] headers) throws IOException {
        return new AsyncListReportWriter(datasetPath, headers[0]);
    }

    @Override
    public void registerDefaultHeader(String dataSetName, String... headers) {
        super.registerDefaultHeader(dataSetName, headers[0]);
    }
}
