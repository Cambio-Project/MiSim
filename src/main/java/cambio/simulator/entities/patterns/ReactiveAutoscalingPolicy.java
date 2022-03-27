package cambio.simulator.entities.patterns;

import java.util.List;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.export.MultiDataPointReporter;
import cambio.simulator.parsing.JsonTypeName;
import desmoj.core.simulator.TimeInstant;

/**
 * A basic implementation of a reactive autoscaling policy. Looks at currently active and queued demand for scaling
 * decision.
 *
 * @author Lion Wagner
 */
@JsonTypeName("reactive")
class ReactiveAutoscalingPolicy implements IAutoscalingPolicy {


    private final transient MultiDataPointReporter reporter = new MultiDataPointReporter("AS");
    private double lowerBound = 0.3;
    private double upperBound = 0.8;
    /**
     * Minimum time an instance has to run before it can be shutdown by down-scaling.
     */
    private double holdTime = 120;
    private transient TimeInstant lastScaleUp = new TimeInstant(0);

    @Override
    public void apply(Microservice owner) {
        TimeInstant presentTime = owner.presentTime();
        int currentInstanceCount = owner.getInstancesCount();

        // double avg = owner.getUtilizationOfInstances().stream().mapToDouble(value -> value).average().orElse(0.0);
        double avg = owner.getAverageRelativeUtilization();
        if (currentInstanceCount <= 0) { //starts a instances if there are none
            owner.setInstancesCount(1);
        } else if (avg >= upperBound) {
            double upScalingFactor = avg / (upperBound - 0.01);
            owner.scaleToInstancesCount((int) Math.max(1, Math.ceil(currentInstanceCount * upScalingFactor)));
            lastScaleUp = presentTime;
        } else if (avg <= lowerBound
            && currentInstanceCount > 1
            && presentTime.getTimeAsDouble() - lastScaleUp.getTimeAsDouble() > holdTime) {
            double downScaleFactor = Math.max(0.01, avg) / lowerBound;
            int newInstanceCount = (int) Math.max(1, Math.ceil(currentInstanceCount * downScaleFactor));
            owner.scaleToInstancesCount(newInstanceCount);
            lastScaleUp = presentTime;
        }
        if (owner.getInstancesCount() != currentInstanceCount) {
            owner.sendTraceNote(String.format("Changed target instance count to %d", owner.getInstancesCount()));
        }
    }
}
