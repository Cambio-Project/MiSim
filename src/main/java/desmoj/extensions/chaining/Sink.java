package desmoj.extensions.chaining;

import desmoj.core.dist.NumericalDist;
import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Model;
import desmoj.extensions.chaining.abstractions.HasPredecessor;
import desmoj.extensions.chaining.abstractions.Station;
import desmoj.extensions.chaining.report.SmartReporter;

/**
 * The Sink represents the end of a production line and has no successor. The Sink inherits all behavior from the
 * Station except the transport time.
 *
 * @param <E> The Entitiy which can be handled by the Sink
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
public class Sink<E extends Entity> extends Station<E> implements
    HasPredecessor<E> {
    /**
     * This Constructor sets the given times, initializes the queues and fills the free service capacity queue. If a
     * given int is lower than zero the sink assumes that there is no restriction. This applies to
     * incomingBufferQueueCapacity, maxEntitiesToHandle, parallelHandledEntities
     *
     * @param incomingBufferQueueCapacity the capatiy of the income queue
     * @param maxEntitiesToHandle         the number of max entities the station can handle
     * @param parallelHandledEntities     the number of max parallel entities a station can handle
     * @param setupTime                   the time the station needs to set up
     * @param serviceTime                 the time the station needs for the service
     * @param recoveryTime                the time the station needs to recover
     * @param owner                       the model owner
     * @param name                        the name of the station
     * @param showInReport                should this construct be in the report
     * @param showInTrace                 should this construct be in the trace
     */
    public Sink(int incomingBufferQueueCapacity, int maxEntitiesToHandle,
                int parallelHandledEntities, NumericalDist<?> setupTime,
                NumericalDist<?> serviceTime, NumericalDist<?> recoveryTime,
                Model owner, String name, boolean showInReport, boolean showInTrace) {

        super(incomingBufferQueueCapacity, maxEntitiesToHandle,
            parallelHandledEntities, setupTime, serviceTime, recoveryTime,
            null, owner, name, showInReport, showInTrace);

    }

    @Override
    protected SmartReporter createDefaultReporter() {

        SmartReporter reporter = super.createDefaultReporter();
        reporter.overrideValueAt("N/A", 2);
        return reporter;
    }

}