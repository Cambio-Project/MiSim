package cambio.simulator.orchestration;

import cambio.simulator.orchestration.environment.*;
import cambio.simulator.orchestration.k8objects.Deployment;
import cambio.simulator.orchestration.management.ManagementPlane;
import desmoj.core.simulator.Model;

import java.util.*;

public class Stats {

    Map<Deployment, List<ScalingRecord>> deploymentRecordsMap = new HashMap<>();
    List<SchedulingRecord> schedulingRecords = new ArrayList<>();

    public static class SchedulingRecord {
        int time;
        int capacityTogether;
        int reservedTogether;
        int amountPodsOnNodes;
        int amountPodsWaiting;

        public int getAmountPodsWaiting() {
            return amountPodsWaiting;
        }

        public void setAmountPodsWaiting(int amountPodsWaiting) {
            this.amountPodsWaiting = amountPodsWaiting;
        }

        public int getAmountPodsOnNodes() {
            return amountPodsOnNodes;
        }

        public void setAmountPodsOnNodes(int amountPodsOnNodes) {
            this.amountPodsOnNodes = amountPodsOnNodes;
        }

        public int getTime() {
            return time;
        }

        public void setTime(int time) {
            this.time = time;
        }

        public int getCapacityTogether() {
            return capacityTogether;
        }

        public void setCapacityTogether(int capacityTogether) {
            this.capacityTogether = capacityTogether;
        }

        public int getReservedTogether() {
            return reservedTogether;
        }

        public void setReservedTogether(int reservedTogether) {
            this.reservedTogether = reservedTogether;
        }
    }

    public static class ScalingRecord {
        int time;
        double avgConsumption;
        int amountPods;

        public ScalingRecord(){}

        public int getTime() {
            return time;
        }

        public void setTime(int time) {
            this.time = time;
        }

        public double getAvgConsumption() {
            return avgConsumption;
        }

        public void setAvgConsumption(double avgConsumption) {
            this.avgConsumption = avgConsumption;
        }

        public int getAmountPods() {
            return amountPods;
        }

        public void setAmountPods(int amountPods) {
            this.amountPods = amountPods;
        }
    }

    private static final Stats instance = new Stats();

    //private constructor to avoid client applications to use constructor
    private Stats() {}

    public static Stats getInstance() {
        return instance;
    }

    public Map<Deployment, List<ScalingRecord>> getDeploymentRecordsMap() {
        return deploymentRecordsMap;
    }

    public List<SchedulingRecord> getSchedulingRecords() {
        return schedulingRecords;
    }

    public void setSchedulingRecords(List<SchedulingRecord> schedulingRecords) {
        this.schedulingRecords = schedulingRecords;
    }

    public void setDeploymentRecordsMap(Map<Deployment, List<ScalingRecord>> deploymentRecordsMap) {
        this.deploymentRecordsMap = deploymentRecordsMap;
    }

    public void createSchedulingStats(Model model){
        int time = (int) model.presentTime().getTimeAsDouble();
        Stats.SchedulingRecord schedulingRecord = new SchedulingRecord();
        schedulingRecord.setTime(time);
        int totalCPU = 0;
        int toalReserved =0;
        for (Node node : ManagementPlane.getInstance().getCluster().getNodes()) {
            totalCPU += node.getTotalCPU();
            toalReserved += node.getReserved();
        }

        schedulingRecord.setCapacityTogether(totalCPU);
        schedulingRecord.setReservedTogether(toalReserved);
        schedulingRecord.setAmountPodsOnNodes(ManagementPlane.getInstance().getAllPodsPlacedOnNodes().size());
        schedulingRecord.setAmountPodsWaiting(ManagementPlane.getInstance().getAmountOfWaitingPods());
        schedulingRecords.add(schedulingRecord);
    }

    public void createScalingStats(Model model){
        int time = (int) model.presentTime().getTimeAsDouble();

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
