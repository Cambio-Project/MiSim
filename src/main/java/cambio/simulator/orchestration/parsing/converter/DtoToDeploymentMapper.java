package cambio.simulator.orchestration.parsing.converter;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.orchestration.k8objects.Deployment;
import cambio.simulator.orchestration.MicroserviceOrchestration;
import cambio.simulator.orchestration.management.ManagementPlane;
import cambio.simulator.orchestration.parsing.*;

import java.rmi.UnexpectedException;
import java.util.*;

public class DtoToDeploymentMapper implements DtoToObjectMapper<Deployment> {

    private Set<Microservice> microservices;
    private K8DeploymentDto k8DeploymentDto = null;

    private DtoToDeploymentMapper() {
    }


    private static final DtoToDeploymentMapper instance = new DtoToDeploymentMapper();


    public static DtoToDeploymentMapper getInstance() {
        return instance;
    }


    public Deployment buildScheme() throws ParsingException, UnexpectedException {

        if (k8DeploymentDto != null && microservices != null) {
            final String deploymentName = k8DeploymentDto.getMetadata().getName();
            final Set<MicroserviceOrchestration> services = new HashSet<>();
            for (SpecDeploymentDto.TemplateDto.SpecContainerDto.ContainerDto containerDto : k8DeploymentDto.getSpec().getTemplate().getSpec().getContainers()) {
                final Optional<Microservice> optionalService = microservices.stream().filter(service -> service.getPlainName().equals(containerDto.getName())).findFirst();
                if (optionalService.isPresent()) {
                    final MicroserviceOrchestration service = (MicroserviceOrchestration) optionalService.get();
                    microservices.remove(service);
                    services.add(service);
                    ManagementPlane.getInstance().connectLoadBalancer(service, service.getLoadBalancer().getLoadBalancingStrategy());
                    if (service.getStartingInstanceCount() != k8DeploymentDto.getSpec().getReplicas()) {
                        throw new ParsingException("Replica count for service " + service.getPlainName() + " in architecture file does not match the replica count" +
                                "provided in the deployment file for " + deploymentName + " (" + service.getStartingInstanceCount() + "/" + k8DeploymentDto.getSpec().getReplicas() + ")");
                    }
                } else {
                    throw new ParsingException("Could not map a single containerized service from the deployment " + deploymentName + " to the architecture file");
                }
            }
            final String schedulerName = ManagementPlane.getInstance().getSchedulerByNameOrStandard(k8DeploymentDto.getSpec().getTemplate().getSpec().getSchedulerName(), k8DeploymentDto.getMetadata().getName());
            final Deployment deployment = new Deployment(ManagementPlane.getInstance().getModel(), deploymentName, ManagementPlane.getInstance().getModel().traceIsOn(), services, k8DeploymentDto.getSpec().getReplicas(), schedulerName);
            this.k8DeploymentDto = null;
            return deployment;
        } else {
            throw new ParsingException("Either the Architecture Model or the K8ObjectDto was not given to this class. " +
                    "Did you forget to set the K8ObjectDto? It resets after every object build");
        }
    }

    public Set<Microservice> getMicroservices() {
        return microservices;
    }

    public void setMicroservices(Set<Microservice> microservices) {
        this.microservices = microservices;
    }

    @Override
    public void setK8ObjectDto(K8ObjectDto k8DeploymentDto) {
        this.k8DeploymentDto = (K8DeploymentDto) k8DeploymentDto;
    }

}
