package cambio.simulator.orchestration.scaling;

import cambio.simulator.entities.NamedEntity;
import cambio.simulator.orchestration.management.ManagementPlane;
import cambio.simulator.orchestration.environment.*;
import cambio.simulator.orchestration.events.DeploymentEvent;
import cambio.simulator.orchestration.k8objects.Deployment;

import java.util.ArrayList;
import java.util.List;

public class HorizontalPodAutoscaler extends NamedEntity implements IAutoScaler {

    private final double holdTimeUp = 5;
    private final double holdTimeDown = 5;
    private static final HorizontalPodAutoscaler instance = new HorizontalPodAutoscaler();

    //private constructor to avoid client applications to use constructor
    private HorizontalPodAutoscaler() {
        super(ManagementPlane.getInstance().getModel(), "HPA", ManagementPlane.getInstance().getModel().traceIsOn());
    }

    public static HorizontalPodAutoscaler getInstance() {
        return instance;
    }

    @Override
    public void apply(Deployment deployment) {
        //https://github.com/kubernetes/kubernetes/blob/8caeec429ee1d2a9df7b7a41b21c626346b456fb/docs/design/horizontal-pod-autoscaler.md#autoscaling-algorithm
//        Scale-up can only happen if there was no rescaling within the last 3 minutes. Scale-down will wait for 5 minutes from the last rescaling.
//        Moreover any scaling will only be made if: avg(CurrentPodsConsumption) / Target drops below 0.9 or increases above 1.1 (10% tolerance)

        boolean upscalingAllowed = false;
        boolean downscalingAllowed = false;

        final double timeAsDouble = presentTime().getTimeAsDouble();
        if (timeAsDouble - deployment.getLastScaleUp().getTimeAsDouble() > holdTimeUp) {
            upscalingAllowed = true;
        }
        if (timeAsDouble - deployment.getLastScaleDown().getTimeAsDouble() > holdTimeDown) {
            downscalingAllowed = true;
        }
        double target = deployment.getAverageUtilization() > 0? deployment.getAverageUtilization() : 1.0 ; //in percent

        if (upscalingAllowed || downscalingAllowed) {
            double sumOfRelativeCPUUsage = 0;
            List<Double> podConsumptions = new ArrayList<>();
            for (Pod pod : deployment.getReplicaSet()) {
                if (pod.getPodState() == PodState.RUNNING) {
                    double podCPUUtiization = 0;
                    for (Container container : pod.getContainers()) {
                        if (container.getContainerState() == ContainerState.RUNNING) {
                            double relativeWorkDemand = container.getMicroserviceInstance().getRelativeWorkDemand();
                            podCPUUtiization += relativeWorkDemand;
                        }
                    }
                    podConsumptions.add(podCPUUtiization);
                    sumOfRelativeCPUUsage += podCPUUtiization;
                }
            }
            double avg2Target = podConsumptions.stream().mapToDouble(d -> d).average().orElse(0) / target;
            System.out.println("-----NEW-----------");
            System.out.println("sumOfRelativeCPUUsage: "+sumOfRelativeCPUUsage);
            if (avg2Target > 0.9 && avg2Target < 1.1) {
                sendTraceNote("No Scaling required for " + deployment + ".");
                System.out.println("avg2Target prohibits scaling: "+avg2Target);
                return;
            }

            int desiredReplicas = (int) Math.ceil((sumOfRelativeCPUUsage / target));

            desiredReplicas = Math.min(desiredReplicas, deployment.getMaxReplicaCount());
            desiredReplicas = Math.max(deployment.getMinReplicaCount(), desiredReplicas);


            if (desiredReplicas != deployment.getCurrentRunningOrPendingReplicaCount()) {
                if (desiredReplicas > deployment.getCurrentRunningOrPendingReplicaCount()) {
                    if (upscalingAllowed) {
                        deployment.setDesiredReplicaCount(desiredReplicas);
                        deployment.setLastScaleUp(presentTime());
                        sendTraceNote("Scaling Up " + deployment + ". From " + deployment.getCurrentRunningOrPendingReplicaCount() + " -> " + desiredReplicas);
                    } else {
                        sendTraceNote("Up scaling not allowed for " + deployment + " due to hold time.");
                    }
                } else {
                    if (downscalingAllowed) {
                        deployment.setDesiredReplicaCount(desiredReplicas);
                        deployment.setLastScaleDown(presentTime());
                        sendTraceNote("Scaling Down " + deployment + ". From " + deployment.getCurrentRunningOrPendingReplicaCount() + " -> " + desiredReplicas);
                    } else {
                        sendTraceNote("Down scaling not allowed for " + deployment + " due to hold time.");
                    }
                }
            } else {
                sendTraceNote("No Scaling required for " + deployment + ".");
            }
        } else {
            sendTraceNote("No Scaling allowed for " + deployment + " due to hold times.");
        }
    }
}
