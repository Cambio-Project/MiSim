package desmoj.extensions.chaining;

import java.util.List;

import desmoj.core.dist.NumericalDist;
import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;
import desmoj.extensions.chaining.abstractions.HasPredecessor;
import desmoj.extensions.chaining.abstractions.Station;

/**
 * The Splitter takes a given Entity and splits it in the parts defined in the List of SplitterOutputs. The Splitter
 * inherits all behavior from the station except the transport time which is defined in the SplitterOutput.
 *
 * @param <EIn> the entity which comes in the splitter
 * @param <EEx> the entity which leaves the splitter
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
public class Splitter<EIn extends Entity, EEx extends Entity> extends Station<EIn> implements
    HasPredecessor<EIn> {
    /**
     * The List of Splitteroutputs
     */
    protected final List<SplitterOutput<EIn, ? extends EEx>> outputConfigurations;

    /**
     * This Constructor sets the given times, initializes the queues and fills the free service capacity queue. If a
     * given int is lower than zero the station assumes that there is no resriction. This applies to
     * incomingBufferQueueCapacity, maxEntitiesToHandle, parallelHandledEntities
     *
     * @param outputConfigurations        the List of Splitteroutputs which contains the information of the outgoing
     *                                    entities and its ratio.
     * @param incomingBufferQueueCapacity the capatiy of the income queue
     * @param maxEntitiesToHandle         the number of max entities the station can handle
     * @param parallelHandledEntities     the number of max parallel entities a station can handle
     * @param setupTime                   the time the station needs to set up
     * @param serviceTime                 the time the station needs for the service
     * @param recoveryTime                the time the station needs tor recover
     * @param owner                       the model owner
     * @param name                        the name of the station
     * @param showInReport                should this construct be in the report
     * @param showInTrace                 should this construct be in the trace
     */
    public Splitter(List<SplitterOutput<EIn, ? extends EEx>> outputConfigurations,
                    int incomingBufferQueueCapacity, int maxEntitiesToHandel, int parallelHandledEntities,
                    NumericalDist<?> setupTime, NumericalDist<?> serviceTime, NumericalDist<?> recoveryTime,
                    Model owner, String name, boolean showInReport, boolean showInTrace) {

        super(incomingBufferQueueCapacity, maxEntitiesToHandel, parallelHandledEntities, setupTime,
            serviceTime, recoveryTime, null, owner, name, showInReport, showInTrace);

        this.outputConfigurations = outputConfigurations;
    }

    @Override
    protected TimeSpan getTransportTime() {
        sendWarning("The Transporttime is defined in the Splitteroutput class", "Splitter : " + getName()
                + " Method: getTransportTime()", "the transport time is not defnied in the Splitter class.",
            "dont call this method. Use the Splitteroutput method");
        return null;
    }

    /**
     * schedules the succsessor events which a defined in the Splitteroutput class with the defined transport time
     */
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void onServiceFinisched(EIn who) {
        for (SplitterOutput<EIn, ?> outputConfig : outputConfigurations) {
            for (int i = 0; i < outputConfig.getNumberOfOutputPartsPerOriginalEntity(); i++) {
                try {
                    Event eventToSchedule;
                    Entity outputEntity;

                    eventToSchedule = outputConfig.getSuccessorEvent().clone();
                    outputEntity = outputConfig.createOutputEntity(who);

                    this.sendTraceNote(Splitter.this.getName() + " starts transporting of  "
                        + outputEntity.getName() + " to the successor (next station)");

                    eventToSchedule.schedule(outputEntity, outputConfig.getTransportTime());
                } catch (CloneNotSupportedException ex) {
                    throw new RuntimeException(ex);
                }

            }
        }

    }

}
