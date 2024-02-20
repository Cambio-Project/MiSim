package cambio.simulator.export;

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

        Map<Double, Number> dataSet =
            (HashMap<Double, Number>) valueSum.computeIfAbsent(datasetsPrefix + dataSetName, s -> new HashMap<>());
        Map<Double, Integer> countSet =
            (HashMap<Double, Integer>) counts.computeIfAbsent(datasetsPrefix + dataSetName, s -> new HashMap<>());
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
                AsyncMultiColumnReportWriter writerThread = getWriter(dataSetName);
                var averagesForDataset = computeAverages(dataSetName, dataSet);
                for (Map.Entry<Double, Number> averageEntry : averagesForDataset.entrySet()) {
                    writerThread.addDataPoint(averageEntry.getKey(), averageEntry.getValue());
                }
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
        // TODO: This is a working solution, but not closely conforming to the new Reporter
        setAverages();
        super.finalizeReport();
    }

}
