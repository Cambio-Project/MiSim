package de.rss.fachstudie.MiSim.entities.microservice;

import co.paralleluniverse.fibers.SuspendExecution;
import de.rss.fachstudie.MiSim.misc.Priority;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

/**
 * @author Lion Wagner
 */
public class InstanceShutdownStartEvent extends Event<MicroserviceInstance> {


    public InstanceShutdownStartEvent(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
        this.setSchedulingPriority(Priority.HIGH);
    }

    @Override
    public void eventRoutine(MicroserviceInstance microserviceInstance) throws SuspendExecution {
       microserviceInstance.startShutdown();
    }
}
