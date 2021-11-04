package desmoj.extensions.chaining;

import desmoj.core.dist.NumericalDist;
import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;
import desmoj.extensions.chaining.abstractions.HasPredecessor;
import desmoj.extensions.chaining.abstractions.HasSuccessor;
import desmoj.extensions.chaining.abstractions.Station;
import desmoj.extensions.chaining.report.SmartReporter;

/**
 * The source creates new Entities and send them to the successor. The Sink inherits all behavior from the station
 * except the possible restriction of the buffer queue. The user has to override the createEntity Method the create the
 * Entity the source should send to the successor.
 *
 * @param <E> The Entitiy which is created by the source
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
public abstract class Source<E extends Entity> extends Station<E> implements HasSuccessor<E> {

    /**
     * Helper class for handling the successor of this station
     */
    private final SuccessorAdministration<E> successorAdministration;
    private boolean started;

    /**
     * This Constructor sets the given times, initializes the queues and fills the free service capacity queue. If a
     * given int is lower than zero the Source assumes that there is no restriction. This applies to
     * incomingBufferQueueCapacity, maxEntitiesToHandle, parallelHandledEntities
     *
     * @param maxEntitiesToProduce     the number of max entities the station can handle
     * @param parallelProducedEntities the number of max parallel entities a station can handle
     * @param setupTime                the time the station needs to set up
     * @param productionTime           the time the station needs for the service
     * @param recoveryTime             the time the station needs tor recover
     * @param transportTime            the time the station needs to transport the entitiy to the next station
     * @param owner                    the model owner
     * @param name                     the name of the station
     * @param showInReport             should this construct be in the report
     * @param showInTrace              should this construct be in the trace
     */
    public Source(int maxEntitiesToProduce, int parallelProducedEntities, NumericalDist<?> setupTime,
                  NumericalDist<?> productionTime, NumericalDist<?> recoveryTime, NumericalDist<?> transportTime,
                  Model owner, String name, boolean showInReport, boolean showInTrace) {

        super(Integer.MAX_VALUE, maxEntitiesToProduce, parallelProducedEntities, setupTime, productionTime,
            recoveryTime, transportTime, owner, name, showInReport, showInTrace);

        successorAdministration = new SuccessorAdministration<E>(getModel(), Source.class.getSimpleName()
            + ": " + name);
    }

    public void setSuccessor(Event<? super E> eventToScheduleWhenFinished) {
        successorAdministration.setSuccessor(eventToScheduleWhenFinished);
    }

    public void setSuccessor(HasPredecessor<? super E> successor) {
        successorAdministration.setSuccessor(successor);
    }

    /**
     * Used to run through the Station class to generate proper statistics.
     *
     * @return a dummy entity
     */
    @SuppressWarnings("unchecked")
    private E createDummyEntity() {
        return (E) new SourceDummyEntity();
    }

    protected abstract E createEntity();

    /**
     * Schedules the successor event with a the created entity and the defined transport time
     */
    @Override
    protected void onServiceFinisched(E who) {
        try {
            this.sendTraceNote(Source.this.getName() + " starts transporting of  " + who.getName()
                + " to the successor (next station)");
            successorAdministration.getSuccessorEvent().clone().schedule(createEntity(), getTransportTime());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }

        insertIncomingEntityIntoQueue(createDummyEntity());
    }

    /**
     * The source starts with creating entities if this method is not called previously.
     */
    public void startCreatingEntities() {
        if (started) {
            sendWarning("SourceOld already started!", "Source : " + getName()
                    + " Method: startCreatingEntities()",
                "the source has already started to create entities",
                "dont call this method more than once on a single object");
        }

        started = true;
        for (int i = 0; i < getRemainingServiceCapacity(); i++) {
            getStartEvent().schedule(createDummyEntity(), new TimeSpan(0));
        }
    }

    @Override
    protected SmartReporter createDefaultReporter() {

        SmartReporter reporter = super.createDefaultReporter();
        reporter.overrideValueAt("N/A", 1);
        return reporter;
    }

    /**
     * Dummy entity class used internally
     *
     * @author Malte Unkrig
     */
    class SourceDummyEntity extends Entity {

        public SourceDummyEntity() {
            super(Source.this.getModel(), "SourceDummyEntity", false);
        }

    }
}
