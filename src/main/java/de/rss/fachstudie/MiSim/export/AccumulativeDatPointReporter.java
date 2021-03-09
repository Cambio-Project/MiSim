package de.rss.fachstudie.MiSim.export;

import desmoj.core.simulator.TimeInstant;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * @author Lion Wagner
 */
public class AccumulativeDatPointReporter extends MultiDataPointReporter {

    public AccumulativeDatPointReporter() {
    }

    public AccumulativeDatPointReporter(String datasets_prefix) {
        super(datasets_prefix);
    }

    public void addDatapoint(String dataSetName, TimeInstant when, Number data) {
        checkArgumentsAreNotNull(dataSetName, when, data);

        Map<TimeInstant, Number> dataSet = (TreeMap<TimeInstant, Number>) dataSets.computeIfAbsent(datasets_prefix + dataSetName, s -> new TreeMap<TimeInstant, Number>());
        dataSet.merge(when, data, (number, number2) -> number.doubleValue() + number2.doubleValue());
    }

    public <T> void addDatapoint(String dataSetName, TimeInstant when, List<T> data) {
        checkArgumentsAreNotNull(dataSetName, when, data);

        Map<TimeInstant, List<T>> dataSet = (TreeMap<TimeInstant, List<T>>) dataSets.computeIfAbsent(datasets_prefix + dataSetName, s -> new TreeMap<TimeInstant, List<T>>());
        dataSet.merge(when, data, (list1, list2) -> {
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
