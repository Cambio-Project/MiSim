package desmoj.extensions.applicationDomains.production;

import java.util.Vector;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.report.Reporter;
import desmoj.core.simulator.Condition;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.ProcessQueue;
import desmoj.core.simulator.QueueBased;
import desmoj.core.simulator.QueueList;
import desmoj.core.simulator.QueueListFifo;
import desmoj.core.simulator.QueueListLifo;
import desmoj.core.simulator.QueueListRandom;
import desmoj.core.simulator.SimProcess;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;

/**
 * A WorkStation is the place, where products (parts) are processed by a machine or parts are assembled by a worker or
 * machine. The list of parts needed for this process are provided by means of a <code>PartsList</code>.
 *
 * @author Soenke Claassen
 *     <p>
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 *     with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *     <p>
 *     Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *     the specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 * @see QueueBased
 * @see PartsList
 */
public class WorkStation extends QueueBased {

    /**
     * The queue, actually storing the master processes waiting for slaves to process them
     */
    protected QueueList<SimProcess> masterQueue;
    /**
     * The array containing all the different slave process queues. It depends on the number of entries (number of
     * different kinds of parts) in the associated <code>PartsList</code>.
     */
    protected ProcessQueue<SimProcess>[] slaveQueues;
    /**
     * Counter for the sim-processes which are refused to be enqueued in the master queue, because the queue capacity is
     * full.
     */
    protected long mRefused;
    /**
     * Array of counters for the slave SimProcesses which are refused to be enqueued in the different slave queues,
     * because their capacity is full.
     */
    protected long[] slavesRefused;
    /**
     * Indicates the method where something has gone wrong. Is passed as a parameter to the methods
     * <code>checkProcess()</code> and
     * <code>checkCondition</code>.
     */
    protected String where;
    /**
     * The number of different kinds of parts needed for processing the parts at this WorkStation. The number will be
     * provided by the
     * <code>PartsList</code>.
     */
    private int numOfParts;
    /**
     * The parts list containing all the parts with their respective quantity needed at this WorkStation to start the
     * manufacturing (processing) process.
     */
    private PartsList partsList;

    /**
     * Constructor for a WorkStation. Actually there are two waiting-queues constructed, one internal
     * <code>QueueList</code> for the masters (like
     * <code>Worker</code> s or <code>MachineProcess</code> es) and one
     * separate <code>ProcessQueue</code> for the slave processes (the parts or goods or products). The queueing
     * discipline and the capacity limit of the underlying queues can be chosen. Highest priority are always first in
     * the queues. All the slave wait queues will have the same queueing discipline and capacity limit as provided in
     * the parameters
     * <code>sSortOrder</code> and <code>sQCapacity</code>. To choose
     * individual settings for each slave wait queue use the method
     * <code>setSQueueCapacity()</code> to change its capacity and/or the
     * method <code>setSQueueStrategy()</code> to change its sort order.
     *
     * @param owner        Model : The model this WorkStation is associated to.
     * @param name         java.lang.String : The WorkStation's name
     * @param partsList    desmoj.PartsList : The list of parts determining the kind and number of parts needed at this
     *                     WorkStation to start processing them here.
     * @param mSortOrder   int : determines the sort order of the underlying master queue implementation. Choose a
     *                     constant from <code>QueueBased</code> like <code>QueueBased.FIFO</code> or
     *                     <code>QueueBased.LIFO</code> or ...
     * @param mQCapacity   int : The capacity of the master queue, that is how many processes can be enqueued. Zero (0)
     *                     means unlimited capacity.
     * @param sSortOrder   int : determines the sort order of the underlying slave queues implementation. Choose a
     *                     constant from <code>QueueBased</code> like <code>QueueBased.FIFO</code> or
     *                     <code>QueueBased.LIFO</code> or ...
     * @param sQCapacity   int : The capacity of the slave queues, that is how many processes can be enqueued. Zero (0)
     *                     means unlimited capacity.
     * @param showInReport boolean : Flag, if WorkStation should produce a report or not.
     * @param showInTrace  boolean : Flag, if trace messages of this WorkStation should be displayed in the trace file.
     */
    public WorkStation(Model owner, String name, PartsList partsList,
                       int mSortOrder, int mQCapacity, int sSortOrder, int sQCapacity,
                       boolean showInReport, boolean showInTrace) {
        // construct QueueBased
        super(owner, name + "_M", showInReport, showInTrace);

        // MASTER queue

        // check if a valid sortOrder is given for the master queue
        // determine the queueing strategy
        switch (mSortOrder) {
            case QueueBased.FIFO:
                masterQueue = new QueueListFifo<SimProcess>();
                break;
            case QueueBased.LIFO:
                masterQueue = new QueueListLifo<SimProcess>();
                break;
            case QueueBased.RANDOM:
                masterQueue = new QueueListRandom<SimProcess>();
                break;
            default:
                sendWarning(
                    "The given mSortOrder parameter " + mSortOrder + " is not valid! "
                        + "A queue with Fifo sort order will be created.",
                    " Constructor of " + getClass().getName() + " : "
                        + getQuotedName() + ".",
                    "A valid positive integer number must be provided to "
                        + "determine the sort order of the queue.",
                    "Make sure to provide a valid positive integer number "
                        + "by using the constants in the class QueueBased, like "
                        + "QueueBased.FIFO, QueueBased.LIFO or QueueBased.RANDOM.");
                masterQueue = new QueueListFifo<SimProcess>();
        }

        // give the QueueList a reference to this QueueBased
        masterQueue.setQueueBased(this);

        // set the capacity of the master queue
        queueLimit = mQCapacity;

        // check if the capacity does make sense
        if (mQCapacity < 0) {
            sendWarning("The given capacity of the master queue is negative! "
                    + "A master queue with unlimited capacity will be created "
                    + "instead.", " Constructor of " + getClass().getName()
                    + " : " + getQuotedName() + ".",
                "A negative capacity for a queue does not make sense.",
                "Make sure to provide a valid positive capacity "
                    + "for the underlying master queue.");
            // set the capacity to the maximum value
            queueLimit = Integer.MAX_VALUE;
        }

        // check if qCapacity is zero (that means unlimited capacity)
        if (mQCapacity == 0) {
            // set the capacity to the maximum value
            queueLimit = Integer.MAX_VALUE;
        }

        // PARTSLIST

        // check for a valid parts list
        if (partsList == null) {
            sendWarning(
                "The given parts list is only a null pointer. "
                    + "No WorkStation can be created!",
                getClass().getName()
                    + ": "
                    + getQuotedName()
                    + ", Constructor: "
                    + "WorkStation(Model owner, String name, PartsList partsList, "
                    + "boolean showInReport, boolean showInTrace) ",
                "The given PartsList is only a null pointer.",
                "Make sure to provide a valid PartsList when constructing a Work"
                    + "Station. Create a PartsList first and then construct a WorkStation.");

            return; // ignore that rubbish and just return
        }

        this.partsList = partsList;

        this.numOfParts = partsList.getNumberOfDiffParts();

        // make an array of ProcessQueues for the slaves (parts)
        this.slaveQueues = new ProcessQueue[numOfParts];

        // SLAVE queue

        // the sort order of the slave queues
        int slaveQSortOrder = sSortOrder;

        // check if a valid sortOrder is given for the slave queues
        if (sSortOrder < 0 || sSortOrder >= 3) {
            sendWarning(
                "The given sSortOrder parameter is negative or too big! "
                    + "Slave queues with Fifo sort order will be created "
                    + "instead.",
                " Constructor of " + getClass().getName() + " : "
                    + getQuotedName() + ".",
                "A valid positive integer number must be provided to "
                    + "determine the sort order of the underlying queues.",
                "Make sure to provide a valid positive integer number "
                    + "by using the constants in the class QueueBased, like "
                    + "QueueBased.FIFO or QueueBased.LIFO.");

            slaveQSortOrder = QueueBased.FIFO;
        }

        // set the capacity of the slave queues
        int slaveQLimit = sQCapacity;

        // check if the capacity does make sense
        if (sQCapacity < 0) {
            sendWarning("The given capacity of the slave queues is negative! "
                    + "Slave queues with unlimited capacity will be created "
                    + "instead.", " Constructor of " + getClass().getName()
                    + " : " + getQuotedName() + ".",
                "A negative capacity for a queue does not make sense.",
                "Make sure to provide a valid positive capacity "
                    + "for the underlying slave queue.");
            // set the capacity to the maximum value
            slaveQLimit = Integer.MAX_VALUE;
        }

        // make the counters for the refused slaves
        slavesRefused = new long[numOfParts];

        // make the queues where we can store the slave processes with the right
        // sort order and capacity limit but don't provide any extra report or
        // trace for these "internal" queues
        for (int i = 0; i < numOfParts; i++) {
            slaveQueues[i] = new ProcessQueue(owner, name + "_S_" + i,
                slaveQSortOrder, slaveQLimit, false, false);
        }

        reset();
    }

    /**
     * Constructor for a WorkStation. Actually there are two waiting-queues constructed, one internal
     * <code>QueueList</code> for the masters (like
     * <code>Worker</code> s or <code>MachineProcess</code> es) and one
     * separate <code>ProcessQueue</code> for the slave processes (the parts or goods or products). Both queues have a
     * FIFO sort order and no capacity limit.
     *
     * @param owner        Model : The model this WorkStation is associated to.
     * @param name         java.lang.String : The WorkStation's name
     * @param partsList    desmoj.PartsList : The list of parts determining the kind and number of parts needed at this
     *                     WorkStation to start processing them here.
     * @param showInReport boolean : Flag, if WorkStation should produce a report or not.
     * @param showInTrace  boolean : Flag, if trace messages of this WorkStation should be displayed in the trace file.
     */
    public WorkStation(Model owner, String name, PartsList partsList,
                       boolean showInReport, boolean showInTrace) {
        // construct QueueBased
        super(owner, name + "_M", showInReport, showInTrace);

        // make an actual queue and give it a reference of this
        // "QueueBased"-WaitQueue for the masters to wait in
        masterQueue = new QueueListFifo<SimProcess>();
        masterQueue.setQueueBased(this);

        // check for a valid parts list
        if (partsList == null) {
            sendWarning(
                "The given parts list is only a null pointer. "
                    + "No WorkStation can be created!",
                getClass().getName()
                    + ": "
                    + getQuotedName()
                    + ", Constructor: "
                    + "WorkStation(Model owner, String name, PartsList partsList, "
                    + "boolean showInReport, boolean showInTrace) ",
                "The given PartsList is only a null pointer.",
                "Make sure to provide a valid PartsList when constructing a Work"
                    + "Station. Create a PartsList first and then construct a WorkStation.");

            return; // ignore that rubbish and just return
        }

        this.partsList = partsList;

        this.numOfParts = partsList.getNumberOfDiffParts();

        // make the counters for the refused slaves
        slavesRefused = new long[numOfParts];

        // make an array of ProcessQueues for the slaves (parts)
        this.slaveQueues = new ProcessQueue[numOfParts];

        // make the queues where we can store the slave processes but don't
        // provide any extra report or trace for these "internal" queues
        for (int i = 0; i < numOfParts; i++) {
            slaveQueues[i] = new ProcessQueue(owner, name + "_S_" + i, false,
                false);
        }

        reset();
    }

    /**
     * Activates the <code>SimProcess</code>, given as a parameter of this method, as the next process. This process
     * should be a master process waiting in the master wait queue.
     *
     * @param process SimProcess : The process that is to be activated as next. Should be a master process.
     */
    protected void activateAsNext(SimProcess process) {
        where = "protected void activateAsNext(SimProcess process)";

        if (process != null) {
            if (!checkProcess(process, where)) // if process is not valid
            {
                return;
            } // just return

            if (process.isScheduled()) // if the process is scheduled already
            {
                process.skipTraceNote(); // don't tell the user, that we ...
                process.cancel(); // get the process from the event-list
            }

            boolean wasBlocked = process.isBlocked();

            if (wasBlocked) {
                process.setBlocked(false); // the process is not blocked
                // anymore
                // and
            } // ready to become activated

            process.skipTraceNote(); // don't tell the user, that we ...
            process.activateAfter(current()); // activate process after the
            // current process

            if (wasBlocked) {
                process.setBlocked(true); // the process status is still
                // "blocked"
            } // end inner if
        } // end outer if
    } // end method

    /**
     * Activates the first master process in the master waiting-queue.
     */
    protected void activateFirstMaster() {
        where = "protected void activateFirstMaster()";

        SimProcess mProcess = masterQueue.first();

        if (mProcess != null) {
            if (!checkProcess(mProcess, where)) // if mProcess is not valid
            {
                return;
            } // just return

            if (mProcess.isScheduled()) // if the master process is scheduled
            // already
            {
                mProcess.skipTraceNote(); // don't tell the user, that we ...
                mProcess.cancel(); // get the process from the event-list
            }

            boolean wasBlocked = mProcess.isBlocked();

            if (wasBlocked) {
                // the process is not blocked anymore and ready to become
                // activated
                mProcess.setBlocked(false);
            }

            mProcess.skipTraceNote(); // don't tell the user, that we ...
            mProcess.activateAfter(current()); // activate mProcess after the
            // current process

            if (wasBlocked) {
                mProcess.setBlocked(true); // the process status is still
                // "blocked"
            } // end inner if
        } // end outer if
    } // end method

    /**
     * Checks if all parts listed in the parts list are available at the moment. If all needed partes are available the
     * processing process at this WorkStation can start.
     *
     * @return boolean :<code>true</code>, if all parts listed in the parts list are available at the moment.
     *     <code>false</code> otherwise
     */
    public synchronized boolean allPartsAvailable() {

        boolean allPartsAvail = true;

        // check for every kind of part
        for (int i = 0; i < numOfParts; i++) {
            if (slaveQueues[i].length() < partsList.getQuantityOfPart(i)) {
                allPartsAvail = false;
            }
        }

        return allPartsAvail;
    }

    /**
     * Returns the master process waiting in the master queue and complying to the given condition. If there is no such
     * master process waiting
     * <code>null</code> is returned.
     *
     * @param cond Condition : The Condition <code>cond</code> is describing the condition to which the master process
     *             must comply to. This has to be implemented by the user in the class:
     *             <code>Condition</code> in the method: <code>check()</code>.
     * @return SimProcess : Returns the first master process in the master queue which complies to the given condition.
     */
    public SimProcess availMaster(Condition cond) {
        where = "SimProcess availMaster(Condition cond)";

        // the current SimProcess is assumed to be a slave looking for a master
        SimProcess slave = currentSimProcess();

        if (!checkProcess(slave, where)) // if master is not valid
        {
            return null;
        } // return null

        if (!checkCondition(cond, where)) // if the condition is not valid
        {
            return null;
        } // return null

        if (masterQueue.isEmpty()) // nobody home to be checked
        {
            return null;
        } // return null

        for (SimProcess master = masterQueue.first(); master != null; master = masterQueue
            .succ(master)) {
            if (cond.check(master)) {
                return master;
            }
        }

        // if no SimProcess complies to the condition just return null
        return null;

    } // end method

    /**
     * Returns an array containing the slave processes of the given kind waiting in their queue and complying to the
     * given condition. If there are no such slaves waiting at the moment <code>null</code> will be returned.
     *
     * @param kind Class : The Class specifying of which kind the processes have to be, we are looking for.
     * @param cond Condition : The Condition <code>cond</code> is describing the condition to which the slave processes
     *             must comply to. This has to be implemented by the user in the class:
     *             <code>Condition</code> in the method: <code>check()</code>.
     * @return SimProcess[] : Returns all the slave processes of the given kind that are complying to the given
     *     condition.
     */
    public SimProcess[] availSlaves(Class kind, Condition cond) {
        where = "SimProcess avail(Class kind, Condition cond)";

        // check kind
        if (kind == null) {
            sendWarning(
                "The given kind of the slave is only a null pointer. "
                    + "No SimProcess can be returned for that kind of slave!",
                getClass().getName()
                    + ": "
                    + getQuotedName()
                    + ", Method: "
                    + "SimProcess[] availSlaves (Class kind, Condition cond)",
                "The given parameter is only a null pointer.",
                "Make sure to provide a valid Class variable for the kind of part you "
                    + "are looking for.");

            return null; // ignore that rubbish and just return null
        }

        if (!checkCondition(cond, where)) // if the condition is not valid
        {
            return null;
        } // return null

        // the index indicating the wait queue for the given kind of slave
        int index = this.partsList.getIndexOfKind(kind);

        // check if there is a wait queue for SimProcesses of the given kind
        if (index > -1) {
            // this Vector will holding all the slaves found complying to the
            // condition
            Vector foundParts = new Vector();

            // get the right wait queue
            ProcessQueue slaveQueue = this.slaveQueues[index];

            // put all the found slaves in the Vector
            for (SimProcess tmp = slaveQueue.first(cond); tmp != null; tmp = slaveQueue
                .succ(tmp, cond)) {
                foundParts.addElement(tmp);
            }

            // copy the Vector to an array
            Object[] foundProcs = foundParts.toArray();

            // make an array of SimProcesses to return
            SimProcess[] foundSlaves = new SimProcess[foundParts.size()];

            // copy the array to the one being returned
            System.arraycopy(foundProcs, 0, foundSlaves, 0, foundProcs.length);

            return foundSlaves;
        }

        return null; // no suitable slave found -> return null
    } // end method

    /**
     * Checks whether the given condition is valid and compatible with the model.
     *
     * @param cond  Condition : Is this Condition a valid one?
     * @param where String : The String representation of the method where this check takes place.
     * @return boolean : Returns whether the Condition is valid or not.
     */
    protected boolean checkCondition(Condition cond, String where) {
        if (cond == null) // if cond is a null pointer instead of a condition
        {
            sendWarning("A non existing condition is used in a "
                    + getClass().getName() + "."
                    + "The attempted action is ignored!", getClass().getName()
                    + ": " + getQuotedName() + ", Method: " + where,
                "The given condition is only a null pointer.",
                "Make sure that only real conditions are used to identify suitable "
                    + "processes.");
            return false;
        }

        if (!isModelCompatible(cond)) // if cond is not modelcompatible
        {
            sendWarning(
                "The condition used to identify a suitable process for a "
                    + "cooperation at a WorkStation does not belong to this model. The "
                    + "attempted action is ignored!", getClass()
                    .getName()
                    + ": " + getQuotedName() + ", Method: " + where,
                "The given condition is not modelcompatible.",
                "Make sure that conditions used to identify suitable processes for a "
                    + "cooperation are belonging to the same model.");
            return false;
        }

        return true;
    }

    /**
     * Checks whether the process trying to cooperate as a master or a slave is a valid SimProcess.
     *
     * @param p     SimProcess : Is this SimProcess a valid one?
     * @param where String : The String representation of the method where this check takes place.
     * @return boolean : Returns whether the sim-process is valid or not.
     */
    protected boolean checkProcess(SimProcess p, String where) {
        if (p == null) // if p is a null pointer instead of a process
        {
            sendWarning(
                "A non existing process is trying to cooperate with "
                    + "another process at a WorkStation. The attempted action is ignored!",
                getClass().getName() + ": " + getQuotedName()
                    + ", Method: " + where,
                "The given process is only a null pointer.",
                "Make sure that only real SimProcesses are trying to cooperate with "
                    + "each other.");
            return false;
        }

        if (!isModelCompatible(p)) // if p is not modelcompatible
        {
            sendWarning(
                "The sim-process trying to cooperate with another process at "
                    + "a WorkStation does not belong to this model. The attempted action is "
                    + "ignored!", getClass().getName() + ": "
                    + getQuotedName() + ", Method: " + where,
                "The given process is not modelcompatible.",
                "Make sure that processes are cooperating only with processes "
                    + "belonging to the same model.");
            return false;
        }

        return true;
    }

    /**
     * Returns a Reporter to produce a report about this WorkStation.
     *
     * @return desmoj.report.Reporter : The Reporter for the queues inside this WorkStation.
     */
    public Reporter createDefaultReporter() {

        // a reporter for the queue statistics of this WorkStation
        return new desmoj.extensions.applicationDomains.production.report.WorkStationReporter(
            this);
    }

    /**
     * Returns an array of all the slave processes needed to start the processing which are available and comply to the
     * conditions given in the parameters. If no or not enough suitable slaves are available
     * <code>null</code> will be returned.
     *
     * @param kinds      java.lang.Class[] : This array is specifying in conjunction with the array conditions which
     *                   slaves have to comply to which conditions so the master will cooperate with them. Must be a
     *                   subset of the parts list.
     * @param conditions desmoj.Condition[] : This array is specifying in conjunction with the array kinds which slaves
     *                   have to comply to which conditions so the master will cooperate with them.
     * @return desmoj.SimProcess[] : An array of all the slave processes which are available and comply to the
     *     conditions given in the parameters and are needed to start the processing. If not enough of all of them are
     *     available <code>null</code> will be returned.
     */
    protected SimProcess[] getAllSuitableSlaves(Class[] kinds,
                                                Condition[] conditions) {
        // make a Vector to store all the parts found
        Vector<SimProcess> suitableSlaves = new Vector<SimProcess>();

        // loop through the whole partsList
        for (int i = 0; i < numOfParts; i++) {
            // does for this kind of part exist a special condition?
            boolean specialCond = false; // not so far

            // loop through the kinds array
            for (int j = 0; j < kinds.length; j++) {
                // does for this kind of part exist a special Condition?
                if (partsList.getKindOfPart(i) == kinds[j]) {
                    // special condition found
                    specialCond = true;

                    // get all the available slaves of that special kind
                    SimProcess[] allAvailSlaves = availSlaves(kinds[j],
                        conditions[j]);

                    if (allAvailSlaves == null
                        || allAvailSlaves.length < partsList
                        .getQuantityOfPart(i)) {
                        return null; // no or not enough suitable slaves
                        // available

                    } else // enough suitable slaves available
                    {
                        // get the number of needed slaves and put them in the
                        // Vector
                        for (int k = 0; k < partsList.getQuantityOfPart(i); k++) {
                            // put the process from the array in the Vector
                            suitableSlaves.addElement(allAvailSlaves[k]);
                        }
                    }
                }
            }

            // if no special condition found, see if there are enough normal
            // processes
            if (!specialCond) {
                // not enough slaves of this kind available
                if (slaveQueues[i].length() < partsList.getQuantityOfPart(i)) {
                    return null;
                } else // enough slaves available, so put them in the Vector
                {
                    // get all parts of this kind
                    for (int m = 0; m < partsList.getQuantityOfPart(i); m++) {
                        // get the first slave from its queue
                        SimProcess slave = slaveQueues[i].first();

                        // add the slave to the Vector
                        suitableSlaves.addElement(slave);
                    }
                }
            }
        } // next kind of part

        // copy the Vector to the array to be returned
        // copy the Vector to an array
        Object[] sParts = suitableSlaves.toArray();

        // make an array of SimProcesses to return
        SimProcess[] suitParts = new SimProcess[suitableSlaves.size()];

        // copy the array to the one being returned
        System.arraycopy(sParts, 0, suitParts, 0, sParts.length);

        // return the array
        return suitParts;
    }

    /**
     * Returns the implemented queueing discipline of the underlying master queue as a String, so it can be displayed in
     * the report.
     *
     * @return String : The String indicating the queueing discipline.
     */
    public String getMQueueStrategy() {

        return masterQueue.getAbbreviation(); // that's it
    }

    /**
     * Returns the number of entities refused to be enqueued in the master queue, because the capacity limit is
     * reached.
     *
     * @return long : The number of entities refused to be enqueued in the master queue.
     */
    public long getMRefused() {

        return mRefused; // that's it
    }

    /**
     * Returns the number of different kinds of parts needed for processing these parts at this WorkStation.
     *
     * @return int : The number of different kinds of parts needed for processing these parts at this WorkStation.
     */
    public int getNumOfParts() {
        return numOfParts;
    }

    /**
     * Returns the parts list of this WorkStation. That is the list listing all the different parts with their
     * quantities needed to start the processing process at this WorkStation.
     *
     * @return desmoj.PartsList : The parts list of this WorkStation
     */
    public PartsList getPartsList() {
        return partsList;
    }

    /**
     * Returns an array of <code>ProcessQueue</code> s where the waiting slaves are stored.
     *
     * @return ProcessQueue[] : An array of <code>ProcessQueue</code> s where the slaves are waiting on masters to
     *     cooperate with.
     */
    public ProcessQueue<SimProcess>[] getSlaveQueues() {
        return this.slaveQueues;
    }

    /**
     * Returns the implemented queueing discipline of the underlying slave queue at the given index as a String, so it
     * can be displayed in the report. To get the index for the kind of part in question use the method
     * <code>getIndexOfKind()</code> of the <code>PartsList</code>.
     *
     * @param index int : The index of the kind of parts we want to get the queueing discipline from. To get that index
     *              use the method
     *              <code>getIndexOfKind()</code> of the <code>PartsList</code>.
     * @return java.lang.String : The String indicating the queueing discipline.
     */
    public String getSQueueStrategy(int index) {

        return this.slaveQueues[index].getQueueStrategy(); // that's it!!
    }

    /**
     * Returns the number of entities refused to be enqueued in the slave's queue, indicated by the index, because the
     * capacity limit is reached.
     *
     * @param index int : Indicating the kind of part for which we want to know its numbr of refused attempts to enqueue
     *              new processes. To get the index of a certain kind of part use the method
     *              <code>getIndexOfKind()</code> of the <code>PartsList</code>.
     * @return long : The number of entities refused to be enqueued in the slave's queue indicated by the index.
     */
    public long getSRefused(int index) {

        return slavesRefused[index];
    }

    /**
     * Returns the average length of the underlying master queue since the last reset. If the time span since the last
     * reset is smaller than the smallest distinguishable timespan epsilon, the current length of the master queue will
     * be returned.
     *
     * @return double : The average master queue length since last reset or current length of the master queue if no
     *     distinguishable periode of time has passed.
     */
    public double mAverageLength() {
        return averageLength(); // of the underlying QueueBased
    }

    /**
     * Returns the average waiting time of all processes who have exited the master queue. Value is valid for the time
     * span since the last reset. Returns 0 (zero) if no process have exited the master queue after the last reset.
     *
     * @return TimeSpan : Average waiting time of all processes since last reset or 0 if no process has exited the
     *     master queue
     */
    public TimeSpan mAverageWaitTime() {
        return averageWaitTime(); // of the underlying QueueBased (for the
        // masters)
    }

    /**
     * Returns a boolean value indicating if the master queue is empty or if any number of SimProcess is currently
     * enqueued in it.
     *
     * @return boolean : Is <code>true</code> if the master queue is empty,
     *     <code>false</code> otherwise
     */
    public boolean mIsEmpty() {
        return masterQueue.isEmpty(); // of the underlying QueueList
    }

    /**
     * Returns the current length of the master queue.
     *
     * @return long : The current master queue length
     */
    public long mLength() {
        return length(); // of the underlying QueueBased
    }

    /**
     * Returns the maximum length of the underlying master queue since the last reset.
     *
     * @return long : The maximum master queue length since the last reset.
     */
    public long mMaxLength() {
        return maxLength(); // of the underlying QueueBased
    }

    /**
     * Returns the point of simulation time with the maximum number of Sim-processes waiting inside the underlying
     * master queue. The value is valid for the period since the last reset.
     *
     * @return desmoj.SimTime : Point of time with maximum master queue length since the last reset.
     */
    public TimeInstant mMaxLengthAt() {
        return maxLengthAt(); // of the underlying QueueBased
    }

    /**
     * Returns the maximum duration in simulation time that an process has spent waiting inside the underlying master
     * queue. The value is valid for the period since the last reset.
     *
     * @return desmoj.core.TimeSpan : Longest waiting time of a process in the master queue since the last reset.
     */
    public TimeSpan mMaxWaitTime() {
        return maxWaitTime(); // of the underlying QueueBased
    }

    /**
     * Returns the point of simulation time when the process with the maximum waiting time exited the underlying master
     * queue. The value is valid for the period since the last reset.
     *
     * @return desmoj.core.TimeInstant : The point of simulation time when the process with the maximum waiting time
     *     exited the master queue.
     */
    public TimeInstant mMaxWaitTimeAt() {
        return maxWaitTimeAt(); // of the underlying QueueBased
    }

    /**
     * Returns the minimumn length of the underlying master queue since the last reset.
     *
     * @return long : The minimum master queue length since the last reset.
     */
    public long mMinLength() {
        return minLength(); // of the underlying QueueBased
    }

    /**
     * Returns the point of simulation time with the minimum number of processes waiting inside the underlying master
     * queue. The value is valid for the period since the last reset.
     *
     * @return desmoj.core.TimeInstant : Point of time with minimum master queue length since the last reset.
     */
    public TimeInstant mMinLengthAt() {
        return minLengthAt(); // of the underlying QueueBased
    }

    /**
     * Returns the standard deviation of the master queue's length. Value is weighted over time.
     *
     * @return double : The standard deviation for the master queue's length weighted over time.
     */
    public double mStdDevLength() {
        return stdDevLength(); // of the underlying QueueBased
    }

    /**
     * Returns the standard deviation of the master queue's processes waiting times.
     *
     * @return desmoj.core.TimeSpan : The standard deviation for the master queue's processes waiting times.
     */
    public TimeSpan mStdDevWaitTime() {
        return stdDevWaitTime(); // of the underlying QueueBased
    }

    /**
     * Returns the number of processes that have passed through the master queue without spending time waiting.
     *
     * @return long : The number of processes who have passed the master queue without waiting
     */
    public long mZeroWaits() {
        return zeroWaits(); // of the underlying QueueBased
    }

    /**
     * This method is to be called from a <code>SimProcess</code> which wants to process the other parts (slaves) as a
     * master . It should be a
     * <code>Worker</code> or a <code>MachineProcess</code>. But any other
     * kind of <code>SimProcess</code> will do, too, but produce a warning. If no suitable or not enough slave processes
     * are available at the moment, the master process will be stored in the master waiting-queue, until enough suitable
     * slaves are available. If the capacity limit of the master queue is reached, the process will not be enqueued and
     * <code>false</code> returned. When enough suitable slaves are available, their
     * <code>cooperate</code> method (in the class <code>SimProcess</code>)
     * will be called. During the processing the master process is the only active one. The slave processes are passive
     * and will be reactivated after the processing is done.
     *
     * @param process Processing : The Processing process is describing the joint action of the two processes. The
     *                processing to be carried out has to be implemented by the user in the class or subclass of
     *                <code>Processing</code> in the method:
     *                <code>cooperation()</code>.
     * @return boolean : Is <code>true</code> if the process can be enqueued successfully in the master queue,
     *     <code>false</code> otherwise (i.e. capacity limit of the master queue is reached).
     */
    public boolean process(Processing process) throws SuspendExecution {
        where = "boolean process(Processing process)";

        // check the Processing
        if (!isModelCompatible(process)) {
            sendWarning(
                "The given Processing object does not "
                    + "belong to this model. The attempted cooperation is ignored!",
                getClass().getName() + ": " + getQuotedName()
                    + ", Method: " + where,
                "The Processing process is not modelcompatible.",
                "Make sure that the processing cooperation belongs to this model.");

            return false; // processing is not modelcompatible
        }

        // the current SimProcess which was calling process() is the master
        SimProcess master = currentSimProcess();

        if (!checkProcess(master, where)) // if the master is no valid process
        {
            return false;
        } // just return false

        // check if capacity limit of master queue is reached
        if (queueLimit <= length()) {
            if (currentlySendDebugNotes()) {
                sendDebugNote("refuses to insert "
                    + master.getQuotedName()
                    + " in master queue, because the capacity limit is reached.");
            }

            if (currentlySendTraceNotes()) {
                sendTraceNote("is refused to be enqueued in "
                    + this.getQuotedName()
                    + "'s master queue because the capacity limit ("
                    + getQueueLimit() + ") of the queue is reached");
            }

            mRefused++; // count the refused ones

            return false; // capacity limit is reached
        }

        // insert the master in its waiting-queue
        masterQueue.insert(master);

        // check if the master has to wait in his queue
        // if not enough parts are available OR the master is not the first to
        // be
        // served
        if ((!allPartsAvailable())
            || master != masterQueue.first()) {
            if (currentlySendTraceNotes()) {
                // tell in the trace where the master is waiting
                sendTraceNote("waits in '" + this.getName() + "'");
            }

            if (allPartsAvailable()) // there are enough slaves available
            {
                activateFirstMaster(); // activate the first master in the
            } // queue to see what he can do

            do { // block the master process
                master.setBlocked(true); // as long as ...(see while)
                master.skipTraceNote(); // don't tell the user, that we
                master.passivate(); // passivate the master process
            } while ((!allPartsAvailable())); // ...not enough slaves are
            // available
        }

        // the master has found enough slaves to start the processing

        masterQueue.remove(master); // remove this master from the wait queue
        master.setBlocked(false); // this master is not blocked anymore

        // activate the new first master process in the master queue
        // to see what he can do
        activateFirstMaster();

        // make a Vector to store all the slaves being processed now
        Vector<SimProcess> neededParts = new Vector<SimProcess>();

        // get all the slaves together
        for (int i = 0; i < numOfParts; i++) {
            // get all parts of one kind
            for (int j = 0; j < partsList.getQuantityOfPart(i); j++) {
                // get the first slave from its queue
                SimProcess slave = slaveQueues[i].first();

                // add the slave to the set of slaves being processed now
                neededParts.addElement(slave);

                if (!checkProcess(slave, where)) // if the slave process is
                // not
                // O.K.
                {
                    return false;
                } // just return false

                // prepare the slave for the cooperation: (set the master for
                // this slave,
                // remove it from its slave queue and unblock it)
                slave.cooperate();
            }
        }

        // copy the Vector to an array
        Object[] nParts = neededParts.toArray();

        // make an array of SimProcesses to return
        SimProcess[] slaveSet = new SimProcess[neededParts.size()];

        // copy the array to the one being processed
        System.arraycopy(nParts, 0, slaveSet, 0, nParts.length);

        // start the real processing (cooperation) with the set of slaves
        process.cooperation(master, slaveSet);

        // the processing process is over, so free and activate all slaves
        for (int k = 0; k < slaveSet.length; k++) {
            // no master is controlling the slave anymore
            slaveSet[k].resetMaster();

            // activate the slave only if it is not a component (part) of a
            // ComplexSimProcess now
            if (!slaveSet[k].isComponent()) {
                // get the slave activated after the master
                slaveSet[k].activateAfter(master);
            }

            // if the slave has become a Component of a ComplexSimProcess, he
            // will
            // be activated when he is removed from the ComplexSimProcess
        }

        return true;
    }

    /**
     * This method is to be called from a <code>SimProcess</code> which wants to process the other parts (slaves) as a
     * master. For a WorkStation it should be a <code>Worker</code> or a <code>MachineProcess</code>. By using this
     * method the master can determine some special slaves complying to certain conditions he would like to process. The
     * <code>Class</code> array kinds must be a subset of the parts list and determines together with the
     * <code>Condition</code> array (which must be of the same length) which kind of parts must comply to which
     * condition so the master will cooperate with them. If no suitable or not enough slave processes are available at
     * the moment, the master process will be stored in the master waiting-queue, until enough suitable slaves are
     * available. If the capacity limit of the master queue is reached, the process will not be enqueued and
     * <code>false</code> returned. When enough suitable slaves are available, their <code>cooperate</code> method (in
     * the class
     * <code>SimProcess</code>) will be called. During the cooperation the
     * master process is the only active one. The slave processes are passive and will be reactivated after the
     * cooperation is done.
     *
     * @param process    desmoj.Processing : The Processing process is describing the joint action of the master and
     *                   slave processes. The processing to be carried out has to be implemented by the user in the
     *                   class: <code>Processing</code> in the method:
     *                   <code>cooperation()</code>.
     * @param kinds      java.lang.Class[] : is specifying in conjunction with the array conditions which slaves have to
     *                   comply to which conditions so the master will cooperate with them. Must be a subset of the
     *                   parts list.
     * @param conditions desmoj.Condition[] : is specifying in conjunction with the array kinds which slaves have to
     *                   comply to which conditions so the master will cooperate with them.
     * @return boolean : Is <code>true</code> if the process can be enqueued successfully, <code>false</code> otherwise
     *     (i.e. capacity limit of the master queue is reached).
     */
    public boolean process(Processing process, Class[] kinds,
                           Condition[] conditions) throws SuspendExecution {
        where = "boolean process(Processing process, Class[] kinds, Condition[] "
            + "conditions)";

        // check if kinds can be a subset of partsList
        if (kinds.length > numOfParts) {
            sendWarning(
                "The array 'kinds' is longer than the number of different "
                    + "parts. The attempted processing is ignored!",
                getClass().getName() + ": " + getQuotedName()
                    + ", Method: " + where,
                "The given array is no subset of the parts list.",
                "Make sure to provide conditions only for the parts (slaves) "
                    + "which are contained in the parts list.");
            return false;
        }

        // check if both given arrays have the same length
        if (kinds.length != conditions.length) {
            sendWarning(
                "The array 'kinds' has not the same length as the array "
                    + "'conditions'. The attempted processing is ignored!",
                getClass().getName() + ": " + getQuotedName()
                    + ", Method: " + where,
                "The given arrays do not have the same length.",
                "Make sure to provide two arrays with the same lenght, where for every "
                    + "kind of part there is a condition.");
            return false;
        }

        // check if one of the array is only a null pointer
        if (kinds == null || conditions == null) {
            sendWarning(
                "The array 'kinds' or the array 'conditions' is only a null "
                    + "pointer. The attempted processing is ignored!",
                getClass().getName() + ": " + getQuotedName()
                    + ", Method: " + where,
                "One of the given arrays is only a null pointer.",
                "Make sure to provide two arrays with the same lenght, where for every "
                    + "kind of part there is a condition.");
            return false;
        }

        // check the Processing
        if (!isModelCompatible(process)) {
            sendWarning(
                "The given Processing object does not "
                    + "belong to this model. The attempted processing is ignored!",
                getClass().getName() + ": " + getQuotedName()
                    + ", Method: " + where,
                "The Processing process is not modelcompatible.",
                "Make sure that the processing cooperation belongs to this model.");

            return false; // processing is not modelcompatible
        }

        // the current SimProcess which was calling process() is the master
        SimProcess master = currentSimProcess();

        if (!checkProcess(master, where)) // if the master is no valid process
        {
            return false;
        } // just return false

        // make a String to remember all the conditions
        String allConds = "";

        // check each condition and kind of parts
        for (int i = 0; i < conditions.length; i++) {
            // if the condition is not O.K.
            if (!checkCondition(conditions[i], where)) {
                return false; // just return false
            }

            // if the kind of part is not in the parts list
            if (partsList.getIndexOfKind(kinds[i]) < 0) {
                sendWarning("The given kind of part is not contained in the "
                        + "parts list. The attempted processing is ignored!",
                    getClass().getName() + ": " + getQuotedName()
                        + ", Method: " + where,
                    "The given kind of part '" + kinds[i].getName()
                        + "' is not contained "
                        + "in the parts list of this WorkStation.",
                    "Make sure to claim special conditions only for parts listed in the "
                        + "parts list of this WorkStation.");

                return false; // return false
            }

            // add this Condition to the String of all conditions
            allConds = allConds + conditions[i].getQuotedName() + " ";
        }

        // check if capacity limit of master queue is reached
        if (queueLimit <= length()) {
            if (currentlySendDebugNotes()) {
                sendDebugNote("refuses to insert "
                    + master.getQuotedName()
                    + " in master queue, because the capacity limit is reached.");
            }

            if (currentlySendTraceNotes()) {
                sendTraceNote("is refused to be enqueued in "
                    + this.getQuotedName()
                    + "'s master queue because the capacity limit ("
                    + getQueueLimit() + ") of the queue is reached");
            }

            mRefused++; // count the refused ones

            return false; // capacity limit is reached
        }

        // insert the master in its waiting-queue
        masterQueue.insert(master);

        // get the array of available and suitable slaves
        SimProcess[] allSuitSlaves = getAllSuitableSlaves(kinds, conditions);

        // check if there are suitable slaves are available right now
        boolean allSuitAvail = (allSuitSlaves != null);

        // check if the master has to wait in his queue
        // if not enough suitable parts are available OR
        // the master is not the first to be served
        if ((!allSuitAvail) || (master != masterQueue.first())) {
            if (currentlySendTraceNotes()) // tell in the trace where the master is waiting
            { // and on what conditions

                sendTraceNote("waits in " + this.getQuotedName() + " for "
                    + allConds);
            }

            // if there are all needed slaves availabe AND
            // this master is not the first in the queue
            if (allPartsAvailable()
                && master != masterQueue.first()) {
                activateFirstMaster(); // activate the first master in the
            } // queue to see what he can do

            do { // block the master process
                master.setBlocked(true); // as long as ...(see while)
                master.skipTraceNote(); // don't tell the user, that we
                master.passivate(); // passivate the master process

                // check again, if enough suitable parts are available now
                allSuitSlaves = getAllSuitableSlaves(kinds, conditions);
                allSuitAvail = (allSuitSlaves != null);

                // if enough suitable slaves are available, break the loop
                if (allSuitAvail) {
                    break;
                }

                // if there are all normal parts available, activate the
                // successor in the
                // master wait queue to see what he can do (pass-fill-rule?)
                if (allPartsAvailable()) {
                    activateAsNext(masterQueue.succ(master));
                }
            } while (true); // endless loop
        }

        // the master has found all the suitable slaves he needs to start the
        // process

        // if there are enough parts available, activate the successor in the
        // master wait queue to see what he can do, too.
        if (allPartsAvailable()) {
            activateAsNext(masterQueue.succ(master));
        }

        // remove this master from the wait queue
        masterQueue.remove(master);
        master.setBlocked(false); // this master is not blocked anymore

        // prepare all the suitable found slaves for processing
        for (int i = 0; i < allSuitSlaves.length; i++) {
            // check the slave SimProcess
            if (!checkProcess(allSuitSlaves[i], where)) // slave process is
            // O.K.?
            {
                return false;
            } // just return false

            // prepare the slave for the cooperation: (set the master for this
            // slave,
            // remove it from its slave queue and unblock it)
            allSuitSlaves[i].cooperate();
        }

        // start the real processing (cooperation) with the set of slaves
        process.cooperation(master, allSuitSlaves);

        // the processing process is over, so free and activate all slaves
        for (int k = 0; k < allSuitSlaves.length; k++) {
            // no master is controlling the slave anymore
            allSuitSlaves[k].resetMaster();

            // activate the slave only if it is not a component (part) of a
            // ComplexSimProcess now
            if (!allSuitSlaves[k].isComponent()) {
                // get the slave activated after the master
                allSuitSlaves[k].activateAfter(master);
            }

            // if the slave has become a Component of a ComplexSimProcess, he
            // will
            // be activated when he is removed from the ComplexSimProcess
        }

        return true;
    }

    /**
     * Resets all statistical counters to their default values. Both, master queue and slave queues are reset. The
     * mininum and maximum length of the queues are set to the current number of queued objects.
     */
    public void reset() {
        super.reset(); // reset of the QueueBased master queue

        for (int i = 0; i < numOfParts; i++) {
            slaveQueues[i].reset(); // reset of the slave queue at index i

            slavesRefused[i] = 0; // reset the statistics of the refused slave
            // processes
        }

        mRefused = 0; // reset the statistics of the refused master processes
    }

    /**
     * Returns the average length of the slave queue at the given index since the last reset. If the time span since the
     * last reset is smaller than the smallest distinguishable timespan epsilon, the current length of the slave queue
     * will be returned.
     *
     * @param index int : Indicating the kind of part (slave) for which we want to know its average length. To get the
     *              index of a certain kind of part (slave) use the method <code>getIndexOfKind()</code> of the
     *              <code>PartsList</code>.
     * @return double : The average length of the slave queue at the given index since last reset or current length of
     *     the slave queue at the given index if no distinguishable periode of time has passed.
     */
    public double sAverageLength(int index) {
        // the average length of the slave ProcessQueue at the given index
        return slaveQueues[index].averageLength();
    }

    /**
     * Returns the average waiting time of all processes who have exited the slave queue indicated by the index.. The
     * returned value is valid for the time span since the last reset. Returns 0 (zero) if no process have exited this
     * slave queue after the last reset.
     *
     * @param index int : Indicating the kind of part (slave) for which we want to know its average waiting time. To get
     *              the index of a certain kind of part (slave) use the method
     *              <code>getIndexOfKind()</code> of the <code>PartsList</code>.
     * @return TimeSpan : Average waiting time of all processes since last reset or 0 if no process has exited this
     *     slave queue.
     */
    public TimeSpan sAverageWaitTime(int index) {
        // return the average wait time of the indicated slave ProcessQueue
        return slaveQueues[index].averageWaitTime();
    }

    /**
     * Sets the capacity of the given slave queue to a new value. But only if the new capacity is equal or larger than
     * the current length of that slave queue! To get the index of the slave queue for which the capacity sould be
     * changed use the method <code>getIndexOfKind()</code> of the
     * <code>PartsList</code>.
     *
     * @param index       int : The index indicating the slave queue for which the capacity will be changed. To get that
     *                    index one can use the method <code>getIndexOfKind()</code> of the
     *                    <code>PartsList</code>.
     * @param newCapacity int : The new capacity of the slave queue indicated by the index.
     */
    public void setSQueueCapacity(int index, int newCapacity) {

        // check the index
        if (index < 0 || index >= numOfParts) {
            sendWarning(
                "The given index determining the slave queue is negative or "
                    + "out of bounds. There is no queue for that index. The capacity "
                    + "will remain unchanged!",
                getClass().getName()
                    + ": "
                    + getQuotedName()
                    + ", Method: "
                    + "void setSQueueCapacity(int index, int newCapacity)",
                "The given index is out of bounds.",
                "Make sure to provide a valid index indicating the slave queue for "
                    + "which the capacity should be changed.");

            return; // ignore that rubbish and just return
        }

        // check if the new capacity is negative OR smaller than the queue
        // length
        if (newCapacity < 0 || newCapacity < sLength(index)) {
            sendWarning(
                "The given capacity is negative or smaller than the current "
                    + "length of the slave queue. The capacity will remain unchanged!",
                getClass().getName()
                    + ": "
                    + getQuotedName()
                    + ", Method: "
                    + "void setSQueueCapacity(int index, int newCapacity)",
                "A capacity that is negative or smaller than the current lenght of "
                    + "the slave queue does not make sense.",
                "Make sure to change the capacity only to a non negative valid value.");

            return; // ignore that rubbish and just return
        }

        // change the capacity of the given slave queue
        slaveQueues[index].setQueueCapacity(newCapacity);
    }

    /**
     * Sets the queueing discipline for the underlying slave queue at the given index. The sort order of the given slave
     * queue can only be changed if it is empty! To get the index for the kind of part for which the queueing discipline
     * should be changed use the method <code>getIndexOfKind()</code> of the <code>PartsList</code>. Please choose a
     * constant from
     * <code>QueueBased</code> like <code>QueueBased.FIFO</code> or
     * <code>QueueBased.LIFO</code> to determine the sort order.
     *
     * @param index     int : The index of the kind of parts for which the queueing discipline should be changed. To get
     *                  that index one can use the method <code>getIndexOfKind()</code> of the
     *                  <code>PartsList</code>.
     * @param sortOrder int : determines the sort order of the underlying slave queues implementation at the given
     *                  index. Choose a constant from
     *                  <code>QueueBased</code> like <code>QueueBased.FIFO</code>
     *                  or <code>QueueBased.LIFO</code>.
     */
    public void setSQueueStrategy(int index, int sortOrder) {

        // check the index
        if (index < 0 || index >= numOfParts) {
            sendWarning(
                "The given index determining the slave queue is negative or "
                    + "out of bounds. There is no queue for that index. The queueing "
                    + "discipline will remain unchanged!",
                getClass().getName()
                    + ": "
                    + getQuotedName()
                    + ", Method: "
                    + "void setSQueueStrategy(int index, int sortOrder)",
                "The given index is out of bounds.",
                "Make sure to provide a valid index indicating the slave queue for "
                    + "which the queueing discipline should be changed.");

            return; // ignore that rubbish and just return
        }

        // check if the given queue is not empty (filled already)
        if (!slaveQueues[index].isEmpty()) {
            sendWarning(
                "The slave queue for which the queueing discipline should be "
                    + "changed is not empty. The queueing discipline will remain unchanged!",
                getClass().getName()
                    + ": "
                    + getQuotedName()
                    + ", Method: "
                    + "void setSQueueStrategy(int index, int sortOrder)",
                "The given slave queue already contains some slaves ordered according a "
                    + "certain order.",
                "Make sure to change the sort order only for empty slave queues.");

            return; // ignore that rubbish and just return
        }

        // check if a valid sort order is given for the slave queue
        if (sortOrder < 0 || sortOrder >= 3) {
            sendWarning(
                "The given sortOrder parameter is negative or too big! "
                    + "The sort order of the queue will remain unchanged!",
                getClass().getName()
                    + ": "
                    + getQuotedName()
                    + ", Method: "
                    + "void setSQueueStrategy(int index, int sortOrder)",
                "A valid positive integer number must be provided to "
                    + "determine the sort order of the underlying queue.",
                "Make sure to provide a valid positive integer number "
                    + "by using the constants in the class QueueBased, like "
                    + "QueueBased.FIFO or QueueBased.LIFO.");

            return; // ignore that rubbish and just return
        }

        // change the sort order (queueing discipline) of the given slave queue
        slaveQueues[index].setQueueStrategy(sortOrder);

    }

    /**
     * Returns a boolean value indicating if the slave queue indicated by the index is empty or if any SimProcess is
     * currently enqueued in it.
     *
     * @param index int : Indicating the kind of part (slave) for which we want to know if its wait queue is empty or
     *              not. To get the index of a certain kind of part (slave) use the method
     *              <code>getIndexOfKind()</code> of the <code>PartsList</code>.
     * @return boolean : Is <code>true</code> if the indicated slave queue is empty, <code>false</code> otherwise.
     */
    public boolean sIsEmpty(int index) {
        // is the indicated wait queue of the slave empty?
        return slaveQueues[index].isEmpty();
    }

    /**
     * Returns the current length of the slave queue indicated by the index.
     *
     * @param index int : Indicating the kind of part (slave) for which we want to know its current queue length. To get
     *              the index of a certain kind of part (slave) use the method
     *              <code>getIndexOfKind()</code> of the <code>PartsList</code>.
     * @return long : The current length of the slave queue indicated by the index.
     */
    public long sLength(int index) {
        // return the length of the indicated slave ProcessQueue
        return slaveQueues[index].length();
    }

    /**
     * Returns the maximum length of the slave queue indicated by the index since the last reset.
     *
     * @param index int : Indicating the kind of part (slave) for which we want to know its maximum length of the queue.
     *              To get the index of a certain kind of part (slave) use the method
     *              <code>getIndexOfKind()</code> of the <code>PartsList</code>.
     * @return long : The maximum length of the indicated slave queue since the last reset.
     */
    public long sMaxLength(int index) {
        // return the maximum length of the indicated slave ProcessQueue
        return slaveQueues[index].maxLength();
    }

    /**
     * Returns the point of simulation time with the maximum number of Sim-processes waiting inside the slave queue
     * indicated by the index. The value is valid for the period since the last reset.
     *
     * @param index int : Indicating the kind of part (slave) for which we want to know when it had its maximum wait
     *              queue length. To get the index of a certain kind of part (slave) use the method
     *              <code>getIndexOfKind()</code> of the <code>PartsList</code>.
     * @return TimeInstant : Point of simulation time when the indicated slave queue had its maximum length since the
     *     last reset.
     */
    public TimeInstant sMaxLengthAt(int index) {
        // return the point of simulation time when the indicated wait queue had
        // its
        // maximum length
        return slaveQueues[index].maxLengthAt();
    }

    /**
     * Returns the maximum duration in simulation time that an process has spent waiting inside the slave queue
     * indicated by the index. The value is valid for the period since the last reset.
     *
     * @param index int : Indicating the kind of part (slave) for which we want to know its maximum waiting time. To get
     *              the index of a certain kind of part (slave) use the method
     *              <code>getIndexOfKind()</code> of the <code>PartsList</code>.
     * @return desmoj.core.TimeSpan : Longest waiting time of a process in the slave queue since the last reset.
     */
    public TimeSpan sMaxWaitTime(int index) {
        return slaveQueues[index].maxWaitTime();
    }

    /**
     * Returns the point of simulation time when the process with the maximum waiting time exited the slave queue
     * indicated by the index. The value is valid for the period since the last reset.
     *
     * @param index int : Indicating the kind of part (slave) for which we want to know the point of simulation time
     *              when the process with the maximum waiting time exited the slave queue. To get the index of a certain
     *              kind of part (slave) use the method
     *              <code>getIndexOfKind()</code> of the <code>PartsList</code>.
     * @return desmoj.core.TimeInstant : The point of simulation time when the process with the maximum waiting time
     *     exited the slave queue.
     */
    public TimeInstant sMaxWaitTimeAt(int index) {
        return slaveQueues[index].maxWaitTimeAt();
    }

    /**
     * Returns the minimumn length of the slave queue indicated by the index since the last reset.
     *
     * @param index int : Indicating the kind of part (slave) for which we want to know its minimum wait queue length.
     *              To get the index of a certain kind of part (slave) use the method
     *              <code>getIndexOfKind()</code> of the <code>PartsList</code>.
     * @return long : The minimum slave queue length since the last reset.
     */
    public long sMinLength(int index) {
        return slaveQueues[index].minLength();
    }

    /**
     * Returns the point of simulation time with the minimum number of processes waiting inside the slave queue
     * indicated by the index. The value is valid for the period since the last reset.
     *
     * @param index int : Indicating the kind of part (slave) for which we want to know the point of simulation time
     *              with the minimum number of processes waiting inside this slave queue. To get the index of a certain
     *              kind of part (slave) use the method
     *              <code>getIndexOfKind()</code> of the <code>PartsList</code>.
     * @return desmoj.core.TimeInstant : Point of time with minimum slave queue length since the last reset.
     */
    public TimeInstant sMinLengthAt(int index) {
        return slaveQueues[index].minLengthAt();
    }

    /**
     * Returns the standard deviation of the length of the slave queue indicated by the index. The value is weighted
     * over time.
     *
     * @param index int : Indicating the kind of part (slave) for which we want to know its standard deviation of the
     *              length of his queue. To get the index of a certain kind of part (slave) use the method
     *              <code>getIndexOfKind()</code> of the <code>PartsList</code>.
     * @return double : The standard deviation for the slave queue's length weighted over time.
     */
    public double sStdDevLength(int index) {
        return slaveQueues[index].stdDevLength();
    }

    /**
     * Returns the standard deviation of the slave queue's processes waiting times. The slave queue is indicated by the
     * index.
     *
     * @param index int : Indicating the kind of part (slave) for which we want to know its standard deviation of
     *              process waiting times. To get the index of a certain kind of part (slave) use the method
     *              <code>getIndexOfKind()</code> of the <code>PartsList</code>.
     * @return TimeSpan : The standard deviation for the slave queue's processes waiting times.
     */
    public TimeSpan sStdDevWaitTime(int index) {
        return slaveQueues[index].stdDevWaitTime();
    }

    /**
     * Returns the number of processes that have passed through the slave queue indicated by the index without spending
     * time waiting.
     *
     * @param index int : Indicating the kind of part (slave) for which we want to know how many parts hav elef tthe
     *              queue without time spent waiting. To get the index of a certain kind of part (slave) use the method
     *              <code>getIndexOfKind()</code> of the
     *              <code>PartsList</code>.
     * @return long : The number of processes who have passed the slave queue without time waiting.
     */
    public long sZeroWaits(int index) {
        return slaveQueues[index].zeroWaits();
    }

    /**
     * This method is called from a sim-process (part or product) which wants to be processed at this WorkStation as a
     * slave. If no suitable master process and enough other slave processes are available at the moment, this slave
     * process will be stored in its slave queue, until a suitable master and enough other parts (or products) are
     * available to start the manufacturing (processing) process. If the capacity limit of tis slave queue is reached,
     * the process will not be enqueued and <code>false</code> returned. During the cooperation the master process is
     * the only active one. The slave processes are passive and will be reactivated after the cooperation is done.
     *
     * @return boolean : Is <code>true</code> if the process can be enqueued successfully, <code>false</code> otherwise
     *     (i.e. capacity limit of the slave queue is reached).
     */
    public boolean waitOnProcessing() throws SuspendExecution {
        where = "boolean waitOnProcessing ()";

        // the current process calling the waitOnCoop()-method is the slave
        SimProcess slave = currentSimProcess();

        if (!checkProcess(slave, where)) // if the slave is not a valid
        // process
        {
            return false;
        } // just return false

        if (slave.getSlaveWaitQueue() != null) // the slave process is already
        { // waiting for a master
            sendWarning(
                "A slave process already waiting in the slave waiting "
                    + "queue: "
                    + slave.getSlaveWaitQueue().getName()
                    + " is trying to initiate a second cooperation. The attempted second "
                    + "cooperation is ignored!",
                getClass().getName() + ": " + getQuotedName()
                    + ", Method: " + where,
                "The slave process can not wait in more than one waiting-queue.",
                "Make sure that slave processes are only cooperaating with one master "
                    + "at a time.");
            return false; // ignore the second cooperation, just return false.
        }

        // determine the slave queue for this kind of part
        int slaveIndex = partsList.getIndexOfKind(slave.getClass());
        ProcessQueue slaveQueue = slaveQueues[slaveIndex];

        // check if capacity limit of slave queue is reached
        if (slaveQueue.getQueueLimit() <= slaveQueue.length()) {
            if (currentlySendDebugNotes()) {
                sendDebugNote("refuses to insert " + slave.getQuotedName()
                    + " in slave queue, because the capacity limit is reached.");
            }

            if (currentlySendTraceNotes()) {
                sendTraceNote("is refused to be enqueued in "
                    + this.getQuotedName()
                    + "'s slave queue because the capacity limit ("
                    + slaveQueue.getQueueLimit() + ") of the queue is reached");
            }

            slavesRefused[slaveIndex]++; // count the refused ones

            return false; // capacity limit is reached
        }

        slaveQueue.insert(slave); // insert the slave process in his queue

        slave.setSlaveWaitQueue(slaveQueue); // tell the sim-process where he
        // is waiting. Will be reset in SimProcess.cooperate(), when the
        // master is leading the slave through the cooperation.

        if (currentlySendTraceNotes()) // tell in the trace where the slave is waiting
        {
            sendTraceNote("waits in " + slaveQueue.getQuotedName());
        }

        // are there masters already waiting?
        if (length() > 0) {
            activateFirstMaster(); // activate the first master in the queue
        }

        slave.setBlocked(true); // the slave process is blocked (in the wq)
        slave.skipTraceNote(); // don't tell the user, that we ...
        slave.passivate(); // passivate the slave process

        return true; // cooperation performed successfully
    }
}