package cambio.simulator.entities.patterns;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.export.MultiDataPointReporter;
import cambio.simulator.misc.TimeUtil;
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
    private transient MultiDataPointReporter reporter = null;
    private double lowerBound = 0.3;
    private double upperBound = 0.8;
    private int minInstances = 1;
    private int maxInstances = Integer.MAX_VALUE;
    /**
     * Minimum time an instance has to run before it can be shutdown by down-scaling.
     */
    private double holdTime = 120;
    private transient TimeInstant lastScaleUp = new TimeInstant(0);

    @Override
    public void apply(Microservice owner) {
        if (reporter == null) {
            reporter = new MultiDataPointReporter(String.format("AS[%s]_", owner.getPlainName()), owner.getModel());
        }

        TimeInstant presentTime = owner.presentTime();
        int currentInstanceCount = owner.getInstancesCount();
        double avg = owner.getAverageRelativeUtilization();
        reporter.addDatapoint("MeasuredUtilization", presentTime, avg);

        if (currentInstanceCount < minInstances) { //starts minimum instances
            owner.setInstancesCount(minInstances);
            reporter.addDatapoint("Decision", presentTime, "Spawn");
            reporter.addDatapoint("InstanceChange", presentTime, minInstances-currentInstanceCount);
        } else if (currentInstanceCount > maxInstances) {
            owner.setInstancesCount(maxInstances);
            reporter.addDatapoint("Decision", presentTime, "Despawn");
            reporter.addDatapoint("InstanceChange", presentTime, maxInstances-currentInstanceCount);
        } else if (avg >= upperBound && currentInstanceCount < maxInstances) {
            double upScalingFactor = avg / (upperBound - 0.01);
            int newInstanceCount = Math.min(maxInstances, (int) Math.max(1, Math.ceil(currentInstanceCount * upScalingFactor)));
            owner.scaleToInstancesCount(newInstanceCount);
            lastScaleUp = presentTime;
            reporter.addDatapoint("Decision", presentTime, "Up");
            reporter.addDatapoint("InstanceChange", presentTime, newInstanceCount - currentInstanceCount);
        } else if (avg <= lowerBound
                && currentInstanceCount > minInstances
                && TimeUtil.subtract(presentTime, lastScaleUp).getTimeAsDouble() > holdTime) {
            System.out.println(presentTime);
            System.out.println(lastScaleUp);
            System.out.println(holdTime);
            double downScaleFactor = Math.max(0.01, avg) / lowerBound;
            int newInstanceCount = Math.max(minInstances, (int) Math.max(1, Math.ceil(currentInstanceCount * downScaleFactor)));
            owner.scaleToInstancesCount(newInstanceCount);
            lastScaleUp = presentTime;
            reporter.addDatapoint("Decision", presentTime, "Down");
            reporter.addDatapoint("InstanceChange", presentTime, newInstanceCount - currentInstanceCount);
        } else {
            reporter.addDatapoint("Decision", presentTime, "Hold");
            reporter.addDatapoint("InstanceChange", presentTime, 0);
        }
        if (owner.getInstancesCount() != currentInstanceCount) {
            owner.sendTraceNote(String.format("Changed target instance count to %d", owner.getInstancesCount()));
        }
    }
}
