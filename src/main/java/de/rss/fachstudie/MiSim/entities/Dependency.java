package de.rss.fachstudie.MiSim.entities;

public class Dependency {
    private String service;
    private String operation;
    private double probability;

    public Dependency() {

    }

    public Dependency(String service, String operation) {
        this.service = service;
        this.operation = operation;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }
}
