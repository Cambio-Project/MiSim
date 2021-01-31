package de.rss.fachstudie.MiSim.events;

import co.paralleluniverse.fibers.SuspendExecution;
import de.rss.fachstudie.MiSim.models.MainModel;
import desmoj.core.simulator.ExternalEvent;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;

public class StatisticEvent extends ExternalEvent {
    private MainModel model;
    private double timeInterval = 0;

    public StatisticEvent(Model owner, String name, boolean showInTraceMode, double interval) {
        super(owner, name, showInTraceMode);

        model = (MainModel) owner;
        timeInterval = interval;
        if(model.debugIsOn()) this.debugOn();
    }

    @Override
    public void eventRoutine() throws SuspendExecution {
        for (int id = 0; id < model.serviceCPU.size(); ++id) {
            for (int instance = 0; instance < model.serviceCPU.get(id).size(); ++instance) {
                model.serviceCPU.get(id).get(instance).collectUsage();
                model.cpuStatistics.get(id).get(instance).update(model.serviceCPU.get(id).get(instance).getMeanUsage(model.getStatisticChunks()));
            }
        }
        schedule(new TimeSpan(timeInterval, model.getTimeUnit()));
    }
}
