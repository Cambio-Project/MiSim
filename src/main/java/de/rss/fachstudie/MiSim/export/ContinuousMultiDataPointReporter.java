package de.rss.fachstudie.MiSim.export;

import java.util.HashMap;

import desmoj.core.simulator.TimeInstant;

/**
 * {@link MultiDataPointReporter} that repeats the last entry before adding a new one. This creates the effect that each
 * entered values plateaus until its updated.
 *
 * <p>
 * This is specifically use in combination with a line graph.
 *
 * <p>
 * If you need raw value output see {@link MultiDataPointReporter}
 *
 * @author Lion Wagner
 * @see MultiDataPointReporter
 */
public class ContinuousMultiDataPointReporter extends MultiDataPointReporter {

    private final HashMap<String, Object> previousEntries = new HashMap<>();

    public ContinuousMultiDataPointReporter() {
    }

    public ContinuousMultiDataPointReporter(String prefix) {
        super(prefix);
    }

    @Override
    public <T> void addDatapoint(String dataSetName, TimeInstant when, T data) {
        super.addDatapoint(dataSetName, when, data);

        if (previousEntries.containsKey(dataSetName)) {
            Object previousData = previousEntries.get(dataSetName);
            TimeInstant timeBeforeWhen = new TimeInstant((when.getTimeInEpsilon() - 1) / (Math.pow(10, 6)));
            if (!super.getDataSets().get(datasetsPrefix + dataSetName).containsKey(timeBeforeWhen.getTimeAsDouble())) {
                super.addDatapoint(dataSetName, timeBeforeWhen, previousData);
            }
        }
        previousEntries.put(dataSetName, data);
    }
}
