package desmoj.extensions.chaining;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.dist.NumericalDist;
import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.Queue;
import desmoj.core.simulator.TimeSpan;
import desmoj.extensions.chaining.abstractions.HasPredecessor;
import desmoj.extensions.chaining.abstractions.HasSuccessor;
import desmoj.extensions.chaining.abstractions.Station;

/**
 * The Merger merges the in the Mergerconfig defined entities into the EEx entity. The Merger waits until enough
 * entities of the required types are in the internal partsQueue and than creates a PartsContainer. With this
 * PartsContainer the StationStartevent is scheduled instantly and the common run through the internal Station stages
 * can be conducted.
 *
 * @param <EIn> the entity which comes in the Transformer
 * @param <EEx> the entity which leaves the Transformer
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
public abstract class Merger<EIn extends Entity, EEx extends Entity> extends Station<EIn> implements
    HasPredecessor<EIn>, HasSuccessor<EEx> {

    /**
     * a map of queues which contains the incoming entities separated by class of the entity
     */
    private final HashMap<Class<? extends EIn>, Queue<EIn>> incomingEntitiesQueueContainer;
    /**
     * the Mergerconfig which holds the required entities and ratios
     */
    private final MergerConfig<EIn> mergerConfig;
    /**
     * Helper class for handling the successor of this station
     */
    private final SuccessorAdministration<EEx> successorAdministration;
    /**
     * This Constructor sets the mergerconfig, the given times, initializes the queues and fills the free service
     * capacity queue. If a given int is lower than zero the station assumes that there is no restriction. This applies
     * to incomingBufferQueueCapacity, maxEntitiesToHandle, parallelHandledEntities. Furthermore the incoming entities
     * queue container is initialized to hold the incomming entities.
     *
     * @param mergerConfig                the Mergerconfig which holds the required entities and ratios
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
    public Merger(MergerConfig<EIn> mergerConfig, int incomingBufferQueueCapacity, int maxEntitiesToHandle,
                  int parallelHandledEntities, NumericalDist<?> setupTime, NumericalDist<?> serviceTime,
                  NumericalDist<?> recoveryTime, NumericalDist<?> transportTime, Model owner, String name,
                  boolean showInReport, boolean showInTrace) {

        super(incomingBufferQueueCapacity, maxEntitiesToHandle, parallelHandledEntities, setupTime,
            serviceTime, recoveryTime, transportTime, owner, name, showInReport, showInTrace);

        this.mergerConfig = mergerConfig;

        incomingEntitiesQueueContainer = new HashMap<Class<? extends EIn>, Queue<EIn>>();
        for (Class<? extends EIn> entityClass : mergerConfig.keySet()) {
            incomingEntitiesQueueContainer.put(entityClass, new Queue<EIn>(getModel(), getName() + "Incoming"
                + entityClass.getSimpleName() + "PufferQueue", false, false));
        }

        successorAdministration = new SuccessorAdministration<EEx>(getModel(), Merger.class.getSimpleName()
            + ": " + name);
    }

    /**
     * Gets a reference to the previously by the constructor created queue for the given class incomingEntityClass If a
     * queue for the entities class cannot be found this method searches for superclasses of the entity. This gives the
     * possibility to use a for instance a Tire class queue for RedTires classes
     *
     * @param incomingEntityClass the entities class to search the appropriate queue
     * @return returns the appropriate queue
     */
    private Queue<EIn> getPartsQueueForIncomingEntityClass(Class<? extends Entity> incomingEntityClass) {
        Queue<EIn> partsQueue;

        partsQueue = incomingEntitiesQueueContainer.get(incomingEntityClass);

        if (partsQueue == null) {
            for (Class<? extends EIn> possibleSuperClass : incomingEntitiesQueueContainer.keySet()) {
                if (possibleSuperClass.isAssignableFrom(incomingEntityClass)) {
                    partsQueue = incomingEntitiesQueueContainer.get(possibleSuperClass);
                    break;
                }
            }
        }

        return partsQueue;
    }

    /**
     * returns the Startevent of the Merger
     */
    @Override
    public Event<EIn> getStartEvent() {
        return new MergerStartEvent(getModel(), getName() + "_MergerStartEvent");
    }

    /**
     * This Method has to be overridden by the user and creates the outgoing entity. The user has the possibility to use
     * the incomming entities of the Merger to define the new entity
     *
     * @param parts a reference to the parts list
     * @return the outgoing entity which leaves the Merger
     */
    protected abstract EEx mergeEntities(Map<Class<? extends EIn>, List<EIn>> parts);

    /**
     * This method schedules the successor event with the defined transport time.
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void onServiceFinisched(EIn who) throws SuspendExecution {
        PartsContainer partsContainer = ((PartsContainer) who);

        try {
            EEx mergedEntity;

            mergedEntity = mergeEntities(partsContainer.getParts());

            this.sendTraceNote(Merger.this.getName() + " starts transporting of  " + mergedEntity.getName()
                + " to the successor (next station)");

            successorAdministration.getSuccessorEvent().clone().schedule(mergedEntity, getTransportTime());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * schedules the StationStartEvent with the internal created Partscontainer
     *
     * @param partsContainer
     */
    @SuppressWarnings("unchecked")
    private void scheduleStationStartEventForPartsContainer(PartsContainer partsContainer) {
        EIn entityToSchedule;

        entityToSchedule = ((EIn) partsContainer);

        super.getStartEvent().schedule(entityToSchedule, new TimeSpan(0));
    }

    /**
     * This method trys to compose the Partscontainer with the entities in the parts queues.
     *
     * @return the PartsContainer if succsess or null if there a not enough entities in the parts queues
     */
    private PartsContainer tryToComposePartsContainerFromAvailableParts() {
        boolean enoughPartsAvailable;
        Map<Class<? extends EIn>, List<EIn>> allParts;

        enoughPartsAvailable = true;

        for (Class<? extends Entity> entityClass : mergerConfig.keySet()) {
            Integer requiredPartsCount = mergerConfig.get(entityClass);

            if (requiredPartsCount > getPartsQueueForIncomingEntityClass(entityClass).length()) {
                enoughPartsAvailable = false;
                break;
            }
        }

        if (enoughPartsAvailable) {
            allParts = new HashMap<Class<? extends EIn>, List<EIn>>();

            for (Class<? extends EIn> entityClass : mergerConfig.keySet()) {
                Integer requiredPartsCount;
                Queue<EIn> partsQueue;
                List<EIn> parts;

                requiredPartsCount = mergerConfig.get(entityClass);
                partsQueue = getPartsQueueForIncomingEntityClass(entityClass);
                parts = new ArrayList<EIn>();

                for (int i = 0; i < requiredPartsCount; i++) {
                    EIn entity = partsQueue.first();
                    partsQueue.remove(entity);
                    parts.add(entity);
                }

                allParts.put(entityClass, parts);
            }
            return new PartsContainer(allParts);
        } else {
            return null;
        }
    }

    public void setSuccessor(Event<? super EEx> eventToScheduleWhenFinished) {
        successorAdministration.setSuccessor(eventToScheduleWhenFinished);
    }

    public void setSuccessor(HasPredecessor<? super EEx> successor) {
        successorAdministration.setSuccessor(successor);
    }

    /**
     * The MergerStartEvent takes an incomming entity and puts it into the queue for the entities type. If there is no
     * queue for the entities type the entity is discarded. If enough entites of the required ratio to create the
     * outgoing entity are available, the Startevent of the Station is scheduled.
     *
     * @author Christian Mentz
     */
    class MergerStartEvent extends Event<EIn> {

        public MergerStartEvent(Model owner, String name) {
            super(owner, name, false);
        }

        @Override
        public void eventRoutine(EIn who) {
            Queue<EIn> partsQueue;
            PartsContainer partsContainer;

            partsQueue = getPartsQueueForIncomingEntityClass(who.getClass());

            if (partsQueue == null) {
                sendWarning(
                    "The Merger received a non specified entity and did not handle it",
                    "MergerStartEvent : " + getName() + " Method: eventRoutine()",
                    "The received entity is nit defnied in the MergerConfig",
                    "Most likely you forgot to define the Mergerconfig properly or the Merger is not appropriate connected to the previous construct");

                throw new RuntimeException("Keine PartsQueue gefunden");
            }

            partsQueue.insert(who);

            partsContainer = tryToComposePartsContainerFromAvailableParts();
            if (partsContainer != null) {
                scheduleStationStartEventForPartsContainer(partsContainer);
            }
        }
    }

    /**
     * Utilitiy class which is created to run through the Station if enough entities are in the internal parts queues.
     *
     * @author Christian Mentz
     */
    class PartsContainer extends Entity {
        /**
         * the parts from the different queues
         */
        private final Map<Class<? extends EIn>, List<EIn>> parts;

        /**
         * Constructor
         *
         * @param parts a map of the parts
         */
        public PartsContainer(Map<Class<? extends EIn>, List<EIn>> parts) {
            super(Merger.this.getModel(), "PartsContainer", true);
            this.parts = parts;
        }

        /**
         * Returns the parts of entities required to create the outgoing entity
         *
         * @return the parts
         */
        public Map<Class<? extends EIn>, List<EIn>> getParts() {
            return parts;
        }

    }

}
