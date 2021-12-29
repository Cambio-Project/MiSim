package cambio.simulator.orchestration.management;

import cambio.simulator.orchestration.loadbalancing.LoadBalancerType;
import cambio.simulator.orchestration.parsing.ConfigDto;
import cambio.simulator.orchestration.parsing.ParsingException;
import cambio.simulator.orchestration.scheduling.SchedulerType;

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
        } else{
            throw new ParsingException("Unknown SchedulerType in config file: "+ configDto.getScheduler());
        }
        final LoadBalancerType loadBalancerType = LoadBalancerType.fromString(configDto.getLoadBalancer());
        if(loadBalancerType!=null){
            loadBalancer = configDto.loadBalancer;
        } else {
            throw new ParsingException("Unknown LoadBalancerType in config file: "+ configDto.loadBalancer);
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
