package de.unistuttgart.sqa.orcas.misim.parsing;

import java.util.Arrays;

import com.google.gson.annotations.SerializedName;
import de.unistuttgart.sqa.orcas.misim.entities.microservice.Microservice;
import de.unistuttgart.sqa.orcas.misim.entities.microservice.Operation;
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
    @SerializedName(value = "loadbalancerStrategy", alternate = "loadbalancer_strategy")
    public String loadbalancerStrategy = null;
    public PatternData[] patterns = new PatternData[0];
    public OperationParser[] operations;

    /**
     * Converts the given data into a {@link Microservice} object.
     *
     * @param model       respective DESMO-J model
     * @param showInTrace whether events of the service should be shown in the trace.
     * @return the new constrcuted {@link Microservice} instance.
     */
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
