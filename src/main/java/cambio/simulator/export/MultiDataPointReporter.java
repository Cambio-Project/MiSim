package cambio.simulator.export;

import java.nio.file.Files;

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

    @Override
    public <T> void addDatapoint(final String dataSetName, final TimeInstant when, final T... data) {
        checkArgumentsAreNotNull(dataSetName, when, data);

        if (!writerThreads.containsKey(dataSetName)) {
            try {
                Files.createDirectories(reportBasePath);
                AsyncMultiColumnReportWriter writerThread =
                    new AsyncMultiColumnReportWriter(
                        reportBasePath.resolve(datasetsPrefix + dataSetName + ".csv"),
                        customHeaders.getOrDefault(dataSetName,
                            new String[] {MiSimReporters.DEFAULT_VALUE_COLUMN_NAME}));
                writerThreads.put(dataSetName, writerThread);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        AsyncMultiColumnReportWriter writerThread = writerThreads.get(dataSetName);
        writerThread.addDataPoint(when.getTimeAsDouble(), data);
    }
}
