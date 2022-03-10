package cambio.simulator.orchestration.scaling;

import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.orchestration.environment.*;
import cambio.simulator.orchestration.k8objects.Deployment;

import java.util.ArrayList;
import java.util.List;

public class HorizontalPodAutoscaler extends AutoScaler {


    public HorizontalPodAutoscaler() {
        this.rename("HPA");
    }

    @Override
    public void apply(Deployment deployment) {
        //https://github.com/kubernetes/kubernetes/blob/8caeec429ee1d2a9df7b7a41b21c626346b456fb/docs/design/horizontal-pod-autoscaler.md#autoscaling-algorithm
//        Scale-up can only happen if there was no rescaling within the last 3 minutes. Scale-down will wait for 5 minutes from the last rescaling.
//        Moreover any scaling will only be made if: avg(CurrentPodsConsumption) / Target drops below 0.9 or increases above 1.1 (10% tolerance)

        final double lastRescaling = deployment.getLastRescaling().getTimeAsDouble();

        final double timePassed = presentTime().getTimeAsDouble() - lastRescaling;
        boolean upscalingAllowed = timePassed >= holdTimeUp;
        boolean downscalingAllowed = timePassed >= holdTimeDown;

        //This allows scaling after initialization is complete and no rescaling was applied before
        if (lastRescaling == 0 && presentTime().getTimeAsDouble() > 0) {
            upscalingAllowed = true;
            downscalingAllowed = true;
        }

        double target = deployment.getAverageUtilization(); //in percent
        target = target / 100;
        if (upscalingAllowed || downscalingAllowed) {
            List<Double> podConsumptions = new ArrayList<>();
            for (Pod pod : deployment.getReplicaSet()) {
                if (pod.getPodState() == PodState.RUNNING) {
                    double podCPUUtilization = 0;
                    for (Container container : pod.getContainers()) {
                        if (container.getContainerState() == ContainerState.RUNNING) {
                            double relativeWorkDemand = container.getMicroserviceInstance().getRelativeWorkDemand();
                            podCPUUtilization += relativeWorkDemand;
                        }
                    }
                    podConsumptions.add(podCPUUtilization);
                }
            }
            double avg2Target = podConsumptions.stream().mapToDouble(d -> d).average().orElse(0) / target;
            if (avg2Target > 0.9 && avg2Target < 1.1) {
                sendTraceNote("No Scaling required for " + deployment + ".");
                return;
            }

            double sumOfRelativeCPUUsage = podConsumptions.stream().mapToDouble(d-> d).sum();

            int desiredReplicas = (int) Math.ceil((sumOfRelativeCPUUsage / target));

            desiredReplicas = Math.min(desiredReplicas, deployment.getMaxReplicaCount());
            desiredReplicas = Math.max(deployment.getMinReplicaCount(), desiredReplicas);


            if (desiredReplicas != deployment.getCurrentRunningOrPendingReplicaCount()) {
                if (desiredReplicas > deployment.getCurrentRunningOrPendingReplicaCount()) {
                    if (upscalingAllowed) {
                        deployment.setDesiredReplicaCount(desiredReplicas);
                        deployment.setLastRescaling(presentTime());
                        sendTraceNote("Scaling Up " + deployment + ". From " + deployment.getCurrentRunningOrPendingReplicaCount() + " -> " + desiredReplicas);
                    } else {
                        sendTraceNote("Up scaling not allowed for " + deployment + " due to hold time.");
                    }
                } else {
                    if (downscalingAllowed) {
                        deployment.setDesiredReplicaCount(desiredReplicas);
                        deployment.setLastRescaling(presentTime());
                        sendTraceNote("Scaling Down " + deployment + ". From " + deployment.getCurrentRunningOrPendingReplicaCount() + " -> " + desiredReplicas);
                    } else {
                        sendTraceNote("Down scaling not allowed for " + deployment + " due to hold time.");
                    }
                }
            } else {
                sendTraceNote("No Scaling required for " + deployment + ".");
            }
        } else {
            if (presentTime().getTimeAsDouble() == 0) {
                sendTraceNote("No scaling allowed for " + deployment + " during initialization.");
            } else {
                sendTraceNote("No scaling allowed for " + deployment + " due to hold times.");
            }
        }
    }
}
