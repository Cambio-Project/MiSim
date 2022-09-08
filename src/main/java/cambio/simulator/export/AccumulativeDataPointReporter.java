package cambio.simulator.export;

import java.util.HashMap;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

/**
 * Accumulates numeric data points. Accumulation is done by adding up all given data per TimeInstant.
 * <p>
 * Since this Reporter does not know when accumulation
 *
 * <b>The data will be converted to double.</b>
 *
 * @author Lion Wagner
 */
public class AccumulativeDataPointReporter extends MultiDataPointReporter {

    private final HashMap<String, HashMap<TimeInstant, Double>> lastValues = new HashMap<>();

    public AccumulativeDataPointReporter(Model model) {
        this("", model);
    }

    public AccumulativeDataPointReporter(String datasetsPrefix, Model model) {
        super(datasetsPrefix, model);
    }

    /**
     * {@inheritDoc}
     */
    public void addDatapoint(String dataSetName, TimeInstant when, Number data) {
        checkArgumentsAreNotNull(dataSetName, when, data);

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

    /**
     * Writes all accumulated datapoints to disk.
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
                super.addDatapoint(datasetName, when, lastValuesForDataSet.get(when));
            }
            lastValuesForDataSet.remove(when);
        }
    }

    @Override
    public void finalizeReport() {
        flush();
        super.finalizeReport();
    }
}
