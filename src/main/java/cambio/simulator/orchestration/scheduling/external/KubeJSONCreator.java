package cambio.simulator.orchestration.scheduling.external;

import cambio.simulator.entities.microservice.Operation;
import cambio.simulator.orchestration.environment.Container;
import cambio.simulator.orchestration.environment.Node;
import cambio.simulator.orchestration.environment.Pod;
import cambio.simulator.orchestration.k8objects.Affinity;
import cambio.simulator.orchestration.k8objects.Deployment;
import cambio.simulator.orchestration.management.ManagementPlane;
import com.google.gson.Gson;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class KubeJSONCreator {

    private static Map<String, String> getMapFromJSON(String jsonString) throws IOException {
        // create Gson instance
        Gson gson = new Gson();
        // create a reader
        // convert JSON file to map
        Map<String, String> map = gson.fromJson(jsonString, Map.class);
        // close reader
        return map;
    }

    private static String getFileContent(String path) throws IOException {
        String actual = Files.readString(Path.of(path));
        return actual;
    }

    public static String getPodListTemplate() throws IOException {
        String podlistTemplateString = getFileContent("src/main/java/cambio/simulator/orchestration/scheduling/external/podlist.json");
        return podlistTemplateString;
    }


    public static String createPod(Pod pod, boolean running) throws IOException, KubeSchedulerException {
        List containers = new ArrayList<>();
        for (Container container : pod.getContainers()) {
            String plainName = container.getMicroserviceInstance().getOwner().getPlainName();
            int requests = pod.getCPUDemand();
            String containerTemplateString = getFileContent("src/main/java/cambio/simulator/orchestration/scheduling/external/container.json");
            containerTemplateString = containerTemplateString.replace("TEMPLATE_CONTAINER_NAME", plainName);
            containerTemplateString = containerTemplateString.replace("TEMPLATE_REQUESTS", String.valueOf(requests));
            containers.add(containerTemplateString);
        }

        String podTemplateString = getFileContent("src/main/java/cambio/simulator/orchestration/scheduling/external/pod.json");


        if (running) {
            String runningStatus = getFileContent("src/main/java/cambio/simulator/orchestration/scheduling/external/status_running.json");
            Optional<Node> nodeForPod = ManagementPlane.getInstance().getNodeForPod(pod);
            if (nodeForPod.isPresent()) {
                Node node = nodeForPod.get();
                runningStatus = runningStatus.replace("TEMPLATE_HOST_IP", node.getNodeIpAddress());
                podTemplateString = podTemplateString.replace("TEMPLATE_STATUS", runningStatus);
                podTemplateString = podTemplateString.replace("TEMPLATE_NODE_NAME", "\"nodeName\": \"" + node.getPlainName() + "\",");
            } else {
                throw new KubeSchedulerException("Could not find the node where " + pod.getName() + " is running on");
            }
        } else {
            String pendingStatus = getFileContent("src/main/java/cambio/simulator/orchestration/scheduling/external/status_pending.json");
            podTemplateString = podTemplateString.replace("TEMPLATE_STATUS", pendingStatus);
            podTemplateString = podTemplateString.replace("TEMPLATE_NODE_NAME", "");
        }

        Deployment deploymentForPod = ManagementPlane.getInstance().getDeploymentForPod(pod);
        Affinity affinity = deploymentForPod.getAffinity();
        Set<String> nodeAffinities = affinity.getNodeAffinities();
        if (affinity.getKey() != null && !nodeAffinities.isEmpty()) {
            String affinityTemplateString = getFileContent("src/main/java/cambio/simulator/orchestration/scheduling/external/affinity.json");

            String nodeAffinitiesString = "[";

            for (String nodeAffinity : nodeAffinities) {
                nodeAffinitiesString += '"' + nodeAffinity + '"' + ",";
            }
            nodeAffinitiesString = nodeAffinitiesString.substring(0, nodeAffinitiesString.length() - 1);
            nodeAffinitiesString += "]";

            affinityTemplateString = affinityTemplateString.replace("TEMPLATE_NODE_NAME", nodeAffinitiesString);
            affinityTemplateString = affinityTemplateString.replace("TEMPLATE_KEY", affinity.getKey());

            podTemplateString = podTemplateString.replace("TEMPLATE_NODE_AFFINITY", affinityTemplateString);
        } else {
            podTemplateString = podTemplateString.replace("TEMPLATE_NODE_AFFINITY", "{}");
        }


        podTemplateString = podTemplateString.replace("TEMPLATE_CONTAINERS", containers.toString());
        podTemplateString = podTemplateString.replace("TEMPLATE_NAME", pod.getName());
        podTemplateString = podTemplateString.replace("TEMPLATE_UID", pod.getName());
        podTemplateString = podTemplateString.replace("TEMPLATE_CONTAINERS", containers.toString());

        return podTemplateString;

    }

    public static String createWatchStreamShellForJSONPod(String podAsJson, String type) throws IOException {
        String watchStreamShell = getFileContent("src/main/java/cambio/simulator/orchestration/scheduling/external/watchStreamShell.json");
        podAsJson = podAsJson.replaceFirst("\\{", "");
        podAsJson = podAsJson.substring(0, podAsJson.lastIndexOf("}"));
        watchStreamShell = watchStreamShell.replace("TEMPLATE_TYPE", type);
        watchStreamShell = watchStreamShell.replace("TEMPLATE_POD", podAsJson);


        if (type.equals("DELTED")) {
            int position = watchStreamShell.indexOf("'creationTimestamp': '2022-01-26T14:02:05Z'");
            StringBuilder sb = new StringBuilder(watchStreamShell);
            sb.insert(position, ",\"deletionTimestamp\": \"2022-01-26T14:04:05Z\"," +
                    "\"deletionGracePeriodSeconds\": 0");
            watchStreamShell = sb.toString();

        }

        return watchStreamShell;

    }

    public static String createNodeList(List<Node> nodes) throws IOException {
//        TEMPLATE_NAME
//        TEMPLATE_UID
//        TEMPLATE_CPU
//        TEMPLATE_IP_ADDRESS
//        TEMPLATE_MACHINE_ID


        ArrayList<String> nodeList = new ArrayList<>();

        for (Node node : nodes) {

            String name = node.getPlainName();
            String cpu = String.valueOf(node.getTotalCPU());
            String nodeIpAddress = node.getNodeIpAddress();
            String machineId = "MachineID-" + name;
            String nodeTemplateString = getFileContent("src/main/java/cambio/simulator/orchestration/scheduling/external/node.json");
            nodeTemplateString = nodeTemplateString.replace("TEMPLATE_NAME", name);
            nodeTemplateString = nodeTemplateString.replace("TEMPLATE_UID", name);
            nodeTemplateString = nodeTemplateString.replace("TEMPLATE_CPU", cpu);
            nodeTemplateString = nodeTemplateString.replace("TEMPLATE_IP_ADDRESS", nodeIpAddress);
            nodeTemplateString = nodeTemplateString.replace("TEMPLATE_MACHINE_ID", machineId);
            nodeList.add(nodeTemplateString);
        }
        String nodeListTemplateString = getFileContent("src/main/java/cambio/simulator/orchestration/scheduling/external/nodeList.json");
        nodeListTemplateString = nodeListTemplateString.replace("TEMPLATE_NODES", nodeList.toString());
        return nodeListTemplateString;
    }

    public static void main(String[] args) throws FileNotFoundException {
//        String pendingPod = createPendingPod(null);
    }
}
