package cambio.simulator.orchestration;

import cambio.simulator.entities.microservice.*;
import cambio.simulator.orchestration.events.RestartContainerEvent;
import cambio.simulator.orchestration.k8objects.Deployment;
import cambio.simulator.orchestration.management.ManagementPlane;
import cambio.simulator.orchestration.environment.Container;
import cambio.simulator.orchestration.environment.ContainerState;
import cambio.simulator.orchestration.environment.Pod;
import cambio.simulator.orchestration.loadbalancing.LoadBalancerOrchestration;
import desmoj.core.simulator.Model;

public class MicroserviceOrchestration extends Microservice {

    LoadBalancerOrchestration loadBalancerOrchestration;

    /**
     * Creates a new instance of a {@link Microservice}.
     */
    public MicroserviceOrchestration(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
    }

    @Override
    public MicroserviceInstance getNextAvailableInstance() throws NoInstanceAvailableException {
        return loadBalancerOrchestration.getNextServiceInstance();
    }


    @Override
    public synchronized void killInstance() {
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
                if (container.getMicroserviceInstance().equals(instanceToKill)) {
                    container.setContainerState(ContainerState.TERMINATED);
                    //Restart terminated container regarding restart policy https://kubernetes.io/docs/concepts/workloads/pods/pod-lifecycle/
                    container.applyBackOffDelayResetIfNecessary();
                    final RestartContainerEvent restartContainerEvent = new RestartContainerEvent(getModel(), "Restart " + container.getQuotedPlainName(), traceIsOn());
                    restartContainerEvent.schedule(container, container.getPlannedExecutionTime());
                    return;
                }
            }
        }
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

    public void setLoadBalancerOrchestration(LoadBalancerOrchestration loadBalancerOrchestration) {
        this.loadBalancerOrchestration = loadBalancerOrchestration;
    }

}
