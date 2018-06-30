package de.rss.fachstudie.MiSim.export;

import de.rss.fachstudie.MiSim.entities.Dependency;
import de.rss.fachstudie.MiSim.entities.Microservice;
import de.rss.fachstudie.MiSim.entities.Operation;
import de.rss.fachstudie.MiSim.models.MainModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The <code>DependencyGraph</code> class is used in order to create the graph that displays the dependencies between
 * all of the systems microservice instances.
 */
public class DependecyGraph {
    private MainModel model;
    private List<Integer> nodes;
    private HashMap<Integer, Microservice> microservices;

    /**
     * Instantiates <code>DependencyGraph</code>.
     *
     * @param model         MainModel: The model which owns this DependencyGraph
     * @param microservices HashMap<Integer, Microservice>
     * @param id            int: The ID of this DependencyGraph
     */
    public DependecyGraph(MainModel model, HashMap<Integer, Microservice> microservices, int id) {
        this.model = model;
        this.microservices = microservices;
        this.nodes = new ArrayList<>();
    }

    public int getIdByName(String name){
        for(int i = 0; i < microservices.size() ; i ++){
            if(name.equals(microservices.get(i).getName())){
                return microservices.get(i).getId();
            }
        }
        return -1;
    }

    /**
     * Create the javascript code for the <code>DependencyGraph</code>.
     *
     * @return String: js code for the graph
     */
    public String printGraph() {
        String nodes = printNodes();
        String links = printLinks();
        String html = "var graphMinimalistic = '" + model.getReport() + "';\n" + "var graph = {nodes:[";
        if(nodes.length() > 2)
            html += nodes.substring(0, nodes.length() - 1) + "], ";
        else
            html += "], ";
        if(links.length() > 2)
            html += "links:[" + links.substring(0, links.length() - 1) + "]};";
        else if(links.length() == 0)
            html += "};";
        else
            html += "]}";
        return html;
    }

    /**
     * Create the javascript code for the nodes of the <code>DependencyGraph</code>.
     *
     * @return String: js code for the nodes of the graph
     */
    private String printNodes() {
        StringBuilder json = new StringBuilder();
        nodes = new ArrayList<>();
        for(Integer id : microservices.keySet()) {
            if(!nodes.contains(id)) {
                StringBuilder labels = new StringBuilder();
                for (Operation op : microservices.get(id).getOperations()) {
                    labels.append("'").append(op.getName()).append("',");
                }
                nodes.add(id);

                int instanceLimit = microservices.get(id).getInstances();
                if(model.getReport().equals("minimalistic")) {
                    instanceLimit = (microservices.get(id).getInstances() < 10) ? microservices.get(id).getInstances() : 10;
                }
                for(int i = 0; i < instanceLimit; ++i) {
                    json.append("{name:'").append(microservices.get(id).getName())
                            .append("',id:")
                            .append(id + microservices.get(id).getInstances() + microservices.keySet().size() * i)
                            .append(",labels:[")
                            .append(labels.substring(0, labels.length() - 1))
                            .append("],group:")
                            .append(id).append("},");
                }
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
        nodes = new ArrayList<>();
        for(Integer id : microservices.keySet()) {

            int instanceLimit = microservices.get(id).getInstances();
            if(model.getReport().equals("minimalistic")) {
                instanceLimit = (microservices.get(id).getInstances() < 10) ? microservices.get(id).getInstances() : 10;
            }

            if(!nodes.contains(id)) {
                nodes.add(id);
                StringBuilder labels = new StringBuilder();
                for (Operation op : microservices.get(id).getOperations()) {
                    labels.append("'").append(op.getName()).append("',");
                    for (Dependency depService : op.getDependencies()) {
                        int depId = getIdByName(depService.getService());
                        json.append("{source:").append(id)
                                .append(",target:").append(depId)
                                .append(",value:").append(instanceLimit)
                                .append("},");
                    }
                }
            }
        }
        return json.toString();
    }
}