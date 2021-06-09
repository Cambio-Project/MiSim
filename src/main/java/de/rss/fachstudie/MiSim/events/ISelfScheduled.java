package de.rss.fachstudie.MiSim.events;

/**
 * Events of an experiment that extend this interface are asked to self schedule on the start of the simulation.
 */
public interface ISelfScheduled {
    /**
     * Schedule yourself for the simulation.
     */
    void doInitialSelfSchedule();
}

