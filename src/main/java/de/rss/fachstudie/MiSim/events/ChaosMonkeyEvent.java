package de.rss.fachstudie.MiSim.events;

import co.paralleluniverse.fibers.SuspendExecution;
import de.rss.fachstudie.MiSim.entities.Microservice;
import de.rss.fachstudie.MiSim.models.MainModel;
import desmoj.core.simulator.ExternalEvent;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;

/**
 * A <code>ChaosMonkeyEvent</code> is an <code>ExternalEvent</code> that gets scheduled at the begin of the experiment.
 * It terminates a specified number of Microservice instances from a specified <code>Microservice</code> in its
 * <code>eventRoutine</code> method.
 */
public class ChaosMonkeyEvent extends ExternalEvent {
    private MainModel model;
    private int instances = 0;
    private int msId = 0;
    private double nextReschedule = 1;

    /**
     * Instantiate a <code>ChaosMonkeyEvent</code>.
     *
     * @param owner       Model: The model that owns this event
     * @param name        String: The name of this event
     * @param showInTrace boolean: Declaration if this event should be shown in the trace
     * @param msId        int: The ID of the microservice whose instances should be terminated
     * @param instances   int: The number of instances of the specified microservice you want to shut down
     */
    public ChaosMonkeyEvent(Model owner, String name, boolean showInTrace, int msId, int instances) {
        super(owner, name, showInTrace);

        model = (MainModel) getModel();
        this.msId = msId;
        this.instances = instances;
    }

    /**
     * The eventRoutine of the <code>ChaosMonkeyEvent</code>. Terminates a specified number of instances of a specified
     * <code>Microservice</code>.
     *
     * @throws SuspendExecution
     */
    @Override
    public void eventRoutine() throws SuspendExecution {

        for (int i = 0; i < instances; ++i) {
            for (Microservice msEntity : model.services.get(msId)) {
                if (!msEntity.isKilled()) {
                    msEntity.setKilled(true);
                    model.serviceCPU.get(msEntity.getId()).get(msEntity.getSid()).getExistingThreads().removeAll();
                    model.serviceCPU.get(msEntity.getId()).get(msEntity.getSid()).getActiveThreads().removeAll();
                    this.instances -= 1;
                    break;
                }
            }
        }

        boolean hasServicesLeft = false;
        for (int instance = 0; instance < model.services.get(msId).size(); ++instance) {
            if (!model.services.get(msId).get(instance).isKilled()) {
                hasServicesLeft = true;
                break;
            }
        }


        if (!hasServicesLeft) {
            model.taskQueues.get(msId).removeAll();
        }

        if (this.instances > 0) {
            schedule(new TimeSpan(nextReschedule, model.getTimeUnit()));
        }
        sendTraceNote("Chaos Monkey " + getQuotedName() + " was executed.");
        sendTraceNote(String.format("There are %s instances left of service %s", hasServicesLeft ? "still" : "no", model.services.get(msId).first().getName()));
    }

    @Override
    public String toString() {
        return "ChaosMonkeyEvent";
    }
}
