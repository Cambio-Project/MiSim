package desmoj.extensions.chaining;

import desmoj.core.dist.NumericalDist;
import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;
import desmoj.extensions.chaining.abstractions.HasPredecessor;
import desmoj.extensions.chaining.abstractions.HasSuccessor;

/**
 * The SplitterOutput is used in the Splitter context to compose a output config fpr the splitter construct.
 *
 * @param <EIn> the entitiy which comes in the splitter
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
public abstract class SplitterOutput<EIn extends Entity, EEx extends Entity>
    implements HasSuccessor<EEx> {

    /**
     * the number of output parts of this output
     */
    private final int numberOfOutputPartsPerOriginalEntity;
    /**
     * the transport time of this output
     */
    private final NumericalDist<?> transportTime;
    /**
     * Helper class for handeling the successor of this station
     */
    private final SuccessorAdministration<EEx> successorAdministration;

    /**
     * Constructor to create a Splitteroutput which can be added to an List which than can passed to the Splitter
     *
     * @param numberOfOutputPartsPerOriginalEntity the number of output parts of this output
     * @param transportTime                        the transport time of this output
     */
    public SplitterOutput(Model owner, String name,
                          int numberOfOutputPartsPerOriginalEntity,
                          NumericalDist<?> transportTime) {
        this.numberOfOutputPartsPerOriginalEntity = numberOfOutputPartsPerOriginalEntity;
        this.transportTime = transportTime;

        successorAdministration = new SuccessorAdministration<EEx>(owner,
            SplitterOutput.class.getSimpleName() + ": " + name);
    }

    public void setSuccessor(Event<? super EEx> eventToScheduleWhenFinished) {
        successorAdministration.setSuccessor(eventToScheduleWhenFinished);
    }

    public void setSuccessor(HasPredecessor<? super EEx> successor) {
        successorAdministration.setSuccessor(successor);
    }

    /**
     * This Method has to be overridden by the user to set the output entity
     *
     * @param originalEntity reference to the original entity
     * @return the output entitiy
     */
    public abstract EEx createOutputEntity(EIn originalEntity);

    /**
     * gets the number of parts
     *
     * @return the number of parts
     */
    public int getNumberOfOutputPartsPerOriginalEntity() {
        return numberOfOutputPartsPerOriginalEntity;
    }

    /**
     * gets the succsessor event which has been set by the user
     *
     * @return the succsessor event
     */
    public Event<? super EEx> getSuccessorEvent() {
        return successorAdministration.getSuccessorEvent();
    }

    /**
     * gets the transport time of the station
     *
     * @return the transport time
     */
    public TimeSpan getTransportTime() {
        if (transportTime == null) {
            return new TimeSpan(0);
        } else {
            return transportTime.sampleTimeSpan();
        }
    }

}