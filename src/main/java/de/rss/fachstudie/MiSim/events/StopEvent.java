package de.rss.fachstudie.MiSim.events;

import de.rss.fachstudie.MiSim.entities.DependencyNode;
import de.rss.fachstudie.MiSim.entities.MessageObject;
import de.rss.fachstudie.MiSim.entities.Microservice;
import de.rss.fachstudie.MiSim.entities.Operation;
import de.rss.fachstudie.MiSim.models.MainModel;
import de.rss.fachstudie.MiSim.resources.Thread;
import desmoj.core.simulator.EventOf3Entities;
import desmoj.core.simulator.Model;

import java.util.List;

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
                try {
                    if (messageObject.hasDependencies()) {

                        // Do this to make sure we don't lose track of nextDependencies if we get killed by CB
                        DependencyNode node = messageObject.getDependency(msEntity, operation);
                        if (node != null) {
                            List<DependencyNode> nextNodes = node.getNextNodes();
                            for (DependencyNode nextNode : nextNodes) {
                                nextNode.removeDependingNode(node);
                            }
                            node.emptyNextNodes();
                        }

                        // Remove finished dependency and check if depending thread can be started
                        DependencyNode depNode = messageObject.removeDependency(msEntity, operation);
                        if (depNode != null) {
                            for (DependencyNode depending : depNode.getDependingNodes()) {
                                if (!depending.hasNextNodes()) {
                                    // This operation is not waiting for any dependencies
                                    Microservice dependingMs = depending.getService();
                                    Thread dependingThread = depending.getThread();
                                    int dependingID = dependingMs.getId();
                                    Operation dependingOp = depending.getOperation();

                                    // add thread to cpu
                                    model.serviceCPU.get(dependingID).get(dependingMs.getSid()).addThread(dependingThread, dependingOp);
                                }
                            }
                        }
                    }
                } catch (NullPointerException e) {
                    System.out.print("hier");
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
                double lifeTime = model.presentTime().getTimeAsDouble() - thread.getCreationTime();
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
