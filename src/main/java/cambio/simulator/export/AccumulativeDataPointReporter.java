package cambio.simulator.export;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import desmoj.core.simulator.TimeInstant;

/**
 * MultiDataPointReporter that sums up all entries for a TimeInstance.
 *
 * @author Lion Wagner
 */
public class AccumulativeDataPointReporter extends MultiDataPointReporter {

    public AccumulativeDataPointReporter() {
    }

    public AccumulativeDataPointReporter(String datasetsPrefix) {
        super(datasetsPrefix);
    }

    /**
     * {@inheritDoc}
     */
    public void addDatapoint(String dataSetName, TimeInstant when, Number data) {
        checkArgumentsAreNotNull(dataSetName, when, data);

        Map<Double, Number> dataSet =
            (HashMap<Double, Number>) dataSets.computeIfAbsent(datasetsPrefix + dataSetName,
                s -> new HashMap<Double, Number>());
        dataSet.merge(when.getTimeAsDouble(), data, (number, number2) -> number.doubleValue() + number2.doubleValue());
    }

    /**
     * {@inheritDoc}
     */
    public <T> void addDatapoint(String dataSetName, TimeInstant when, List<T> data) {
        checkArgumentsAreNotNull(dataSetName, when, data);

        Map<Double, List<T>> dataSet =
            (HashMap<Double, List<T>>) dataSets.computeIfAbsent(datasetsPrefix + dataSetName,
                s -> new HashMap<Double, List<T>>());
        dataSet.merge(when.getTimeAsDouble(), data, (list1, list2) -> {
            list1.addAll(list2);
            return list1;
        });
    }


    private <T> void checkArgumentsAreNotNull(String dataSetName, TimeInstant when, T data) {
        Objects.requireNonNull(dataSetName);
        Objects.requireNonNull(when);
        Objects.requireNonNull(data);
    }
}
