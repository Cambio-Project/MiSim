package de.rss.fachstudie.MiSim.entities;

import de.rss.fachstudie.MiSim.events.StopEvent;
import de.rss.fachstudie.MiSim.resources.Thread;

/**
 * A class to save a triplet of previous operations
 */
public class Predecessor {
    private Microservice entity;
    private Thread thread;
    private StopEvent stopEvent;

    public Predecessor(Microservice e, Thread t, StopEvent s) {
        entity = e;
        thread = t;
        stopEvent = s;
    }

    public Microservice getEntity() {
        return entity;
    }

    public Thread getThread() {
        return thread;
    }

    public StopEvent getStopEvent() {
        return stopEvent;
    }
}
