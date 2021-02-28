package de.rss.fachstudie.MiSim.entities.microservice;

import co.paralleluniverse.fibers.SuspendExecution;
import de.rss.fachstudie.MiSim.entities.microservice.MicroserviceInstance;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.Model;

/**
 * @author Lion Wagner
 */
public class InstanceStartupEvent extends Event<MicroserviceInstance> {

    public InstanceStartupEvent(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
    }

    @Override
    public void eventRoutine(MicroserviceInstance microserviceInstance) throws SuspendExecution {

        microserviceInstance.start();
    }
}
