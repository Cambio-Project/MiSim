package de.rss.fachstudie.MiSim.parsing;

import desmoj.core.dist.ContDistUniform;

/**
 * @author Lion Wagner
 */
public class GeneratorPOJO {
    //General Properties
    public String microservice;
    public String operation;

    //TODO: Randomized Generator
    public ContDistUniform timeToCreate;

    //Interval Generator
    public double interval;
    public double start;

    //LIMBO Generator
    public String limbo_model;
    public boolean repeating = false;
    public double repetition_skip = 1000;

}
