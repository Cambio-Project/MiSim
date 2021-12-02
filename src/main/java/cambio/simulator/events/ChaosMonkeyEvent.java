package cambio.simulator.events;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.misc.Priority;
import cambio.simulator.orchestration.events.RestartContainerEvent;
import cambio.simulator.orchestration.k8objects.Deployment;
import cambio.simulator.orchestration.management.ManagementPlane;
import cambio.simulator.parsing.JsonTypeName;
import co.paralleluniverse.fibers.SuspendExecution;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import desmoj.core.simulator.Model;

/**
 * A <code>ChaosMonkeyEvent</code> is an <code>ExternalEvent</code> that gets scheduled at the begin of the experiment.
 * It terminates a specified number of <code>MicroserviceInstance</code>s from a specified <code>Microservice</code> in
 * its
 * <code>eventRoutine</code> method.
 */
@JsonTypeName(value = "chaosmonkey", alternativeNames = {"chaos_monkey", "monkey"})
public class ChaosMonkeyEvent extends SelfScheduledExperimentAction {
    @Expose
    @SerializedName(value = "instances", alternate = {"instance_count", "killed_instance_count", "killed_instances"})
    private int instances;

    @Expose
    private Microservice microservice;

    public ChaosMonkeyEvent(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
    }

    /**
     * Instantiate a <code>ChaosMonkeyEvent</code>.
     *
     * @param owner        Model: The model that owns this event
     * @param name         String: The name of this event
     * @param showInTrace  boolean: Declaration if this event should be shown in the trace
     * @param microservice int: The target microservice whose instances should be terminated
     * @param instances    int: The number of instances of the specified microservice you want to shut down, can be
     *                     greater than the number of currently running instances
     */
    public ChaosMonkeyEvent(Model owner, String name, boolean showInTrace, Microservice microservice, int instances) {
        super(owner, name, showInTrace);

        this.microservice = microservice;
        this.instances = instances;
        setSchedulingPriority(Priority.LOW);
    }

    /**
     * The eventRoutine of the <code>ChaosMonkeyEvent</code>. Terminates a specified number of instances of a specified
     * <code>Microservice</code>.
     * Also tries to note the remaining number of instances in the trace.
     */
    @Override
    public void eventRoutine() throws SuspendExecution {

        microservice.killInstances(instances);

        boolean hasServicesLeft = microservice.getInstancesCount() > 0;
        sendTraceNote("Chaos Monkey " + getQuotedName() + " was executed.");
        sendTraceNote(String.format("There are %s instances left of service %s",
                hasServicesLeft ? String.format("still %d", microservice.getInstancesCount()) : "no",
                microservice.getName()));

        for (Deployment deployment : ManagementPlane.getInstance().getDeployments()) {
            if (deployment.getServices().contains(microservice)) {
                //Immediately restart terminated containers regarding restart policy
                final RestartContainerEvent restartContainerEvent = new RestartContainerEvent(getModel(), "Check for terminated containers that need to be restarted", traceIsOn());
                restartContainerEvent.schedule(deployment, getModel().presentTime());
                return;
            }
        }
    }

    @Override
    public String toString() {
        return "ChaosMonkeyEvent";
    }

}
