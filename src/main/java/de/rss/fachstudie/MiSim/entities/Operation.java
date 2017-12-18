package de.rss.fachstudie.MiSim.entities;

import de.rss.fachstudie.MiSim.models.MainModel;
import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Model;

import java.util.SortedMap;

/**
 * An operation connects two microservice instances. During a specified time interval the service
 * performs operations and uses a portion of the microservice's computing power.
 *
 * model:           reference to the experiment model
 * name:            the given name of the operation, defined by the input
 * service:         name of the the owning microservice
 * pattern:         resilience pattern
 * duration:        time interval the operation needs to finish
 * CPU:             the needed computing power
 * probability:     the operation is only executed if a certain probability is reached
 * dependencies:    an array containing dependant operations of other services
 */
public class Operation extends Entity {
    private MainModel model;
    private String name = "";
    private int demand = 0;
    private Pattern[] opatterns = null;
    private SortedMap<String, String>[] dependencies;

    public Operation(Model model, String s, boolean b) {
        super(model, s, b);

        this.model = (MainModel) model;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public Pattern[] getPatterns() {
        if (opatterns == null) {
            opatterns = new Pattern[]{};
        }
        return opatterns;
    }

    public void setPatterns(Pattern[] patterns) {
        if (opatterns == null) {
            opatterns = new Pattern[]{};
        }
        this.opatterns = patterns;
    }

    public Pattern getPattern(String name) {
        if (opatterns == null) {
            opatterns = new Pattern[]{};
        }
        for (Pattern pattern : opatterns) {
            if (pattern.getName().equals(name))
                return pattern;
        }
        return null;
    }

    public boolean hasPattern(String name) {
        if (opatterns == null) {
            opatterns = new Pattern[]{};
        }
        for (Pattern pattern : opatterns) {
            if (pattern.getName().equals(name))
                return true;
        }
        return false;
    }

    public SortedMap<String, String>[] getDependencies() {
        return dependencies;
    }

    public void setDependencies(SortedMap<String, String>[] operations) {
        this.dependencies = operations;
    }

    public int getDemand() {
        return demand;
    }

    public void setDemand(int demand) {
        this.demand = demand;
    }
}
