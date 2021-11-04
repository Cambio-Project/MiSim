package desmoj.extensions.applicationDomains.harbour;

import desmoj.core.report.Reporter;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.Reportable;
import desmoj.core.simulator.SimProcess;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeOperations;
import desmoj.core.statistic.StatisticObject;

/**
 * Block is the place where containers (or other kind of goods) can be stored by transpoters or cranes and retrieved by
 * transporters or cranes. The storing is possible using <code>store()</code> of the Block. With the
 * <code>retrieve()</code> a container can be retrieved from the Block. A
 * Block is a part of the container <code>Yard</code>. There are no queues for processes want to store or retrieve one
 * container. If no or not enough containers are available to retrieve, the tries to do that will be refused. The Block
 * has a certain capacity that measures in TEUs. So a 20-foot-container weigths one TEU and 40-foot-container 2 TEUs. If
 * the Block is filled to it's capacity the tries to store a container there will be refused. Yard is part of the
 * composite design pattern as described in [Gamm97] page 163 in which it represents the the component class. Block is
 * derived from Reportable, which provides the report functionality for the Block.
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
 * @see Reportable
 */
public class Block extends Reportable {

    /**
     * The number of TEUs that are free, can be used for the storing.
     */
    protected long avail_to_reserve;
    /**
     * The number of TEUs that are aready occupied and are avalaible for the retrieving.
     */
    protected long avail_to_plan;
    /**
     * The current capacity of the Block.
     */
    protected long currentCapacity;
    /**
     * The max. capacity of the Block.
     */
    protected long capacity;
    /**
     * The initial TEUs in the Block
     */
    protected long init = 0;
    /**
     * The number of processes having used this Block to store products
     */
    protected long producers = 0;
    /**
     * The number of consumers having retrieved containers from this Block
     */
    protected long consumers = 0;
    /**
     * The minimum number of TEUs in the Block
     */
    protected long min;
    /**
     * The maximum number of TEUs in the Block
     */
    protected long max;
    /**
     * Weighted sum of available TEUs in the Block over the time (must be divided by the total time to get the average
     * available TEUs!)
     */
    protected double wSumAvail = 0.0;
    /**
     * Indicates the method where something has gone wrong. Is passed as a parameter to the method
     * <code>checkProcess()</code>.
     */
    protected String where;
    /**
     * The Holding area this block is assigned to.
     */
    protected HoldingArea ho = null;
    /**
     * Te conatiner typ of this block: the typ of the containers that can be stored/retrieved from this block. 0-empty,
     * 1- normal, 2-reefer, 4-overlarge, 5- danger containers
     */
    protected int ctyp;
    /**
     * The typ of the Block: 0-export,1-import,2-mixed
     */
    private int typ;
    /**
     * The time in the simulation this Block was used for last.
     */
    private TimeInstant lastUsage;

    /**
     * Constructor for a Block of a certain typ with a certain capacity.
     *
     * @param owner        desmoj.Model : The model this Block is associated to.
     * @param name         java.lang.String : The Block's name.
     * @param typ          int : The Block's type. It can be only 0 - for export containers, 1- for import containers or
     *                     2 - mixed: for import and export containers.
     * @param capacity     long : The maximum capacity (TEU) of this Block.
     * @param showInReport boolean : Flag, if this Block should produce a report or not.
     * @param showInTrace  boolean : Flag for trace to produce trace messages.
     */
    public Block(Model owner, String name, int typ, long capacity,
                 boolean showInReport, boolean showInTrace) {

        super(owner, name, showInReport, showInTrace); // make a Reportable

        // reset the Block
        reset();

        // check the typ
        if ((typ < 0) || ((typ != 1) && (typ != 2) && (typ != 0))) {
            sendWarning(
                "The given typ for  a block  is " + "not right. ",
                getClass().getName()
                    + ": "
                    + getQuotedName()
                    + ", Constructor: Block(Model owner, String name, "
                    + "int typ, long capacity, boolean showInReport,boolean showInTrace)",
                "Tne negative value of the typ or typ not 1 or 0,2 for a block does not make sense.",
                "Make sure to provide a valid positive typ for a block "
                    + "for the Block to be constructed.");
            return;
        }
        // set the typ
        this.typ = typ;

        // check the capacity
        if (capacity == 0) {
            capacity = Long.MAX_VALUE;
        }

        if (capacity < 0) {
            sendWarning("Attempt to construct a Block with a negativ capacity."
                    + " The capacity will be converted to the positive value!",
                "Block: " + getName()
                    + " Constructor: Block(Model owner, String name, "
                    + "int typ, " + "long capacity, "
                    + "boolean showInReport, boolean showInTrace)",
                "A negative capacity does not make sense for a block.",
                "Make sure to initialize a Block always with a positive capacity.");

            // set it to the positive value of capacity
            capacity = Math.abs(capacity);
        }

        this.capacity = capacity;
        this.currentCapacity = 0;
        this.avail_to_reserve = this.capacity;
        this.avail_to_plan = 0;
        this.max = 0;
        this.min = 0;

    }

    /**
     * Constructor for a Block with an initial number of TEUs and a certain capacity.
     *
     * @param owner        desmoj.Model : The model this Block is associated to.
     * @param name         java.lang.String : The Block's name.
     * @param typ          int : The Block's type. It can be only 0 - for export containers, 1- for import containers or
     *                     2 - mixed: for import and export containers.
     * @param capacity     long : The maximum capacity of this Block.
     * @param init         long : The initial occupied capacity of the Block starts with. Must be positive!
     * @param showInReport boolean : Flag, if this Block should produce a report or not.
     * @param showInTrace  boolean : Flag for trace to produce trace messages.
     */
    public Block(Model owner, String name, int typ, long capacity, long init,
                 boolean showInReport, boolean showInTrace) {

        super(owner, name, showInReport, showInTrace); // make a Reportable

        // reset the block
        reset();

        // check the typ
        if ((typ < 0) || ((typ != 1) && (typ != 2) && (typ != 0))) {
            sendWarning(
                "The given typ for  a block  is " + "not right. ",
                getClass().getName()
                    + ": "
                    + getQuotedName()
                    + ", Constructor: Block(Model owner, String name, "
                    + "int typ, long capacity,long init, boolean showInReport,boolean showInTrace)",
                "Tne negative value of the typ or typ not 1 or 0,2 for a block does not make sense.",
                "Make sure to provide a valid positive typ for a block "
                    + "for the Block to be constructed.");
            return;
        }
        // set the typ
        this.typ = typ;

        // check the capacity
        if (capacity == 0) {
            capacity = Long.MAX_VALUE;
        }

        if (capacity < 0) {
            sendWarning("Attempt to construct a Block with a negativ capacity."
                    + " The capacity will be converted to the positive value!",
                "Block: " + getName()
                    + " Constructor: Block(Model owner, String name, "
                    + "int typ, " + "long capacity, "
                    + "boolean showInReport, boolean showInTrace)",
                "A negative capacity does not make sense for a block.",
                "Make sure to initialize a Block always with a positive capacity.");

            // set it to the positive value of capacity
            capacity = Math.abs(capacity);
        }

        // check the initial occupied capacity
        if (init < 0) {
            sendWarning(
                "Attempt to construct a Block with a negativ initialization."
                    + " The capacity will be converted to the positive value!",
                "Block: " + getName()
                    + " Constructor: Block(Model owner, String name, "
                    + "int typ, " + "long capacity,  long init"
                    + "boolean showInReport, boolean showInTrace)",
                "A negative initialization does not make sense for a block.",
                "Make sure to initialize a Block always with a positive capacity.");

            // set it to the positive value of capacity
            init = Math.abs(init);
        }

        this.capacity = capacity;
        this.currentCapacity = init;
        this.init = init;
        this.avail_to_reserve = this.capacity - this.init;
        this.avail_to_plan = init;
        this.max = init;
        this.min = init;

    }

    /**
     * Constructor for a Block with an initial number of TEUs, a certain capacity and a certain container typ.
     *
     * @param owner        desmoj.Model : The model this Block is associated to.
     * @param name         java.lang.String : The Block's name.
     * @param typ          int : The Block's type. It can be only 0 - for export containers, 1- for import containers or
     *                     2 - mixed: for import and export containers.
     * @param capacity     long : The maximum capacity of this Block.
     * @param ctyp         int : The container type of this Block. It can be only 0- for empty, 1- for normal, 2- for
     *                     reefer, 4- for overlarge, 5- for danger containers.
     * @param init         long : The initial occupied capacity of the Block starts with. Must be positive!
     * @param showInReport boolean : Flag, if this Block should produce a report or not.
     * @param showInTrace  boolean : Flag for trace to produce trace messages.
     */
    public Block(Model owner, String name, int typ, long capacity, long init,
                 int ctyp, boolean showInReport, boolean showInTrace) {

        this(owner, name, typ, capacity, init, showInReport, showInTrace); // make
        // a
        // Block

        // check the container typ
        if ((ctyp < 0)
            || ((ctyp != 1) && (ctyp != 2) && (ctyp != 0) && (ctyp != 3)
            && (ctyp != 4) && (ctyp != 5))) {
            sendWarning(
                "The given container typ for  a block  is " + "not right. ",
                getClass().getName()
                    + ": "
                    + getQuotedName()
                    + ", Constructor: Block(Model owner, String name, "
                    + "int typ, long capacity,long init, int ctyp, boolean showInReport,boolean showInTrace)",
                "Tne negative value of the container typ or container typ not 1 or 0,2,3,4,5 for a block does not make sense.",
                "Make sure to provide a valid positive container typ for a block "
                    + "for the Block to be constructed.");
            return;
        }

        this.ctyp = ctyp;
    }

    /**
     * Gets the maximum number of TEUs in the Block.
     *
     * @return long : The maximum number of TEUs in the Block.
     */
    public long getMaximum() {

        return this.max;
    }

    /**
     * Gets the minimum number of TEUs (goods) in the Block.
     *
     * @return long : The minimum number of TEUs (goods) in the Block.
     */
    public long getMinimum() {

        return this.min;
    }

    /**
     * Gets the occupation rate of the Block.
     *
     * @return double : The occupation rate of the Block.
     */
    public double OccupRate() {

        double occupRate = (double) this.getCurrentCapacity()
            / this.getCapacity();
        return occupRate;
    }

    /**
     * Returns a Reporter to produce a report about this Block.
     *
     * @return desmoj.report.Reporter : The Reporter reporting about the statistics of this Block.
     */
    public Reporter createDefaultReporter() {

        return new desmoj.extensions.applicationDomains.harbour.report.BlockReporter(
            this);
    }

    /**
     * Method for producers to make the Block store a number of n TEUs (a container). When the capacity of the block can
     * not hold the additional incoming TEUs the try to store will be refused.
     *
     * @param n long : The weight of the container to be stored in this Block. n must be positive.
     * @return boolean : Is <code>true</code> if the container can been stored successfully, <code>false</code>
     *     otherwise.
     */
    public boolean store(long n) {

        where = "boolean store (long n)";

        SimProcess currentProcess = currentSimProcess();

        if (!checkProcess(currentProcess, where)) // if the current process
        {
            return false;
        }

        if (this.currentCapacity + n > this.capacity) {
            sendWarning(
                "Attempt to store  units "
                    + " in a Block that is full or has not enough free place. The attempted action is ignored!",
                "Block: " + getName() + " Method: " + where,
                "It does not make sense to store that way in a Block.",
                "Make sure to find another Block to store  units.");

            return false; // go to where you came from;

        }

        this.currentCapacity = this.currentCapacity + n;

        if (this.currentCapacity > max) {
            this.max = this.currentCapacity;
        }

        this.producers = this.producers + 1;

        this.avail_to_plan = this.avail_to_plan + n;

        updateStatistics();

        // tell in the trace what the process is storing in the Block
        if (currentlySendTraceNotes()) {
            sendTraceNote("stores " + n + " units to '" + this.getName() + "'");
        }

        // for debugging purposes
        if (currentlySendDebugNotes()) {
            sendDebugNote("stores " + n + " units from "
                + currentProcess.getQuotedName() + "<br>" + "and has now "
                + getCurrentCapacity() + " units in the block.");
        }

        return true;
    }

    /**
     * Returning the average number of TEUs available in the Block over the time since the last reset of the Block.
     *
     * @return double : The average number of TEUs available in the Block over the time since the last reset of the
     *     Block.
     */
    public double avgCapacity() {

        TimeInstant now = presentTime(); // what is the time?
        // how long since the last reset
        double diff = now.getTimeAsDouble() - resetAt().getTimeAsDouble();

        // update the weighted sum of available TEUs
        double wSumAvl = wSumAvail
            + ((double) currentCapacity * (now.getTimeAsDouble() - lastUsage
            .getTimeAsDouble()));

        if (diff < TimeOperations.getEpsilonSpan().getTimeAsDouble()) // diff is not long enough
        {
            sendWarning("A Division-by-Zero error occured in a calculation. "
                    + "The UNDEFINED Value: -1.0 is returned as result.",
                "Block: " + getName() + " Method: double avgCapacity ()",
                "The Time difference is shorter than epsilon.",
                "Make sure not to use avgCapacity() right after a reset.");
            return -1;
        }
        // return the rounded average
        return StatisticObject.round(wSumAvl / diff);
    }

    /**
     * Updates the statistics for the current capacity.
     */
    protected void updateStatistics() {
        TimeInstant now = presentTime(); // what is the time?

        this.wSumAvail = this.wSumAvail
            + (this.currentCapacity * (now.getTimeAsDouble() - lastUsage
            .getTimeAsDouble()));

        this.lastUsage = now;

    }

    /**
     * Method for consumers to make the Block retrieve a number of n TEUs (a container). When the block is empty the try
     * to retrieve will be refused.
     *
     * @param n long : The weight of the container to be stored in this Block. n must be positive.
     * @return boolean : Is <code>true</code> if the container can been stored successfully, <code>false</code>
     *     otherwise.
     */
    public boolean retrieve(long n) {

        where = "boolean retrieve (long n)";

        SimProcess currentProcess = currentSimProcess();

        if (!checkProcess(currentProcess, where)) // if the current process
        {
            return false;
        }

        if (this.currentCapacity < n) {
            sendWarning(
                "Attempt to retrieve  units "
                    + " from a Block that is empty. The attempted action is ignored!",
                "Block: " + getName() + " Method: " + where,
                "It does not make sense to retrieve that way from a Block.",
                "Make sure to find another Block to retrieve units.");
            return false; // go to where you came from;

        }
        this.currentCapacity = this.currentCapacity - n;
        if (this.currentCapacity < this.min) {
            this.min = this.currentCapacity;
        }
        this.consumers = this.consumers + 1;
        this.avail_to_reserve = this.avail_to_reserve + n;

        updateStatistics();

        // tell in the trace what the process is storing in the Block
        if (currentlySendTraceNotes()) {
            sendTraceNote("retrieves " + n + " units from '" + this.getName()
                + "'");
        }

        // for debugging purposes
        if (currentlySendDebugNotes()) {
            sendDebugNote("retrieves " + n + " units for "
                + currentProcess.getQuotedName() + "<br>" + "and has now "
                + getCurrentCapacity() + " units in the block.");
        }

        return true;
    }

    /**
     * Gets the number of the processes that stored in this Block.
     *
     * @return long : producers number.
     */
    public long getProducers() {

        return this.producers;

    }

    /**
     * Gets the number of the processes that retrieved from this Block.
     *
     * @return long : consumers number.
     */
    public long getConsumers() {

        return this.consumers;
    }

    /**
     * Method for consumers to make the Block plan a number of n TEUs (a place for a container) to be retrieved there
     * later. When all the containers are already planed for the later retrieving the try to plan will be refused.
     *
     * @param n long : The number of TEUs that a container that is in the Block weights. n must be positive.
     * @return boolean : Is <code>true</code> if a container can been planed successfully, <code>false</code> otherwise.
     */
    public boolean plan(long n) {

        where = "boolean plan (long n)";

        SimProcess currentProcess = currentSimProcess();

        if (!checkProcess(currentProcess, where)) // if the current process
        {
            return false;
        }

        if (this.avail_to_plan < n) {
            sendWarning(
                "Attempt to plan some place"
                    + " in a Block that is empty or already planed. The attempted action is ignored!",
                "Block: " + getName() + " Method: " + where,
                "It does not make sense to plan that way in a Block.",
                "Make sure to find another Block to plan some place.");
            return false;
        } else {

            this.avail_to_plan = this.avail_to_plan - n;

            // tell in the trace what the process is storing in the Block
            if (currentlySendTraceNotes()) {
                sendTraceNote("plans " + n + " units to '" + this.getName()
                    + "'");
            }

            // for debugging purposes
            if (currentlySendDebugNotes()) {
                sendDebugNote("plans " + n + " units for "
                    + currentProcess.getQuotedName() + "<br>"
                    + "and has now " + this.avail_to_plan
                    + " units in the block to plan.");
            }

            return true;
        }

    }

    /**
     * Gets the initial number of TEUs the Block starts with.
     *
     * @return long : The initial number of TEUs the Block starts with.
     */
    public long getInitial() {

        return this.init;
    }

    /**
     * To reset the statistics of this Block. The current capacity and the number of avalaible TEUs for the storing and
     * retrieving, reserving and planing are not changed. But all statistic counters are reset. The
     * <code>Reportable</code> is also reset.
     */
    public void reset() {
        super.reset(); // reset the Reportable also

        this.max = this.currentCapacity;
        this.min = this.currentCapacity;
        this.producers = 0;
        this.consumers = 0;
        this.wSumAvail = 0.0;
        this.lastUsage = presentTime();

    }

    /**
     * Method to check if the Block is free (it can be stored there) for at least some TEUs.
     *
     * @param n long : The number of TEUs that are at least must be avalaible (free).
     * @return boolean : Is <code>true</code> if the Block is free,
     *     <code>false</code> otherwise.
     */
    public boolean isFree(long n) {

        // are there some TEUs to reserve

        return this.avail_to_reserve >= n;
    }

    /**
     * Gets the typ of the Block.
     *
     * @return long : The typ of the Stock.
     */
    public int getTyp() {

        return this.typ;
    }

    /**
     * Sets the typ of this Block to a new value.
     *
     * @param t int : The new typ of this Block.
     */
    public void setTyp(int t) {

        this.typ = t;
    }

    /**
     * Method for producers to make the Block reserve a number of n TEUs (a place for a container) to be stored there
     * later. When the whole available capacity is already reserved the try to reserve will be refused.
     *
     * @param n long : The number of TEUs that are needed for a container to be stored in the Block. n must be
     *          positive.
     * @return boolean : Is <code>true</code> if a place for a container can been reserved successfully,
     *     <code>false</code> otherwise.
     */
    public boolean reserve(long n) {

        where = "boolean reserve (long n)";

        SimProcess currentProcess = currentSimProcess();

        if (!checkProcess(currentProcess, where)) // if the current process
        {
            return false;
        }

        if (this.avail_to_reserve < n) {
            sendWarning(
                "Attempt to reserve some place"
                    + " in a Block that is full or already reserved. The attempted action is ignored!",
                "Block: " + getName() + " Method: " + where,
                "It does not make sense to reserve that way in a Block.",
                "Make sure to find another Block to reserve some place.");
            return false;
        } else {

            this.avail_to_reserve = this.avail_to_reserve - n;

            // tell in the trace what the process is storing in the Block
            if (currentlySendTraceNotes()) {
                sendTraceNote("reserves " + n + " units in '" + this.getName()
                    + "'");
            }

            // for debugging purposes
            if (currentlySendDebugNotes()) {
                sendDebugNote("reserves " + n + " units for "
                    + currentProcess.getQuotedName() + "<br>"
                    + "and has now " + this.avail_to_reserve
                    + " units in the block to reserve.");
            }

            return true;
        }
    }

    /**
     * Gets the max. capacity of the Block.
     *
     * @return long : The capacity of the Block.
     */
    public long getCapacity() {

        return this.capacity;
    }

    /**
     * Gets the current capacity of the Block.
     *
     * @return long : The current capacity of the Block.
     */
    public long getCurrentCapacity() {

        return this.currentCapacity;
    }

    /**
     * Checks whether the entity using the bin is a valid process.
     *
     * @param p     SimProcess : Is this SimProcess a valid one?
     * @param where String : The method having called <code>checkProcess()</code> as a String.
     * @return boolean : Returns whether the sim-process is valid or not.
     */
    protected boolean checkProcess(SimProcess p, String where) {
        if (p == null) // if p is a null pointer instead of a process
        {
            sendWarning(
                "A non existing process is trying to use a Block object. "
                    + "The attempted action is ignored!", "Bin: "
                    + getName() + " Method: " + where,
                "The process is only a null pointer.",
                "Make sure that only real SimProcesses are using Blocks.");
            return false;
        }

        if (!isModelCompatible(p)) // if p is not modelcompatible
        {
            sendWarning("The process trying to use a Block object does not "
                    + "belong to this model. The attempted action is ignored!",
                "Bin: " + getName() + " Method: " + where,
                "The process is not modelcompatible.",
                "Make sure that processes are using only Blocks within their model.");
            return false;
        }

        return true;
    }

    /**
     * Gets the holding area this Block is assigned to.
     *
     * @return <code>HoldingArea</code>: The Holding area the Block is
     *     assigned to.
     */
    public HoldingArea getHO() {

        return this.ho;
    }

    /**
     * Assigns this Block to a new holding area.
     *
     * @param ho <code>HoldingArea</code>: The new holding area of this
     *           Block.
     */
    public void setHO(HoldingArea ho) {

        this.ho = ho;

    }

    /**
     * Gets the container typ of the Block: what kind of container is that Block for.
     *
     * @return long : The container typ of the Block.
     */
    public int getCTyp() {

        return this.ctyp;
    }

    /**
     * Sets the container typ of this Block to a new value.
     *
     * @param ctyp int : The new container typ of this Block.
     */
    public void setCTyp(int ctyp) {

        this.ctyp = ctyp;
    }

}