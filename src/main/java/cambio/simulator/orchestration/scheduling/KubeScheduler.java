package cambio.simulator.orchestration.scheduling;

import cambio.simulator.entities.NamedEntity;
import cambio.simulator.orchestration.environment.Cluster;
import cambio.simulator.orchestration.environment.Pod;
import cambio.simulator.orchestration.management.ManagementPlane;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;

public class KubeScheduler implements IScheduler{

    Cluster cluster;
    LinkedList<Pod> podWaitingQueue = new LinkedList<>();

    private static final KubeScheduler instance = new KubeScheduler();

    //private constructor to avoid client applications to use constructor
    private KubeScheduler() {
//        super(ManagementPlane.getInstance().getModel(), "KubeScheduler", ManagementPlane.getInstance().getModel().traceIsOn());
        this.cluster = ManagementPlane.getInstance().getCluster();
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
            URL url = new URL ("http://localhost:3200/api/scheduler/schedule");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);


            JSONObject jsonInputString = new JSONObject();
            jsonInputString.put("title", "node_test");


            try(OutputStream os = con.getOutputStream()) {
                byte[] input = jsonInputString.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            try(BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                System.out.println(response.toString());

                JSONObject jsonObj = new JSONObject(response.toString());
                System.out.println("Will schedule pod on node: " + jsonObj.get("node"));
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public Pod getNextPodFromWaitingQueue() {
        return null;
    }

    @Override
    public LinkedList<Pod> getPodWaitingQueue() {
        return podWaitingQueue;
    }

    public static void main(String[] args) {
        KubeScheduler.getInstance().schedulePods();
    }
}
