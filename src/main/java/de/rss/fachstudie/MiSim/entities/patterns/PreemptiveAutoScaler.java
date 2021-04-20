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
    @SuppressWarnings("FieldMayBeFinal")
    private double lowerBound = 0.2;
    @FromJson
    @SuppressWarnings("FieldMayBeFinal")
    private double upperBound = 0.8;
    @FromJson
    @SuppressWarnings("FieldMayBeFinal")
    private double holdTime = 30; //Time that an instance at least has to run

    private TimeInstant lastScaleUp = new TimeInstant(0);

    public PreemptiveAutoScaler(Model model, String name, boolean showInTrace, Microservice owner) {
        super(model, name, showInTrace, owner);
    }

    @Override
    protected void onTriggered() {

        int currentInstanceCount = owner.getInstancesCount();
        double avg = owner.getUtilizationOfInstances().stream().mapToDouble(value -> value).average().orElse(0.0);
        if (currentInstanceCount <= 0) { //starts a instances if there are none
            owner.setInstancesCount(1);
        } else if (avg >= upperBound) {
            owner.scaleToInstancesCount(currentInstanceCount + 1);
            lastScaleUp = presentTime();
        } else if (avg <= lowerBound && currentInstanceCount > 2 && presentTime().getTimeAsDouble() - lastScaleUp.getTimeAsDouble() > holdTime) {
            owner.scaleToInstancesCount(currentInstanceCount - 1);
        }
        if (owner.getInstancesCount() != currentInstanceCount) {
            sendTraceNote(String.format("Changed target instance count to %d", owner.getInstancesCount()));
        }
    }

}
