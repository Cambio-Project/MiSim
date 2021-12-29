package cambio.simulator.orchestration.parsing;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.models.ArchitectureModel;
import cambio.simulator.orchestration.MicroserviceOrchestration;
import cambio.simulator.orchestration.Util;
import cambio.simulator.orchestration.k8objects.Deployment;
import cambio.simulator.orchestration.k8objects.K8Object;
import cambio.simulator.orchestration.management.ManagementPlane;
import cambio.simulator.orchestration.parsing.converter.DtoToDeploymentMapper;
import cambio.simulator.orchestration.parsing.converter.DtoToObjectMapper;
import cambio.simulator.orchestration.parsing.converter.HPAManipulator;
import cambio.simulator.orchestration.parsing.converter.K8ObjectManipulator;
import cambio.simulator.orchestration.scaling.HorizontalPodAutoscaler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class YAMLParser {

    Set<String> remainingFilePaths = new HashSet<>();

    private YAMLParser() {
    }

    private static final YAMLParser instance = new YAMLParser();

    public static YAMLParser getInstance() {
        return instance;
    }


    public String getKindAsString(String src) {
        Yaml yaml = new Yaml();
        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(src);
            Map<String, Object> obj = yaml.load(inputStream);
            final Object kind = obj.get("kind");
            if (kind != null) {
                return kind.toString();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Call this to read file if it is a deployment it will be parsed now, otherwise returned null
     *
     * @param src
     * @return
     * @throws ParsingException
     * @throws IOException
     */
    public K8ObjectDto createK8ObjectDto(String src) throws ParsingException, IOException {
        final String s = this.getKindAsString(src);
        final K8Kind k8Kind = Util.searchEnum(K8Kind.class, s);
        if (k8Kind == null) {
            throw new ParsingException("Could not identify kind '" + s + "' of Kubernetes Object in YAML file at " + src);
        }
        Class<?> targetClass = null;
        switch (k8Kind) {
            case DEPLOYMENT:
                targetClass = K8DeploymentDto.class;
                break;
            case HORIZONTALPODAUTOSCALER:
                //Will be dealt with after k8objects have been initialized.
                remainingFilePaths.add(src);
                return null;
        }

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
        K8ObjectDto k8objectDto = (K8ObjectDto) mapper.readValue(new File(src), targetClass);
        return k8objectDto;
    }

    public K8Object buildK8Object(K8ObjectDto k8object, DtoToObjectMapper<?> dtoToObjectMapper, Set<Microservice> microservices) throws ParsingException {
        if (k8object != null && dtoToObjectMapper != null) {
            dtoToObjectMapper.setK8ObjectDto(k8object);
            dtoToObjectMapper.setMicroservices(microservices);
            return (K8Object) dtoToObjectMapper.buildScheme();
        }
        return null;
    }

    /**
     * Call this to read file containing objects (like HPA) which influence previously parsed deployments
     *
     * @param src
     * @throws ParsingException
     * @throws IOException
     */
    public void applyManipulation(String src) throws ParsingException, IOException {
        final String s = this.getKindAsString(src);
        final K8Kind k8Kind = Util.searchEnum(K8Kind.class, s);
        Class targetClass = null;
        K8ObjectManipulator k8ObjectManipulator = null;
        switch (k8Kind) {
            case HORIZONTALPODAUTOSCALER:
                targetClass = K8HPADto.class;
                k8ObjectManipulator = HPAManipulator.getInstance();
                break;
            default:
                throw new ParsingException("Could not identify kind '" + s + "' of Kubernetes Object in YAML file at " + src);
        }

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
        K8ObjectDto k8object = (K8ObjectDto) mapper.readValue(new File(src), targetClass);

        if (k8object != null && k8ObjectManipulator != null) {
            k8ObjectManipulator.setK8ObjectDto(k8object);
            k8ObjectManipulator.manipulate();
        }
    }

    public void createK8ObjectsFromFiles(ArchitectureModel architectureModel, String dir_path) throws ParsingException {
        final Set<String> fileNames;
        Set<K8DeploymentDto> k8ObjectDtos = new HashSet<>();
        try {
            fileNames = Util.getInstance().listFilesUsingJavaIO(dir_path);
            for (String fileName : fileNames) {
                String filePath = dir_path + "/" + fileName;
                final K8ObjectDto k8ObjectDto = this.createK8ObjectDto(filePath);
                if (k8ObjectDto != null) {
                    if (k8ObjectDto instanceof K8DeploymentDto) {
                        k8ObjectDtos.add((K8DeploymentDto) k8ObjectDto);
                    }
                }
            }
            Set<Microservice> microservicesFromArchitecture = new HashSet<>(architectureModel.getMicroservices());
            final Map<K8ObjectDto, Set<Microservice>> mapping = createMapping(microservicesFromArchitecture, k8ObjectDtos);

            //Create Deployments
            for (Map.Entry<K8ObjectDto, Set<Microservice>> entry : mapping.entrySet()) {
                final K8Object k8Object = buildK8Object(entry.getKey(), DtoToDeploymentMapper.getInstance(), entry.getValue());
                if (k8Object instanceof Deployment) {
                    ManagementPlane.getInstance().getDeployments().add((Deployment) k8Object);
                } else {
                    throw new ParsingException("The parser returned an unknown K8Object");
                }
            }

            //Read other k8s objects that refer to deployments (e.g. HPA)
            for (String filePath : this.getRemainingFilePaths()) {
                try {
                    this.applyManipulation(filePath);
                } catch (ParsingException | IOException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }


        } catch (ParsingException |
                IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

    /**
     * Implements the desired behavior:
     * - if service is specified in k8s deployments and in architecture model -> one deployment created
     * - if service is specified in k8s deployments but not in architecture model -> should result in a warning will
     * not be created and not simulated
     * - if service is not specified in k8s deployments but in the architecture model -> automatically create deployment,
     * autoscaler, load balancer, scheduler etc. from default values or entry from architecture file
     *
     * @param microservices
     * @param k8ObjectDtos
     * @return Map<K8ObjectDto, Set < Microservice>>
     */
    public Map<K8ObjectDto, Set<Microservice>> createMapping(Set<Microservice> microservices, Set<K8DeploymentDto> k8ObjectDtos) {
        Map<K8ObjectDto, Set<Microservice>> map = new HashMap<>();
        for (K8DeploymentDto k8DeploymentDto : k8ObjectDtos) {
            Set<Microservice> microserviceSet = new HashSet<>();
            for (SpecDeploymentDto.TemplateDto.SpecContainerDto.ContainerDto containerDto : k8DeploymentDto.getSpec().getTemplate().getSpec().getContainers()) {
                final Optional<Microservice> optionalService = microservices.stream().filter(service -> service.getPlainName().equals(containerDto.getName())).findFirst();
                if (optionalService.isPresent()) {
                    final MicroserviceOrchestration service = (MicroserviceOrchestration) optionalService.get();
                    microservices.remove(service);
                    microserviceSet.add(service);
                } else {
                    System.out.println("WARNING: The container " + containerDto.getName() + " from the given deployment " + k8DeploymentDto.getMetadata().getName() +
                            " is not simulated because there is no corresponding microservice in the architecture file.");
                }
            }
            if (!microserviceSet.isEmpty()) {
                map.put(k8DeploymentDto, microserviceSet);
            } else {
                System.out.println("WARNING: The whole deployment " + k8DeploymentDto.getMetadata().getName() +
                        " is not simulated because no matching microservices from the architecture file were found.");
            }
        }

        //create default deployments for remaining microservices from the architecture file
        microservices.forEach(this::createDefaultDeployment);

        return map;

    }

    public void createDefaultDeployment(Microservice microservice) {
        final String deploymentName = microservice.getPlainName() + "-deployment";
        final Set<MicroserviceOrchestration> services = new HashSet<>();
        services.add((MicroserviceOrchestration) microservice);
        ManagementPlane.getInstance().connectLoadBalancer((MicroserviceOrchestration) microservice, microservice.getLoadBalancer().getLoadBalancingStrategy());
        final String schedulerName = "firstFit";
        final Deployment deployment = new Deployment(ManagementPlane.getInstance().getModel(), deploymentName, ManagementPlane.getInstance().getModel().traceIsOn(), services, microservice.getStartingInstanceCount(), schedulerName);
        deployment.setAutoScaler(new HorizontalPodAutoscaler());
        System.out.println("INFO: Creating deployment " + deploymentName +" only from architecture file. There is no corresponding YAML file");
        ManagementPlane.getInstance().getDeployments().add(deployment);
    }

    public static ConfigDto parseConfigFile(String src) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
        final ConfigDto configDto = mapper.readValue(new File(src), ConfigDto.class);
        return configDto;

    }

    public Set<String> getRemainingFilePaths() {
        return remainingFilePaths;
    }

}

