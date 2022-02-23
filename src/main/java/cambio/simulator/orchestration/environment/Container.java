package cambio.simulator.orchestration.environment;

import cambio.simulator.entities.NamedEntity;
import cambio.simulator.entities.microservice.InstanceState;
import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.entities.microservice.Operation;
import cambio.simulator.entities.patterns.InstanceOwnedPattern;
import cambio.simulator.orchestration.management.ManagementPlane;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

import java.sql.Time;
import java.util.Arrays;

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

    public void die(){
        microserviceInstance.die();
        setContainerState(ContainerState.TERMINATED);
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

    public void incrementBackOffDelay(){
        if(backOffDelay==LIMIT_BACK_OFF_DELAY){
            return;
        }
        backOffDelay =  Math.min(backOffDelay*2, LIMIT_BACK_OFF_DELAY);
    }

    public void resetBackOffDelay(){
        backOffDelay = 10;
    }

    public int getBackOffDelay() {
        return backOffDelay;
    }

    /**
     * returns the TimeInstant when the next attempt of restarting this container is planned
     * @return TimeInstant
     */
    public TimeInstant getPlannedExecutionTime() {
        return new TimeInstant(getBackOffDelay() + getModel().presentTime().getTimeAsDouble());
    }

    /**
     * Should be called when a Container has died due to a ChaosMonkeyEvent that has killed a
     * MicroServiceInstance. This call regards the back off delay of each container.
     */
    public void restartTerminatedContainer() {
        if(getContainerState().equals(ContainerState.TERMINATED)){
            incrementBackOffDelay();
            lastRetry = presentTime();
            restart();
        } else {
            sendTraceNote("No need to restart " + this.getQuotedPlainName() + " because it is not terminated");
        }
    }

    public void restart(){
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

    public void applyBackOffDelayResetIfNecessary(){
        if(lastRetry!=null){
            final double timeAsDouble = presentTime().getTimeAsDouble();
            if (timeAsDouble - lastRetry.getTimeAsDouble() > RESET_BACK_OFF_DELAY_AFTER_TIME) {
                resetBackOffDelay();
            }
        }
    }
}
