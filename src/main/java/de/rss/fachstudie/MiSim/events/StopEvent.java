package de.rss.fachstudie.MiSim.events;

import de.rss.fachstudie.MiSim.entities.MessageObject;
import de.rss.fachstudie.MiSim.entities.Microservice;
import de.rss.fachstudie.MiSim.entities.Operation;
import de.rss.fachstudie.MiSim.entities.Predecessor;
import de.rss.fachstudie.MiSim.models.MainModel;
import de.rss.fachstudie.MiSim.resources.Thread;
import desmoj.core.simulator.EventOf3Entities;
import desmoj.core.simulator.Model;

/**
 * The <code>StopEvent</code> is an <code>EventOf3Entities</code> which gets a <code>Microservice</code>,
 * <code>Thread</code> and <code>MessageObject</code> instance and gets called when this <code>Thread</code> is done
 * performing a specified <code>Operation</code>.
 */
public class StopEvent extends EventOf3Entities<Microservice, Thread, MessageObject> {
    private MainModel model;
    private int id;
    private String operation;

    /**
     * Instantiate a <code>StopEvent</code>.
     *
     * @param owner       Model: The model that owns this event
     * @param name        String: The name of this event
     * @param showInTrace boolean: Whether or not this event should appear in the trace
     * @param id          int: The ID of the microservice
     * @param operation   String: The name of the operation
     */
    StopEvent(Model owner, String name, Boolean showInTrace, int id, String operation) {
        super(owner, name, showInTrace);

        this.id = id;
        this.operation = operation;
        model = (MainModel) owner;
    }

    /**
     * The <code>eventRoutine</code> method of <code>StopEvent</code>.
     * Collects statistics and notifies depending operations that this operation is finished.
     *
     * @param msEntity      Microservice
     * @param thread        Thread
     * @param messageObject MessageObject
     */
    @Override
    public void eventRoutine(Microservice msEntity, Thread thread, MessageObject messageObject) {
        for (Operation operation : msEntity.getOperations()) {
            if (operation.getName().equals(this.operation)) {
                // Free stacked and waiting operations
                if (messageObject.getDependency().size() > 0) {

                    Predecessor predecessor = messageObject.removeDependency();
                    Microservice previousMs = predecessor.getEntity();
                    Thread previousThread = predecessor.getThread();
                    int previousId = previousMs.getId();

                    // add thread to cpu
                    model.serviceCPU.get(previousId).get(previousMs.getSid()).addThread(previousThread, operation);
                }

                // Remove the message object from the task queue and the thread from the cpu
                model.taskQueues.get(id).remove(messageObject);
                model.serviceCPU.get(id).get(msEntity.getSid()).removeExistingThread(thread);


                // Statistics
                // CPU
                model.cpuStatistics.get(id).get(msEntity.getSid()).update(model.serviceCPU.get(id).get(msEntity.getSid()).getMeanUsage(model.getStatisticChunks()));
                //model.serviceCPU.get(id).get(msEntity.getSid()).collectUsage();
                // Threads
                model.activeThreadStatistics.get(id).get(msEntity.getSid()).update(model.serviceCPU.get(id).get(msEntity.getSid()).getActiveThreads().size());
                model.existingThreadStatistics.get(id).get(msEntity.getSid()).update(model.serviceCPU.get(id).get(msEntity.getSid()).getExistingThreads().size());
                // Response Time
                model.responseStatisitcs.get(id).get(msEntity.getSid()).update(model.presentTime().getTimeAsDouble() - thread.getCreationTime());
                // Task Queue
                model.taskQueueStatistics.get(id).update(model.taskQueues.get(id).size());

            }
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }
}
