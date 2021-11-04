package desmoj.extensions.chaining;

import desmoj.core.dist.NumericalDist;
import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.Model;
import desmoj.extensions.chaining.abstractions.HasPredecessor;
import desmoj.extensions.chaining.abstractions.HasSuccessor;
import desmoj.extensions.chaining.abstractions.Station;

/**
 * The ServiceStation represents a standard operating station which can process entities. It has exacty the same
 * behaviour like the Station but can be used in the modelling process due to no abstract modifier. This construct has
 * its right to exist to have the possibility to implement further behaviour or extensions.
 *
 * @param <E> The Entitiy which is passed through the ServiceStation
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
public class ServiceStation<E extends Entity> extends Station<E> implements HasPredecessor<E>,
    HasSuccessor<E> {

    /**
     * Helper class for handling the successor of this station
     */
    private final SuccessorAdministration<E> successorAdministration;

    /**
     * This Constructor sets the given times, initializes the queues and fills the free service capacity queue. If a
     * given int is lower than zero the station assumes that there is no resriction. This applies to
     * incomingBufferQueueCapacity, maxEntitiesToHandle, parallelHandledEntities
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
     * @param showInRrichteport           should this construct be in the report
     * @param showInTrace                 should this construct be in the trace
     */
    public ServiceStation(int incomingBufferQueueCapacity, int maxEntitiesToHandel,
                          int parallelHandledEntities, NumericalDist<?> setupTime, NumericalDist<?> serviceTime,
                          NumericalDist<?> recoveryTime, NumericalDist<?> transportTime, Model owner, String name,
                          boolean showInReport, boolean showInTrace) {

        super(incomingBufferQueueCapacity, maxEntitiesToHandel, parallelHandledEntities, setupTime,
            serviceTime, recoveryTime, transportTime, owner, name, showInReport, showInTrace);

        successorAdministration = new SuccessorAdministration<E>(getModel(),
            ServiceStation.class.getSimpleName() + ": " + name);
    }

    /**
     * schedules the succsessor event with a the given entitiy and the defined transport time
     */
    @Override
    protected void onServiceFinisched(E who) {
        try {
            this.sendTraceNote(ServiceStation.this.getName() + " starts transporting of  " + who.getName()
                + " to the successor (next station)");

            successorAdministration.getSuccessorEvent().clone().schedule(who, getTransportTime());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public void setSuccessor(Event<? super E> eventToScheduleWhenFinished) {
        successorAdministration.setSuccessor(eventToScheduleWhenFinished);
    }

    public void setSuccessor(HasPredecessor<? super E> successor) {
        successorAdministration.setSuccessor(successor);
    }

}
