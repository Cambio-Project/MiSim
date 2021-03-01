package de.rss.fachstudie.MiSim.events;

import co.paralleluniverse.fibers.SuspendExecution;
import de.rss.fachstudie.MiSim.models.MainModel;
import desmoj.core.simulator.ExternalEvent;
import desmoj.core.simulator.Model;

/**
 * A <code>FinishEvent</code> is an <code>ExternalEvent</code> that is called
 */
public class FinishEvent extends ExternalEvent {
    private MainModel model;

    public FinishEvent(Model owner,  String name, boolean showInTraceMode) {
        super(owner, name, showInTraceMode);

        model = (MainModel) owner;
    }

    @Override
    public void eventRoutine() throws SuspendExecution {
        // Finish all threads in the task queue and save the response time
        for (int id = 0; id < model.serviceCPU.size(); ++id) {
            for (int instance = 0; instance < model.serviceCPU.get(id).size(); ++instance) {
                model.serviceCPU.get(id).get(instance).releaseUnfinishedThreads();
            }
        }
    }
}
