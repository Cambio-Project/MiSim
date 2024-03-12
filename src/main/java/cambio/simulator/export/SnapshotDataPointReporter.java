package cambio.simulator.export;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.UnaryOperator;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

/**
 * Reports last value for the given time bucket.
 */
public class SnapshotDataPointReporter extends BucketMultiDataPointReporter {
    /**
     * Last received datapoint values.
     */
    private final HashMap<String, ArrayList<Number>> finalDataPoints = new HashMap<>();

    public SnapshotDataPointReporter(Model model) {
        super(model);
    }

    public SnapshotDataPointReporter(String datasetsPrefix, Model model) {
        super(datasetsPrefix, model);
    }

    public SnapshotDataPointReporter(String datasetsPrefix, Model model, UnaryOperator<TimeInstant> bucketingFunction) {
        super(datasetsPrefix, model, bucketingFunction);
    }

    public SnapshotDataPointReporter(Model model, UnaryOperator<TimeInstant> bucketingFunction) {
        super(model, bucketingFunction);
    }

    /**
     * {@inheritDoc}
     *
     * @see MultiDataPointReporter#addDatapoint(String, TimeInstant, Object[])
     */
    public void addDatapoint(final String dataSetName, TimeInstant when, final Number data) {
        checkArgumentsAreNotNull(dataSetName, when, data);
        when = bucketingFunction.apply(when);
        ArrayList<Number> dataSet =
            finalDataPoints.computeIfAbsent(datasetsPrefix + dataSetName, s -> new ArrayList<>());
        int index = (int) Math.round(when.getTimeAsDouble());
        int maxListIndex = dataSet.size() - 1;

        if (index <= maxListIndex) {
            dataSet.set(index, data);
        } else if (index == maxListIndex + 1) {
            dataSet.add(data);
        } else {
            fillUpData(dataSet, index - 1);
            dataSet.add(data);
        }
    }

    private void fillUpData(ArrayList<Number> list, int until) {
        Number lastValue = list.get(list.size() - 1);
        while (list.size() - 1 < until) {
            list.add(lastValue);
        }
    }


    @Override
    public void finalizeReport() {
        flush();
        super.finalizeReport();
    }


    @SafeVarargs
    private <T> void internalAddDatapoint(String dataSetName, double when, T... data) {
        AsyncMultiColumnReportWriter writerThread = getWriter(dataSetName);
        writerThread.addDataPoint(when, data);
    }

    /**
     * Writes all accumulated data-points to disk.
     */
    public void flush() {
        finalDataPoints.keySet().forEach(this::flush);
    }

    /**
     * Writes the given dataset to disk.
     */
    public void flush(String datasetName) {
        if (finalDataPoints.containsKey(datasetName)) {
            ArrayList<Number> lastValuesForDataSet = finalDataPoints.get(datasetName);
            for (int i = 0; i < lastValuesForDataSet.size(); i++) {
                this.internalAddDatapoint(datasetName, i, lastValuesForDataSet.get(i));
            }
        }
    }

    @Override
    public void registerDefaultHeader(String dataSetName, String... headers) {
        super.registerDefaultHeader(dataSetName, headers[0]);
    }

    // TODO: Pattern of late flushing has been reused over multiple reporter classes and could be abstracted
    @Override
    protected AsyncMultiColumnReportWriter createWriter(Path datasetPath, String[] headers) throws IOException {
        return new AsyncMultiColumnReportWriter(datasetPath, headers[0]);
    }
}
