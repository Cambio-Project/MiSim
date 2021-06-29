package de.rss.fachstudie.MiSim.parsing;

import de.rss.fachstudie.MiSim.entities.microservice.Microservice;
import de.rss.fachstudie.MiSim.entities.microservice.Operation;
import desmoj.core.simulator.Model;

import java.util.Arrays;

/**
 * POJO that can hold the data about a Microservice read from an architecture file.
 *
 * @author Lion Wagner
 */
public class MicroservicePOJO {
    public String name = "";
    public int instances = 0;
    public int capacity = 0;
    public String loadbalancer_strategy = null;
    public PatternData[] patterns = new PatternData[0];
    public OperationParser[] operations;

    public Microservice convertToMicroservice(Model model, boolean showInTrace) {
        final Microservice service = new Microservice(model, name, showInTrace);
        service.setLoadBalancingStrategy(loadbalancer_strategy);
        service.setCapacity(capacity);
        service.setName(name);
        service.setInstancesCount(instances);

        Operation[] operations_obj = Arrays.stream(operations)
                .map(operationParser -> operationParser.convertToOperation(model, showInTrace, service))
                .toArray(value -> new Operation[operations.length]);
        service.setPatternData(patterns);
        service.setOperations(operations_obj);


        return service;
    }

}
