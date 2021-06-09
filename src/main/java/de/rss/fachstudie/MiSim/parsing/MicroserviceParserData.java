package de.rss.fachstudie.MiSim.parsing;

import java.util.Arrays;

import de.rss.fachstudie.MiSim.entities.microservice.Microservice;
import de.rss.fachstudie.MiSim.entities.microservice.Operation;
import desmoj.core.simulator.Model;

/**
 * Data class that can hold the data about a Microservice read from an architecture file.
 *
 * @author Lion Wagner
 */
public class MicroserviceParserData {
    public String name = "";
    public int instances = 0;
    public int capacity = 0;
    public String loadbalancerStrategy = null;
    public PatternData[] patterns = new PatternData[0];
    public OperationParser[] operations;

    public Microservice convertToMicroservice(Model model, boolean showInTrace) {
        final Microservice service = new Microservice(model, name, showInTrace);
        service.setLoadBalancingStrategy(loadbalancerStrategy);
        service.setCapacity(capacity);
        service.setName(name);
        service.setInstancesCount(instances);

        Operation[] operationsObj = Arrays.stream(operations)
            .map(operationParser -> operationParser.convertToOperation(model, showInTrace, service))
            .toArray(value -> new Operation[operations.length]);
        service.setPatternData(patterns);
        service.setOperations(operationsObj);


        return service;
    }

}
