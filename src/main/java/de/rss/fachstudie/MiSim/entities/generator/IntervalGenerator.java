package de.rss.fachstudie.MiSim.entities.generator;

import de.rss.fachstudie.MiSim.entities.Operation;
import de.rss.fachstudie.MiSim.models.MainModel;
import desmoj.core.dist.ContDistUniform;
import desmoj.core.dist.NumericalDist;
import desmoj.core.simulator.TimeInstant;

/**
 * @author Lion Wagner
 */
public final class IntervalGenerator extends Generator {

    private final double interval;
    private final NumericalDist<Double> dist;

    public IntervalGenerator(MainModel model, String name, boolean showInTrace, Operation operation, double interval) {
        super(model, name, showInTrace, operation);
        this.interval = interval;
        dist = new ContDistUniform(model, name, interval, interval, true, true);
    }

    @Override
    protected TimeInstant getNextTargetTime(final TimeInstant lastTargetTime) {
        double nextTargetTime_d = lastTargetTime != null ? lastTargetTime.getTimeAsDouble() : 0;
        return new TimeInstant(nextTargetTime_d + interval);
    }

    @Override
    protected TimeInstant getFirstTargetTime() {
        return presentTime();
    }
}
