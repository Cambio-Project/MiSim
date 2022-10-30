package cambio.simulator.export;

import java.nio.file.Files;
import java.util.Collection;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import org.jetbrains.annotations.NotNull;

/**
 * @author Lion Wagner
 */
public class ListCollectingReporter extends MultiDataPointReporter {


    public ListCollectingReporter(Model model) {
        super(model);
    }

    public ListCollectingReporter(@NotNull String datasetsPrefix,
                                  @NotNull Model model) {
        super(datasetsPrefix, model);
    }

    @Override
    public <T> void addDatapoint(String dataSetName, TimeInstant when, T data) {
        if (!writerThreads.containsKey(dataSetName)) {
            try {
                Files.createDirectories(reportBasePath);
                AsyncReportWriter<?> writerThread = new AsyncListReportWriter(
                    reportBasePath.resolve(datasetsPrefix + dataSetName + ".csv"));
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
