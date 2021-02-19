package de.rss.fachstudie.MiSim.entities.networking;

import de.rss.fachstudie.MiSim.entities.Operation;
import de.rss.fachstudie.MiSim.models.MainModel;
import desmoj.core.simulator.TimeInstant;

/**
 * @author Lion Wagner
 */
public final class IntervalGenerator extends Generator {

    private final double interval;

    public IntervalGenerator(MainModel model, String name, boolean showInTrace, Operation operation, double interval) {
        super(model, name, showInTrace, operation);
        nextTargetTime = new TimeInstant(0);
        this.interval = interval;
    }

    @Override
    protected TimeInstant getNextTargetTime(final TimeInstant lastTargetTime) {
        double nextTargetTime_d = lastTargetTime != null ? lastTargetTime.getTimeAsDouble() : 0;
        return new TimeInstant(nextTargetTime_d + interval);
    }
}
