package cambio.simulator.orchestration.parsing.converter;

import cambio.simulator.models.ArchitectureModel;
import cambio.simulator.orchestration.environment.Container;
import cambio.simulator.orchestration.k8objects.Deployment;
import cambio.simulator.orchestration.k8objects.K8Object;
import cambio.simulator.orchestration.k8objects.Service;
import cambio.simulator.orchestration.management.ManagementPlane;
import cambio.simulator.orchestration.parsing.ContainerDto;
import cambio.simulator.orchestration.parsing.K8ObjectDto;
import cambio.simulator.orchestration.parsing.ParsingException;
import cambio.simulator.orchestration.parsing.SpecDeploymentDto;

import java.util.*;
import java.util.stream.Collectors;

public class DtoToDeploymentMapper implements DtoToObjectMapper {

    private ArchitectureModel architectureModel = null;
    private K8ObjectDto<SpecDeploymentDto> k8ObjectDto = null;

    private DtoToDeploymentMapper() {}


    private static final DtoToDeploymentMapper instance = new DtoToDeploymentMapper();


    public static DtoToDeploymentMapper getInstance() {
        return instance;
    }


    public Deployment buildScheme() throws ParsingException {

        if (k8ObjectDto != null && architectureModel != null) {
            final String deploymentName = k8ObjectDto.getMetadata().getName();
            final Set<Service> services = new HashSet<>();
            for (ContainerDto containerDto : k8ObjectDto.getSpec().getTemplate().getSpec().getContainers()) {
                final Optional<Service> optionalService = architectureModel.getServices().stream().filter(service -> service.getPlainName().equals(containerDto.getName())).findFirst();
                if (optionalService.isPresent()) {
                    final Service service = optionalService.get();
                    services.add(service);
                    if (service.getStartingInstanceCount() != k8ObjectDto.getSpec().getReplicas()) {
                        throw new ParsingException("Replica count for service " + service.getPlainName() + " in architecture file does not match the replica count" +
                                "provided in the deployment file for " + deploymentName + " (" + service.getStartingInstanceCount() + "/" + k8ObjectDto.getSpec().getReplicas() + ")");
                    }
                } else {
                    throw new ParsingException("Could not map a single containerized service from the deployment " + deploymentName + " to the architecture file");
                }
            }
            if (services.isEmpty()) {
                throw new ParsingException("Could not match a containerized service of the deployment " + deploymentName + "with the services provided in the architecture file");
            }
            ManagementPlane.getInstance().connectLoadBalancersToServices(services);
            final Deployment deployment = new Deployment(ManagementPlane.getInstance().getModel(), deploymentName, ManagementPlane.getInstance().getModel().traceIsOn(), services, k8ObjectDto.getSpec().getReplicas(), "firstFit");
            this.k8ObjectDto = null;
            return deployment;
        } else {
            throw new ParsingException("Either the Architecture Model or the K8ObjectDto was not given to this class. " +
                    "Did you forget to set the K8ObjectDto? It resets after every object build");
        }

    }

    public ArchitectureModel getArchitectureModel() {
        return architectureModel;
    }

    public void setArchitectureModel(ArchitectureModel architectureModel) {
        this.architectureModel = architectureModel;
    }

    public K8ObjectDto<SpecDeploymentDto> getK8ObjectDto() {
        return k8ObjectDto;
    }

    public void setK8ObjectDto(K8ObjectDto<SpecDeploymentDto> k8ObjectDto) {
        this.k8ObjectDto = k8ObjectDto;
    }
}
