package de.rss.fachstudie.MiSim.resources;

import de.rss.fachstudie.MiSim.entities.MessageObject;
import de.rss.fachstudie.MiSim.entities.Microservice;
import de.rss.fachstudie.MiSim.events.StopEvent;
import de.rss.fachstudie.MiSim.models.MainModel;
import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Model;

/**
 * A Thread describes a part of a microservice instance.
 * This thread can performs work in form of operations.
 *
 * id:  the service id it belongs to
 * tid: the thread id (map to the number of existing threads in the service)
 */
public class Thread extends Entity {
    MainModel model;
    private int id;
    private int sid;
    private int tid;
    private int demand;
    private StopEvent endEvent;
    private Microservice service;
    private MessageObject mobject;
    private double creationTime;
    private boolean isBlocked;

    public Thread(Model owner, String name, boolean b, int demand, StopEvent end, Microservice service, MessageObject mo) {
        super(owner, name, b);

        model = (MainModel) owner;
        this.id = service.getId();
        this.sid = service.getSid();
        this.tid = model.serviceCPU.get(service.getId()).get(service.getSid()).getExistingThreads().size();
        this.demand = demand;
        this.endEvent = end;
        this.service = service;
        this.mobject = mo;
        creationTime = model.presentTime().getTimeAsDouble();
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

    public int getTid() {
        return tid;
    }

    public void setTid(int tid) {
        this.tid = tid;
    }

    public double getCreationTime() {
        return creationTime;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        this.isBlocked = blocked;
    }

    public int getDemand() {
        return this.demand;
    }

    public void subtractDemand(double value) {
        if(demand - value > 0)
            this.demand -= value;
        else
            demand = 0;
    }

    public StopEvent getEndEvent() {
        return endEvent;
    }

    public void setEndEvent(StopEvent endEvent) {
        this.endEvent = endEvent;
    }

    public Microservice getService() {
        return service;
    }

    public void setService(Microservice service) {
        this.service = service;
    }

    public MessageObject getMobject() {
        return mobject;
    }

    public void setMobject(MessageObject mobject) {
        this.mobject = mobject;
    }

    public void scheduleEndEvent() {
        endEvent.schedule(service, this, mobject);
    }

}
