package desmoj.extensions.chaining;

import java.util.ArrayList;

import desmoj.core.dist.NumericalDist;
import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.Model;
import desmoj.extensions.chaining.abstractions.HasPredecessor;
import desmoj.extensions.chaining.abstractions.HasSuccessor;

/**
 * The Tranformer takes a given entitiy and transforms it into a new Type of entity. The user has to override the method
 * transformEntity() to define the entitiy which the transformer should emit . The Tranformer inherits from the Splitter
 * and reduces its complexity for the new purpose. Within the Transformer executes the same behaviour like the
 * Splitter.
 *
 * @param <EIn> the entitiy which comes in the Tranformer
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
public abstract class Transformer<EIn extends Entity, EEx extends Entity>
    extends Splitter<EIn, EEx> implements HasSuccessor<EEx> {

    /**
     * * This Constructor sets the given times, initializes the queues and fills the free service capacity queue. If a
     * given int is lower than zero the Source assumes that there is no resriction. This applies to
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
     * @param showInReport                should this construct be in the report
     * @param showInTrace                 should this construct be in the trace
     */
    public Transformer(int incomingBufferQueueCapacity,
                       int maxEntitiesToHandle, int parallelHandledEntities,
                       NumericalDist<?> setupTime, NumericalDist<?> serviceTime,
                       NumericalDist<?> recoveryTime, NumericalDist<?> transportTime,
                       Model owner, String name, boolean showInReport, boolean showInTrace) {

        super(new ArrayList<SplitterOutput<EIn, ? extends EEx>>(),
            incomingBufferQueueCapacity, maxEntitiesToHandle,
            parallelHandledEntities, setupTime, serviceTime, recoveryTime,
            owner, name, showInReport, showInTrace);

        outputConfigurations.add(new SplitterOutput<EIn, EEx>(owner, name
            + "Output", 1, transportTime) {

            @Override
            public EEx createOutputEntity(EIn originalEntity) {
                return transformEntity(originalEntity);
            }
        });

    }

    public void setSuccessor(Event<? super EEx> eventToScheduleWhenFinished) {
        outputConfigurations.get(0).setSuccessor(eventToScheduleWhenFinished);
    }

    public void setSuccessor(HasPredecessor<? super EEx> successor) {
        outputConfigurations.get(0).setSuccessor(successor);
    }

    /**
     * This Method has to be overridden to create the output entity of the Transformer. The method gives the user the
     * possibility to use the originalEntity in the creation of the transormEntity
     *
     * @param originalEntity a reference to the original entity
     * @return the entity the Transformer sends to the next construct
     */
    protected abstract EEx transformEntity(EIn originalEntity);

}
