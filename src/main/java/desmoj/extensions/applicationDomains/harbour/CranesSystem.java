package desmoj.extensions.applicationDomains.harbour;

import desmoj.core.report.Reporter;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.ProcessQueue;
import desmoj.core.simulator.QueueBased;
import desmoj.core.simulator.SimProcess;
import desmoj.core.simulator.TimeInstant;

/**
 * A CranesSystem represents the system of the cranes that manages the queues of the cranes and transporter
 * (external/internal), give the statistics about the cranes and both queues.
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
 * @see QueueBased
 */
public class CranesSystem extends QueueBased {

    /**
     * The queue, actually storing the transporter processes waiting for cranes to unload/load them.
     */
    protected ProcessQueue<SimProcess> transporterQueue;

    /**
     * The queue, actually storing the working cranes processes.
     */
    protected ProcessQueue<Crane> workingCranesQueue;

    /**
     * The queue, actually storing the cranes processes that are idle.
     */
    protected ProcessQueue<Crane> cranesQueue;

    /**
     * Counter for the whole number of the loaded units of all the cranes of this CranesSystems.
     */
    protected long sumNumLoadedUnits;

    /**
     * Counter for the whole number of the unloaded units of all the cranes of this CranesSystems.
     */
    protected long sumNumUnloadedUnits;

    /**
     * Counter for the whole time that all the cranes of this CranesSystems needed for the loading of the transporters.
     */
    protected double sumLoadTime;

    /**
     * Counter for the whole time that all the cranes of this CranesSystems needed for the unloading of the
     * transporters.
     */
    protected double sumUnloadTime;

    /**
     * Indicates the method where something has gone wrong. Is passed as a parameter to the methods
     * <code>checkProcess()</code> and
     * <code>checkCondition</code>.
     */
    private String where;

    /**
     * Constructor for a CranesSystem. The queueing discipline and the capacity limit of the underlying queues for the
     * cranes and for the transporters can be chosen.
     *
     * @param owner        Model : The model this CranesSystem is associated to.
     * @param name         java.lang.String : The CranesSystem's name
     * @param csortOrder   int : determines the sort order of the underlying cranes queue implementation. Choose a
     *                     constant from <code>QueueBased</code> like <code>QueueBased.FIFO</code> or
     *                     <code>QueueBased.LIFO</code> or ...
     * @param cQCapacity   int : The capacity of the cranes queue, that is how many cranes can be enqueued. Zero (0)
     *                     means unlimited capacity.
     * @param tsortOrder   int : determines the sort order of the underlying transporter queue implementation. Choose a
     *                     constant from
     *                     <code>QueueBased</code> like <code>QueueBased.FIFO</code>
     *                     or <code>QueueBased.LIFO</code> or ...
     * @param tQCapacity   int : The capacity of the transporter queue, that is how many transporters can be enqueued.
     *                     Zero (0) means unlimited capacity.
     * @param showInReport boolean : Flag, if this CranesSystem should produce a report or not.
     * @param showInTrace  boolean : Flag for trace to produce trace messages.
     */
    public CranesSystem(Model owner, String name, int cSortOrder,
                        int cQCapacity, int tSortOrder, int tQCapacity,
                        boolean showInReport, boolean showInTrace) {
        // construct QueueBased
        super(owner, name, showInReport, showInTrace);

        // CRANES queue

        // the sort order of the cranes queue
        int cranesQSortOrder = cSortOrder;

        // check if a valid sortOrder is given for the cranes queue
        if (cSortOrder < 0 || cSortOrder >= 3) {
            sendWarning(
                "The given cSortOrder parameter is negative or too big! "
                    + "Cranes queue with Fifo sort order will be created "
                    + "instead.",
                " Constructor of " + getClass().getName() + " : "
                    + getQuotedName() + ".",
                "A valid positive integer number must be provided to "
                    + "determine the sort order of the underlying queues.",
                "Make sure to provide a valid positive integer number "
                    + "by using the constants in the class QueueBased, like "
                    + "QueueBased.FIFO or QueueBased.LIFO.");

            cranesQSortOrder = QueueBased.FIFO;
        }

        // set the capacity of the cranes queue
        int cranesQLimit = cQCapacity;

        // check if the capacity does make sense
        if (cQCapacity < 0) {
            sendWarning("The given capacity of the cranes queue is negative! "
                    + "Cranes queue with unlimited capacity will be created "
                    + "instead.", " Constructor of " + getClass().getName()
                    + " : " + getQuotedName() + ".",
                "A negative capacity for a queue does not make sense.",
                "Make sure to provide a valid positive capacity "
                    + "for the underlying jobs queue.");
            // set the capacity to the maximum value
            cranesQLimit = Integer.MAX_VALUE;
        }

        // create the queues for the cranes (idle/working cranes)
        cranesQueue = new ProcessQueue<Crane>(owner, name + "_C", cranesQSortOrder,
            cranesQLimit, false, false);
        workingCranesQueue = new ProcessQueue<Crane>(owner, name + "_WC",
            cranesQSortOrder, cranesQLimit, false, false);

        // TRANSPORTER queue (external or internal)

        // the sort order of the transporter queue
        int transQSortOrder = tSortOrder;

        // check if a valid sortOrder is given for the transporter queue
        if (tSortOrder < 0 || tSortOrder >= 3) {
            sendWarning(
                "The given tSortOrder parameter is negative or too big! "
                    + "Transporter queue with Fifo sort order will be created "
                    + "instead.",
                " Constructor of " + getClass().getName() + " : "
                    + getQuotedName() + ".",
                "A valid positive integer number must be provided to "
                    + "determine the sort order of the underlying queues.",
                "Make sure to provide a valid positive integer number "
                    + "by using the constants in the class QueueBased, like "
                    + "QueueBased.FIFO or QueueBased.LIFO.");

            transQSortOrder = QueueBased.FIFO;
        }

        // set the capacity of the transporter queues
        int transQLimit = tQCapacity;

        // check if the capacity does make sense
        if (tQCapacity < 0) {
            sendWarning(
                "The given capacity of the transporter queue is negative! "
                    + "Transporter queue with unlimited capacity will be created "
                    + "instead.", " Constructor of "
                    + getClass().getName() + " : " + getQuotedName()
                    + ".",
                "A negative capacity for a queue does not make sense.",
                "Make sure to provide a valid positive capacity "
                    + "for the underlying jobs queue.");
            // set the capacity to the maximum value
            transQLimit = Integer.MAX_VALUE;
        }

        // create the queue for the transporters
        transporterQueue = new ProcessQueue<SimProcess>(owner, name + "_T",
            transQSortOrder, transQLimit, false, false);

        // reset the cranes system
        reset();

    } // end of constructor

    /**
     * Returns the number of the loaded units of all the cranes.
     *
     * @return long : The number of the loaded by the cranes units.
     */
    public long getSumNumLoadedUnits() {

        return this.sumNumLoadedUnits;
    }

    /**
     * Change the number of the loaded units of all the cranes to a new value.
     *
     * @param n long : The number of loaded units of this CranesSystem that must be added to the old value.
     */
    public void addSumNumLoadedUnits(long n) {

        this.sumNumLoadedUnits = this.sumNumLoadedUnits + n;
    }

    /**
     * Returns the number of the unloaded units of all the cranes.
     *
     * @return long : The number of the unloaded by the cranes units.
     */
    public long getSumNumUnloadedUnits() {

        return this.sumNumUnloadedUnits;
    }

    /**
     * Change the number of the unloaded units of all the cranes to a new value.
     *
     * @param n long : The number of unloaded units of this CranesSystem that must be added to the old value.
     */
    public void addSumNumUnloadedUnits(long n) {

        this.sumNumUnloadedUnits = this.sumNumUnloadedUnits + n;
    }

    /**
     * Returns a Reporter to produce a report about this CranesSystem.
     *
     * @return desmoj.report.Reporter : The Reporter for the queues inside this CranesSystem.
     */
    public Reporter createDefaultReporter() {

        return new desmoj.extensions.applicationDomains.harbour.report.CranesSystemReporter(
            this);
    }

    /**
     * This method is used to add a crane to the cranes queue of this CranesSystem.
     *
     * @param c Crane : The Crane to enqueue.
     */
    public boolean addCrane(Crane c) {

        where = " boolean addCrane (SimProcess c)";

        // get the process that wants to add a crane
        SimProcess currentProcess = currentSimProcess();

        if (!checkProcess(currentProcess, where)) // check the current process
        {
            return false;
        }

        if (this.cranesQueue.getQueueLimit() <= cranesQueue.length()) // check
        // if
        // capac.
        // limit
        // of
        // queue
        // is
        // reached
        {
            if (currentlySendDebugNotes()) {
                sendDebugNote("refuses to insert "
                    + currentProcess.getQuotedName()
                    + " in cranes queue, because the capacity limit is reached. ");
            }

            if (currentlySendTraceNotes()) {
                sendTraceNote("is refused to be enqueued in "
                    + this.getQuotedName() + "because the capacity limit ("
                    + cranesQueue.getQueueLimit() + ") of the "
                    + "queue is reached");
            }

        }

        return this.cranesQueue.insert(c); // insert the crane
    }

    /**
     * Checks whether the process using the CranesSystem is a valid process.
     *
     * @param p SimProcess : Is this SimProcess a valid one?
     * @return boolean : Returns whether the sim-process is valid or not.
     */
    protected boolean checkProcess(SimProcess p, String where) {
        if (p == null) // if p is a null pointer instead of a process
        {
            sendWarning(
                "A non existing process is trying to use a CranesSystem "
                    + "object. The attempted action is ignored!",
                "CranesSystem: " + getName() + " Method: " + where,
                "The process is only a null pointer.",
                "Make sure that only real SimProcesses are using CranesSystems.");
            return false;
        }

        if (!isModelCompatible(p)) // if p is not modelcompatible
        {
            sendWarning(
                "The process trying to use a CranesSystem object does"
                    + " not belong to this model. The attempted action is ignored!",
                "CranesSystem: " + getName() + " Method: " + where,
                "The process is not modelcompatible.",
                "Make sure that processes are using only Cranessystems within"
                    + " their model.");
            return false;
        }
        return true;
    }

    /**
     * Returns the n cranes of this cranes system that are idle now.
     *
     * @return Crane[] : The idle cranes.
     */
    public Crane[] getCranes(int n) {

        if (n <= 0) {
            sendWarning(
                "Attempt to take negative number of cranes or nothing from a Cranes system "
                    + "The attempted action is ignored!",
                "CranesSystem: " + getName() + " Method:getCranes (int n)",
                "It doesn't make sense to take a number of cranes this way  ",
                "Make sure to take only positive number of cranes (or at least one crane) of the CranesSystem");

            return null; // ignore that rubbish
        }

        if (n > cranesQueue.getQueueLimit()) {
            sendWarning(
                "Attempt to take more cranes from a Cranes System than it has "
                    + "The attempted action is ignored!",
                "CranesSystem: " + getName() + " Method:getCranes (int n)",
                "It doesn't make sense to take a number of cranes this way ",
                "Make sure to take only positive number of cranes (or at least one crane) of the CranesSystem");

            return null; // ignore that rubbish
        }

        // if there aren't enough cranes
        if (n > cranesQueue.length()) {
            return null;
        }

        // get all the cranes
        Crane[] cranes = new Crane[n];

        Crane c;

        for (int i = 0; i < n; i++) {
            SimProcess s = cranesQueue.first();
            cranesQueue.remove(s);
            c = (Crane) s;
            cranes[i] = c;
        }

        return cranes;
    }

    /**
     * Returns the cranes queue of this cranes system.
     *
     * @return <code>ProcessQueue<Crane></code>: The cranes queue.
     */
    public ProcessQueue<Crane> getCranesQueue() {

        return this.cranesQueue;
    }

    /**
     * Returns the transporter queue of this cranes system.
     *
     * @return <code>ProcessQueue<SimProcess></code>: The transporter queue.
     */
    public ProcessQueue<SimProcess> getTransporterQueue() {

        return this.transporterQueue;
    }

    /**
     * Resets all statistical counters to their default values. Both, cranes queue and transporter queue are reset. The
     * mininum and maximum length of the queues are set to the current number of queued objects.
     */
    public void reset() {

        super.reset(); // reset of the QueueBased

        transporterQueue.reset(); // reset of the transporter queue

        cranesQueue.reset(); // reset of the cranes queue

        // reset all other cranes statistic values
        sumNumLoadedUnits = 0;

        sumNumUnloadedUnits = 0;

        sumLoadTime = 0.0;

        sumUnloadTime = 0.0;
    }

    /**
     * This method is used to add a transporter (external/internal) to the transporter queue of this CranesSystem.
     *
     * @param t <code>SimProcess</code>: The transporter that me be
     *          enqueued.
     */
    public boolean addTransporter(SimProcess t) {

        where = " boolean addTransporter (SimProcess t )";

        // get the process that wants to add a transporter
        SimProcess currentProcess = currentSimProcess();

        if (!checkProcess(currentProcess, where)) // check the current process
        {
            return false;
        }

        if (this.transporterQueue.getQueueLimit() <= transporterQueue.length()) // check
        // if
        // capac.
        // limit
        // of
        // queue
        // is
        // reached
        {
            if (currentlySendDebugNotes()) {
                sendDebugNote("refuses to insert "
                    + currentProcess.getQuotedName()
                    + " in transporter queue, because the capacity limit is reached. ");
            }

            if (currentlySendTraceNotes()) {
                sendTraceNote("is refused to be enqueued in "
                    + this.getQuotedName() + "because the capacity limit ("
                    + transporterQueue.getQueueLimit() + ") of the "
                    + "queue is reached");
            }

        }

        return this.transporterQueue.insert(t); // insert the transporter
    }

    /**
     * Returns the last transporter of the transporter queue.
     *
     * @return SimProcess : The last transporter of the transporter queue.
     */
    public SimProcess getTransporter() {

        return this.transporterQueue.last();
    }

    /**
     * Returns the average utilization of a crane of this cranes system.
     *
     * @return double : The average utilisation pro a crane.
     */
    public double avgUsage() {

        // get the current time
        TimeInstant now = presentTime();

        // get the avg. usage of a crane
        double result = (this.getSumLoadTime() + this.getSumUnloadTime())
            / (this.cranesQueue.getQueueLimit() * now.getTimeAsDouble());
        return result;
    }

    /**
     * Returns the whole loading time of all the cranes.
     *
     * @return double : The whole loading time of this cranes system.
     */
    public double getSumLoadTime() {

        return this.sumLoadTime;
    }

    /**
     * Returns the whole unloading time of all the cranes.
     *
     * @return double : The whole unloading time of this cranes system.
     */
    public double getSumUnloadTime() {

        return this.sumUnloadTime;
    }

    /**
     * Change the whole time that all the cranes needed to load the transporters to a new value.
     *
     * @param t double : The loading time of this CranesSystem that must be added to the old value.
     */
    public void addSumLoadTime(double t) {

        this.sumLoadTime = this.sumLoadTime + t;
    }

    /**
     * Change the whole time that all the cranes needed to unload the transporters to a new value.
     *
     * @param t double : The unloading time of this CranesSystem that must be added to the old value.
     */
    public void addSumUnloadTime(double t) {

        this.sumUnloadTime = this.sumUnloadTime + t;
    }

    /**
     * Returns the queue of the now working cranes of this cranes system.
     *
     * @return <code>ProcessQueue</code>: The queue of the working cranes.
     */
    public ProcessQueue<Crane> getWorkingCranes() {

        return this.workingCranesQueue;
    }
}