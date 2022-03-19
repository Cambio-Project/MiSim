package cambio.simulator.orchestration.events;

import cambio.simulator.events.SelfScheduledExperimentAction;
import cambio.simulator.misc.Priority;
import cambio.simulator.orchestration.Util;
import cambio.simulator.orchestration.k8objects.Deployment;
import cambio.simulator.parsing.JsonTypeName;
import co.paralleluniverse.fibers.SuspendExecution;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import desmoj.core.simulator.Model;

@JsonTypeName(value = "aiming_chaos_monkey_pods")
public class AimingChaosMonkeyForPodsEvent extends SelfScheduledExperimentAction {
    @Expose
    @SerializedName(value = "instances", alternate = {"instance_count", "killed_instance_count", "killed_instances"})
    private int instances;

    @Expose
    @SerializedName(value = "deployment")
    private String deploymentName;

    @Expose
    @SerializedName(value = "service")
    private String service;

    @Expose
    @SerializedName(value = "retries")
    private int retries;

    public AimingChaosMonkeyForPodsEvent(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
    }

    /**
     * Instantiate a <code>ChaosMonkeyForPodsEvent</code>.
     *
     * @param owner          Model: The model that owns this event
     * @param name           String: The name of this event
     * @param showInTrace    boolean: Declaration if this event should be shown in the trace
     * @param deploymentName String: The target deployment whose pod instances should be terminated
     * @param instances      int: The number of instances of the specified deployment you want to shut down, can be
     *                       greater than the number of currently running instances
     */
    public AimingChaosMonkeyForPodsEvent(Model owner, String name, boolean showInTrace, String deploymentName, int instances, String service, int retries) {
        super(owner, name, showInTrace);

        this.deploymentName = deploymentName;
        this.instances = instances;
        this.service = service;
        this.retries = retries;
        setSchedulingPriority(Priority.LOW);
    }

    /**
     * The eventRoutine of the <code>ChaosMonkeyForPodsEvent</code>. Terminates a specified number of instances of a specified
     * <code>Deployment</code>.
     * Also tries to note the remaining number of instances in the trace.
     */
    @Override
    public void eventRoutine() throws SuspendExecution {
        final Deployment deployment = Util.getInstance().findDeploymentByName(deploymentName);
        if (deployment != null) {
            deployment.killPodInstances(instances, retries, service);

            sendTraceNote("Aiming Chaos Monkey was applied on " + service + " from the "+ deployment.getQuotedName());
            boolean hasServicesLeft = deployment.getCurrentRunningOrPendingReplicaCount() > 0;
        } else {
            sendTraceNote("Could not execute AimingChaosMonkeyForPodsEvent because the deployment from the " +
                    "given experiment file with the name '" + deploymentName + "' is unknown");
        }
    }

    @Override
    public String toString() {
        return "AimingChaosMonkeyForPodsEvent";
    }

}
