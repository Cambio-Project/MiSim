package de.rss.fachstudie.MiSim.events;

import co.paralleluniverse.fibers.SuspendExecution;
import de.rss.fachstudie.MiSim.entities.microservice.Microservice;
import de.rss.fachstudie.MiSim.misc.Priority;
import desmoj.core.simulator.ExternalEvent;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

import java.util.Objects;

/**
 * A <code>ChaosMonkeyEvent</code> is an <code>ExternalEvent</code> that gets scheduled at the begin of the experiment.
 * It terminates a specified number of Microservice instances from a specified <code>Microservice</code> in its
 * <code>eventRoutine</code> method.
 */
public class ChaosMonkeyEvent extends ExternalEvent {
    private final int instances;
    private final Microservice microservice;
    private TimeInstant targetTime;

    /**
     * Instantiate a <code>ChaosMonkeyEvent</code>.
     *
     * @param owner        Model: The model that owns this event
     * @param name         String: The name of this event
     * @param showInTrace  boolean: Declaration if this event should be shown in the trace
     * @param microservice int: The ID of the microservice whose instances should be terminated
     * @param instances    int: The number of instances of the specified microservice you want to shut down
     */
    public ChaosMonkeyEvent(Model owner, String name, boolean showInTrace, Microservice microservice, int instances) {
        super(owner, name, showInTrace);

        this.microservice = microservice;
        this.instances = instances;
        setSchedulingPriority(Priority.HIGH);
    }

    /**
     * The eventRoutine of the <code>ChaosMonkeyEvent</code>. Terminates a specified number of instances of a specified
     * <code>Microservice</code>.
     *
     * @throws SuspendExecution
     */
    @Override
    public void eventRoutine() throws SuspendExecution {

        microservice.killInstances(instances);

        boolean hasServicesLeft = microservice.getInstancesCount() > 0;
        sendTraceNote("Chaos Monkey " + getQuotedName() + " was executed.");
        sendTraceNote(String.format("There are %s instances left of service %s",
                hasServicesLeft ? String.format("still %n", microservice.getInstancesCount()) : "no", microservice.getName()));
    }

    @Override
    public String toString() {
        return "ChaosMonkeyEvent";
    }


    public TimeInstant getTargetTime() {
        return targetTime;
    }

    public void setTargetTime(TimeInstant targetTime) {
        Objects.requireNonNull(targetTime);
        this.targetTime = targetTime;
    }
}
