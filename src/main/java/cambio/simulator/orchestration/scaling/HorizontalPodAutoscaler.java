package cambio.simulator.orchestration.scaling;

import cambio.simulator.entities.NamedEntity;
import cambio.simulator.orchestration.ManagementPlane;
import cambio.simulator.orchestration.environment.*;
import cambio.simulator.orchestration.events.DeploymentEvent;
import cambio.simulator.orchestration.k8objects.Deployment;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

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
        Double target = 50.0; //in percent

        if (upscalingAllowed || downscalingAllowed) {
            double sumOfRelativeCPUUsage = 0;
            for (Pod pod : deployment.getReplicaSet()) {
                if (pod.getPodState() == PodState.RUNNING) {
                    for (Container container : pod.getContainers()) {
                        if (container.getContainerState() == ContainerState.RUNNING) {
                            //TODO get the right metric - connection to CPUrequest in yaml file?
                            final double relativeWorkDemand = container.getMicroserviceInstance().getRelativeWorkDemand();
                            sumOfRelativeCPUUsage += relativeWorkDemand;
                        }
                    }
                }
            }
            int desiredReplicas = (int) Math.ceil((sumOfRelativeCPUUsage / target));

            if (desiredReplicas > 0) {
                desiredReplicas = Math.min(desiredReplicas, deployment.getMaxReplicaCount());
            } else {
                desiredReplicas = deployment.getMinReplicaCount();
            }

            if (desiredReplicas != deployment.getCurrentRunningOrPendingReplicaCount()) {
                deployment.setDesiredReplicaCount(desiredReplicas);
                if (desiredReplicas > deployment.getCurrentRunningOrPendingReplicaCount()) {
                    if (upscalingAllowed) {
                        deployment.setLastScaleUp(presentTime());
                        sendTraceNote("Scaling Up " + deployment + ". From " + deployment.getCurrentRunningOrPendingReplicaCount() + " -> " + desiredReplicas);
                        final DeploymentEvent deploymentEvent = new DeploymentEvent(getModel(), String.format("Starting with deployment of %s ", deployment.getQuotedName()), traceIsOn());
                        deploymentEvent.schedule(deployment, presentTime());
                    } else {
                        sendTraceNote("Up scaling not allowed for " + deployment + " due to hold time.");
                    }
                }
                if (desiredReplicas < deployment.getCurrentRunningOrPendingReplicaCount()) {
                    if (downscalingAllowed) {
                        deployment.setLastScaleDown(presentTime());
                        sendTraceNote("Scaling Down " + deployment + ". From " + deployment.getCurrentRunningOrPendingReplicaCount() + " -> " + desiredReplicas);
                        final DeploymentEvent deploymentEvent = new DeploymentEvent(getModel(), String.format("Starting with deployment of %s ", deployment.getQuotedName()), traceIsOn());
                        deploymentEvent.schedule(deployment, presentTime());
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
    //regard any scaling will only be made if: avg(CurrentPodsConsumption) / Target drops below 0.9 or increases above 1.1 (10% tolerance) from https://github.com/kubernetes/kubernetes/blob/8caeec429ee1d2a9df7b7a41b21c626346b456fb/docs/design/horizontal-pod-autoscaler.md#autoscaling-algorithm
}
