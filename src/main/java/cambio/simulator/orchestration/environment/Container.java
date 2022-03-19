package cambio.simulator.orchestration.environment;

import cambio.simulator.entities.NamedEntity;
import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.orchestration.MicroserviceOrchestration;
import cambio.simulator.orchestration.events.HealthCheckEvent;
import cambio.simulator.orchestration.events.TryToRestartContainerEvent;
import cambio.simulator.orchestration.events.RestartStartContainerAndMicroServiceInstanceEvent;
import cambio.simulator.orchestration.events.StartContainerAndMicroServiceInstanceEvent;
import cambio.simulator.orchestration.management.ManagementPlane;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;

/**
 * Basically represents a 1:1-relationsship to @link{MicroserviceInstance} with a coupled @link{ContainerState}
 */
public class Container extends NamedEntity {
    private MicroserviceInstance microserviceInstance;
    private ContainerState containerState;

    private int restartAttemptsLeft = 0;
    private int backOffDelay = 10;
    private static int LIMIT_BACK_OFF_DELAY = 300;
    private static int RESET_BACK_OFF_DELAY_AFTER_TIME = 600;
    private TimeInstant lastRetry = null;

    public Container(Model model, String name, boolean showInTrace, MicroserviceInstance microserviceInstance) {
        super(model, name, showInTrace);
        this.microserviceInstance = microserviceInstance;
        this.containerState = ContainerState.WAITING;
    }

    public void start() {
        StartContainerAndMicroServiceInstanceEvent startMicroServiceEvent = new StartContainerAndMicroServiceInstanceEvent(getModel(), "StartContainerEvent", traceIsOn());
        startMicroServiceEvent.schedule(this, new TimeSpan(((MicroserviceOrchestration) getMicroserviceInstance().getOwner()).getStartTime()));
    }

    //Restart terminated container regarding restart policy https://kubernetes.io/docs/concepts/workloads/pods/pod-lifecycle/
    public void restart(){
        applyBackOffDelayResetIfNecessary();
        final TryToRestartContainerEvent tryToRestartContainerEvent = new TryToRestartContainerEvent(getModel(), "Restart " + getQuotedPlainName(), traceIsOn());
        tryToRestartContainerEvent.schedule(this, new TimeSpan(getBackOffDelay()));
    }

    public void die() {
        MicroserviceInstance instanceToKill = getMicroserviceInstance();
        instanceToKill.die();
        instanceToKill.getOwner().getInstancesSet().remove(instanceToKill);

        Pod pod = ManagementPlane.getInstance().getPodForContainer(this);
        if (pod != null) {

            setContainerState(ContainerState.TERMINATED);

            long count = pod.getContainers().stream().filter(container1 -> container1.getContainerState().equals(ContainerState.RUNNING)).count();
            //If no container is running inside this pod, then mark this pod as FAILED
            if (count == 0) {
                pod.setPodState(PodState.FAILED);
                sendTraceNote("Pod " + pod.getQuotedName() + " was set to FAILED because it has not a single running container inside");
                HealthCheckEvent healthCheckEvent = new HealthCheckEvent(getModel(), "HealthCheckEvent - After Pod failed", traceIsOn());
                healthCheckEvent.schedule(new TimeSpan(HealthCheckEvent.delay));

            } else {
                //Restart terminated container regarding restart policy https://kubernetes.io/docs/concepts/workloads/pods/pod-lifecycle/
                restart();
                return;
            }
        } else {
            throw new IllegalStateException("Pod should never be null. When a container dies it must have been in a pod before.");
        }
    }

    public MicroserviceInstance getMicroserviceInstance() {
        return microserviceInstance;
    }

    public void setMicroserviceInstance(MicroserviceInstance microserviceInstance) {
        this.microserviceInstance = microserviceInstance;
    }

    public ContainerState getContainerState() {
        return containerState;
    }

    public void setContainerState(ContainerState containerState) {
        this.containerState = containerState;
    }

    public void incrementBackOffDelay() {
        if (backOffDelay == LIMIT_BACK_OFF_DELAY) {
            return;
        }
        backOffDelay = Math.min(backOffDelay * 2, LIMIT_BACK_OFF_DELAY);
    }

    public void resetBackOffDelay() {
        backOffDelay = 10;
    }

    public int getBackOffDelay() {
        return backOffDelay;
    }

    public void applyBackOffDelayResetIfNecessary() {
        if (lastRetry != null) {
            final double timeAsDouble = presentTime().getTimeAsDouble();
            if (timeAsDouble - lastRetry.getTimeAsDouble() > RESET_BACK_OFF_DELAY_AFTER_TIME) {
                resetBackOffDelay();
            }
        }
    }

    public TimeInstant getLastRetry() {
        return lastRetry;
    }

    public void setLastRetry(TimeInstant lastRetry) {
        this.lastRetry = lastRetry;
    }

    public int getRestartAttemptsLeft() {
        return restartAttemptsLeft;
    }

    public void setRestartAttemptsLeft(int restartAttemptsLeft) {
        this.restartAttemptsLeft = restartAttemptsLeft;
    }

    public boolean canRestartOtherwiseDecrease(){
        if(restartAttemptsLeft>0){
            restartAttemptsLeft--;
            return false;
        } else {
            return true;
        }
    }
}
