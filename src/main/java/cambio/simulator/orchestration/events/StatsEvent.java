package cambio.simulator.orchestration.events;

import cambio.simulator.entities.NamedExternalEvent;
import cambio.simulator.misc.Priority;
import cambio.simulator.orchestration.Stats;
import cambio.simulator.orchestration.environment.Container;
import cambio.simulator.orchestration.environment.ContainerState;
import cambio.simulator.orchestration.environment.Pod;
import cambio.simulator.orchestration.environment.PodState;
import cambio.simulator.orchestration.k8objects.Deployment;
import cambio.simulator.orchestration.management.ManagementPlane;
import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Model;

import java.util.*;

public class StatsEvent extends NamedExternalEvent {


    public StatsEvent(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
        this.setSchedulingPriority(Priority.HIGH);
    }

    @Override
    public void eventRoutine() throws SuspendExecution {

        Map<Deployment, List<Stats.ScalingRecord>> deploymentRecordsMap = Stats.getInstance().getDeploymentRecordsMap();
        int time = (int) getModel().presentTime().getTimeAsDouble();

        List<Deployment> deployments = ManagementPlane.getInstance().getDeployments();
        for (Deployment deployment : deployments) {
            List<Double> podConsumptions = new ArrayList<>();

            Stats.ScalingRecord scalingRecord = new Stats.ScalingRecord();
            scalingRecord.setTime(time);

            for (Pod pod : deployment.getRunningReplicas()) {
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
            double avg = podConsumptions.stream().mapToDouble(d -> d).average().orElse(0);
//            sendTraceNote("Average for  " + deployment.getQuotedName() + " has the current work demand: " + avg);
            scalingRecord.setAvgConsumption(avg);
            scalingRecord.setAmountPods(deployment.getRunningReplicas().size());

            List<Stats.ScalingRecord> scalingRecords = deploymentRecordsMap.get(deployment);
            if (scalingRecords != null) {
                scalingRecords.add(scalingRecord);
            } else {
                ArrayList<Stats.ScalingRecord> scalingRecordList = new ArrayList<>();
                scalingRecordList.add(scalingRecord);
                deploymentRecordsMap.put(deployment, scalingRecordList);
            }
        }
    }
}
