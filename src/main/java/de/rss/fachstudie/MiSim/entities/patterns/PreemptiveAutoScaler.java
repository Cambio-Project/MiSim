package de.rss.fachstudie.MiSim.entities.patterns;

import de.rss.fachstudie.MiSim.entities.microservice.Microservice;
import de.rss.fachstudie.MiSim.parsing.FromJson;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

/**
 * @author Lion Wagner
 * @see https://doi.org/10.1109/MASCOTS.2014.32
 */
public final class PreemptiveAutoScaler extends PeriodicServiceOwnedPattern {

    @FromJson
    private double lowerBound = 0.2;
    @FromJson
    private double upperBound = 0.8;

    @FromJson
    private double holdTime = 30; //Time that an instance atleast has to run

    private TimeInstant lastScaleup = new TimeInstant(0);

    public PreemptiveAutoScaler(Model model, String name, boolean showInTrace, Microservice owner) {
        super(model, name, showInTrace, owner);
    }

    @Override
    protected void onTriggered() {

        int currentInstanceCount = owner.getInstancesCount();
        double max = owner.getUtilizations().stream().mapToDouble(value -> value).average().orElse(0.0);
        if (currentInstanceCount <= 0) { //starts a instances if there are none
            owner.setInstancesCount(1);
        } else if (max >= upperBound) {
            owner.scaleToInstancesCount(currentInstanceCount + 1);
            lastScaleup = presentTime();
        } else if (max <= lowerBound && currentInstanceCount > 2 && presentTime().getTimeAsDouble() - lastScaleup.getTimeAsDouble() > holdTime) {
            owner.scaleToInstancesCount(currentInstanceCount - 1);
        }
        if (owner.getInstancesCount() != currentInstanceCount) {
            sendTraceNote(String.format("Changed target instance count to %d", owner.getInstancesCount()));
        }
    }

}
