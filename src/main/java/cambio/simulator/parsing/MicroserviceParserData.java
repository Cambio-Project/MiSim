package cambio.simulator.parsing;

import java.util.Arrays;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.entities.microservice.Operation;
import com.google.gson.annotations.SerializedName;
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
        service.setInstancesCount(instances);

        Operation[] operationsObj = Arrays.stream(operations)
            .map(operationParser -> operationParser.convertToOperation(model, showInTrace, service))
            .toArray(value -> new Operation[operations.length]);
        service.setPatternData(patterns);
        service.setOperations(operationsObj);


        return service;
    }

}
