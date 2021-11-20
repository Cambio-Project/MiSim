package cambio.simulator.orchestration.k8objects;

import cambio.simulator.entities.microservice.*;
import cambio.simulator.orchestration.ManagementPlane;
import cambio.simulator.orchestration.environment.Container;
import cambio.simulator.orchestration.environment.ContainerState;
import cambio.simulator.orchestration.environment.Pod;
import cambio.simulator.orchestration.loadbalancing.LoadBalancerOrchestration;
import desmoj.core.simulator.Model;

import java.util.*;

public class Service extends Microservice {

    LoadBalancerOrchestration loadBalancer;

    public LoadBalancerOrchestration getLoadBalancer() {
        return loadBalancer;
    }

    public void setLoadBalancer(LoadBalancerOrchestration loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    /**
     * Creates a new instance of a {@link Microservice}.
     */
    public Service(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
    }

    @Override
    public MicroserviceInstance getNextAvailableInstance() throws NoInstanceAvailableException {
        return loadBalancer.getNextServiceInstance();
    }


    @Override
    public synchronized void killInstance() {
        //TODO: use UniformDistribution form desmoj
        MicroserviceInstance instanceToKill =
                instancesSet.stream().findAny().orElse(null); //selects an element of the stream, not
        if (instanceToKill == null) {
            return;
        }
        instanceToKill.die();
        instancesSet.remove(instanceToKill);
        reporter.addDatapoint("InstanceCount", presentTime(), instancesSet.size());

        for (Pod pod : getDeployment().getReplicaSet()) {
            for (Container container : pod.getContainers()) {
                if(container.getMicroserviceInstance().equals(instanceToKill)){
                    container.setContainerState(ContainerState.TERMINATED);
                    //Immediately restart terminated containers regarding restart policy
//                    final RestartContainerEvent restartContainerEvent = new RestartContainerEvent(getModel(), "Restarting " + container, traceIsOn());
//                    restartContainerEvent.schedule(pod, getModel().presentTime());
                    return;
                }
            }
        }
    }

    public int getCpuDemand() {
        return Arrays.stream(super.getOperations()).mapToInt(Operation::getDemand).sum();
    }

    public MicroserviceInstance createMicroServiceInstance() {
        MicroserviceInstance changedInstance;
        changedInstance =
                new MicroserviceInstance(getModel(), String.format("[%s]_I%d", getName(), instanceSpawnCounter),
                        this.traceIsOn(), this, instanceSpawnCounter);
        changedInstance.activatePatterns(instanceOwnedPatternConfigurations);

        instanceSpawnCounter++;
        instancesSet.add(changedInstance);
        return changedInstance;
    }

    public Deployment getDeployment() {
        for (Deployment deployment : ManagementPlane.getInstance().getDeployments()) {
            if (deployment.getServices().contains(this)) {
                return deployment;
            }
        }
        return null;
    }

}
