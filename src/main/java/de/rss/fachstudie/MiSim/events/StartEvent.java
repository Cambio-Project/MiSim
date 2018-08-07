package de.rss.fachstudie.MiSim.events;

import co.paralleluniverse.fibers.SuspendExecution;
import de.rss.fachstudie.MiSim.entities.Dependency;
import de.rss.fachstudie.MiSim.entities.MessageObject;
import de.rss.fachstudie.MiSim.entities.Microservice;
import de.rss.fachstudie.MiSim.entities.Operation;
import de.rss.fachstudie.MiSim.models.MainModel;
import de.rss.fachstudie.MiSim.resources.Thread;
import desmoj.core.dist.ContDistUniform;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;

import java.util.List;

/**
 * The <code>StartEvent</code> gets a <code>MessageObject</code> and schedules a <code>TimeSpan</code>
 * during which an operation of a microservice is performed on one of that microservices instances.
 */
public class StartEvent extends Event<MessageObject> {
    private MainModel model;
    private int id;
    private String operation;

    /**
     * Instantiates a <code>StartEvent</code> which schedules a <code>TimeSpan</code> during which an operation of a
     * microservice is performed on one of that microservices instances.
     *
     * @param owner       Model: The model which owns this event
     * @param name        String: The name of this event
     * @param showInTrace boolean: Whether or not this event should be displayed in the trace
     * @param id          int: The ID of the microservice
     * @param operation   String: The name of the operation which will be performed
     */
    public StartEvent(Model owner, String name, boolean showInTrace, int id, String operation) {
        super(owner, name, showInTrace);

        this.id = id;
        this.operation = operation;
        model = (MainModel) owner;
    }

    /**
     * Chooses the service with most resources and space available.
     *
     * @param id: The ID of the microservice
     * @return Mircroservice
     */
    private Microservice getServiceEntity(int id) {
        double min = Double.POSITIVE_INFINITY;
        int i = 0;
        for (int instance = 0; instance < model.services.get(id).size(); ++instance) {
            if (!model.services.get(id).get(instance).isKilled()) {
                if (model.serviceCPU.get(id).get(instance).getExistingThreads().size() < min) {
                    min = model.serviceCPU.get(id).get(instance).getExistingThreads().size();
                    i = instance;
                }
            }
        }
        //model.log("retuning instance: " + i);
        return model.services.get(id).get(i);
    }

    /**
     * The <code>eventRoutine</code> of the <code>StartEvent</code> which selects a microservice instance on which to
     * perfom this <code>Operation</code>, creates a <code>Thread</code> on that instance for the <code>Operation</code>
     * and schedules the <code>StopEvent</code> of that <code>Operation</code>
     *
     * @param messageObject MessageObject
     * @throws SuspendExecution
     */
    @Override
    public void eventRoutine(MessageObject messageObject) throws SuspendExecution {

        Operation op = model.allMicroservices.get(id).getOperation(operation);
        Microservice msEntity = getServiceEntity(id);
        StopEvent msEndEvent = new StopEvent(model, "", model.getShowStopEvent(), id, operation);
        Thread thread = new Thread(model, "", false, op.getDemand(), msEndEvent, msEntity, messageObject, op);

        boolean hasResourceLimiter = msEntity.hasPattern("Resource Limiter");
        int resourceLimit = Integer.MAX_VALUE;
        double ratio = (model.services.get(id).get(0).getCapacity() / model.services.get(id).get(0).getOperation(operation).getDemand());

        if (ratio >= 1) {
            resourceLimit = model.services.get(id).size() *
                    (model.services.get(id).get(0).getCapacity() / model.services.get(id).get(0).getOperation(operation).getDemand());
        } else {
            resourceLimit = model.services.get(id).size();
        }

        model.serviceCPU.get(id).get(msEntity.getSid()).checkCircuitBreakers();

        if (!hasResourceLimiter || model.taskQueues.get(id).size() < resourceLimit) {

            model.taskQueues.get(id).insert(messageObject);


            boolean availServices = false;
            for (Microservice m : model.services.get(id)) {
                if (!m.isKilled()) {
                    availServices = true;
                    break;
                }
            }

            // Check if there are available services
            if (availServices) {

                if (!model.serviceCPU.get(id).get(msEntity.getSid()).getOpenCircuits().contains(operation)) {
                    model.serviceCPU.get(id).get(msEntity.getSid()).addExistingThread(thread, op);

                    // Are there dependant operations
                    if (op.getDependencies().length > 0) {

                        for (Dependency dependency : op.getDependencies()) {

                            // Roll probability
                            ContDistUniform prob = new ContDistUniform(model, "", 0.0, 1.0, false, false);
                            double probability = dependency.getProbability();

                            if (prob.sample() <= probability) {

                                String nextOperation = dependency.getOperation();
                                String nextService = dependency.getService();

                                int nextServiceId = model.getIdByName(nextService);

                                // Add Dependency to messageObject
                                Microservice nextServiceEntity = getServiceEntity(nextServiceId);
                                Operation nextOperationEntity = model.allMicroservices.get(nextServiceId).getOperation(nextOperation);
                                messageObject.addDependency(msEntity, op, nextServiceEntity, nextOperationEntity, thread);

                                // Immediately start dependant operation
                                StartEvent nextEvent = new StartEvent(model, "", model.getShowStartEvent(), nextServiceId, nextOperation);
                                nextEvent.schedule(messageObject, new TimeSpan(0, model.getTimeUnit()));
                            } else {
                                // add thread to cpu
                                model.serviceCPU.get(id).get(msEntity.getSid()).addThread(thread, op);
                            }
                        }
                    } else {
                        // add thread to cpu
                        model.serviceCPU.get(id).get(msEntity.getSid()).addThread(thread, op);
                    }
                } else {
                    // fail fast
                    double last = 0;
                    List<Double> values = model.circuitBreakerStatistics.get(id).get(thread.getSid()).getDataValues();
                    if (values != null)
                        last = values.get(values.size() - 1);
                    model.circuitBreakerStatistics.get(id).get(thread.getSid()).update(last + 1);

                    msEndEvent.schedule(msEntity, thread, messageObject);
                }
            } else {
                msEndEvent.schedule(msEntity, thread, messageObject);
            }
        } else {
            // Resource Limiter
            double last = 0;
            List<Double> values = model.resourceLimiterStatistics.get(id).get(msEntity.getSid()).getDataValues();
            if (values != null)
                last = values.get(values.size() - 1);
            model.resourceLimiterStatistics.get(id).get(msEntity.getSid()).update(last + 1);
        }

        // Statistics
        // CPU
        model.cpuStatistics.get(id).get(msEntity.getSid()).update(model.serviceCPU.get(id).get(msEntity.getSid()).getMeanUsage(model.getStatisticChunks()));
        //model.serviceCPU.get(id).get(msEntity.getSid()).collectUsage();
        // Thread
        model.activeThreadStatistics.get(id).get(msEntity.getSid()).update(model.serviceCPU.get(id).get(msEntity.getSid()).getActiveThreads().size());
        model.existingThreadStatistics.get(id).get(msEntity.getSid()).update(model.serviceCPU.get(id).get(msEntity.getSid()).getExistingThreads().size());
        // Task Queue
        model.taskQueueStatistics.get(id).update(model.taskQueues.get(id).size());
    }
}
