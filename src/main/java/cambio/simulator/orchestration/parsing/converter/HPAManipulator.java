package cambio.simulator.orchestration.parsing.converter;

import cambio.simulator.orchestration.Util;
import cambio.simulator.orchestration.k8objects.Deployment;
import cambio.simulator.orchestration.parsing.K8Kind;
import cambio.simulator.orchestration.management.ManagementPlane;
import cambio.simulator.orchestration.parsing.*;
import cambio.simulator.orchestration.scaling.HorizontalPodAutoscaler;

import java.util.List;
import java.util.Optional;

public class HPAManipulator implements K8ObjectManipulator {

    private K8HPADto k8HPADto = null;

    private HPAManipulator() {
    }

    private static final HPAManipulator instance = new HPAManipulator();

    public static HPAManipulator getInstance() {
        return instance;
    }

    @Override
    public void manipulate() throws ParsingException {
        if (k8HPADto != null) {
            final SpecHPADto.ScaleTargetRefDto scaleTargetRef = k8HPADto.getSpec().getScaleTargetRef();
            if (scaleTargetRef != null) {
                final K8Kind k8Kind = Util.searchEnum(K8Kind.class, scaleTargetRef.getKind());
                if (k8Kind!=null && k8Kind.equals(K8Kind.DEPLOYMENT)) {
                    final String deploymentName = scaleTargetRef.getName();
                    final Optional<Deployment> optionalDeployment = ManagementPlane.getInstance().getDeployments().stream().filter(deployment -> deployment.getPlainName().equals(deploymentName)).findFirst();
                    if (optionalDeployment.isPresent()) {
                        final Deployment deployment = optionalDeployment.get();
                        deployment.setAutoScaler(new HorizontalPodAutoscaler());
                        final int minReplicas = k8HPADto.getSpec().getMinReplicas();
                        final int maxReplicas = k8HPADto.getSpec().getMaxReplicas();
                        final List<SpecHPADto.MetricDto> metrics = k8HPADto.getSpec().getMetrics();
                        if (metrics.size() != 1) {
                            throw new ParsingException("Currently, only exact one 'metric' is supported for the HPA " +
                                    "configuration. Make sure there is only one entry for the metric");
                        }
                        final ResourceDto resource = metrics.get(0).getResource();
                        if (!resource.getName().equals("cpu")) {
                            throw new ParsingException("Currently, only 'cpu' is supported as metric");
                        }
                        final ResourceDto.TargetDto target = resource.getTarget();
                        if (!target.getType().equals("Utilization")) {
                            throw new ParsingException("Currently, only 'Utilization' is allowed as target type");
                        }
                        deployment.setMinReplicaCount(minReplicas);
                        deployment.setMaxReplicaCount(maxReplicas);
                        deployment.setAverageUtilization(target.getAverageUtilization());
                        k8HPADto = null;
                    } else {
                        throw new ParsingException("Could not find an existing deployment object by the given name: " + deploymentName);
                    }
                } else {
                    throw new ParsingException("Does not recognize value '"+scaleTargetRef.getKind()+"'. Currently, only 'Deployment' is supported as kind for the 'scaleTargetRef'");
                }
            } else {
                throw new ParsingException("Could not find the 'scaleTargetRef' definition");
            }
        } else {
            throw new ParsingException("Did you forget to set the K8ObjectDto? It resets after every object build");
        }

    }

    @Override
    public void setK8ObjectDto(K8ObjectDto k8ObjectDto) {
        k8HPADto = (K8HPADto) k8ObjectDto;
    }
}
