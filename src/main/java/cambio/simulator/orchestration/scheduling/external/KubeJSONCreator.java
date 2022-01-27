package cambio.simulator.orchestration.scheduling.external;

import cambio.simulator.entities.microservice.Operation;
import cambio.simulator.orchestration.environment.Container;
import cambio.simulator.orchestration.environment.Node;
import cambio.simulator.orchestration.environment.Pod;
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


//    private static Map<String, String> getMapFromJSON(String path) throws IOException {
//        // create Gson instance
//        Gson gson = new Gson();
//        // create a reader
//        Reader reader = Files.newBufferedReader(Paths.get(path));
//        // convert JSON file to map
//        Map<String, String> map = gson.fromJson(reader, Map.class);
//        // close reader
//        reader.close();
//        return map;
//    }

    private static String getFileContent(String path) throws IOException {
        String actual = Files.readString(Path.of(path));
        return actual;
    }

    public static String getPodListTemplate() throws IOException {
        String podlistTemplateString = getFileContent("src/main/java/cambio/simulator/orchestration/scheduling/external/podlist.json");
        return podlistTemplateString;
    }


    public static String createPendingPod(Pod pod) throws IOException {
        List containers = new ArrayList<>();
        for (Container container : pod.getContainers()) {
            String plainName = container.getMicroserviceInstance().getOwner().getPlainName();
            int requests = container.calculateRequests();
            String containerTemplateString = getFileContent("src/main/java/cambio/simulator/orchestration/scheduling/external/container.json");
            containerTemplateString = containerTemplateString.replace("TEMPLATE_CONTAINER_NAME", plainName);
            containerTemplateString = containerTemplateString.replace("TEMPLATE_REQUESTS", String.valueOf(requests));
            containers.add(containerTemplateString);
        }

        String podTemplateString = getFileContent("src/main/java/cambio/simulator/orchestration/scheduling/external/pod.json");
        podTemplateString = podTemplateString.replace("TEMPLATE_NAME", pod.getName());
        podTemplateString = podTemplateString.replace("TEMPLATE_UID", pod.getName());
        podTemplateString = podTemplateString.replace("TEMPLATE_CONTAINERS", containers.toString());

        return podTemplateString;

    }

    public static String createNodeList(List<Node> nodes) throws IOException {
//        TEMPLATE_NAME
//        TEMPLATE_UID
//        TEMPLATE_CPU
//        TEMPLATE_IP_ADDRESS
//        TEMPLATE_MACHINE_ID

        String ipAddress = "192.168.49.";
        int counter = 1;

        ArrayList<String> nodeList = new ArrayList<>();

        for (Node node : nodes) {
            String name = node.getName();
            String cpu = String.valueOf(node.getTotalCPU());
            String nodeIpAddress = ipAddress + counter++;
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
