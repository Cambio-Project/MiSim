package cambio.simulator.orchestration.scheduling;

import cambio.simulator.entities.NamedEntity;
import cambio.simulator.orchestration.environment.Cluster;
import cambio.simulator.orchestration.environment.Pod;
import cambio.simulator.orchestration.management.ManagementPlane;
import cambio.simulator.orchestration.scheduling.external.KubeJSONCreator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class KubeScheduler implements IScheduler {

    static String API_URL = "http://127.0.0.1:8000/update/";

    Cluster cluster;
    LinkedList<Pod> podWaitingQueue = new LinkedList<>();

    private static final KubeScheduler instance = new KubeScheduler();

    //private constructor to avoid client applications to use constructor
    private KubeScheduler() {
//        super(ManagementPlane.getInstance().getModel(), "KubeScheduler", ManagementPlane.getInstance().getModel().traceIsOn());
        this.cluster = ManagementPlane.getInstance().getCluster();


        try {
            String nodeList = KubeJSONCreator.createNodeList(cluster.getNodes());
            post(nodeList, "nodes");


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

    @Override
    public void schedulePods() {

        try {
            List pendingPods = new ArrayList<>();
            while (podWaitingQueue.peek() != null) {
                String pendingPod = KubeJSONCreator.createPendingPod(getNextPodFromWaitingQueue());
                pendingPods.add(pendingPod);
            }

            //TODO braucht auch alle anderen platzierten pods

            if (!pendingPods.isEmpty()) {
                String podListTemplateString = KubeJSONCreator.getPodListTemplate();
                podListTemplateString = podListTemplateString.replace("TEMPLATE_POD_LIST", pendingPods.toString());

                post(podListTemplateString, "pods");
            }



            //TODO Lese neue Zuordnung aus
            //TODO Füge nicht geschedulte Pods wieder in queue

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
//    https://www.baeldung.com/httpurlconnection-post
    public void post(String content, String path) throws IOException {
        URL url = new URL(API_URL + path);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);


        JSONObject jsonInputString = new JSONObject();
        jsonInputString.put("data", content);
        try (OutputStream os = con.getOutputStream()) {
            byte[] input = jsonInputString.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }


        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(con.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            System.out.println(response);
        }

    }

    @Override
    public Pod getNextPodFromWaitingQueue() {
        return podWaitingQueue.poll();
    }

    @Override
    public LinkedList<Pod> getPodWaitingQueue() {
        return podWaitingQueue;
    }

    public static void main(String[] args) {
        KubeScheduler.getInstance().schedulePods();
    }
}
