package cambio.simulator.orchestration.management;

import cambio.simulator.orchestration.loadbalancing.LoadBalancerType;
import cambio.simulator.orchestration.parsing.ConfigDto;
import cambio.simulator.orchestration.parsing.ParsingException;
import cambio.simulator.orchestration.scheduling.SchedulerType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultValues {
    String scheduler;
    String loadBalancer;

    private static final DefaultValues instance = new DefaultValues();

    //private constructor to avoid client applications to use constructor
    private DefaultValues() {
    }

    public static DefaultValues getInstance() {
        return instance;
    }

    public void setDefaultValuesFromConfigFile(ConfigDto configDto) throws ParsingException {
        final SchedulerType schedulerType = SchedulerType.fromString(configDto.getScheduler());
        if (schedulerType != null) {
            scheduler = schedulerType.getName();
        } else {
            final List<String> possibleValues = Arrays.stream(SchedulerType.values()).map(schedulerType1 -> schedulerType1.getName()).collect(Collectors.toList());
            throw new ParsingException("Unknown SchedulerType in config file: " + configDto.getScheduler() + "\nPossible values are: " + possibleValues);
        }
        final LoadBalancerType loadBalancerType = LoadBalancerType.fromString(configDto.getLoadBalancer());
        if (loadBalancerType != null) {
            loadBalancer = configDto.loadBalancer;
        } else {
            final List<String> possibleValues = Arrays.stream(LoadBalancerType.values()).map(loadBalancerType1 -> loadBalancerType1.getConfigName()).collect(Collectors.toList());
            throw new ParsingException("Unknown LoadBalancerType in config file: " + configDto.loadBalancer + "\nPossible values are: " + possibleValues);
        }
    }

    public String getScheduler() {
        return scheduler;
    }

    public void setScheduler(String scheduler) {
        this.scheduler = scheduler;
    }

    public String getLoadBalancer() {
        return loadBalancer;
    }

    public void setLoadBalancer(String loadBalancer) {
        this.loadBalancer = loadBalancer;
    }
}
