package cambio.simulator.export;

import java.nio.file.Files;
import java.util.Collection;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

/**
 * @author Lion Wagner
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
        if (!writerThreads.containsKey(dataSetName)) {
            try {
                Files.createDirectories(reportBasePath);
                AsyncListReportWriter writerThread = new AsyncListReportWriter(
                    reportBasePath.resolve(datasetsPrefix + dataSetName + ".csv"),
                    customHeaders.getOrDefault(dataSetName,
                        new String[] {MiSimReporters.DEFAULT_VALUE_COLUMN_NAME})[0]);
                writerThreads.put(dataSetName, writerThread);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        AsyncListReportWriter writer = writerThreads.get(dataSetName);
        for (T d : data) {
            writer.addDataPoint(when.getTimeAsDouble(), d);
        }
    }


    public <T> void addDatapoint(String dataSetName, TimeInstant when, Collection<T> data) {
        data.forEach(d -> this.addDatapoint(dataSetName, when, d));
    }
}
