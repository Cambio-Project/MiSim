package de.rss.fachstudie.MiSim.entities;

import de.rss.fachstudie.MiSim.entities.patterns.Pattern;
import de.rss.fachstudie.MiSim.models.MainModel;
import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Model;

/**
 * A Microservice represents a collection of services.
 * Each instance is able to call operations to another service instance.
 *
 * model:       reference to the experiment model
 * id:          internal unique number to identify a service
 * sid:         service id (maps to the number of existing instances)
 * name:        the given name of the service, defined by the input
 * CPU:         the computing power a microservice has available
 * instances:   number of instances a service can create
 * operations:  an array of dependent operations
 */
public class Microservice extends Entity{
    private MainModel model;
    private boolean killed = false;
    private int id;
    private int sid;
    private String name = "";
    private int capacity = 0;
    private int instances = 0;
    private Pattern[] spatterns = null;
    private Operation[] operations;

    public Microservice(Model owner, String name, boolean showInTrace){
        super(owner, name , showInTrace);

        this.model = (MainModel) owner;
        spatterns = new Pattern[]{};
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

    public void setPatterns(Pattern[] patterns) {
        if (spatterns == null) {
            spatterns = new Pattern[]{};
        }
        this.spatterns = patterns;
    }

    /**
     * Check if the <code>Microservice</code> implements the passed pattern.
     *
     * @param name String: The name of the pattern
     * @return boolean: True if the pattern is implemented
     * False if the pattern isn't implemented
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

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getInstances() {
        return instances;
    }

    public void setInstances(int numberOfInstances) {
        this.instances = numberOfInstances;
    }

    public Operation[] getOperations() {
        return operations;
    }

    public Operation getOperation(String name) {
        for(Operation o : operations) {
            if(o.getName().equals(name)) {
                return o;
            }
        }
        return null;
    }

    public void setOperations(Operation[] operations) {
        this.operations = operations;
    }
}
