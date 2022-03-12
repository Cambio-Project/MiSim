package cambio.simulator.orchestration.scaling;

import cambio.simulator.orchestration.k8objects.Deployment;
import cambio.simulator.orchestration.management.ManagementPlane;

import java.util.Random;

public class RandomAutoscaler extends AutoScaler {

    public RandomAutoscaler() {
        this.rename("RandomAutoScaler");
        holdTimeUp = 0;
        holdTimeDown = 0;
    }

    @Override
    public void apply(Deployment deployment) {
        final double lastRescaling = deployment.getLastRescaling().getTimeAsDouble();
        final double timePassed = presentTime().getTimeAsDouble() - lastRescaling;
        boolean upscalingAllowed = timePassed >= holdTimeUp;
        boolean downscalingAllowed = timePassed >= holdTimeDown;

        //This allows scaling after initialization is complete and no rescaling was applied before
        if (lastRescaling == 0 && presentTime().getTimeAsDouble() > 0) {
            upscalingAllowed = true;
            downscalingAllowed = true;
        }


        if (upscalingAllowed || downscalingAllowed) {
            int desiredReplicas = random.nextInt((deployment.getMaxReplicaCount() - deployment.getMinReplicaCount()) +1) + deployment.getMinReplicaCount();

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
