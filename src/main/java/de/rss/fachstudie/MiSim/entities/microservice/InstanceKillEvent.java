package de.rss.fachstudie.MiSim.entities.microservice;

import co.paralleluniverse.fibers.SuspendExecution;
import de.rss.fachstudie.MiSim.entities.microservice.MicroserviceInstance;
import de.rss.fachstudie.MiSim.misc.Priority;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.Model;

/**
 * @author Lion Wagner
 */
public class InstanceKillEvent extends Event<MicroserviceInstance> {
    public InstanceKillEvent(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
        this.setSchedulingPriority(Priority.VERY_HIGH);
    }

    @Override
    public void eventRoutine(MicroserviceInstance microserviceInstance) throws SuspendExecution {
        microserviceInstance.die();
    }
}
