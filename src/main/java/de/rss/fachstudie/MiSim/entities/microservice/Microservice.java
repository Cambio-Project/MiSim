package de.rss.fachstudie.MiSim.entities.microservice;

import de.rss.fachstudie.MiSim.entities.patterns.InstanceOwnedPattern;
import de.rss.fachstudie.MiSim.entities.patterns.LoadBalancer;
import de.rss.fachstudie.MiSim.entities.patterns.LoadBalancingStrategy;
import de.rss.fachstudie.MiSim.entities.patterns.ServiceOwnedPattern;
import de.rss.fachstudie.MiSim.export.ContinuousMultiDataPointReporter;
import de.rss.fachstudie.MiSim.export.MultiDataPointReporter;
import de.rss.fachstudie.MiSim.parsing.PatternData;
import desmoj.core.dist.NumericalDist;
import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.Model;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A Microservice is one of the core Entities of the simulation. It represents the meta layer of a microservice that is
 * usually present in its managing platform, e.g. CloudFoundry.
 * <p>
 * Specifically, it can take care of starting, killing and shutting down {@code MicroserviceInstance}s (in the following
 * just called instances) and provides meta data to each instance. For example, a {@code Microservice} object knows
 * which resilience patterns should be implemented by each instance and how many resources each instances is assigned.
 * Naturally it also knows the status of all existing (including killed ones) instances of this service.
 * <p>
 * Further it has the ability to apply resilience patterns such as autoscaling and different types of load balancing to
 * itself.
 * <p>
 * The interface of a {@code Microservice} is defined via its operations.
 *
 * @author Lion Wagner
 * @see MicroserviceInstance
 * @see LoadBalancingStrategy
 * @see ServiceOwnedPattern
 * @see InstanceOwnedPattern
 */
public class Microservice extends Entity {
    private boolean started = false;
    private String name = "";
    private int capacity = 0;
    private int targetInstanceCount = 0;
    private int instanceSpawnCounter = 0; // running counter to create instance ID's

    private final Set<MicroserviceInstance> instancesSet = new HashSet<>();

    private Operation[] operations;

    private PatternData[] patternsData;
    private ServiceOwnedPattern[] serviceOwnedPatterns;

    private final LoadBalancer loadBalancer;

    private final MultiDataPointReporter reporter;

    public Microservice(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
        setName(name);
        loadBalancer = new LoadBalancer(model, "Loadbalancer of " + this.getQuotedName(), traceIsOn(), instancesSet);
        setLoadBalancingStrategy("random");//defaulting to random lb
        reporter = new ContinuousMultiDataPointReporter(String.format("S[%s]_", name));
    }

    public synchronized void start() {
        started = true;
        scaleToInstancesCount(targetInstanceCount);
        serviceOwnedPatterns = Arrays.stream(patternsData)
                .map(patternData -> patternData.tryGetServiceOwnedPatternOrNull(this))
                .filter(Objects::nonNull)
                .toArray(ServiceOwnedPattern[]::new);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void setPatternData(PatternData[] patterns) {
        this.patternsData = patterns;
    }

    public int getInstancesCount() {
        return instancesSet.size();
    }

    public synchronized void setInstancesCount(final int numberOfInstances) {
        targetInstanceCount = numberOfInstances;
        if (started) {
            scaleToInstancesCount(numberOfInstances);
        }
    }

    public synchronized void scaleToInstancesCount(final int targetInstanceCount) {
        if (!started) {
            throw new IllegalStateException("Microservice was not started. Use start() first or setInstanceCount()");
        }

        while (getInstancesCount() != targetInstanceCount) {
            Event<MicroserviceInstance> changeEvent;
            MicroserviceInstance changedInstance;

            if (getInstancesCount() < targetInstanceCount) {
                //TODO: restart shutdown instances instead of creating new ones
                changedInstance = new MicroserviceInstance(getModel(), String.format("[%s]_I%d", getName(), instanceSpawnCounter), this.traceIsOn(), this, instanceSpawnCounter);
                changedInstance.activatePatterns(patternsData);
                instanceSpawnCounter++;
                changeEvent = new InstanceStartupEvent(getModel(), "Instance Startup of " + changedInstance.getQuotedName(), traceIsOn());
                instancesSet.add(changedInstance);
            } else {
                //tires to find the least used instance to shut it down
                changedInstance = instancesSet.stream().min(Comparator.comparingDouble(MicroserviceInstance::getUsage)).get();
                changeEvent = new InstanceShutdownStartEvent(getModel(), String.format("Instance %s Shutdown Start", changedInstance.getQuotedName()), traceIsOn());
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
        for (int i = 0; i < numberOfInstances; i++) {
            killInstance();
        }
    }

    /**
     * Kills a random instance. Can be called on a service that has 0 running instances.
     */
    public synchronized void killInstance() {
        //TODO: use UniformDistribution form desmoj
        MicroserviceInstance instanceToKill = instancesSet.stream().findAny().orElse(null); //selects an element of the stream, not
        if (instanceToKill == null) return;
        instanceToKill.die();
        instancesSet.remove(instanceToKill);
        reporter.addDatapoint("InstanceCount", presentTime(), instancesSet.size());
    }

    public Operation[] getOperations() {
        return operations;
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
        Pattern searchPattern = Pattern.compile(String.format("^(\\Q%s\\E_)?\\(?\\Q%s\\E\\)?(#[0-9]+)?$", this.getName(), name));

        return Arrays.stream(operations)
                .filter(operation -> searchPattern.matcher(operation.getName()).matches())
                .findAny()
                .orElse(null);
    }

    public void setOperations(Operation[] operations) {
        this.operations = operations;
    }

    /**
     * Injector for load balancing strategy for easier json parsing.
     *
     * @param loadBalancingStrategy name of the strategy that is to be applied
     */
    public void setLoadBalancingStrategy(String loadBalancingStrategy) {
        loadBalancer.setLoadBalancingStrategy(LoadBalancingStrategy.fromName(getModel(), loadBalancingStrategy));
    }


    @Override
    public String toString() {
        return this.getName();
    }

    @Override
    public String getQuotedName() {
        return "'" + this.getName() + "'";
    }


    public MicroserviceInstance getNextAvailableInstance() throws NoInstanceAvailableException {
        return loadBalancer.getNextInstance();
    }


    public void applyDelay(NumericalDist<Double> dist, Operation operation_src, Operation operation_trg) {
        if (operation_trg == null) {
            if (operation_src == null) {
                //delay all operations
                for (Operation operation : operations) {
                    operation.applyExtraDelay(dist);
                }
                return;
            }
        }
        operation_src.applyExtraDelay(dist, operation_trg);
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

}
