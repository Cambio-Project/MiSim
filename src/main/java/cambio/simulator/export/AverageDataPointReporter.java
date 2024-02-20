package cambio.simulator.export;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

/**
 * MultiDataPointReporter that averages all entries for a TimeInstance over a given time bucket.
 *
 * @author Sebastian Frank
 */
public class AverageDataPointReporter extends BucketMultiDataPointReporter {
    /**
     * Indicates that averages need to be (re-)computed. For performance reasons, only compute the averages in the
     * end when data got updated.
     */
    private boolean update = false;

    /**
     * Sum of the added datapoint values. Required to compute the average.
     */
    private final HashMap<String, HashMap<Double, Number>> valueSum = new HashMap<>();
    /**
     * Counter for added datapoint values. Required to compute the average.
     */
    private final HashMap<String, HashMap<Double, Integer>> counts = new HashMap<>();

    private final HashMap<String, HashMap<Double, Number>> avgValues = new HashMap<>();

    /**
     * Constructs a new data point reporter.
     *
     * @see BucketMultiDataPointReporter#BucketMultiDataPointReporter(Model)
     */
    public AverageDataPointReporter(Model model) {
        super(model);
    }

    /**
     * Constructs a new data point reporter.
     *
     * @see BucketMultiDataPointReporter#BucketMultiDataPointReporter(Model, UnaryOperator)
     */
    public AverageDataPointReporter(final Model model, final UnaryOperator<TimeInstant> bucketFunction) {
        super(model, bucketFunction);
    }

    /**
     * Constructs a new data point reporter.
     *
     * @see BucketMultiDataPointReporter#BucketMultiDataPointReporter(String, Model)
     */
    public AverageDataPointReporter(final String datasetsPrefix, final Model model) {
        super(datasetsPrefix, model);
    }

    /**
     * Constructs a new data point reporter.
     *
     * @see BucketMultiDataPointReporter#BucketMultiDataPointReporter(String, Model, UnaryOperator)
     */
    public AverageDataPointReporter(final String datasetsPrefix, final Model model,
                                    final UnaryOperator<TimeInstant> bucketFunction) {
        super(datasetsPrefix, model, bucketFunction);
    }

    /**
     * {@inheritDoc}
     *
     * @see MultiDataPointReporter#addDatapoint(String, TimeInstant, Object[])
     */
    public void addDatapoint(final String dataSetName, TimeInstant when, final Number data) {
        checkArgumentsAreNotNull(dataSetName, when, data);
        when = bucketingFunction.apply(when);
        update = true;

        Map<Double, Number> dataSet = valueSum.computeIfAbsent(datasetsPrefix + dataSetName, s -> new HashMap<>());
        Map<Double, Integer> countSet = counts.computeIfAbsent(datasetsPrefix + dataSetName, s -> new HashMap<>());
        dataSet.merge(when.getTimeAsDouble(), data, (number, number2) -> number.doubleValue() + number2.doubleValue());
        countSet.merge(when.getTimeAsDouble(), 1, Integer::sum);
    }

    /**
     * Sets the recent average values in the result data. Required before export can be executed.
     */
    private void setAverages() {
        if (update) {
            for (Map.Entry<String, HashMap<Double, Number>> entry : valueSum.entrySet()) {
                var dataSetName = entry.getKey();
                var dataSet = entry.getValue();
                var averagesForDataset = computeAverages(dataSetName, dataSet);
                avgValues.put(dataSetName, averagesForDataset);
            }
        }
    }

    /**
     * Computes the average values for a given data set containing sums.
     *
     * @param dataSetName the identifier of the given data set.
     * @param dataSet     a data set from {@link #valueSum}. The given data set will not be modified.
     * @return the given data set but with average values.
     */
    private HashMap<Double, Number> computeAverages(final String dataSetName,
                                                    final HashMap<Double, Number> dataSet) {
        HashMap<Double, Number> dataSetValues = (HashMap<Double, Number>) dataSet.clone();
        for (Map.Entry<Double, Number> entry2 : dataSetValues.entrySet()) {
            // Divide the sum data in the data set by the according count to get the average
            dataSetValues.merge(entry2.getKey(), counts.get(dataSetName).get(entry2.getKey()),
                (number, number2) -> number.doubleValue() / number2.doubleValue());
        }
        return dataSetValues;
    }

    @Override
    public void finalizeReport() {
        setAverages();
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
        avgValues.keySet().forEach(this::flush);
    }

    /**
     * Writes the given dataset to disk.
     */
    public void flush(String datasetName) {
        if (avgValues.containsKey(datasetName)) {
            HashMap<Double, Number> lastValuesForDataSet = avgValues.get(datasetName);
            lastValuesForDataSet.keySet().stream().sorted()
                .forEach(timeInstant -> this.flush(datasetName, timeInstant));
        }
    }

    /**
     * Writes the given TimeInstant (timestamp) of a specific dataset to disk.
     */
    public void flush(String datasetName, double when) {
        if (avgValues.containsKey(datasetName)) {
            HashMap<Double, Number> lastValuesForDataSet = avgValues.get(datasetName);
            if (lastValuesForDataSet.containsKey(when)) {
                internalAddDatapoint(datasetName, when, lastValuesForDataSet.remove(when));
            }
        }
    }

    @Override
    public void registerDefaultHeader(String dataSetName, String... headers) {
        super.registerDefaultHeader(dataSetName, headers[0]);
    }

    @Override
    protected AsyncMultiColumnReportWriter createWriter(Path datasetPath, String[] headers) throws IOException {
        return new AsyncMultiColumnReportWriter(datasetPath, headers[0]);
    }

}
