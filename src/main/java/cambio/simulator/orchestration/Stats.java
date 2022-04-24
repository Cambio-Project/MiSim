package cambio.simulator.orchestration;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.orchestration.environment.*;
import cambio.simulator.orchestration.k8objects.Deployment;
import cambio.simulator.orchestration.management.ManagementPlane;
import desmoj.core.simulator.Model;

import java.util.*;

import static cambio.simulator.entities.networking.NetworkRequestTimeoutEvent.microservicetimoutmap;
//import static cambio.simulator.entities.patterns.CircuitBreaker.microserviceCanceledMap;


public class Stats {
    //only for MiSim
    Map<Microservice, List<ScalingRecord>> microServiceRecordsMap = new HashMap<>();

    //only in orchestration mode
    Map<Deployment, List<ScalingRecord>> deploymentRecordsMap = new HashMap<>();
    List<SchedulingRecord> schedulingRecords = new ArrayList<>();
    Map<Node, List<NodePodSchedulingRecord>> node2PodMap = new HashMap<>();

    List<NodePodEventRecord> nodePodEventRecords = new ArrayList<>();

    public static class NodePodEventRecord{
        int time;
        String podName;
        String nodeName;
        String scheduler;
        String event;
        String outcome;
        String info;
        int desiredState;
        int currentState;

        public int getTime() {
            return time;
        }

        public void setTime(int time) {
            this.time = time;
        }

        public String getPodName() {
            return podName;
        }

        public void setPodName(String podName) {
            this.podName = podName;
        }

        public String getNodeName() {
            return nodeName;
        }

        public void setNodeName(String nodeName) {
            this.nodeName = nodeName;
        }

        public String getScheduler() {
            return scheduler;
        }

        public void setScheduler(String scheduler) {
            this.scheduler = scheduler;
        }

        public String getEvent() {
            return event;
        }

        public void setEvent(String event) {
            this.event = event;
        }

        public String getInfo() {
            return info;
        }

        public void setInfo(String info) {
            this.info = info;
        }

        public String getOutcome() {
            return outcome;
        }

        public void setOutcome(String outcome) {
            this.outcome = outcome;
        }

        public int getDesiredState() {
            return desiredState;
        }

        public void setDesiredState(int desiredState) {
            this.desiredState = desiredState;
        }

        public int getCurrentState() {
            return currentState;
        }

        public void setCurrentState(int currentState) {
            this.currentState = currentState;
        }
    }


    public static class NodePodSchedulingRecord {
        int time;
        Map<Deployment, Integer> deploymentPodScheduledMap = new HashMap<>();

        public int getTime() {
            return time;
        }

        public void setTime(int time) {
            this.time = time;
        }

        public Map<Deployment, Integer> getDeploymentPodScheduledMap() {
            return deploymentPodScheduledMap;
        }

        public void setDeploymentPodScheduledMap(Map<Deployment, Integer> deploymentPodScheduledMap) {
            this.deploymentPodScheduledMap = deploymentPodScheduledMap;
        }
    }

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
        Map<Microservice, Integer> microservicetimoutmap = new HashMap<>();
        Map<Microservice, Integer> microserviceCanceledMap = new HashMap<>();
        Map<Pod, Double> podDoubleHashMap = new HashMap<>();
        Map<MicroserviceInstance, Double> microserviceInstanceDoubleHashMap = new HashMap<>();


        public ScalingRecord() {
        }

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

        public Map<Microservice, Integer> getMicroservicetimoutmap() {
            return microservicetimoutmap;
        }

        public void setMicroservicetimoutmap(Map<Microservice, Integer> microservicetimoutmap) {
            this.microservicetimoutmap = microservicetimoutmap;
        }

        public Map<Microservice, Integer> getMicroserviceCanceledMap() {
            return microserviceCanceledMap;
        }

        public void setMicroserviceCanceledMap(Map<Microservice, Integer> microserviceCanceledMap) {
            this.microserviceCanceledMap = microserviceCanceledMap;
        }

        public Map<Pod, Double> getPodDoubleHashMap() {
            return podDoubleHashMap;
        }

        public void setPodDoubleHashMap(Map<Pod, Double> podDoubleHashMap) {
            this.podDoubleHashMap = podDoubleHashMap;
        }

        public Map<MicroserviceInstance, Double> getMicroserviceInstanceDoubleHashMap() {
            return microserviceInstanceDoubleHashMap;
        }

        public void setMicroserviceInstanceDoubleHashMap(Map<MicroserviceInstance, Double> microserviceInstanceDoubleHashMap) {
            this.microserviceInstanceDoubleHashMap = microserviceInstanceDoubleHashMap;
        }
    }

    private static final Stats instance = new Stats();

    //private constructor to avoid client applications to use constructor
    private Stats() {
    }

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

    public void createSchedulingStats(Model model) {
        int time = (int) model.presentTime().getTimeAsDouble();
        Stats.SchedulingRecord schedulingRecord = new SchedulingRecord();
        schedulingRecord.setTime(time);
        int totalCPU = 0;
        int toalReserved = 0;
        for (Node node : ManagementPlane.getInstance().getCluster().getNodes()) {
            totalCPU += node.getTotalCPU();
            toalReserved += node.getReserved();
        }

        schedulingRecord.setCapacityTogether(totalCPU);
        schedulingRecord.setReservedTogether(toalReserved);
        schedulingRecord.setAmountPodsOnNodes(ManagementPlane.getInstance().getAllPodsPlacedOnNodes().size());
        schedulingRecord.setAmountPodsWaiting(ManagementPlane.getInstance().getAmountOfWaitingPods());
        schedulingRecords.add(schedulingRecord);

        //Add node2pod records


        List<Deployment> deployments = ManagementPlane.getInstance().getDeployments();
        List<Node> nodes = ManagementPlane.getInstance().getCluster().getNodes();
        for (Node node : nodes) {
            NodePodSchedulingRecord nodePodSchedulingRecord = new NodePodSchedulingRecord();
            nodePodSchedulingRecord.setTime(time);
            Map<Deployment, Integer> deploymentPodScheduledMap = nodePodSchedulingRecord.getDeploymentPodScheduledMap();

            for (Deployment deployment : deployments) {
                deploymentPodScheduledMap.put(deployment, 0);
            }

            for (Pod pod : node.getPods()) {
                Deployment deploymentForPod = pod.getOwner();
                if (deploymentForPod != null) {
                    if (deployments.contains(deploymentForPod)) {
                        if (deploymentPodScheduledMap.get(deploymentForPod) != null) {
                            deploymentPodScheduledMap.put(deploymentForPod, deploymentPodScheduledMap.get(deploymentForPod) + 1);
                        }
                    }
                }
            }

            List<NodePodSchedulingRecord> nodePodSchedulingRecords = node2PodMap.get(node);
            if (nodePodSchedulingRecords != null) {
                nodePodSchedulingRecords.add(nodePodSchedulingRecord);
            } else {
                ArrayList<NodePodSchedulingRecord> schedulingRecords = new ArrayList<>();
                schedulingRecords.add(nodePodSchedulingRecord);
                node2PodMap.put(node, schedulingRecords);
            }
        }


    }

    public void createScalingStats(Model model) {
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

                        //add event info timeoutEvent
                        Microservice owner = container.getMicroserviceInstance().getOwner();
                        Integer integer = microservicetimoutmap.get(owner);
                        if (integer != null) {
                            scalingRecord.getMicroservicetimoutmap().put(owner, Integer.valueOf(integer));
                        } else {
                            scalingRecord.getMicroservicetimoutmap().put(owner, 0);
                        }

//                        //add event info canceledEvent
//                        owner = container.getMicroserviceInstance().getOwner();
//                        integer = microserviceCanceledMap.get(owner);
//                        if (integer != null) {
//                            scalingRecord.getMicroserviceCanceledMap().put(owner, Integer.valueOf(integer));
//                        } else {
//                            scalingRecord.getMicroserviceCanceledMap().put(owner, 0);
//                        }


                        if (container.getContainerState() == ContainerState.RUNNING) {
                            double relativeWorkDemand = container.getMicroserviceInstance().getRelativeWorkDemand();
                            podCPUUtilization += relativeWorkDemand;
                        }
                    }
                    podConsumptions.add(podCPUUtilization);
                    scalingRecord.getPodDoubleHashMap().put(pod, podCPUUtilization);
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

    public Map<Microservice, List<ScalingRecord>> getMicroServiceRecordsMap() {
        return microServiceRecordsMap;
    }

    public void setMicroServiceRecordsMap(Map<Microservice, List<ScalingRecord>> microServiceRecordsMap) {
        this.microServiceRecordsMap = microServiceRecordsMap;
    }

    public Map<Node, List<NodePodSchedulingRecord>> getNode2PodMap() {
        return node2PodMap;
    }

    public void setNode2PodMap(Map<Node, List<NodePodSchedulingRecord>> node2PodMap) {
        this.node2PodMap = node2PodMap;
    }

    public List<NodePodEventRecord> getNodePodEventRecords() {
        return nodePodEventRecords;
    }

    public void setNodePodEventRecords(List<NodePodEventRecord> nodePodEventRecords) {
        this.nodePodEventRecords = nodePodEventRecords;
    }
}
