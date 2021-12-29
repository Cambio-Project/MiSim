package cambio.simulator.orchestration.parsing.converter;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.models.ArchitectureModel;
import cambio.simulator.orchestration.k8objects.Deployment;
import cambio.simulator.orchestration.k8objects.K8Object;
import cambio.simulator.orchestration.parsing.K8ObjectDto;
import cambio.simulator.orchestration.parsing.ParsingException;
import cambio.simulator.orchestration.parsing.SpecDeploymentDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public interface DtoToObjectMapper<T> {
    T buildScheme() throws ParsingException;

    void setMicroservices(Set<Microservice> microservices);

    void setK8ObjectDto(K8ObjectDto k8ObjectDto);
}
