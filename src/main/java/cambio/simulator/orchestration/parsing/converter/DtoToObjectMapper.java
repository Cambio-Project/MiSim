package cambio.simulator.orchestration.parsing.converter;

import cambio.simulator.models.ArchitectureModel;
import cambio.simulator.orchestration.k8objects.Deployment;
import cambio.simulator.orchestration.k8objects.K8Object;
import cambio.simulator.orchestration.parsing.K8ObjectDto;
import cambio.simulator.orchestration.parsing.ParsingException;
import cambio.simulator.orchestration.parsing.SpecDeploymentDto;

import java.util.ArrayList;
import java.util.List;

public interface DtoToObjectMapper {
    K8Object buildScheme() throws ParsingException;

    void setArchitectureModel(ArchitectureModel architectureModel);

    void setK8ObjectDto(K8ObjectDto<SpecDeploymentDto> k8ObjectDto);
}
