package cambio.simulator.orchestration;

import cambio.simulator.entities.patterns.ILoadBalancingStrategy;
import cambio.simulator.entities.patterns.RandomLoadBalanceStrategy;
import cambio.simulator.orchestration.k8objects.Deployment;
import cambio.simulator.orchestration.loadbalancing.LeastUtilizationLoadBalanceStrategyOrchestration;
import cambio.simulator.orchestration.loadbalancing.LoadBalancerOrchestration;
import cambio.simulator.orchestration.loadbalancing.LoadBalancerType;
import cambio.simulator.orchestration.loadbalancing.RandomLoadBalanceStrategyOrchestration;
import cambio.simulator.orchestration.management.DefaultValues;
import cambio.simulator.orchestration.management.ManagementPlane;
import cambio.simulator.orchestration.parsing.ParsingException;
import cambio.simulator.orchestration.scheduling.*;

import java.io.File;
import java.rmi.UnexpectedException;
import java.util.*;
import java.util.concurrent.TimeUnit;
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
            } else {
                throw new UnexpectedException("You forgot to link to the default Load Balancing Strategy. Add it here!");
            }
        } else {
            throw new UnexpectedException("Should not happen. The loadBalancerType is checked during parsing the config file");
        }
    }

    public SchedulerType getSchedulerTypeByNameOrStandard(String schedulerName, String deploymentName) {
        if (schedulerName != null) {
            final SchedulerType schedulerType = SchedulerType.fromString(schedulerName);
            if (schedulerType != null) {
                return schedulerType;
            } else {
                System.out.print("[WARNING]: Unknown scheduler name '" + schedulerName + "' for deployment " + deploymentName + ".");
            }
        } else {
            System.out.print("[INFO]: No scheduler was selected for deployment " + deploymentName + ".");
        }
        System.out.println(" Using default Scheduler '" + SchedulerType.fromString(DefaultValues.getInstance().getScheduler()).getDisplayName() + "'");
        return SchedulerType.fromString(DefaultValues.getInstance().getScheduler());
    }

    public Scheduler getSchedulerInstanceByType(SchedulerType schedulerType) throws UnexpectedException {
        if(schedulerType.equals(SchedulerType.RANDOM)){
            return RandomScheduler.getInstance();
        } else if(schedulerType.equals(SchedulerType.FIRSTFIT)){
            return FirstFitScheduler.getInstance();
        } else if(schedulerType.equals(SchedulerType.KUBE)) {
            return KubeScheduler.getInstance();
        }else if(schedulerType.equals(SchedulerType.ROUNDROBIN)) {
            return RoundRobinScheduler.getInstance();
        }
        throw new UnexpectedException("This SchedulerType is not linked to a Schedulerinstance yet. Do it here!");
    }

    public void connectLoadBalancer(MicroserviceOrchestration microserviceOrchestration, ILoadBalancingStrategy loadBalancingStrategy) throws UnexpectedException {
        //This case occurs when no load balancing strategy was given in the architecture file. MiSim automatically has defaulted to randomStrategy. We have to use our default strategy.
        if (loadBalancingStrategy instanceof RandomLoadBalanceStrategy) {
            final ILoadBalancingStrategy defaultLoadBalancingStrategy = Util.getDefaultLoadBalancingStrategy();
            System.out.println("[INFO]: No load balancer was selected for microservice '" + microserviceOrchestration.getPlainName() + "'. Using default load balancer '" + LoadBalancerType.fromString(DefaultValues.getInstance().getLoadBalancer()).getDisplayName() + "'");
            microserviceOrchestration.setLoadBalancerOrchestration(new LoadBalancerOrchestration(ManagementPlane.getInstance().getModel(), LoadBalancerType.fromString(DefaultValues.getInstance().getLoadBalancer()).getDisplayName(), ManagementPlane.getInstance().getModel().traceIsOn(), defaultLoadBalancingStrategy, microserviceOrchestration));
            return;
        }
        if (loadBalancingStrategy instanceof RandomLoadBalanceStrategyOrchestration) {
            microserviceOrchestration.setLoadBalancerOrchestration(new LoadBalancerOrchestration(ManagementPlane.getInstance().getModel(), LoadBalancerType.RANDOM.getDisplayName(), ManagementPlane.getInstance().getModel().traceIsOn(), new RandomLoadBalanceStrategyOrchestration(), microserviceOrchestration));
        } else if (loadBalancingStrategy instanceof LeastUtilizationLoadBalanceStrategyOrchestration) {
            microserviceOrchestration.setLoadBalancerOrchestration(new LoadBalancerOrchestration(ManagementPlane.getInstance().getModel(), LoadBalancerType.LEAST_UTIL.getDisplayName(), ManagementPlane.getInstance().getModel().traceIsOn(), new LeastUtilizationLoadBalanceStrategyOrchestration(), microserviceOrchestration));
        } else {
            throw new UnexpectedException("Unknown Load Balancing Strategy: " + loadBalancingStrategy);
        }
    }

    public static long nanoSecondsToMilliSeconds(long nanosecs) {
        return TimeUnit.MILLISECONDS.convert(nanosecs, TimeUnit.NANOSECONDS);
    }
}
