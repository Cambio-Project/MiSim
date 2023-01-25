package cambio.simulator.export;

import java.nio.file.Files;
import java.util.Collection;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

/**
 * @author Lion Wagner
 */
public class ListCollectingReporter extends MultiDataPointReporter {


    public ListCollectingReporter(Model model) {
        super(model);
    }

    public ListCollectingReporter(String datasetsPrefix, Model model) {
        super(datasetsPrefix, model);
    }

    @Override
    @SafeVarargs
    public final <T> void addDatapoint(String dataSetName, TimeInstant when, T... data) {
        if (!writerThreads.containsKey(dataSetName)) {
            try {
                Files.createDirectories(reportBasePath);
                AsyncReportWriter<?> writerThread = new AsyncListReportWriter(
                    reportBasePath.resolve(datasetsPrefix + dataSetName + ".csv"),
                    customHeaders.getOrDefault(dataSetName, new String[] {MiSimReporters.DEFAULT_VALUE_COLUMN_NAME})[0]);
                writerThreads.put(dataSetName, writerThread);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.addDatapoint(dataSetName, when, data);
    }


    public <T> void addDatapoint(String dataSetName, TimeInstant when, Collection<T> data) {
        data.forEach(d -> this.addDatapoint(dataSetName, when, d));
    }
}
