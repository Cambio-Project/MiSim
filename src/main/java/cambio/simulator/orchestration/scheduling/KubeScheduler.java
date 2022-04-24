package cambio.simulator.orchestration.scheduling;

import cambio.simulator.entities.NamedEntity;
import cambio.simulator.orchestration.Stats;
import cambio.simulator.orchestration.environment.Cluster;
import cambio.simulator.orchestration.environment.Node;
import cambio.simulator.orchestration.environment.Pod;
import cambio.simulator.orchestration.management.ManagementPlane;
import cambio.simulator.orchestration.scheduling.external.KubeJSONCreator;
import cambio.simulator.orchestration.scheduling.external.KubeSchedulerException;
import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class KubeScheduler extends Scheduler {

    static String API_URL = "http://127.0.0.1:8000/";
    static String PATH_PODS = "update/ADD";
    static String PATH_NODES = "updateNodes";

    private static int counter = 1;

    //mirrors the internal cache of running pods that are known by the scheduler
    Set<Pod> internalRunningPods = new HashSet<>();
    static int COUNTER = 1;

    private static final KubeScheduler instance = new KubeScheduler();

    //private constructor to avoid client applications to use constructor
    private KubeScheduler() {
        this.rename("KubeScheduler");

        List<String> nodeList = new ArrayList<>();
        try {
            for (Node node : cluster.getNodes()) {

                String nodeJSON = KubeJSONCreator.createNode(node);
                String watchStreamShellForJSONNode = KubeJSONCreator.createWatchStreamShellForJSONPod(nodeJSON, "ADDED", "Node");
                nodeList.add(watchStreamShellForJSONNode);

            }

            String finalNodeString = "";
            for (String nodeJSON : nodeList) {
                finalNodeString += nodeJSON;
            }
            post(finalNodeString, 0, "", PATH_NODES);
        } catch (IOException e) {
            System.out.println("[INFO]: No connection to API server established. The kube scheduler is not supported in this run");
            //e.printStackTrace();
        }
    }

    public static KubeScheduler getInstance() {
        return instance;
    }


    @Override
    public SchedulerType getSchedulerType() {
        return SchedulerType.KUBE;
    }


    @Override
    public void schedulePods() {
        System.out.println("Call kube-scheduler: " + COUNTER++);
        try {


            List<String> podList = new ArrayList<>();
            Map<String, String> deletedPodMap = new HashMap<>();
            List<String> podNames = new ArrayList<>();
            int numberOfPendingPods = podWaitingQueue.size();



//            //Inform the scheduler that pods have been removed from nodes
            List<Pod> allPodsPlacedOnNodes = ManagementPlane.getInstance().getAllPodsPlacedOnNodes();
            List<Pod> foundToRemove = new ArrayList<>();
            for (Pod pod : internalRunningPods) {
                //If MiSim does not hold the pod from the scheduler cache anymore, tell the scheduler that it was deleted
                if (!allPodsPlacedOnNodes.contains(pod)) {
                    String runningPod = KubeJSONCreator.createPod(pod, true);
                    String deletedWatchStreamShellForJSONPod = KubeJSONCreator.createWatchStreamShellForJSONPod(runningPod, "DELETED", "Pod");
                    podList.add(0, deletedWatchStreamShellForJSONPod);
                    foundToRemove.add(pod);
                    System.out.println("In this iteration the following pod will be removed " + pod.getQuotedName() + " from node " + pod.getLastKnownNode().getQuotedName());
                }
            }
            internalRunningPods.removeAll(foundToRemove);



            //Tell the scheduler that pods are already running on nodes (Scheduled by other schedulers)
            for (Pod pod : allPodsPlacedOnNodes) {
                if (!internalRunningPods.contains(pod)) {
                    String runningPod = KubeJSONCreator.createPod(pod, true);
                    String watchStreamShellForJSONPod = KubeJSONCreator.createWatchStreamShellForJSONPod(runningPod, "ADDED", "Pod");
                    podList.add(0, watchStreamShellForJSONPod);
                    internalRunningPods.add(pod);
                }
            }

            //Add pods from the waiting queue
            while (podWaitingQueue.size() != 0) {
                Pod nextPodFromWaitingQueue = getNextPodFromWaitingQueue();
                podNames.add(nextPodFromWaitingQueue.getName());
                String pendingPod = KubeJSONCreator.createPod(nextPodFromWaitingQueue, false);
                String watchStreamShellForJSONPod = KubeJSONCreator.createWatchStreamShellForJSONPod(pendingPod, "ADDED", "Pod");
                //Already prepare DELETED objects for the watchstream. The API can then give this objects to the scheduler by itself
                String deletedWatchStreamShellForJSONPod = KubeJSONCreator.createWatchStreamShellForJSONPod(pendingPod, "DELETED", "Pod");
                podList.add(watchStreamShellForJSONPod);
                deletedPodMap.put(nextPodFromWaitingQueue.getName(), deletedWatchStreamShellForJSONPod);
            }

            String finalPodString = "";
            for (String podJSON : podList) {
                finalPodString += podJSON;
            }

            if(finalPodString.equals("")){
                return;
            }

            JSONObject response = post(finalPodString, numberOfPendingPods, new JSONObject(deletedPodMap).toString(), PATH_PODS);

            Map<String, Object> responseMap = response.toMap();
            ArrayList<Map<String, String>> bindList = (ArrayList) responseMap.get("bindingList");

            for (Map<String, String> map : bindList) {
                String boundNode = map.get("boundNode");
                String podName = map.get("podName");


                Node candidateNode = ManagementPlane.getInstance().getCluster().getNodeByName(boundNode);
                Pod pod = ManagementPlane.getInstance().getPodByName(podName);

                if (candidateNode == null) {
                    throw new KubeSchedulerException("The node that was selected by the kube-scheduler does not exist in the Simulation");
                } else if (pod == null) {
                    throw new KubeSchedulerException("The pod that was selected by the kube-scheduler does not exist in the Simulation");
                }

                if (!candidateNode.addPod(pod)) {
                    throw new KubeSchedulerException("The selected node has not enough resources to run the selected pod. The kube-scheduler must have calculated wrong");
                }

                internalRunningPods.add(pod);

                //only for reporting
                Stats.NodePodEventRecord record = new Stats.NodePodEventRecord();
                record.setTime((int) presentTime().getTimeAsDouble());
                record.setPodName(podName);
                record.setNodeName(boundNode);
                record.setScheduler("kube");
                record.setEvent("Binding");
                record.setOutcome("Success");
                record.setInfo("N/A");
                record.setDesiredState(pod.getOwner().getDesiredReplicaCount());
                record.setCurrentState(ManagementPlane.getInstance().getAmountOfPodsOnNodes(pod.getOwner()));
                Stats.getInstance().getNodePodEventRecords().add(record);

                System.out.println(podName + " was bound on " + boundNode);

                sendTraceNote(this.getQuotedName() + " has scheduled " + pod.getQuotedName() + " on node " + candidateNode);
            }


            ArrayList<Map<String, String>> failedList = (ArrayList) responseMap.get("failedList");
            for (Map<String, String> map : failedList) {
                String podName = map.get("podName");
                Pod pod = ManagementPlane.getInstance().getPodByName(podName);
                podWaitingQueue.add(pod);

                //only for reporting
                Stats.NodePodEventRecord record = new Stats.NodePodEventRecord();
                record.setTime((int) presentTime().getTimeAsDouble());
                record.setPodName(podName);
                record.setNodeName("N/A");
                record.setScheduler("kube");
                record.setEvent("Binding");
                record.setOutcome("Failed");
                record.setInfo(map.get("status"));
                record.setDesiredState(pod.getOwner().getDesiredReplicaCount());
                record.setCurrentState(ManagementPlane.getInstance().getAmountOfPodsOnNodes(pod.getOwner()));
                Stats.getInstance().getNodePodEventRecords().add(record);

                System.out.println(this.getQuotedName() + " was not able to schedule pod " + pod + ". Reason: " + map.get("status"));
                sendTraceNote(this.getQuotedName() + " was not able to schedule pod " + pod + ". Reason: " + map.get("status"));
                sendTraceNote(this.getQuotedName() + " has send " + pod + " back to the Pod Waiting Queue");
            }
        } catch (IOException | KubeSchedulerException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }


    //    https://www.baeldung.com/httpurlconnection-post
    public JSONObject post(String content, int numberPendingPods, String deletedPods, String path) throws IOException {
        URL url = new URL(API_URL + path);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);


        JSONObject jsonInputString = new JSONObject();
        jsonInputString.put("data", content);
        jsonInputString.put("numberPendingPods", numberPendingPods);
        jsonInputString.put("deletedPods", deletedPods);
        try (OutputStream os = con.getOutputStream()) {
            byte[] input = jsonInputString.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }


        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return new JSONObject(response.toString());
        }

    }

}
