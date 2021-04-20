package de.rss.fachstudie.MiSim.export;

import desmoj.core.simulator.TimeInstant;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * @author Lion Wagner
 */
public class AccumulativeDataPointReporter extends MultiDataPointReporter {

    public AccumulativeDataPointReporter() {
    }

    public AccumulativeDataPointReporter(String datasets_prefix) {
        super(datasets_prefix);
    }

    public void addDatapoint(String dataSetName, TimeInstant when, Number data) {
        checkArgumentsAreNotNull(dataSetName, when, data);

        Map<Double, Number> dataSet = (TreeMap<Double, Number>) dataSets.computeIfAbsent(datasets_prefix + dataSetName, s -> new TreeMap<Double, Number>());
        dataSet.merge(when.getTimeAsDouble(), data, (number, number2) -> number.doubleValue() + number2.doubleValue());
    }

    public <T> void addDatapoint(String dataSetName, TimeInstant when, List<T> data) {
        checkArgumentsAreNotNull(dataSetName, when, data);

        Map<Double, List<T>> dataSet = (TreeMap<Double, List<T>>) dataSets.computeIfAbsent(datasets_prefix + dataSetName, s -> new TreeMap<Double, List<T>>());
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
