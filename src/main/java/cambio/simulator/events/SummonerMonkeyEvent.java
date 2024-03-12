package cambio.simulator.events;

import cambio.simulator.entities.NamedSimProcess;
import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.misc.Priority;
import cambio.simulator.parsing.JsonTypeName;
import co.paralleluniverse.fibers.SuspendExecution;
import com.google.gson.annotations.Expose;
import desmoj.core.simulator.Model;

/**
 * A {@code SummonerMonkeyEvent} is an {@code ExternalEvent} that gets scheduled at the begin of the experiment. It
 * starts a specified number of {@code MicroserviceInstance}s of a specified {@code Microservice} in its {@code
 * eventRoutine} method.
 */
@JsonTypeName(value = "summoner", alternativeNames = {"summoner_monkey", "summonermonkey"})
public class SummonerMonkeyEvent extends SelfScheduledExperimentAction {
    @Expose
    private int instances;
    @Expose
    private Microservice microservice;

    public SummonerMonkeyEvent(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
    }

    /**
     * Instantiate a {@code SummonerMonkeyEvent}.
     *
     * @param owner        Model: The model that owns this event
     * @param name         String: The name of this event
     * @param showInTrace  boolean: Declaration if this event should be shown in the trace
     * @param microservice int: The ID of the microservice whose instances should be started
     * @param instances    int: The number of instances of the specified microservice should be started
     */
    public SummonerMonkeyEvent(Model owner, String name, boolean showInTrace, Microservice microservice,
                               int instances) {
        super(owner, name, showInTrace);

        this.microservice = microservice;
        this.instances = instances;
        setSchedulingPriority(Priority.HIGH);
    }

    /**
     * The eventRoutine of the {@code SummonerMonkeyEvent}. Starts a specified number of instances of a specified {@code
     * Microservice}.
     */
    @Override
    public void onRoutineExecution() throws SuspendExecution {
        synchronized (NamedSimProcess.class) {
            microservice.scaleToInstancesCount(microservice.getInstancesCount() + instances);

            sendTraceNote("Summoner Monkey " + getQuotedName() + " was executed.");
            sendTraceNote(String.format("There are now %s instances of service %s", microservice.getInstancesCount(),
                microservice.getName()));
        }
    }

    @Override
    public String toString() {
        return getName();
    }
}
