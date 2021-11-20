package cambio.simulator.models;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.orchestration.k8objects.Service;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import desmoj.core.dist.ContDistNormal;

/**
 * Class that contains the architectural information provided by the architecture file.
 *
 * @author Lion Wagner
 */
public class ArchitectureModel {

    @Expose
    private Microservice[] microservices;

    @Expose
    @SerializedName(value = "network_latency", alternate = {"network_delay", "delay", "latency"})
    private ContDistNormal networkLatency;


    /**
     * Gets all available microservices.
     *
     * @return all microservices
     */
    public Set<Microservice> getMicroservices() {
        return new HashSet<>(Arrays.asList(microservices));
    }

    public Set<Service> getServices() {
        return Arrays.stream(microservices).map(Service.class::cast).collect(Collectors.toSet());
    }
}
