package cambio.simulator.orchestration.scheduling;

import cambio.simulator.entities.NamedEntity;
import cambio.simulator.orchestration.environment.Cluster;
import cambio.simulator.orchestration.environment.Node;
import cambio.simulator.orchestration.environment.Pod;
import cambio.simulator.orchestration.management.ManagementPlane;
import cambio.simulator.orchestration.scheduling.external.KubeJSONCreator;
import cambio.simulator.orchestration.scheduling.external.KubeSchedulerException;
import org.json.JSONObject;

import java.io.*;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class KubeScheduler extends NamedEntity implements IScheduler {

    static String API_URL = "http://127.0.0.1:8000/update/";
    static String PATH_PODS = "pods";
    static String PATH_NODES = "nodes";

    private static int counter = 1;

    Cluster cluster;
    List<Pod> podWaitingQueue = new ArrayList<>();

    private static final KubeScheduler instance = new KubeScheduler();

    //private constructor to avoid client applications to use constructor
    private KubeScheduler() {
        super(ManagementPlane.getInstance().getModel(), "KubeScheduler", ManagementPlane.getInstance().getModel().traceIsOn());
        this.cluster = ManagementPlane.getInstance().getCluster();


        try {
            String nodeList = KubeJSONCreator.createNodeList(cluster.getNodes());
            post(nodeList, 0, PATH_NODES);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static KubeScheduler getInstance() {
        return instance;
    }


    @Override
    public SchedulerType getSchedulerType() {
        return SchedulerType.KUBE;
    }

    //TODO Plugins für Scheduler testen, schreiben
    @Override
    public void schedulePods() {
        if (podWaitingQueue.isEmpty()){
            sendTraceNote(this.getQuotedName() + " has no pods left for scheduling");
            return;
        }
        try {
            String[] cmd_Scheduler = {"/bin/sh", "-c", "cd /home/ittaq/Documents/Uni/Masterarbeit/scheduler; ./kube-scheduler --master 127.0.0.1:8000 --config config.txt"};
            Process process_Scheduler = Runtime.getRuntime().exec(cmd_Scheduler);

            List<String> podList = new ArrayList<>();
            int numberOfPendingPods = podWaitingQueue.size();
            while (podWaitingQueue.size() != 0) {
                String pendingPod = KubeJSONCreator.createPod(getNextPodFromWaitingQueue(), false);
                podList.add(pendingPod);
            }

            for (Pod pod : ManagementPlane.getInstance().getAllPodsPlacedOnNodes()) {
                String runningPod = KubeJSONCreator.createPod(pod, true);
                podList.add(runningPod);
            }

            if (!podList.isEmpty()) {
                String podListTemplateString = KubeJSONCreator.getPodListTemplate();
                podListTemplateString = podListTemplateString.replace("TEMPLATE_RESOURCE_VERSION", String.valueOf(counter++));
                podListTemplateString = podListTemplateString.replace("TEMPLATE_POD_LIST", podList.toString());
                JSONObject response = post(podListTemplateString, numberOfPendingPods, PATH_PODS);

                Map<String, Object> responseMap = response.toMap();
                ArrayList<Map<String, String>> bindList = (ArrayList) responseMap.get("bindingList");
                for (Map<String, String> map : bindList) {
                    String bindedNode = map.get("bindedNode");
                    String podName = map.get("podName");


                    Node candidateNode = ManagementPlane.getInstance().getCluster().getNodeByName(bindedNode);
                    Pod pod = ManagementPlane.getInstance().getPodByName(podName);

                    if (candidateNode == null) {
                        throw new KubeSchedulerException("The node that was selected by the kube-scheduler does not exist in the Simulation");
                    } else if (pod == null) {
                        throw new KubeSchedulerException("The pod that was selected by the kube-scheduler does not exist in the Simulation");
                    }

                    if (!candidateNode.addPod(pod)) {
                        throw new KubeSchedulerException("The selected node has not enough resources to run the selected pod. The kube-scheduler must have calculated wrong");
                    }
                    sendTraceNote(this.getQuotedName() + " has deployed " + pod.getQuotedName() + " on node " + candidateNode);
                }


                ArrayList<Map<String, String>> failedList = (ArrayList) responseMap.get("failedList");
                for (Map<String, String> map : failedList) {
                    String podName = map.get("podName");
                    Pod pod = ManagementPlane.getInstance().getPodByName(podName);
                    podWaitingQueue.add(pod);
                    sendTraceNote(this.getQuotedName() + " was not able to schedule pod " + pod + ". Insufficient resources!");
                    sendTraceNote(this.getQuotedName() + " has send " + pod + " back to the Pod Waiting Queue");
                }
            }

            String[] cmd = {"/bin/sh", "-c", "kill `pidof kube-scheduler`"};
            Runtime.getRuntime().exec(cmd);

            process_Scheduler.destroy();

        } catch (IOException | KubeSchedulerException e) {
            String[] cmd = {"/bin/sh", "-c", "kill `pidof kube-scheduler`"};
            try {
                Runtime.getRuntime().exec(cmd);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            System.exit(1);
        }
    }

    //    https://www.baeldung.com/httpurlconnection-post
    public JSONObject post(String content, int numberPendingPods, String path) throws IOException {
        URL url = new URL(API_URL + path);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);


        JSONObject jsonInputString = new JSONObject();
        jsonInputString.put("data", content);
        jsonInputString.put("numberPendingPods", numberPendingPods);
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

    @Override
    public Pod getNextPodFromWaitingQueue() {
        if (!podWaitingQueue.isEmpty()) {
            Pod pod = podWaitingQueue.get(0);
            podWaitingQueue.remove(pod);
            return pod;
        }
        return null;
    }

    @Override
    public List<Pod> getPodWaitingQueue() {
        return podWaitingQueue;
    }

}
