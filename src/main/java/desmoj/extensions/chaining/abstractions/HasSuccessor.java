package desmoj.extensions.chaining.abstractions;

import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Event;

/**
 * Interface which indicates that a station has a successor
 *
 * @param <E> The Entitiy which can be handled by the station
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
public interface HasSuccessor<E extends Entity> {

    /**
     * This method is used to set the successor event
     *
     * @param eventToScheduleWhenFinished the event to be set as successor event
     */
    void setSuccessor(Event<? super E> eventToScheduleWhenFinished);

    /**
     * This method is used to set the successor event
     *
     * @param successor the successor of the current station
     */
    void setSuccessor(HasPredecessor<? super E> successor);

}
