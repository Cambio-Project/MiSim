package cambio.simulator.orchestration.environment;

import cambio.simulator.entities.NamedEntity;
import cambio.simulator.entities.microservice.InstanceState;
import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.entities.patterns.InstanceOwnedPattern;
import cambio.simulator.orchestration.MicroserviceOrchestration;
import cambio.simulator.orchestration.events.RestartContainerEvent;
import cambio.simulator.orchestration.events.RestartStartContainerAndMicroServiceInstanceEvent;
import cambio.simulator.orchestration.events.StartContainerAndMicroServiceInstanceEvent;
import cambio.simulator.orchestration.management.ManagementPlane;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

/**
 * Basically represents a 1:1-relationsship to @link{MicroserviceInstance} with a coupled @link{ContainerState}
 */
public class Container extends NamedEntity {
    private MicroserviceInstance microserviceInstance;
    private ContainerState containerState;


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
        startMicroServiceEvent.schedule(this, getPlannedExecutionTime(((MicroserviceOrchestration) getMicroserviceInstance().getOwner()).getStartTime()));
    }

    public void restart() {
        RestartStartContainerAndMicroServiceInstanceEvent restartStartContainerAndMicroServiceInstanceEvent = new RestartStartContainerAndMicroServiceInstanceEvent(getModel(), "RestartContainerEvent", traceIsOn());
        restartStartContainerAndMicroServiceInstanceEvent.schedule(this, getPlannedExecutionTime(((MicroserviceOrchestration) getMicroserviceInstance().getOwner()).getStartTime()));
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
//                        //Return because the orchestration tasks will recognize the failed pod and tries to restart it and all of its containers. Like in the event of a ChaosMonkeyForPodsEvent
//                        return;
            }

            //Restart terminated container regarding restart policy https://kubernetes.io/docs/concepts/workloads/pods/pod-lifecycle/
            applyBackOffDelayResetIfNecessary();
            final RestartContainerEvent restartContainerEvent = new RestartContainerEvent(getModel(), "Restart " + this.getQuotedPlainName(), traceIsOn());
            restartContainerEvent.schedule(this, getPlannedExecutionTime(getBackOffDelay()));
            return;
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

    /**
     * returns the TimeInstant when the next attempt of restarting this container is planned
     *
     * @param seconds
     * @return
     */
    public TimeInstant getPlannedExecutionTime(int seconds) {
        //Assuming that all scheduled containers are already running at simulation start
        if (getModel().presentTime().getTimeAsDouble() == 0) {
            return getModel().presentTime();
        }
        return new TimeInstant(seconds + getModel().presentTime().getTimeAsDouble());
    }

    /**
     * Should be called when a Container has died due to a ChaosMonkeyEvent that has killed a
     * MicroServiceInstance. This call regards the back off delay of each container.
     */
    public void restartTerminatedContainer() {
        if (getContainerState().equals(ContainerState.TERMINATED)) {
            incrementBackOffDelay();
            lastRetry = presentTime();
            restart();
        } else {
            sendTraceNote("No need to restart " + this.getQuotedPlainName() + " because it is not terminated");
        }
    }

    public void restartContainer() {
        MicroserviceInstance microserviceInstance = getMicroserviceInstance();
        //state must be switched from KILLED to SHUTDOWN. Otherwise start method would throw error
        microserviceInstance.setState(InstanceState.SHUTDOWN);
        microserviceInstance.getPatterns().forEach(InstanceOwnedPattern::start);
        microserviceInstance.start();
        setContainerState(ContainerState.RUNNING);
        //Needs to be added again to MicroServiceInstances. Otherwise, a following chaos monkey event would not find an instance to kill
        microserviceInstance.getOwner().getInstancesSet().add(microserviceInstance);
        sendTraceNote(microserviceInstance.getQuotedName() + " was restarted");
    }

    public void applyBackOffDelayResetIfNecessary() {
        if (lastRetry != null) {
            final double timeAsDouble = presentTime().getTimeAsDouble();
            if (timeAsDouble - lastRetry.getTimeAsDouble() > RESET_BACK_OFF_DELAY_AFTER_TIME) {
                resetBackOffDelay();
            }
        }
    }
}
