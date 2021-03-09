package de.rss.fachstudie.MiSim.export;

import desmoj.core.simulator.TimeInstant;

import java.util.HashMap;

/**
 * {@link MultiDataPointReporter} that repeats the last entry before adding a new one. This creates the effect that each
 * entered values plateaus until its updated.
 * <p>
 * This is specifically use in combination with a line graph.
 * <p>
 * If you need raw value output see {@link MultiDataPointReporter}
 *
 * @author Lion Wagner
 * @see MultiDataPointReporter
 */
public class ContinuousMultiDataPointReporter extends MultiDataPointReporter {

    private HashMap<String, Object> previous_entries = new HashMap<>();

    public ContinuousMultiDataPointReporter() {
    }

    public ContinuousMultiDataPointReporter(String datasets_prefix) {
        super(datasets_prefix);
    }

    @Override
    public <T> void addDatapoint(String dataSetName, TimeInstant when, T data) {
        super.addDatapoint(dataSetName, when, data);

        if (previous_entries.containsKey(dataSetName)) {
            Object previousData = previous_entries.get(dataSetName);
            TimeInstant timeBeforeWhen = new TimeInstant((when.getTimeInEpsilon() - 1) / (Math.pow(10, 6)));
            if (!super.getDataSets().get(datasets_prefix + dataSetName).containsKey(timeBeforeWhen)) {
                super.addDatapoint(dataSetName, timeBeforeWhen, previousData);
            }
        }
        previous_entries.put(dataSetName, data);
    }
}
