package cambio.simulator.export;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.entities.microservice.Operation;
import cambio.simulator.entities.networking.DependencyDescription;
import cambio.simulator.models.ExperimentMetaData;
import cambio.simulator.models.MainModel;

/**
 * The <code>DependencyGraph</code> class is used in order to create the graph that displays the dependencies between
 * all of the systems microservice instances.
 */
public class DependencyGraph {
    private static final int hashMultiplier = 11;


    private final MainModel model;
    private final Collection<Microservice> microservices;

    /**
     * Instantiates a <code>DependencyGraph</code>.
     *
     * @param model         MainModel: The model which owns this DependencyGraph
     * @param microservices all known services
     */
    public DependencyGraph(MainModel model, Collection<Microservice> microservices) {
        this.model = model;
        this.microservices = microservices;
    }

    /**
     * Create the javascript code for the <code>DependencyGraph</code>.
     *
     * @return String: js code for the graph
     */
    public String printGraph() {
        String nodes = printNodes();
        String links = printLinks();
        String html =
            "var graphMinimalistic = '" + ExperimentMetaData.get().getReportType() + "';\n" + "var graph = {nodes:[";
        if (nodes.length() > 2) {
            html += nodes.substring(0, nodes.length() - 1) + "], ";
        } else {
            html += "], ";
        }
        if (links.length() > 2) {
            html += "links:[" + links.substring(0, links.length() - 1) + "]};";
        } else if (links.length() == 0) {
            html += "};";
        } else {
            html += "]}";
        }
        return html;
    }

    /**
     * Create the javascript code for the nodes of the <code>DependencyGraph</code>.
     *
     * @return String: js code for the nodes of the graph
     */
    private String printNodes() {
        StringBuilder json = new StringBuilder();
        for (Microservice ms : microservices) {
            String labels = String
                .join(",", Arrays.stream(ms.getOperations()).map(Operation::getQuotedName).collect(Collectors.toSet()));
            int instanceLimit =
                !ExperimentMetaData.get().getReportType().equals("minimalistic") ? ms.getInstancesCount() :
                    Math.min(ms.getInstancesCount(), 10);

            for (int i = 0; i < instanceLimit; ++i) {
                long instanceIdentifier = ms.hashCode() * hashMultiplier + i;
                json.append("{name:").append(ms.getQuotedName())
                    .append(",id:")
                    .append(instanceIdentifier)
                    .append(",labels:[")
                    .append(labels)
                    .append("],group:")
                    .append(ms.getIdentNumber()).append("},");
            }

        }
        return json.toString();
    }

    /**
     * Create the javascript code for the links between the nodes in the <code>DependencyGraph</code>.
     *
     * @return String: js code for links in the graphs
     */
    private String printLinks() {
        StringBuilder json = new StringBuilder();

        for (Microservice ms : microservices) {

            int instanceLimit =
                !ExperimentMetaData.get().getReportType().equals("minimalistic") ? ms.getInstancesCount() :
                    Math.min(ms.getInstancesCount(), 10);

            StringBuilder labels = new StringBuilder();
            for (Operation op : ms.getOperations()) {
                labels.append("'").append(op.getName()).append("',");
                for (DependencyDescription depService : op.getDependencies()) {
                    long depId = depService.getTargetMicroservice().getIdentNumber();
                    json.append("{source:").append(ms.getIdentNumber())
                        .append(",target:").append(depId)
                        .append(",value:").append(instanceLimit)
                        .append("},");
                }
            }

        }
        return json.toString();
    }
}