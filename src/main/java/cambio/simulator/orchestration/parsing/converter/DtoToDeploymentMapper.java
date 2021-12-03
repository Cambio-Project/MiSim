package cambio.simulator.orchestration.parsing.converter;

import cambio.simulator.models.ArchitectureModel;
import cambio.simulator.orchestration.k8objects.Deployment;
import cambio.simulator.orchestration.k8objects.Service;
import cambio.simulator.orchestration.management.ManagementPlane;
import cambio.simulator.orchestration.parsing.*;

import java.util.*;

public class DtoToDeploymentMapper implements DtoToObjectMapper<Deployment> {

    private ArchitectureModel architectureModel = null;
    private K8DeploymentDto k8DeploymentDto = null;

    private DtoToDeploymentMapper() {
    }


    private static final DtoToDeploymentMapper instance = new DtoToDeploymentMapper();


    public static DtoToDeploymentMapper getInstance() {
        return instance;
    }


    public Deployment buildScheme() throws ParsingException {

        if (k8DeploymentDto != null && architectureModel != null) {
            final String deploymentName = k8DeploymentDto.getMetadata().getName();
            final Set<Service> services = new HashSet<>();
            for (ContainerDto containerDto : k8DeploymentDto.getSpec().getTemplate().getSpec().getContainers()) {
                final Optional<Service> optionalService = architectureModel.getServices().stream().filter(service -> service.getPlainName().equals(containerDto.getName())).findFirst();
                if (optionalService.isPresent()) {
                    final Service service = optionalService.get();
                    services.add(service);
                    if (service.getStartingInstanceCount() != k8DeploymentDto.getSpec().getReplicas()) {
                        throw new ParsingException("Replica count for service " + service.getPlainName() + " in architecture file does not match the replica count" +
                                "provided in the deployment file for " + deploymentName + " (" + service.getStartingInstanceCount() + "/" + k8DeploymentDto.getSpec().getReplicas() + ")");
                    }
                } else {
                    throw new ParsingException("Could not map a single containerized service from the deployment " + deploymentName + " to the architecture file");
                }
            }
            if (services.isEmpty()) {
                throw new ParsingException("Could not match a containerized service of the deployment " + deploymentName + "with the services provided in the architecture file");
            }
            ManagementPlane.getInstance().connectLoadBalancersToServices(services);
            final Deployment deployment = new Deployment(ManagementPlane.getInstance().getModel(), deploymentName, ManagementPlane.getInstance().getModel().traceIsOn(), services, k8DeploymentDto.getSpec().getReplicas(), "firstFit");
            this.k8DeploymentDto = null;
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

    @Override
    public void setK8ObjectDto(K8ObjectDto k8DeploymentDto) {
        this.k8DeploymentDto = (K8DeploymentDto) k8DeploymentDto;
    }

}
