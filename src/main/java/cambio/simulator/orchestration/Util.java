package cambio.simulator.orchestration;

import cambio.simulator.entities.patterns.ILoadBalancingStrategy;
import cambio.simulator.orchestration.k8objects.Deployment;
import cambio.simulator.orchestration.loadbalancing.LeastUtilizationLoadBalanceStrategyOrchestration;
import cambio.simulator.orchestration.loadbalancing.LoadBalancerType;
import cambio.simulator.orchestration.loadbalancing.RandomLoadBalanceStrategyOrchestration;
import cambio.simulator.orchestration.management.DefaultValues;
import cambio.simulator.orchestration.management.ManagementPlane;
import cambio.simulator.orchestration.parsing.ParsingException;

import java.io.File;
import java.rmi.UnexpectedException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Util {

    private static final Util instance = new Util();

    //private constructor to avoid client applications to use constructor
    private Util() {}

    public static Util getInstance() {
        return instance;
    }

    public Set<String> listFilesUsingJavaIO(String dir) throws ParsingException {
        final File directory = new File(dir);
        if(directory.exists()){
            return Stream.of(directory.listFiles())
                    .filter(file -> !file.isDirectory())
                    .map(File::getName)
                    .collect(Collectors.toSet());
        }
        throw new ParsingException("Could not find the directory: " + dir);
    }

    public Deployment findDeploymentByName(String name){
        final Optional<Deployment> first =
                ManagementPlane.getInstance().getDeployments().stream().filter(deployment -> deployment.getPlainName().equals(name)).findFirst();
        return first.orElse(null);
    }

    public static <T extends Enum<?>> T searchEnum(Class<T> enumeration,
                                                   String search) {
        for (T each : enumeration.getEnumConstants()) {
            if (each.name().compareToIgnoreCase(search) == 0) {
                return each;
            }
        }
        return null;
    }

    public static ILoadBalancingStrategy getDefaultLoadBalancingStrategy() throws UnexpectedException {
        final LoadBalancerType loadBalancerType = LoadBalancerType.fromString(DefaultValues.getInstance().getLoadBalancer());
        if(loadBalancerType!=null) {
            if(loadBalancerType.equals(LoadBalancerType.RANDOM)){
                return new RandomLoadBalanceStrategyOrchestration();
            } else if (loadBalancerType.equals(LoadBalancerType.LEAST_UTIL)){
                return new LeastUtilizationLoadBalanceStrategyOrchestration();
            }
        } else {
            throw new UnexpectedException("Should not happen. The loadBalancerType is checked during parsing the config file");
        }
        return null;
    }
}
