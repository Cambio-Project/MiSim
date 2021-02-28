package de.rss.fachstudie.MiSim.parsing;

import de.rss.fachstudie.MiSim.entities.Operation;
import de.rss.fachstudie.MiSim.entities.microservice.Microservice;
import de.rss.fachstudie.MiSim.entities.patterns.Pattern;
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
    public Pattern[] spatterns = null;
    public OperationPOJO[] operations;

    public Microservice convertToMicroservice(Model model, boolean showInTrace) {
        final Microservice service = new Microservice(model, name, showInTrace);
        service.setLoadBalancingStrategy(loadbalancer_strategy);
        service.setCapacity(capacity);
        service.setPatterns(spatterns);
        service.setName(name);
        service.setInstancesCount(instances);

        Operation[] operations_obj = Arrays.stream(operations)
                .map(operationPOJO -> operationPOJO.convertToOperation(model, showInTrace, service))
                .toArray(value -> new Operation[operations.length]);

        service.setOperations(operations_obj);

        return service;
    }

}
