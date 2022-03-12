package cambio.simulator.orchestration.parsing.converter;

import cambio.simulator.orchestration.Util;
import cambio.simulator.orchestration.k8objects.Deployment;
import cambio.simulator.orchestration.management.ManagementPlane;
import cambio.simulator.orchestration.parsing.*;
import cambio.simulator.orchestration.scaling.RandomAutoscaler;

import java.util.Optional;
import java.util.Random;

public class RandomAutoScalerManipulator implements K8ObjectManipulator {

    private K8HPADto k8HPADto = null;

    private RandomAutoScalerManipulator() {
    }

    private static final RandomAutoScalerManipulator instance = new RandomAutoScalerManipulator();

    public static RandomAutoScalerManipulator getInstance() {
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
                        deployment.setAutoScaler(new RandomAutoscaler());
                        deployment.getAutoScaler().setRandom(new Random(ManagementPlane.getInstance().getExperimentSeed()+deploymentName.hashCode()));
                        final int minReplicas = k8HPADto.getSpec().getMinReplicas();
                        final int maxReplicas = k8HPADto.getSpec().getMaxReplicas();
                        deployment.setMinReplicaCount(minReplicas);
                        deployment.setMaxReplicaCount(maxReplicas);
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
