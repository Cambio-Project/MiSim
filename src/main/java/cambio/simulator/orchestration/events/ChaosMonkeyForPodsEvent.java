package cambio.simulator.orchestration.events;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.events.SelfScheduledExperimentAction;
import cambio.simulator.misc.Priority;
import cambio.simulator.orchestration.Util;
import cambio.simulator.orchestration.k8objects.Deployment;
import cambio.simulator.orchestration.management.ManagementPlane;
import cambio.simulator.parsing.JsonTypeName;
import co.paralleluniverse.fibers.SuspendExecution;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import desmoj.core.simulator.Model;

@JsonTypeName(value = "chaosmonkey_pods", alternativeNames = {"chaos_monkey_pods", "monkey_pods"})
public class ChaosMonkeyForPodsEvent extends SelfScheduledExperimentAction {
    @Expose
    @SerializedName(value = "instances", alternate = {"instance_count", "killed_instance_count", "killed_instances"})
    private int instances;

    @Expose
    @SerializedName(value = "deployment")
    private String deploymentName;

    public ChaosMonkeyForPodsEvent(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
    }

    /**
     * Instantiate a <code>ChaosMonkeyEvent</code>.
     *
     * @param owner        Model: The model that owns this event
     * @param name         String: The name of this event
     * @param showInTrace  boolean: Declaration if this event should be shown in the trace
     * @param deploymentName String: The target deployment whose pod instances should be terminated
     * @param instances    int: The number of instances of the specified microservice you want to shut down, can be
     *                     greater than the number of currently running instances
     */
    public ChaosMonkeyForPodsEvent(Model owner, String name, boolean showInTrace, String deploymentName, int instances) {
        super(owner, name, showInTrace);

        this.deploymentName = deploymentName;
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
        final Deployment deployment = Util.getInstance().findDeploymentByName(deploymentName);
        if(deployment!=null){
            deployment.killPodInstances(instances);

            boolean hasServicesLeft = deployment.getCurrentRunningOrPendingReplicaCount() > 0;
            sendTraceNote("Chaos Monkey for Pods was applied on" + deployment.getQuotedName());
            sendTraceNote(String.format("There are %s pods left of deployment %s",
                    hasServicesLeft ? String.format("still %d", deployment.getCurrentRunningOrPendingReplicaCount()) : "no",
                    deployment.getName()));
        }
    }

    @Override
    public String toString() {
        return "ChaosMonkeyForPodsEvent";
    }

}
