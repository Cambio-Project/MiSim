package desmoj.extensions.chaining;

import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.Model;
import desmoj.extensions.chaining.abstractions.HasPredecessor;

/**
 * Helper class to setup successor behaviour.
 *
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
public class SuccessorAdministration<E extends Entity> {

    private final Model owner;
    private final String location;
    /**
     * the successor event to be scheduled
     */
    private Event<? super E> successorEvent;

    public SuccessorAdministration(Model owner, String location) {
        this.owner = owner;
        this.location = location;
    }

    public void setSuccessor(Event<? super E> eventToScheduleWhenFinished) {
        successorEvent = eventToScheduleWhenFinished;
    }

    public void setSuccessor(HasPredecessor<? super E> successor) {
        setSuccessor(successor.getStartEvent());
    }

    public Event<? super E> getSuccessorEvent() {
        if (successorEvent != null) {
            return successorEvent;
        } else {
            owner.sendWarning(
                "Not successor scheduled.",
                location,
                "No successor has been scheduled for this chainable construct. A dummy event is created to avaiod a NullPointerException.",
                "Please set a successor for all your chainable constructs.");
            return new Event<E>(owner, "DummyEvent", true) {
                @Override
                public void eventRoutine(E who) {
                    sendTraceNote("Dummy event executed because no successor(event) was set.");
                }
            };
        }

    }

}
