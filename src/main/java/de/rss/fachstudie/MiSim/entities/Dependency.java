package de.rss.fachstudie.MiSim.entities;

import de.rss.fachstudie.MiSim.entities.microservice.Microservice;

public class Dependency {
    private String service;
    private String operation;
    private Microservice microservice;
    private Operation operation_instance;
    private double probability = 1;

    public Dependency() {

    }

    public Dependency(String service, String operation) {
        this(service, operation, 1);
    }

    public Dependency(String service, String operation, double probability) {
        this.service = service;
        this.operation = operation;
        this.probability = probability;
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

    public Microservice getMicroservice() {
        return microservice;
    }

    public void setMicroservice(Microservice microservice) {
        this.microservice = microservice;
    }

    public Operation getOperation_instance() {
        return operation_instance;
    }

    public void setOperation_instance(Operation operation_instance) {
        this.operation_instance = operation_instance;
    }
}
