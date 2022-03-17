package cambio.simulator.orchestration;

import cambio.simulator.entities.microservice.*;
import cambio.simulator.orchestration.environment.PodState;
import cambio.simulator.orchestration.events.HealthCheckEvent;
import cambio.simulator.orchestration.k8objects.Deployment;
import cambio.simulator.orchestration.management.ManagementPlane;
import cambio.simulator.orchestration.environment.Container;
import cambio.simulator.orchestration.environment.ContainerState;
import cambio.simulator.orchestration.environment.Pod;
import cambio.simulator.orchestration.loadbalancing.LoadBalancerOrchestration;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;

public class MicroserviceOrchestration extends Microservice {

    LoadBalancerOrchestration loadBalancerOrchestration;

    private int startTime = 0;

    /**
     * Creates a new instance of a {@link Microservice}.
     */
    public MicroserviceOrchestration(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
    }

    @Override
    public MicroserviceInstance getNextAvailableInstance() throws NoInstanceAvailableException {
        //TraceNote For debugging purposes
        //sendTraceNote("Finding next instance using " + loadBalancerOrchestration.getPlainName());
        return loadBalancerOrchestration.getNextServiceInstance();
    }


    @Override
    public synchronized void killInstance() {
        MicroserviceInstance instanceToKill =
                instancesSet.stream().filter(microserviceInstance -> microserviceInstance.getState().equals(InstanceState.RUNNING)).findFirst().orElse(null); //selects an element of the stream, not
        if (instanceToKill == null) {
            return;
        }
        Container containerForMicroServiceInstance = ManagementPlane.getInstance().getContainerForMicroServiceInstance(instanceToKill);
        containerForMicroServiceInstance.die();
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

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }
}
