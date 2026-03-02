package cambio.simulator.entities.patterns;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.export.MultiDataPointReporter;
import cambio.simulator.misc.TimeUtil;
import cambio.simulator.parsing.JsonTypeName;
import desmoj.core.simulator.TimeInstant;

/**
 * AN implementation based on Kubernetes
 * <a href="https://github.com/kubernetes/kubernetes/blob/8caeec429ee1d2a9df7b7a41b21c626346b456fb/docs/design/horizontal-pod-autoscaler.md#autoscaling-algorithm">Horizontal Pod Autoscaler</a>.
 *
 * <p>
 * By default, scale-up can only happen if there was no rescaling within the last 3 minutes. Scale-down will wait for 5
 * minutes from the last rescaling. Moreover, any scaling will only be made if: avg(CurrentPodsConsumption) / Target
 * drops below 0.9 or increases above 1.1 (10% tolerance)
 *
 * <p>
 * TODO Maybe also include via adapter, upscaling/downscaling behavior not 100% as in Kubernetes, e.g. see
 *      <a href="https://github.com/kubernetes/kubernetes/blob/master/pkg/apis/autoscaling/types.go#L113">HorizontalPodAutoscalerBehavior Implementation</a>
 *      In particular, Kubernetes mentions different holdTimes for up- and downscaling, as well as a 30s evaluation
 *      frequency (MiSim probably evaluates every time unit. Thus, reacts faster.)
 */
@JsonTypeName("hpa")
public class HorizontalPodAutoscalingPolicy implements IAutoscalingPolicy {
    private transient MultiDataPointReporter reporter = null;
    private double targetUtilization = 0.8;
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
        double avg2Target = owner.getAverageRelativeUtilization() / targetUtilization;
        // Tolerance area

        int newInstanceCount = (int) Math.ceil(avg2Target * currentInstanceCount);

        newInstanceCount = Math.min(newInstanceCount, maxInstances);
        newInstanceCount = Math.max(minInstances, newInstanceCount);

        if (currentInstanceCount < minInstances) { //starts minimum instances
            owner.setInstancesCount(minInstances);
            reporter.addDatapoint("Decision", presentTime, "Spawn");
            reporter.addDatapoint("InstanceChange", presentTime, minInstances - currentInstanceCount);
        } else if (currentInstanceCount > maxInstances) {
            owner.setInstancesCount(maxInstances);
            reporter.addDatapoint("Decision", presentTime, "Despawn");
            reporter.addDatapoint("InstanceChange", presentTime, maxInstances - currentInstanceCount);
        } else if (avg2Target > 0.9 && avg2Target < 1.1) {
            // Tolerance area
            reporter.addDatapoint("Decision", presentTime, "Hold");
            reporter.addDatapoint("InstanceChange", presentTime, 0);
        } else if (avg2Target > 1 && currentInstanceCount < maxInstances) {
            owner.scaleToInstancesCount(newInstanceCount);
            lastScaleUp = presentTime;
            reporter.addDatapoint("Decision", presentTime, "Up");
            reporter.addDatapoint("InstanceChange", presentTime, newInstanceCount - currentInstanceCount);
        } else if (avg2Target < 1 && currentInstanceCount > minInstances && TimeUtil.subtract(presentTime,
                lastScaleUp).getTimeAsDouble() > holdTime) {
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