package desmoj.core.simulator;

/**
 * Provides the basic frame for user defined events. Derive from this class to design special real time external events
 * for a model. To use real time external events, always create a new object of this class.
 *
 * @author Felix Klueckmann
 *     <p>
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 *     with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *     <p>
 *     Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *     the specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 */

public class RealTimeEventWrapper {

    /**
     * A nanosecond timestamp that will be associated to the encapsulated ExternalEvent.
     */
    private long _nanos;

    /**
     * The ExternalEvent encapsulated by this RealTimEventWrapper
     */
    private final ExternalEvent _myExternalEvent;

    /**
     * Creates an real time wrapper for an external event. This constructor will set the nanosecond timestamp to the
     * current value of System.nanoTime().
     */
    public RealTimeEventWrapper(ExternalEvent externalEvent) {
        this._myExternalEvent = externalEvent;
        this.setNanos(System.nanoTime());
    }

    /**
     * Creates an real time wrapper for an external event.
     */
    public RealTimeEventWrapper(ExternalEvent externalEvent, long nanoTimeStamp) {
        this._myExternalEvent = externalEvent;
        this._nanos = nanoTimeStamp;
    }

    /**
     * Returns the encapsulated ExternalEvent
     */
    ExternalEvent getExternalEvent() {
        return _myExternalEvent;
    }

    /**
     * Schedules the external event to happen at the simulation time equivalent to the nanosecond timestamp of this
     * RealTimeEventWrapper.
     */
    public void realTimeSchedule() {
        _myExternalEvent.getModel().getExperiment().getScheduler()
            .realTimeSchedule(this);

    }

    /**
     * Returns the nanosecond timestamp associated with the encapsulated ExternalEvent.	 *
     */
    public long getNanos() {
        return _nanos;
    }

    /**
     * Sets the nanosecond timestamp of this RealTimeEventWrapper. Use method System.nanoTime() to get a time stamp.
     */
    public void setNanos(long nanos) {
        this._nanos = nanos;
    }

}
