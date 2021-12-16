package cambio.simulator.entities.microservice;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import cambio.simulator.entities.NamedEntity;
import cambio.simulator.entities.networking.InternalRequest;
import cambio.simulator.entities.patterns.ILoadBalancingStrategy;
import cambio.simulator.entities.patterns.InstanceOwnedPattern;
import cambio.simulator.entities.patterns.InstanceOwnedPatternConfiguration;
import cambio.simulator.entities.patterns.LoadBalancer;
import cambio.simulator.entities.patterns.ServiceOwnedPattern;
import cambio.simulator.export.ContinuousMultiDataPointReporter;
import cambio.simulator.export.MultiDataPointReporter;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import desmoj.core.dist.NumericalDist;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.Model;

/**
 * A Microservice is one of the core Entities of the simulation. It represents the meta layer of a microservice that is
 * usually present in its managing platform, e.g. CloudFoundry.
 *
 * <p>
 * Specifically, it can take care of starting, killing and shutting down {@link MicroserviceInstance}s (in the following
 * just called instances) and provides meta data to each instance. For example, a {@link Microservice} object knows
 * which resilience patterns should be implemented by each instance and how many resources each instances is assigned.
 * Naturally it also knows the status of all existing (including killed ones) instances of this service.
 *
 * <p>
 * Further it has the ability to apply resilience patterns such as autoscaling and different types of load balancing to
 * itself.
 *
 * <p>
 * The interface of a {@link Microservice} is defined via its operations.
 *
 * @author Lion Wagner
 * @see MicroserviceInstance
 * @see ILoadBalancingStrategy
 * @see ServiceOwnedPattern
 * @see InstanceOwnedPattern
 */
public class Microservice extends NamedEntity {
    protected final transient Set<MicroserviceInstance> instancesSet = new HashSet<>();
    protected final transient MultiDataPointReporter reporter;
    @Expose
    @SerializedName(value = "loadbalancer_strategy", alternate = "load_balancer")
    private final LoadBalancer loadBalancer;
    protected transient boolean started = false;
    protected transient int instanceSpawnCounter = 0; // running counter to create instance ID's

    @Expose
    @SerializedName(value = "name")
    private String plainName = ""; //TODO: fix this whole naming confusion thing
    @Expose
    private int capacity = 1;

    @Expose
    @SerializedName(value = "instances", alternate = {"starting_instance_count", "starting_instances"})
    private int startingInstanceCount = 1;

    @Expose
    private Operation[] operations = new Operation[0];

    @Expose
    @SerializedName(value = "i_patterns",
        alternate = {"instance_patterns", "patterns", "i_pattern", "instance_pattern"})
    protected InstanceOwnedPatternConfiguration[] instanceOwnedPatternConfigurations =
        new InstanceOwnedPatternConfiguration[0];

    @Expose
    @SerializedName(value = "s_patterns", alternate = {"service_patterns", "s_pattern", "service_pattern"})
    private ServiceOwnedPattern[] serviceOwnedPatterns = new ServiceOwnedPattern[0];


    /**
     * Creates a new instance of a {@link Microservice}.
     */
    public Microservice(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
        //default load balancer
        loadBalancer = new LoadBalancer(model, "Loadbalancer", traceIsOn(), null);
        reporter = new ContinuousMultiDataPointReporter(String.format("S[%s]_", name));
    }

    /**
     * Starts this {@link Microservice}. This procedure includes the starting of the defined amount of instances and the
     * initiation of the {@link ServiceOwnedPattern}s.
     */
    public synchronized void start() {
        started = true;
        scaleToInstancesCount(startingInstanceCount);
    }

    @Override
    public String toString() {
        return this.getName();
    }

    @Override
    public String getQuotedName() {
        return "'" + this.getName() + "'";
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }


    public int getInstancesCount() {
        return instancesSet.size();
    }

    /**
     * Similar to {@link Microservice#scaleToInstancesCount(int)} but also overwrites the general target instance count
     * of this service.
     *
     * @param numberOfInstances amount of instance that this service should target.
     */
    public synchronized void setInstancesCount(final int numberOfInstances) {
        startingInstanceCount = numberOfInstances;
        if (started) {
            scaleToInstancesCount(numberOfInstances);
        }
    }

    /**
     * Schedules the immeidate start or shutdown of {@link MicroserviceInstance}s until the amount of active instances
     * reaches the target instance count.
     *
     * <p>
     * TODO: restart instances that were shut down.
     *
     * @param targetInstanceCount amount of instance to which this service should scale.
     */
    public synchronized void scaleToInstancesCount(final int targetInstanceCount) {
        if (!started) {
            throw new IllegalStateException("Microservice was not started. Use start() first or setInstanceCount()");
        }

        while (getInstancesCount() != targetInstanceCount) {
            Event<MicroserviceInstance> changeEvent;
            MicroserviceInstance changedInstance;

            if (getInstancesCount() < targetInstanceCount) {
                //TODO: restart shutdown instances instead of creating new ones
                changedInstance =
                    new MicroserviceInstance(getModel(), String.format("[%s]_I%d", getName(), instanceSpawnCounter),
                        this.traceIsOn(), this, instanceSpawnCounter);
                changedInstance.activatePatterns(instanceOwnedPatternConfigurations);
                instanceSpawnCounter++;
                changeEvent =
                    new InstanceStartupEvent(getModel(), "Instance Startup of " + changedInstance.getQuotedName(),
                        traceIsOn());
                instancesSet.add(changedInstance);
            } else {
                //tires to find the least used instance to shut it down
                changedInstance =
                    instancesSet.stream().min(Comparator.comparingDouble(MicroserviceInstance::getUsage)).get();
                changeEvent = new InstanceShutdownStartEvent(getModel(),
                    String.format("Instance %s Shutdown Start", changedInstance.getQuotedName()), traceIsOn());
                instancesSet.remove(changedInstance);
            }
            changeEvent.schedule(changedInstance, presentTime());
        }

        reporter.addDatapoint("InstanceCount", presentTime(), instancesSet.size());

    }


    /**
     * Kills the given number of services many random instances. Accepts numbers larger than the current amount of
     * instances.
     *
     * @param numberOfInstances number of instances that should be instantly killed
     */
    public synchronized void killInstances(final int numberOfInstances) {
        final int maxKills = Math.max(0, Math.min(numberOfInstances, this.instancesSet.size()));
        for (int i = 0; i < maxKills; i++) {
            killInstance();
        }
    }

    /**
     * Kills a random instance. Can be called on a service that has 0 running instances.
     */
    public synchronized void killInstance() {
        //TODO: use UniformDistribution form desmoj
        MicroserviceInstance instanceToKill =
            instancesSet.stream().findAny().orElse(null); //selects an element of the stream, not
        if (instanceToKill == null) {
            return;
        }
        instanceToKill.die();
        instancesSet.remove(instanceToKill);
        reporter.addDatapoint("InstanceCount", presentTime(), instancesSet.size());
    }

    public Operation[] getOperations() {
        return operations;
    }

    public void setOperations(Operation[] operations) {
        this.operations = operations;
    }

    /**
     * Searches an {@code Operation} that has the name that is given as an argument. The real name of the operation may
     * differ. It may starts with the name of this mircoservice instance or ands with a '#' and a number.
     *
     * @param name name of the operation that should be found
     * @return an operation that has exactly that name, {@code null} if not found
     */
    public Operation getOperationByName(String name) {

        //format of the name: (this.getName()_)name(#[0-9]+), (..) being 'optional' and [..] 'pick one from'
        Pattern searchPattern =
            Pattern.compile(String.format("^(\\Q%s\\E_)?\\(?\\Q%s\\E\\)?(#[0-9]+)?$", this.getName(), name));

        return Arrays.stream(operations)
            .filter(operation -> searchPattern.matcher(operation.getName()).matches())
            .findAny()
            .orElse(null);
    }


    public MicroserviceInstance getNextAvailableInstance() throws NoInstanceAvailableException {
        return loadBalancer.getNextInstance(instancesSet);
    }


    /**
     * Applies the given delay distribution to the given operations.
     *
     * @param dist         {@link NumericalDist} of the delay.
     * @param operationSrc {@link Operation} of this {@link Microservice} that should be affected, can be set to {@code
     *                     null} to affect all {@link Operation}s
     * @param operationTrg target {@link Operation} of the operationSrc that should be affected, can be set to {@code
     *                     null} to affect all outgoing {@link InternalRequest}s
     */
    public void applyDelay(NumericalDist<Double> dist, Operation operationSrc, Operation operationTrg) {
        if (operationTrg == null) {
            if (operationSrc == null) {
                //delay all operations
                for (Operation operation : operations) {
                    operation.applyExtraDelay(dist);
                }
                return;
            }
        }
        operationSrc.applyExtraDelay(dist, operationTrg);
    }

    public void finalizeStatistics() {
        reporter.addDatapoint("InstanceCount", presentTime(), instancesSet.size());
    }

    public List<Double> getRelativeUtilizationOfInstances() {
        return instancesSet.stream().map(MicroserviceInstance::getRelativeWorkDemand).collect(Collectors.toList());
    }

    public double getAverageRelativeUtilization() {
        return instancesSet.stream().mapToDouble(MicroserviceInstance::getRelativeWorkDemand).average().orElse(0);
    }

    public List<Double> getUtilizationOfInstances() {
        return instancesSet.stream().map(MicroserviceInstance::getUsage).collect(Collectors.toList());
    }

    public double getAverageUtilization() {
        return getUtilizationOfInstances().stream().mapToDouble(value -> value).average().orElse(0);
    }

    public int getStartingInstanceCount() {
        return startingInstanceCount;
    }

    public LoadBalancer getLoadBalancer() {
        return loadBalancer;
    }
}
