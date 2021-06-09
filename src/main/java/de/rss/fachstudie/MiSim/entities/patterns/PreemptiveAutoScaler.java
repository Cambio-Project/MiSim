package de.rss.fachstudie.MiSim.entities.patterns;

import java.util.List;

import de.rss.fachstudie.MiSim.entities.microservice.Microservice;
import de.rss.fachstudie.MiSim.export.MultiDataPointReporter;
import de.rss.fachstudie.MiSim.parsing.FromJson;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

/**
 * Autoscaler that periodically checks all instances of a service. If the average CPU utilization is above the target
 * threshold a new instance will be created. If the utilization is below a threshold the service will be downscaled. The
 * instance with the lowest utilization will be shutdown. After an upscale the service is locked from downscaling during
 * the {@code holdTime} to prevent a too early downscaling during a workload ramp up.
 *
 * @author Lion Wagner
 * @see <a href=https://doi.org/10.1109/MASCOTS.2014.32>Auto-scaling Strategies for Cloud Computing Environments</a>
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
    private final transient MultiDataPointReporter reporter = new MultiDataPointReporter("AS");

    public PreemptiveAutoScaler(Model model, String name, boolean showInTrace, Microservice owner) {
        super(model, name, showInTrace, owner);
    }

    @Override
    protected void onTriggered() {

        List<Double> utils = owner.getRelativeUtilizationOfInstances();
        reporter.addDatapoint("_Util", presentTime(), utils);

        int currentInstanceCount = owner.getInstancesCount();
        // double avg = owner.getUtilizationOfInstances().stream().mapToDouble(value -> value).average().orElse(0.0);
        double avg = owner.getAverageRelativeUtilization();
        if (currentInstanceCount <= 0) { //starts a instances if there are none
            owner.setInstancesCount(1);
        } else if (avg >= upperBound) {
            owner.scaleToInstancesCount(currentInstanceCount + 1);
            lastScaleUp = presentTime();
        } else if (avg <= lowerBound
            && currentInstanceCount > 1
            && presentTime().getTimeAsDouble() - lastScaleUp.getTimeAsDouble() > holdTime) {
            owner.scaleToInstancesCount(currentInstanceCount - 1);
        }
        if (owner.getInstancesCount() != currentInstanceCount) {
            sendTraceNote(String.format("Changed target instance count to %d", owner.getInstancesCount()));
        }
    }

}
