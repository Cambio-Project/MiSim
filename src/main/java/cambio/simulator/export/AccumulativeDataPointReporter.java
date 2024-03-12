package cambio.simulator.export;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.UnaryOperator;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

/**
 * Accumulates numeric data points. Accumulation is done by adding up all given data per TimeInstant.
 *
 * <p>
 * Since this Reporter does not know when accumulation <b>The data will be converted to double.</b>
 *
 * @author Lion Wagner
 */
public class AccumulativeDataPointReporter extends BucketMultiDataPointReporter {

    private final HashMap<String, HashMap<TimeInstant, Double>> lastValues = new HashMap<>();

    /**
     * Constructs a new data point reporter.
     *
     * @see BucketMultiDataPointReporter#BucketMultiDataPointReporter(Model)
     */
    public AccumulativeDataPointReporter(Model model) {
        this("", model);
    }

    /**
     * Constructs a new data point reporter.
     *
     * @see BucketMultiDataPointReporter#BucketMultiDataPointReporter(String, Model)
     */
    public AccumulativeDataPointReporter(String datasetsPrefix, Model model) {
        super(datasetsPrefix, model);

    }


    /**
     * Constructs a new data point reporter.
     *
     * @see BucketMultiDataPointReporter#BucketMultiDataPointReporter(Model, UnaryOperator)
     */
    public AccumulativeDataPointReporter(final Model model, final UnaryOperator<TimeInstant> bucketFunction) {
        super(model, bucketFunction);
    }

    /**
     * Constructs a new data point reporter.
     *
     * @see BucketMultiDataPointReporter#BucketMultiDataPointReporter(String, Model, UnaryOperator)
     */
    public AccumulativeDataPointReporter(final String datasetsPrefix, final Model model,
                                         final UnaryOperator<TimeInstant> bucketFunction) {
        super(datasetsPrefix, model, bucketFunction);
    }

    /**
     * {@inheritDoc}
     */
    public void addDatapoint(String dataSetName, TimeInstant when, Number data) {
        checkArgumentsAreNotNull(dataSetName, when, data);
        when = bucketingFunction.apply(when);

        if (lastValues.containsKey(dataSetName)) {
            HashMap<TimeInstant, Double> lastValuesForDataSet = lastValues.get(dataSetName);
            if (lastValuesForDataSet.containsKey(when)) {
                lastValuesForDataSet.put(when, lastValuesForDataSet.get(when) + data.doubleValue());
            } else {
                lastValuesForDataSet.put(when, data.doubleValue());
            }
        } else {
            lastValues.put(dataSetName, new HashMap<>());
            lastValues.get(dataSetName).put(when, data.doubleValue());
        }
    }

    @SafeVarargs
    @Override
    public final <T> void addDatapoint(String dataSetName, TimeInstant when, T... data) {
        if (data.length > 0 && data[0] instanceof Number) {
            for (T datum : data) {
                addDatapoint(dataSetName, when, (Number) datum);
            }
        } else {
            throw new UnsupportedOperationException(
                "This method is not supported. Use addDatapoint(String, TimeInstant, Number) instead.");
        }
    }

    @SafeVarargs
    private <T> void internalAddDatapoint(String dataSetName, TimeInstant when, T... data) {
        checkArgumentsAreNotNull(dataSetName, when, data);
        AsyncMultiColumnReportWriter writerThread = getWriter(dataSetName);
        writerThread.addDataPoint(when.getTimeAsDouble(), data);
    }

    /**
     * Writes all accumulated data-points to disk.
     */
    public void flush() {
        lastValues.keySet().forEach(this::flush);
    }

    /**
     * Writes the given dataset to disk.
     */
    public void flush(String datasetName) {
        if (lastValues.containsKey(datasetName)) {
            HashMap<TimeInstant, Double> lastValuesForDataSet = lastValues.get(datasetName);
            lastValuesForDataSet.keySet().stream().sorted()
                .forEach(timeInstant -> this.flush(datasetName, timeInstant));
        }
    }

    /**
     * Writes the given TimeInstant (timestamp) of a specific dataset to disk.
     */
    public void flush(String datasetName, TimeInstant when) {
        if (lastValues.containsKey(datasetName)) {
            HashMap<TimeInstant, Double> lastValuesForDataSet = lastValues.get(datasetName);
            if (lastValuesForDataSet.containsKey(when)) {
                internalAddDatapoint(datasetName, when, lastValuesForDataSet.remove(when));
            }
        }
    }


    @Override
    public void finalizeReport() {
        flush();
        super.finalizeReport();
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
