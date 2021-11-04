package desmoj.extensions.applicationDomains.harbour;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.advancedModellingFeatures.Stock;
import desmoj.core.advancedModellingFeatures.WaitQueue;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.SimProcess;
import desmoj.core.simulator.TimeOperations;
import desmoj.core.simulator.TimeSpan;

/**
 * A Crane represents any kind of cranes (containerbridges, gantry crane) which loads/unloads containers(goods)
 * onto/from an external/internal transporter or into/from yard in a container terminal. Crane is derived from
 * SimProcess. Its
 * <code>lifeCycle()</code> must be implemented to spezify the behavior of the
 * Crane.
 *
 * @author Eugenia Neufeld
 *     <p>
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 *     with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *     <p>
 *     Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *     the specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 * @see SimProcess
 * @see C_Control
 */
public abstract class Crane extends SimProcess {

    /**
     * The buffer capacity of this crane.
     */
    private int bufferCapacity;

    /**
     * The number of the units this crane has unloaded.
     */
    private long unloadedUnits = 0;

    /**
     * The number of the units this crane has loaded.
     */
    private long loadedUnits = 0;

    /**
     * The number of the units this crane has to load.
     */
    private long NumToLoadUnits = 0;

    /**
     * The number of the units this crane has to unload.
     */
    private long NumToUnloadUnits = 0;

    /**
     * The ship this crane is assigned to.
     */
    private Ship ship = null;

    /**
     * The crane control this crane belongs to.
     */
    private C_Control cs;

    /**
     * The buffer of this crane.
     */
    private Stock buffer;

    /**
     * The queue where this crane has to wait for an internal Transporter (AGV, chassis) to load it.
     */
    private WaitQueue wLoadQueue = null;

    /**
     * The queue where this crane has to wait for an internal Transporter (AGV, chassis) to unload it.
     */
    private WaitQueue wUnloadQueue = null;

    /**
     * The whole loading time of this crane.
     */
    private double sumTimeLoad = 0.0;

    /**
     * The whole unloading time of this crane.
     */
    private double sumTimeUnload = 0.0;

    /**
     * Constructs a Crane which will loads/unloads containers(goods) onto/from an external/internal transporter or
     * into/from yard in a container terminal. Implement its <code>lifeCycle</code> method to specify its behavior.
     *
     * @param owner          desmoj.Model : The model this Crane is associated to.
     * @param name           java.lang.String : The name of this Crane.
     * @param bufferCapacity int : The maximum number of containers(goods) this Crane can store in its buffer.
     * @param cs             <code>C_Control</code>: The crane control which this Crane
     *                       belongs to.
     * @param showInTrace    boolean : Flag, if this Crane should produce a trace output or not.
     */
    public Crane(Model owner, String name, int bufferCapacity, C_Control cs,
                 boolean showInTrace) {

        super(owner, name, showInTrace); // make a sim-process

        // check the capacity of the buffer
        if (bufferCapacity <= 0) {
            sendWarning(
                "The given number of capacity  for a buffer of a crane  is "
                    + "negative or zero. This number  will be set to one!",
                getClass().getName() + ": " + getQuotedName()
                    + ", Constructor: Crane(Model owner, String name, "
                    + "int bufferCapacity, boolean showInTrace)",
                "Tne negative number for the capacity  of a buffer or zero for a crane does not make sense.",
                "Make sure to provide a valid positive number for buffer capacity for a crane "
                    + "for the Crane to be constructed.");

            this.bufferCapacity = 1;

        }
        // set the attributes
        this.bufferCapacity = bufferCapacity;

        this.cs = cs;

        // make a buffer
        this.buffer = new Stock(owner, this.getQuotedName() + "_buffer", 0,
            this.bufferCapacity, false, true);

    } // end of constructor

    /**
     * Constructs a Crane which will loads/unloads containers(goods) onto/from an external/internal transporter or
     * into/from yard in a container terminal. Implement its <code>lifeCycle</code> method to specify its behavior.
     *
     * @param owner       desmoj.Model : The model this Crane is associated to.
     * @param name        java.lang.String : The name of this Crane.
     * @param cs          <code>C_Control</code>: The crane control which this Crane
     *                    belongs to.
     * @param showInTrace boolean : Flag, if this Crane should produce a trace output or not.
     */
    public Crane(Model owner, String name, C_Control cs, boolean showInTrace) {

        super(owner, name, showInTrace); // make a sim-process

        this.cs = cs;

        // make a WaitQueue for the waiting there for an internal transporter to
        // unload/load it
        this.wLoadQueue = new WaitQueue(owner, name + "_WaitLoadQueue", 0, 1,
            0, 0, false, true);

        // make a WaitQueue for the waiting there for an internal transporter to
        // unload/load it
        this.wUnloadQueue = new WaitQueue(owner, name + "_WaitUnloadQueue", 0,
            1, 0, 0, false, true);
    }

    /**
     * This method describes the loading action of this crane. In this simple case only the time it takes to load a
     * container (an unit). The number and time of loaded units were counted in this method.
     *
     * @param time TimeSpan : The loading time.
     */
    public void load(TimeSpan time) throws SuspendExecution {

        // trace that the crane is loading now
        if (currentlySendTraceNotes()) {
            sendTraceNote("loads an unit for "
                + time
                + " until "
                + TimeOperations.add(presentTime(), time));
        }

        // skip the trace
        skipTraceNote();
        // hold for the loading time
        hold(time);

        // change the statistics of the crane
        this.loadedUnits = this.loadedUnits + 1;
        this.cs.getCS().addSumNumLoadedUnits(1);
        this.cs.getCS().addSumLoadTime(time.getTimeAsDouble());
        this.sumTimeLoad = this.sumTimeLoad + time.getTimeAsDouble();

    }

    /**
     * This method describes the unloading action of this crane. In this simple case only the time it takes to unload a
     * container (an unit). The number and time of unloaded units were counted in this method.
     *
     * @param time TimeSpan : The unloading time.
     */
    public void unload(TimeSpan time) throws SuspendExecution {

        // trace that the crane is unloading now
        if (currentlySendTraceNotes()) {
            sendTraceNote("unloads an unit  for "
                + time
                + " until "
                + TimeOperations.add(presentTime(), time));
        }

        // skip the trace
        skipTraceNote();

        // hold for the unloading time
        hold(time);

        // change the statistics of the crane
        this.unloadedUnits = this.unloadedUnits + 1;
        this.cs.getCS().addSumNumUnloadedUnits(1);
        this.cs.getCS().addSumUnloadTime(time.getTimeAsDouble());
        this.sumTimeUnload = this.sumTimeUnload + time.getTimeAsDouble();

    }

    /**
     * This method describes the driving of the crane ( crane speader). In this simple case only the time it takes to
     * drive.
     *
     * @param time TimeSpan : The time to drive.
     */
    public void drive(TimeSpan time) throws SuspendExecution {

        // trace with the note that the crane is driving now
        if (currentlySendTraceNotes()) {
            sendTraceNote("drives for "
                + time
                + " until "
                + TimeOperations.add(presentTime(), time));
        }
        skipTraceNote();
        // hold for the driving time
        hold(time);
    }

    /**
     * Returns the number of the units that this Crane has loaded.
     *
     * @return long : The number of the loaded of this Crane.
     */
    public long getNumLoadedUnits() {

        return this.loadedUnits;
    }

    /**
     * Returns the number of the units that this Crane has unloaded.
     *
     * @return long : The number of the unloaded of this Crane.
     */
    public long getNumUnloadedUnits() {

        return this.unloadedUnits;
    }

    /**
     * To reset the statistics about unloaded und loaded units of this Crane.
     */
    public void reset() {

        this.loadedUnits = 0;
        this.unloadedUnits = 0;

    }

    /**
     * Returns the ship this Crane is assigned to.
     *
     * @return <code>Ship</code>: The ship of this Crane.
     */
    public Ship getShip() {

        return ship;
    }

    /**
     * Sets the ship this crane is assigned to.
     *
     * @param s Ship : The new ship for this crane .
     */
    public void setShip(Ship s) {

        this.ship = s;
    }

    /**
     * Returns the crane control this crane belongs to.
     *
     * @return <code>C_Control</code>: The crane control of this Crane.
     */
    public C_Control getCS() {

        return cs;
    }

    /**
     * Sets the crane control of this crane to a new value.
     *
     * @param cs <code>C_Control</code>: The new crane control for this
     *           crane.
     */
    public void setCS(C_Control cs) {

        this.cs = cs;
    }

    /**
     * Returns the buffer of this crane.
     *
     * @return <code>Stock</code>: The buffer of this Crane.
     */
    public Stock getBuffer() {

        return this.buffer;
    }

    /**
     * Sets the buffer of this crane to a new value.
     *
     * @param b <code>Stock</code>: The new buffer for this crane.
     */
    public void setBuffer(Stock b) {

        // if the buffer has been already used
        if ((buffer.getProducers() > 0) || (buffer.getConsumers() > 0)) {
            sendWarning(
                "Attempt to change the buffer that already in use "
                    + " by a crane. The buffer remain unchanged!",
                "Crane: " + this.getName()
                    + " Method: void setBuffer (Stock b)",
                "The buffer which has already be used can not"
                    + " be changed afterwards.",
                "Do not try to change the buffer which might have been"
                    + " used already. Or reset the buffer of the crane before changing it.");

            return; // without setting of the new buffer
        }
        this.buffer = b;
    }

    /**
     * Returns the number of the units this crane has to load.
     *
     * @return long : The number of the units this crane has to load.
     */
    public long getNumToLoadUnits() {

        return this.NumToLoadUnits;
    }

    /**
     * Sets the number of the units this crane to has to load.
     *
     * @param n long : The number of the units this crane has to load.
     */
    public void setNumToLoadUnits(long n) {

        this.NumToLoadUnits = n;
    }

    /**
     * Returns the number of the units this crane has to unload.
     *
     * @return long : The number of the units this crane has to unload.
     */
    public long getNumToUnloadUnits() {

        return this.NumToUnloadUnits;
    }

    /**
     * Sets the number of the units this crane to has to unload.
     *
     * @param n long : The number of the units this crane has to unload.
     */
    public void setNumToUnloadUnits(long n) {

        this.NumToUnloadUnits = n;
    }

    /**
     * This method describes the loading action of this crane if has to wait for an internal transporter before loading
     * it. In this simple case only the time it takes to load the internal transporter with the
     * <code>Loading</code> action.
     *
     * @param loading <code>Loading</code>: The loading process.
     */
    public boolean load(Loading loading) throws SuspendExecution {

        return this.wLoadQueue.cooperate(loading);
    }

    /**
     * This method describes the unloading action of this crane if has to wait for an internal transporter before
     * unloading it. In this simple case only the time it takes to unload the internal transporter with the
     * <code>Unloading</code> action.
     *
     * @param unloading <code>Unloading</code>: The unloading process.
     */
    public boolean unload(Unloading unloading) throws SuspendExecution {

        return this.wUnloadQueue.cooperate(unloading);

    }

    /**
     * This method must be used by an internal transporter if he wants to be unloaded by this crane. If the crane is not
     * idle, the internal trasnporter will wait by the crane for his unloading.
     */
    public boolean waitOnUnloading() throws SuspendExecution {

        return this.wUnloadQueue.waitOnCoop();
    }

    /**
     * This method must be used by an internal transporter if he wants to be loaded by this crane. If the crane is not
     * idle, the internal trasnporter will wait by the crane for his loading.
     */
    public boolean waitOnLoading() throws SuspendExecution {

        return this.wLoadQueue.waitOnCoop();
    }

    /**
     * Returns the queue where this Crane waits to load an internal transporter.
     *
     * @return <code>WaitQueue</code>: The waiting load queue of this Crane.
     */
    public WaitQueue getWLoadQueue() {

        return this.wLoadQueue;
    }

    /**
     * Sets the queue where this crane waits for an internal transpoter to load it to a new value.
     *
     * @param wq <code>WaitQueue</code>: The waiting load queue for this
     *           crane .
     */
    public void setWLoadQueue(WaitQueue wq) {

        // if the queue is already in use
        if ((this.wLoadQueue.getObservations() > 0)
            || (this.wLoadQueue.getSlaveQueue().getObservations() > 0)) {
            sendWarning(
                "Attempt to change the wait load queue that already in use "
                    + " by a crane. The wait queue remain unchanged!",
                "Crane: " + this.getName()
                    + " Method: void setWLoadQueue (WaitQueue wq)",
                "The wait queue which has already be used can not"
                    + " be changed afterwards.",
                "Do not try to change the wait load queue which might have been"
                    + " used already. Or reset this queue of the crane before changing it.");

            return; // without setting of the new wait queue
        }

        this.wLoadQueue = wq;
    }

    /**
     * Returns the queue where this Crane waits to unload an internal transporter.
     *
     * @return <code>WaitQueue</code>: The waiting unload queue of this
     *     Crane.
     */
    public WaitQueue getWUnloadQueue() {

        return this.wUnloadQueue;
    }

    /**
     * Sets the queue where this crane waits for an internal transpoter to unload it to a new value.
     *
     * @param wq <code>WaitQueue</code>: The waiting unload queue for this
     *           crane .
     */
    public void setWUnloadQueue(WaitQueue wq) {

        // if the queue is already in use
        if ((this.wUnloadQueue.getObservations() > 0)
            || (this.wUnloadQueue.getSlaveQueue().getObservations() > 0)) {
            sendWarning(
                "Attempt to change the wait unload queue that already in use "
                    + " by a crane. The wait queue remain unchanged!",
                "Crane: " + this.getName()
                    + " Method: void setWUnloadQueue (WaitQueue wq)",
                "The wait queue which has already be used can not"
                    + " be changed afterwards.",
                "Do not try to change the wait unload queue which might have been"
                    + " used already. Or reset this queue of the crane before changing it.");

            return; // without setting of the new wait queue
        }

        this.wUnloadQueue = wq;
    }

    /**
     * Returns the whole loading time of this crane.
     *
     * @return double : The whole loading time of this Crane.
     */
    public double getLoadTime() {

        return this.sumTimeLoad;
    }

    /**
     * Returns the whole unloading time of this crane.
     *
     * @return double : The whole unloading time of this Crane.
     */
    public double getUnloadTime() {

        return this.sumTimeUnload;
    }

}