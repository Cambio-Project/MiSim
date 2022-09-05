package desmoj.extensions.chaining.abstractions;

import java.util.ArrayList;
import java.util.List;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.dist.NumericalDist;
import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.ExternalEvent;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.Queue;
import desmoj.core.simulator.QueueBased;
import desmoj.core.simulator.Reportable;
import desmoj.core.simulator.TimeSpan;
import desmoj.extensions.chaining.report.SmartReporter;
import desmoj.extensions.chaining.report.SmartReporter.HeaderValuePair;

/**
 * The Station is an abstract super class of all chaining constructs. This class handles most of the processing code
 * like the incoming buffer queue, setup queue and being handled queue. It contains the settings for
 * maxEntitiesToHandle, parallelHandledEntities, setupTime, serviceTime, recoveryTime, and transportTime
 *
 * @param <E> The Entity which can be handled by the station
 * @author Christian Mentz
 *     <p>
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 *     with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *     <p>
 *     Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *     the specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 */
public abstract class Station<E extends Entity> extends Reportable {

    /**
     * the max entities a station can handle
     */
    private final int maxEntitiesToHandle;
    /**
     * the setuptime a station needs to set up the entity
     */
    private final NumericalDist<?> setupTime;
    /**
     * the service time a entitiy is serviced
     */
    private final NumericalDist<?> serviceTime;
    /**
     * the time needed to send the entitiy to the succsessor
     */
    private final NumericalDist<?> transportTime;
    /**
     * the buffer queue of incomming entitites
     */
    private final Queue<E> incomingBufferQueue;
    /**
     * the setup queue of entities which are in setup
     */
    private final Queue<E> setupQueue;
    /**
     * the queue of entities which are in service
     */
    private final Queue<E> entitiesBeeingServicedQueue;
    /**
     * the queue of free capacities
     */
    private final Queue<ServiceCapacity> freeServiceCapacityQueue;
    /**
     * the recovery time which a station needsto handle a the next entitiy
     */
    private final NumericalDist<?> recoveryTime;
    /**
     * the capacity of max parallel handled entities of a station
     */
    private final int maxAvailableServiceCapacity;
    /**
     * the entities a station has handled so far
     */
    private int handledEntitiesSoFar;
    /**
     * This Constructor sets the given times, initializes the queues and fills the free service capacity queue. This
     * Construktor is never called directly but rather via a super call of the inheriting classes. If a given int is
     * lower than zero the station assumes that there is no resriction. This applies to incomingBufferQueueCapacity,
     * maxEntitiesToHandle, parallelHandledEntities
     *
     * @param incomingBufferQueueCapacity the capatiy of the income queue
     * @param maxEntitiesToHandle         the number of max entities the station can handle
     * @param parallelHandledEntities     the number of max parallel entities a station can handle
     * @param setupTime                   the time the station needs to set up
     * @param serviceTime                 the time the station needs for the service
     * @param recoveryTime                the time the station needs tor recover
     * @param transportTime               the time the station needs to transport the entitiy to the next station
     * @param owner                       the model owner
     * @param name                        the name of the station
     * @param showInReport                should this construct be in the report
     * @param showInTrace                 should this construct be in the trace
     */
    public Station(int incomingBufferQueueCapacity, int maxEntitiesToHandle, int parallelHandledEntities,
                   NumericalDist<?> setupTime, NumericalDist<?> serviceTime, NumericalDist<?> recoveryTime,
                   NumericalDist<?> transportTime, Model owner, String name, boolean showInReport,
                   boolean showInTrace) {
        super(owner, name, showInReport, showInTrace);

        if (incomingBufferQueueCapacity < 0) {
            incomingBufferQueueCapacity = Integer.MAX_VALUE;
        }

        if (maxEntitiesToHandle < 0) {
            maxEntitiesToHandle = Integer.MAX_VALUE;
        }

        if (parallelHandledEntities < 0) {
            parallelHandledEntities = Integer.MAX_VALUE;
        }

        this.maxEntitiesToHandle = maxEntitiesToHandle;
        this.maxAvailableServiceCapacity = parallelHandledEntities;

        this.setupTime = setupTime;
        this.serviceTime = serviceTime;
        this.recoveryTime = recoveryTime;
        this.transportTime = transportTime;

        incomingBufferQueue = new Queue<E>(getModel(), name + "PufferQueue", QueueBased.FIFO,
            incomingBufferQueueCapacity, false, false);
        setupQueue = new Queue<E>(getModel(), name + "SetupQueue", false, false);
        entitiesBeeingServicedQueue = new Queue<E>(getModel(), name + "HandleQueue", false, false);
        freeServiceCapacityQueue = new Queue<ServiceCapacity>(getModel(), name + "FreeServiceCapacityQueue",
            false, false);

        for (int i = 0; i < parallelHandledEntities; i++) {
            releaseServiceCapacity(); // fill up capacity queue
        }
        freeServiceCapacityQueue.reset();

    }

    /**
     * Checks if another entitiy can handled by the station
     *
     * @return true if anthoder entity can be handled
     */
    private boolean canAnotherEntityBeHandled() {
        return handledEntitiesSoFar < maxEntitiesToHandle;
    }

    /**
     * occupies a service capacity
     */
    private void claimServiceCapacity() {
        if (!freeServiceCapacityQueue.remove(0)) {
            throw new RuntimeException("Internal Error. Tried to remove none existing service capacity.");
        }
    }

    /**
     * Creates an SmartReporter and fills it with the relevant statistics of the station
     */
    @Override
    protected SmartReporter createDefaultReporter() {
        List<HeaderValuePair> pairs;

        pairs = new ArrayList<HeaderValuePair>();

        pairs.add(new HeaderValuePair("Title", getName()));
        pairs.add(new HeaderValuePair("In", incomingBufferQueue.getObservations()
            + incomingBufferQueue.length()));
        pairs.add(new HeaderValuePair("Out", entitiesBeeingServicedQueue.getObservations()));
        pairs.add(new HeaderValuePair("Waiting", incomingBufferQueue.length()));
        pairs.add(new HeaderValuePair("In progress", entitiesBeeingServicedQueue.length()
            + setupQueue.length()));
        pairs.add(new HeaderValuePair("max.WaitQ", incomingBufferQueue.maxLength()));
        pairs.add(new HeaderValuePair("avg.WaitQ", incomingBufferQueue.averageLength()));
        pairs.add(new HeaderValuePair("max.WaitT", incomingBufferQueue.maxWaitTime()));
        pairs.add(new HeaderValuePair("avg.WaitT", incomingBufferQueue.averageWaitTime()));
        pairs.add(new HeaderValuePair("avg.SetupT", setupQueue.averageWaitTime()));
        pairs.add(new HeaderValuePair("avg.ProcessT", entitiesBeeingServicedQueue.averageWaitTime()));
        pairs.add(new HeaderValuePair("max.avail.Capacity", maxAvailableServiceCapacity));
        pairs.add(new HeaderValuePair("max.used.Capacity", maxAvailableServiceCapacity
            - freeServiceCapacityQueue.minLength()));
        pairs.add(new HeaderValuePair("avg.used.Capacity", maxAvailableServiceCapacity
            - freeServiceCapacityQueue.averageLength()));

        return new SmartReporter("ServiceStations", 5100, pairs, this);
    }

    /**
     * gets the recovery time of the station
     *
     * @return the recovery time
     */
    private TimeSpan getRecoveryTime() {
        if (recoveryTime == null) {
            return new TimeSpan(0);
        } else {
            return recoveryTime.sampleTimeSpan();
        }
    }

    /**
     * gets the remaining service capacity
     *
     * @return the remaining service capacity
     */
    protected int getRemainingServiceCapacity() {
        return freeServiceCapacityQueue.length();
    }

    /**
     * gets the service end
     *
     * @return the servcie end
     */
    private ServiceEndEvent getServiceEndEvent() {
        return new ServiceEndEvent(getModel(), Station.this.getName() + "_ServiceEndEvent");
    }

    /**
     * gets the service time of the station
     *
     * @return the service time
     */
    private TimeSpan getServiceTime() {
        if (serviceTime == null) {
            return new TimeSpan(0);
        } else {
            return serviceTime.sampleTimeSpan();
        }
    }

    /**
     * gets the setup time of the station
     *
     * @return the setup time
     */
    protected TimeSpan getSetupTime() {
        if (setupTime == null) {
            return new TimeSpan(0);
        } else {
            return setupTime.sampleTimeSpan();
        }
    }

    /**
     * gets the start event of this station
     *
     * @return the start event
     */
    public Event<E> getStartEvent() {
        return new StationStartEvent(getModel(), Station.this.getName() + "_StationStartEvent");
    }

    /**
     * gets the transport time of the station
     *
     * @return the transport time
     */
    protected TimeSpan getTransportTime() {
        if (transportTime == null) {
            return new TimeSpan(0);
        } else {
            return transportTime.sampleTimeSpan();
        }
    }

    /**
     * inserts an incoming entitiy into the buffer queue
     *
     * @param entityToInsert the entitiy to be inserted
     * @return true if the entitiy is inserted succsessfully
     */
    protected boolean insertIncomingEntityIntoQueue(E entityToInsert) {
        if (incomingBufferQueue.length() < incomingBufferQueue.getQueueLimit()) {
            // Puffer voll Warnung ausgeben
        }

        return incomingBufferQueue.insert(entityToInsert);
    }

    /**
     * checks if service capaciy is remaining
     *
     * @return true if capacity is remaining
     */
    private boolean isServiceCapacityRemaining() {
        return getRemainingServiceCapacity() > 0;
    }

    /**
     * this method can be overridden by an inherited class
     *
     * @param who the entitiy of the scheduled event
     */
    protected void onServiceFinisched(E who) throws SuspendExecution {
        // Overridable
    }

    /**
     * this method releases service capacity
     */
    private void releaseServiceCapacity() {
        freeServiceCapacityQueue.insert(new ServiceCapacity());
    }

    /**
     * If the Station can handle another entity and service capacity is remaining this method removes the first entity
     * from the buffer queue, increases handledEntitiesSoFar, insert the entity in the setupQueue and claims service
     * capacity. Than the service start event is scheduled.
     */
    private void scheduleServiceStartEventIfPossible() throws SuspendExecution {
        boolean canHandleAnotherEntity = canAnotherEntityBeHandled();
        boolean parallelHandlingCapacityRemaining = isServiceCapacityRemaining();

        if (canHandleAnotherEntity && parallelHandlingCapacityRemaining) {
            E entityToHandle;

            entityToHandle = incomingBufferQueue.first();

            incomingBufferQueue.remove(entityToHandle);
            setupQueue.insert(entityToHandle);
            handledEntitiesSoFar++;
            claimServiceCapacity();

            this.sendTraceNote(Station.this.getName() + " inserts " + entityToHandle.getName()
                + " into internal setup queue to wait for progressing");

            new ServiceStartEvent(getModel(), Station.this.getName() + "_ServiceStartEvent").schedule(
                entityToHandle, getSetupTime());
        }
    }

    /**
     * A class which is used to handle the free capacity of a station
     *
     * @author Christian Mentz
     */
    private class ServiceCapacity extends Entity {

        /**
         * Constructor
         */
        public ServiceCapacity() {
            super(Station.this.getModel(), "ServiceCapacity", false);
        }

    }

    /**
     * Event which is called after a service is finished. It realeases a used capacity and calls
     * scheduleServiceStartEventIfPossible() to handle the remaining entities in the buffer queue.
     *
     * @author Christian Mentz
     */
    private class ServiceCapacityReleasedEvent extends ExternalEvent {

        /**
         * Constructor
         */
        public ServiceCapacityReleasedEvent(Model owner, String name) {
            super(owner, name, false);
        }

        @Override
        public void eventRoutine() throws SuspendExecution {
            releaseServiceCapacity();// Inserts a service capacity to the queue
            if (!incomingBufferQueue.isEmpty()) {
                scheduleServiceStartEventIfPossible();
            }
        }

    }

    /**
     * Event which is called after the servicestart event. The entitiy is removed from the entitiesBeeingServicedQueue
     * and onServiceFinisched is called. An inheriting class can override the onServiceFinisched Method in which the
     * succsessor event is scheduled. Than ServiceEndEvent schedules a ServiceCapacityReleasedEvent with the defined
     * recoveryTime.
     *
     * @author Christian Mentz
     */
    private class ServiceEndEvent extends Event<E> {

        public ServiceEndEvent(Model owner, String name) {
            super(owner, name, false);
        }

        @Override
        public void eventRoutine(E who) throws SuspendExecution {
            entitiesBeeingServicedQueue.remove(who);
            Station.this.sendTraceNote(Station.this.getName() + " finishes processing of  " + who.getName()
                + "and removes it from internal handle queue");
            onServiceFinisched(who);

            new ServiceCapacityReleasedEvent(getModel(), Station.this.getName()
                + "ServiceCapacityReleasedEvent").schedule(getRecoveryTime());
        }
    }

    /**
     * This event is the service event. It removes the entity from the setup queue and inserts it in the
     * entitiesBeeingServicedQueue. Than the service end is scheduled with the prevoius defined timespan.
     *
     * @author Christian Mentz
     */
    class ServiceStartEvent extends Event<E> {

        public ServiceStartEvent(Model owner, String name) {
            super(owner, name, false);

        }

        @Override
        public void eventRoutine(E who) throws SuspendExecution {
            setupQueue.remove(who);
            entitiesBeeingServicedQueue.insert(who);
            Station.this.sendTraceNote(Station.this.getName() + " inserts " + who.getName()
                + " into internal handle queue and starts progressing");
            getServiceEndEvent().schedule(who, getServiceTime());
        }
    }

    /**
     * This event is beeing called at first when a predecessor is finished with his process. The passed entitiy is
     * inserted in the buffer queue and the service start is called if possible.
     *
     * @author Christian Mentz
     */
    class StationStartEvent extends Event<E> {

        public StationStartEvent(Model owner, String name) {
            super(owner, name, false);

        }

        @Override
        public void eventRoutine(E who) throws SuspendExecution {
            boolean entityInsertedIntoBufferQueue;

            entityInsertedIntoBufferQueue = insertIncomingEntityIntoQueue(who);

            if (entityInsertedIntoBufferQueue) {
                scheduleServiceStartEventIfPossible();
            }
        }
    }

}
