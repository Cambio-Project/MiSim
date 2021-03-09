package de.rss.fachstudie.MiSim.entities.microservice;

import de.rss.fachstudie.MiSim.entities.Operation;
import de.rss.fachstudie.MiSim.entities.networking.NoInstanceAvailableException;
import de.rss.fachstudie.MiSim.entities.patterns.LoadBalancer;
import de.rss.fachstudie.MiSim.entities.patterns.LoadBalancingStrategy;
import de.rss.fachstudie.MiSim.entities.patterns.Pattern;
import de.rss.fachstudie.MiSim.export.ContinuousMultiDataPointReporter;
import de.rss.fachstudie.MiSim.export.MultiDataPointReporter;
import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.Model;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

/**
 * A Microservice represents a collection of services. Each instance is able to call operations to another service
 * instance.
 * <p>
 * model:       reference to the experiment model id:          internal unique number to identify a service sid: service
 * id (maps to the number of existing instances) name:        the given name of the service, defined by the input CPU:
 * the computing power a microservice has available instances:   number of instances a service can create operations: an
 * array of dependent operations
 */
public class Microservice extends Entity {
    private boolean killed = false;
    private boolean started = false;
    private int id;
    private int sid;
    private String name = "";
    private int capacity = 0;
    private final Set<MicroserviceInstance> instancesSet = new HashSet<>();
    private int targetInstanceCount = 0;
    private final LoadBalancer loadBalancer;
    private Pattern[] spatterns = null;
    private Operation[] operations;
    private int instanceSpawnCounter = 0;
    private final MultiDataPointReporter reporter;

    public Microservice(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
        setName(name);
        spatterns = new Pattern[]{};
        loadBalancer = new LoadBalancer(model, "Loadbalancer of " + this.getQuotedName(), traceIsOn(), instancesSet);
        setLoadBalancingStrategy("random");//defaulting to random lb
        reporter = new ContinuousMultiDataPointReporter(String.format("S[%s]_", name));
    }

    public synchronized void start() {
        started = true;
        scaleToInstancesCount(targetInstanceCount);
    }

    public boolean isKilled() {
        return killed;
    }

    public void setKilled(boolean killed) {
        this.killed = killed;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSid() {
        return sid;
    }

    public void setSid(int sid) {
        this.sid = sid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Pattern[] getPatterns() {
        if (spatterns == null) {
            spatterns = new Pattern[]{};
        }
        return spatterns;
    }

    public synchronized void setPatterns(Pattern[] patterns) {
        if (spatterns == null) {
            spatterns = new Pattern[]{};
        }
        this.spatterns = patterns;
    }

    /**
     * Check if the <code>Microservice</code> implements the passed pattern.
     *
     * @param name String: The name of the pattern
     * @return boolean: True if the pattern is implemented False if the pattern isn't implemented
     */
    public boolean hasPattern(String name) {
        if (spatterns == null) {
            spatterns = new Pattern[]{};
        }
        for (Pattern pattern : spatterns) {
            if (pattern.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public Pattern getPattern(String name) {
        if (spatterns == null) {
            spatterns = new Pattern[]{};
        }
        for (Pattern pattern : spatterns) {
            if (pattern.getName().equals(name))
                return pattern;
        }
        return null;
    }

    public int getCapacity() {
        return capacity;
    }

    public synchronized void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getInstancesCount() {
        return instancesSet.size();
    }

    public synchronized void setInstancesCount(final int numberOfInstances) {
        targetInstanceCount = numberOfInstances;
        if (started)
            scaleToInstancesCount(numberOfInstances);
    }


    public synchronized void scaleToInstancesCount(final int numberOfInstances) {
        if (!started)
            throw new IllegalStateException("Microservice was not started. Use start() first or setInstanceCount()");


        while (getInstancesCount() != numberOfInstances) {
            Event<MicroserviceInstance> changeEvent;
            MicroserviceInstance changedInstance;

            if (getInstancesCount() < numberOfInstances) {
                changedInstance = new MicroserviceInstance(getModel(), String.format("[%s]_I%d", getName(), instanceSpawnCounter), this.traceIsOn(), this, instanceSpawnCounter);
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
     * @param numberOfInstances
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
        MicroserviceInstance instanceToKill = instancesSet.stream().findFirst().orElse(null);
        if (instanceToKill == null) return;
        instanceToKill.die();
        instancesSet.remove(instanceToKill);
        reporter.addDatapoint("InstanceCount", presentTime(), instancesSet.size());
    }


    public Operation[] getOperations() {
        return operations;
    }

    public Operation getOperation(String name) {
        return Arrays.stream(operations)
                .filter(operation -> operation.getName().matches(String.format("^(%s_)?%s(#[0-9]*)?$", this.getName(), name)))
                .findFirst()
                .orElse(null);
    }

    public void setOperations(Operation[] operations) {
        this.operations = operations;
        for (Operation operation : operations) {
            operation.setOwner(this);
        }
    }

    /**
     * Injector for load balancing strategy for easier json parsing.
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


    public void finalizeStatistics() {
        reporter.addDatapoint("InstanceCount", presentTime(), instancesSet.size());
    }
}
