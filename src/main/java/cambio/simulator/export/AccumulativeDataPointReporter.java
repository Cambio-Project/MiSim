package cambio.simulator.export;

import java.util.HashMap;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

/**
 * MultiDataPointReporter that sums up all entries for a TimeInstance.
 *
 * @author Lion Wagner
 */
public class AccumulativeDataPointReporter extends MultiDataPointReporter {

    private final HashMap<String, Double> lastValues = new HashMap<>();

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
            double lastValue = lastValues.get(dataSetName);
            lastValues.put(dataSetName, lastValue + data.doubleValue());
        } else {
            lastValues.put(dataSetName, data.doubleValue());
        }

        super.addDatapoint(dataSetName, when, lastValues.get(dataSetName));
    }
}
