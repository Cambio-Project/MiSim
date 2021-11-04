package desmoj.extensions.applicationDomains.production;

import java.util.Vector;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.advancedModellingFeatures.WaitQueue;
import desmoj.core.simulator.Condition;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.SimProcess;

/**
 * The TransportJunction is the place where <code>Transporter</code> s pick up the goods to move them around in a
 * manufacturing system. The goods are normally modeled as some kind of <code>SimProcess</code> es. So what happens at
 * the TransportJunction is some kind of Rendez-vous synchronisation, where <code>Transporter</code> s meet the goods to
 * carry them around. There is one wait queue for the masters (<code>Transporter</code>s) and one queue for the slaves
 * (<code>SimProcess</code> es), where they have to wait for each other to cooperate. The <code>Transporter</code> s are
 * the masters which perform the transportation. The corporate transportation process is described in the method
 * <code>cooperation</code> in a subclass of
 * <code>Transportation</code>. The goods (<code>SimProcess</code> es)
 * transported as slaves keep still during the transport and will be reactivated thereafter. The main difference to a
 * normal <code>WaitQueue</code> is, that in this case one master (<code>Transporter</code>) can cooperate with more
 * than one slave process at a time. The number of slaves processes which will be transported togehter will be
 * determined by the master
 * <code>Transporter</code> process. It depends on the number of available
 * slaves and will be between the minLoad and the capacity of the
 * <code>Transporter</code>. Note that a <code>Transporter</code> with a
 * large minimumLoad in front of the wait queue might block all following
 * <code>Transporter</code> s in the queue until enough goods are available to
 * satisfy his minimum load. Use different priorities for the different
 * <code>Transporter</code> s to cope with this problem. Or set the passBy
 * flag to <code>true</code> to make it possible for transporters with a low minimumLoad to pass by the other
 * transporters. Use the methods
 * <code>setPassBy()</code> and <code>getPassBy()</code>. The first sort
 * criteria for the queues is always highest priorities first, the second queueing discipline of the underlying queues
 * and the capacity limit can be determined by the user (default is FIFO and unlimited capacity). TransportJunction is
 * derived from <code>WaitQueue</code> which in turn is derived from <code>QueueBased</code>, which provides all the
 * statistical functionality for the queues.
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
 * @see WaitQueue
 * @see desmoj.core.simulator.QueueBased
 * @see desmoj.core.advancedModellingFeatures.ProcessCoop
 */
public class TransportJunction extends WaitQueue {

    /**
     * Flag to indicate whether a transporter can pass by other transporters in the master queue which are enqueued
     * before that transporter in the queue. Is <code>false</code> per default.
     */
    private boolean passBy = false;

    /**
     * Constructor for a TransportJunction. There are two waiting-queues constructed, one internal
     * <code>QueueList</code> for the
     * <code>Transporter</code> s (masters) and one separate
     * <code>ProcessQueue</code> for the slave processes. The queueing
     * discipline and the capacity limit of the underlying queues can be chosen. Highest priority are always first in
     * the queues.
     *
     * @param owner        desmoj.Model : The model this TransportJunction is associated to.
     * @param name         java.lang.String : The name of this TransportJunction.
     * @param mSortOrder   int : determines the sort order of the underlying master queue implementation. Choose a
     *                     constant from <code>QueueBased</code> like <code>QueueBased.FIFO</code> or
     *                     <code>QueueBased.LIFO</code> or ...
     * @param mQCapacity   int : The capacity of the master queue, that is how many processes can be enqueued. Zero (0)
     *                     means unlimited capacity.
     * @param sSortOrder   int : determines the sort order of the underlying slave queue implementation. Choose a
     *                     constant from <code>QueueBased</code> like <code>QueueBased.FIFO</code> or
     *                     <code>QueueBased.LIFO</code> or ...
     * @param sQCapacity   int : The capacity of the slave queue, that is how many processes can be enqueued. Zero (0)
     *                     means unlimited capacity.
     * @param showInReport boolean : Flag, if TransportJunction should produce a report or not.
     * @param showInTrace  boolean : Flag, if trace messages of this TransportJunction should be displayed in the trace
     *                     file.
     */
    public TransportJunction(Model owner, String name, int mSortOrder,
                             int mQCapacity, int sSortOrder, int sQCapacity,
                             boolean showInReport, boolean showInTrace) {
        super(owner, name, mSortOrder, mQCapacity, sSortOrder, sQCapacity,
            showInReport, showInTrace);
    }

    /**
     * Constructor for a TransportJunction. There are two waiting-queues constructed, one internal
     * <code>QueueList</code> for the
     * <code>Transporter</code> s (masters) and one separate
     * <code>ProcessQueue</code> for the slave processes. Both queues have a
     * FIFO sort order and no capacity limit.
     *
     * @param owner        desmoj.Model : The model this TransportJunction is associated to.
     * @param name         java.lang.String : The TransportJunction's name
     * @param showInReport boolean : Flag, if TransportJunction should produce a report or not.
     * @param showInTrace  boolean : Flag, if trace messages of this TransportJunction should be displayed in the trace
     *                     file.
     */
    public TransportJunction(Model owner, String name, boolean showInReport,
                             boolean showInTrace) {
        super(owner, name, showInReport, showInTrace);
    }

    /**
     * Returns an array of available slave SimProcesses which comply to a given condition at this moment.
     *
     * @param cond desmoj.Condition : The condition to which the sim-processes we are looking for must comply.
     * @return desmoj.SimProcess[] : The array of available slave SimProcesses which comply to the given condition. If
     *     no suitable SimProcess is available <code>null</code> will be returned.
     */
    public synchronized SimProcess[] availableSet(Condition cond) {

        where = "synchronized SimProcess[] availableSet(Condition cond)";

        // check the condition
        if (!checkCondition(cond, where)) // if the condition is not valid
        {
            return null;
        } // just return null

        // get the first SimProcess fulfilling the given condition
        SimProcess first = slaveQueue.first(cond);

        // check if any SimProcess fulfilling the condition is available
        if (first == null) {
            return null; // no such slave available
        } else {
            // make a Vector to hold all the sim-processes we find
            Vector foundSlaves = new Vector();

            // loop through all the sim-processes fulfilling the given condition
            for (SimProcess tmp = first; tmp != null; tmp = slaveQueue.succ(
                tmp, cond)) {
                foundSlaves.addElement(tmp);
            }

            // make an array to hold all the found SimProcesses
            SimProcess[] foundProcs = new SimProcess[foundSlaves.size()];

            // copy the Vector into that array
            foundSlaves.copyInto(foundProcs);

            // return that array
            return foundProcs;
        }

    }

    /**
     * Returns a transporter process waiting in the transporter (master) queue complying to the given condition. If
     * there is no such transporter waiting
     * <code>null</code> is returned.
     *
     * @param cond Condition : The Condition <code>cond</code> is describing the condition to which the trasnporter must
     *             comply to. This has to be implemented by the user in the class:
     *             <code>Condition</code> in the method: <code>check()</code>.
     * @return Transporter : Returns the first transporter in the master queue which complies to the given condition.
     */
    public Transporter availTransporter(Condition cond) {
        where = "Transporter availTransporter(Condition cond)";

        // the current SimProcess is assumed to be a slave looking for a master
        SimProcess slave = currentSimProcess();

        if (!checkProcess(slave, where)) // if slave is not valid
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

        for (Transporter master = (Transporter) masterQueue.first(); master != null; master = (Transporter) masterQueue
            .succ(master)) {
            if (cond.check(master)) {
                return master;
            }
        }

        // if no Transporter complies to the condition just return null
        return null;

    } // end method

    /**
     * This method is inherited from the class <code>WaitQueue</code> and will be overwritten here to use the more
     * suitable method
     * <code>transport(Transportation)</code>. If the capacity limit of the
     * master queue is reached, the process will not be enqueued and
     * <code>false</code> returned.
     *
     * @param transportation Transportation : The transportation process transportation is describing the joint action
     *                       of the processes. The transport to be carried out has to be implemented by the user in the
     *                       class:
     *                       <code>Transportation</code> in the method:
     *                       <code>cooperation()</code>.
     * @return boolean : Is <code>true</code> if the process can be enqueued successfully, <code>false</code> otherwise
     *     (i.e. capacity limit of the master queue is reached).
     */
    public boolean cooperate(Transportation transportation) throws SuspendExecution {
        // call the new method especially for transport purposes from this
        // TransportJunction
        return transport(transportation);
    }

    /**
     * This method is inherited from the class <code>WaitQueue</code> and will be overwritten here to use the more
     * suitable method
     * <code>transport(Transportation, Condition)</code>.
     *
     * @param transportation Transportation : The transportation process transportation is describing the joint action
     *                       of the processes. The transport to be carried out has to be implemented by the user in the
     *                       class:
     *                       <code>Transportation</code> in the method:
     *                       <code>cooperation()</code>.
     * @param cond           Condition : The Condition <code>cond</code> is describing the condition to which the slave
     *                       process must comply to. This has to be implemented by the user in the class:
     *                       <code>Condition</code> in the method: <code>check()</code>.
     * @return boolean : Is <code>true</code> if the process can be enqueued successfully, <code>false</code> otherwise
     *     (i.e. capacity limit of the master queue is reached).
     */
    public boolean cooperate(Transportation transportation, Condition cond) throws SuspendExecution {
        // call the new method especially for transport purposes from this
        // TransportJunction
        return transport(transportation, cond);
    }

    /**
     * Returns a Reporter to produce a report about this TransportJunction.
     *
     * @return desmoj.report.Reporter : The Reporter for the queues inside this TransportJunction.
     */
    public desmoj.core.report.Reporter createDefaultReporter() {
        return new desmoj.extensions.applicationDomains.production.report.TransportReporter(
            this);
        // a reporter for the queue statistics
    }

    /**
     * Returns whether a transporter can pass by other transporters which are enqueued before him in the queue.
     *
     * @return boolean : Indicates whether transporters can pass by other transporters which are enqueued before them in
     *     the queue.
     */
    public boolean getPassBy() {
        return passBy;
    }

    /**
     * Sets the flag passBy to a new value. PassBy is indicating whether transporters can pass by other transporters
     * which are enqueued before them in the queue.
     *
     * @param newPassBy boolean : The new value of passBy. Set it to <code>true</code> if you want transporters to pass
     *                  by other transporters which are enqueued before them in the queue. Set it to
     *                  <code>false</code> if you don't want transporters to
     *                  overtake other transporters in the queue.
     */
    public void setPassBy(boolean newPassBy) {
        this.passBy = newPassBy; // that's all!
    }

    /**
     * This method is to be called from a <code>Transporter</code> which wants to transport goods as a master. If not
     * enough suitable goods (slave processes) are available at the moment, the transporter process will be stored in
     * the master waiting-queue, until enough suitable slaves are available. If the capacity limit of the master queue
     * is reached, the process will not be enqueued and <code>false</code> returned. When enough suitable slaves are
     * available their
     * <code>prepareTransport()</code> method (in the class
     * <code>SimProcess</code>) will be called. During the transportation
     * process the master process is the only active one. The slave process is passive and will be reactivated after the
     * transportation is done.
     *
     * @param transportation Transportation : The transportation process transportation is describing the joint action
     *                       of the processes. The transportation to be carried out has to be implemented by the user in
     *                       the class: <code>Transportation</code> in the method: <code>transport()</code>.
     * @return boolean : Is <code>true</code> if the process can be enqueued successfully, <code>false</code> otherwise
     *     (i.e. capacity limit of the master queue is reached).
     */
    public boolean transport(Transportation transportation) throws SuspendExecution {
        where = "boolean transport(Transportation transportation)";

        // check the ProcessCoop
        if (!isModelCompatible(transportation)) {
            sendWarning(
                "The given Transportation object does not "
                    + "belong to this model. The attempted transportation is ignored!",
                getClass().getName() + ": " + getQuotedName()
                    + ", Method: " + where,
                "The Transportation is not modelcompatible.",
                "Make sure that the Transportation belongs to this model.");

            return false; // transport is not modelcompatible
        }

        // the current SimProcess which was calling transport() is the master
        SimProcess currntProc = currentSimProcess();

        Transporter master; // declare the variable for later use

        // check if it is a Transporter
        if (currntProc instanceof Transporter) {
            master = (Transporter) currntProc; // cast it to the right type
        } else {
            sendWarning(
                "The sim-process using a TransportJunction is not a "
                    + "Transporter. The attempted action is ignored!",
                getClass().getName() + ": " + getQuotedName()
                    + ", Method: " + where,
                "A TransportJunction is designed to let Transporters pick "
                    + "up other SimProcesses for transportation purposes.",
                "Make sure that only Transporters are trying to cooperate "
                    + "as masters in a TransportJunction with other SimProcesses.");

            return false; // ignore that rubbish
        }

        if (!checkProcess(master, where)) // if the master is no valid process
        {
            return false;
        } // just return false

        // check if capacity limit of master queue is reached
        if (queueLimit <= length()) {
            // to have a queue capacity limit which is less than the number of
            // transporters does not make much sense
            sendWarning(
                "The queue capacity of the TransportJunction can not hold "
                    + "all the transporters. That does not make much sense!",
                getClass().getName() + ": " + getQuotedName()
                    + ", Method: " + where,
                "There are more transporters than the queue in the "
                    + "TransportJunction can hold. The remainder of the "
                    + "transporters will get lost.",
                "Make sure to provide a queue capacity in the Transport"
                    + "Junction which can hold all of the transporters, so no "
                    + "one gets lost.");

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

        // is it possible for this process to pass by?
        if (passBy == false) {
            // check if the master has to wait in his queue; in case of:
            // not enough slaves available OR
            // this master is not the first to be served
            if (slaveQueue.length() < master.getMinLoad()
                || master != masterQueue.first()) {
                // tell in the trace where the master is waiting
                if (currentlySendTraceNotes()) {
                    sendTraceNote("waits in " + this.getQuotedName());
                }

                if (slaveQueue.length() > 0) // there are slaves waiting
                {
                    activateFirst(); // activate the first master in the
                } // queue to see what he can do

                do { // block the master process
                    master.setBlocked(true); // as long as ...(see while)
                    master.skipTraceNote(); // don't tell the user, that we
                    master.passivate(); // passivate the master process
                } while (slaveQueue.length() < master.getMinLoad()); // not
                // enough
                // slaves
            }
        } // end if (passBy = false)

        else // the transporter can pass by other transporters in the queue
        {
            if (slaveQueue.length() < master.getMinLoad()
                || master != masterQueue.first()) {
                // if this transporter is not the first in the queue OR
                if (master != masterQueue.first()
                    || slaveQueue.length() > 0) // there are slaves waiting
                {
                    // we have to make sure that no other transporter in front
                    // of this
                    // current process in the master queue could be satisfied,
                    // so activate
                    // the first transporter in the queue to see what he can do.
                    // He will
                    // pass the activation on to his successors until this
                    // process will be
                    // activated again to get his goods to transport.
                    // (hopefully)
                    activateFirst();
                }

                // only if not enough slaves are available the master has to
                // wait
                if (slaveQueue.length() < master.getMinLoad()) {
                    // tell in the trace where the master is waiting
                    if (currentlySendTraceNotes()) {
                        sendTraceNote("waits in " + this.getQuotedName());
                    }
                }

                // block and passivate the transporter until enough goods are
                // available
                do { // block the master process
                    master.setBlocked(true); // as long as ...(see while)
                    master.skipTraceNote(); // don't tell the user, that we
                    master.passivate(); // passivate the master process

                    // activate the transporter in the master queue to see what
                    // he can do
                    activateAsNext((SimProcess) masterQueue.succ(master));
                } while (slaveQueue.length() < master.getMinLoad()); // not
                // enough
                // slaves
            }
        } // end else (passBy = true)

        // the master has found slave(s) to cooperate with...

        masterQueue.remove(master); // remove this master from the wait queue
        master.setBlocked(false); // this master is not blocked anymore

        // activate the new first transporter in the master queue
        activateFirst();

        // determine how many slaves will be transported
        int units;
        if (slaveQueue.length() < master.getCapacity()) {
            units = slaveQueue.length();
        } else {
            units = master.getCapacity();
        }

        // make the array of slaves which will be transported
        SimProcess[] goods = new SimProcess[units];

        // fill the array of goods to be transported
        for (int i = 0; i < units; i++) {
            // get the first slave from its queue
            SimProcess slave = slaveQueue.first();

            if (!checkProcess(slave, where)) // if the slave process is not
            // O.K.
            {
                return false;
            } // just return

            // put the slave in the array
            goods[i] = slave;

            // prepare the slave for the transport
            slave.prepareTransport(); // removes the slave from its queue also
        }

        // start the real transport with the array of slaves
        transportation.transport(master, goods);

        // release and activate all the slaves again
        for (int i = 0; i < units; i++) {
            // the transport is over, so no master is controlling the slave
            // anymore
            goods[i].resetMaster();

            // the master is done with the transportation
            // so get the slave activated after him
            goods[i].activateAfter(master);
        }

        return true;
    }

    /**
     * This method is to be called from a <code>Transporter</code> who wants to transport goods which comply to a
     * certain condition. The condition must be specified in the method <code>check()</code> in a class derived from
     * <code>Condition</code>. If not enough suitable goods (slave processes) are available at the moment, the
     * transporter process will be stored in the master waiting-queue, until enough suitable slaves are available. If
     * the capacity limit of the master queue is reached, the process will not be enqueued and <code>false</code>
     * returned. When enough suitable slaves are available their
     * <code>prepareTransport()</code> method (in the class
     * <code>SimProcess</code>) will be called. During the transportation
     * process the master process is the only active one. The slave process is passive and will be reactivated after the
     * transportation is done.
     *
     * @param transportation Transportation : The transportation process is describing the joint action of the
     *                       processes. The transportation to be carried out has to be implemented by the user in the
     *                       class:
     *                       <code>Transportation</code> in the method:
     *                       <code>transport()</code>.
     * @param cond           Condition : The Condition <code>cond</code> is describing the condition to which the slave
     *                       process must comply. This has to be implemented by the user in the class:
     *                       <code>Condition</code> in the method: <code>check()</code>.
     * @return boolean : Is <code>true</code> if the process can be enqueued successfully, <code>false</code> otherwise
     *     (i.e. capacity limit of the master queue is reached).
     */
    public boolean transport(Transportation transportation, Condition cond) throws SuspendExecution {
        where = "boolean transport(Transportation transportation, Condition cond)";

        // check the ProcessCoop
        if (!isModelCompatible(transportation)) {
            sendWarning(
                "The given Transportation object does not "
                    + "belong to this model. The attempted transportation is ignored!",
                getClass().getName() + ": " + getQuotedName()
                    + ", Method: " + where,
                "The Transportation is not modelcompatible.",
                "Make sure that the Transportation belongs to this model.");

            return false; // transport is not modelcompatible
        }

        // the current SimProcess which was calling transport() is the master
        SimProcess currntProc = currentSimProcess();

        Transporter master; // declare the variable for later use

        // check if it is a Transporter
        if (currntProc instanceof Transporter) {
            master = (Transporter) currntProc; // cast it to the right type
        } else {
            sendWarning(
                "The sim-process using a TransportJunction is not a "
                    + "Transporter. The attempted action is ignored!",
                getClass().getName() + ": " + getQuotedName()
                    + ", Method: " + where,
                "A TransportJunction is designed to let Transporters pick "
                    + "up other SimProcesses for transportation purposes.",
                "Make sure that only Transporters are trying to cooperate "
                    + "as masters in a TransportJunction with other SimProcesses.");

            return false; // ignore that rubbish
        }

        if (!checkProcess(master, where)) // if the master is no valid process
        {
            return false;
        } // just return false

        if (!checkCondition(cond, where)) // if the condition is not valid
        {
            return false;
        } // just return false

        // check if capacity limit of master queue is reached
        if (queueLimit <= length()) {
            // to have a queue capacity limit which is less than the number of
            // transporters does not make much sense
            sendWarning(
                "The queue capacity of the TransportJunction can not hold "
                    + "all the transporters. That does not make much sense!",
                getClass().getName() + ": " + getQuotedName()
                    + ", Method: " + where,
                "There are more transporters than the queue in the "
                    + "TransportJunction can hold. The remainder of the "
                    + "transporters will get lost.",
                "Make sure to provide a queue capacity in the Transport"
                    + "Junction which can hold all of the transporters, so no "
                    + "one gets lost.");

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

        // get the array of suitable slaves
        SimProcess[] foundProcs = availableSet(cond);

        // is it possible for this master process to pass by?
        if (passBy == false) {

            // check if the master has to wait in his queue; in case of:
            // no OR not enough suitable slaves available OR
            // this master is not the first to be served
            if (foundProcs == null || foundProcs.length < master.getMinLoad()
                || master != masterQueue.first()) {
                if (currentlySendTraceNotes()) // tell in the trace where the master is
                // waiting
                { // and on what condition
                    sendTraceNote("waits in " + this.getQuotedName() + " for "
                        + cond.getQuotedName());
                }

                // if there are slaves waiting AND this master is not the first
                // in the queue
                if (slaveQueue.length() > 0
                    && master != masterQueue.first()) {
                    activateFirst(); // activate the first master in the
                } // queue to see what he can do

                do { // block the master process
                    master.setBlocked(true); // as long as ...(see while)
                    master.skipTraceNote(); // don't tell the user, that we
                    master.passivate(); // passivate the master process

                    // check again if suitable slaves are available
                    foundProcs = availableSet(cond);

                    // if there are slaves waiting
                    if (slaveQueue.length() > 0) {
                        // activate the next master in the queue to see what he
                        // can do
                        activateAsNext((SimProcess) masterQueue.succ(master));
                    }
                }
                // no OR not enough suitable slaves available
                while (foundProcs == null
                    || foundProcs.length < master.getMinLoad());
            }

            // the master has found enough suitable slave(s) to cooperate
            // with...
        } else // the master process is allowed to pass by other processes
        // (passBy = true)
        {
            // check if the master has to wait in his queue; in case of:
            // no OR not enough suitable slaves available OR
            // this master is not the first to be served
            if (foundProcs == null || foundProcs.length < master.getMinLoad()
                || master != masterQueue.first()) {
                // if this transporter is not the first in the queue OR
                if (master != masterQueue.first()
                    || slaveQueue.length() > 0) // there are slaves waiting
                {
                    // we have to make sure that no other transporter in front
                    // of this
                    // current process in the master queue could be satisfied,
                    // so activate
                    // the first transporter in the queue to see what he can do.
                    // He will
                    // pass the activation on to his successors until this
                    // process will be
                    // activated again to get his goods to transport.
                    // (hopefully)
                    activateFirst();
                }

                // only if not enough slaves are available the master has to
                // wait
                if (slaveQueue.length() < master.getMinLoad()) {
                    // tell in the trace where the master is waiting and on what
                    // condition
                    if (currentlySendTraceNotes()) {
                        sendTraceNote("waits in " + this.getQuotedName()
                            + " for " + cond.getQuotedName());
                    }
                }

                do { // block the master process
                    master.setBlocked(true); // as long as ...(see while)
                    master.skipTraceNote(); // don't tell the user, that we
                    master.passivate(); // passivate the master process

                    // check again if suitable slaves are available
                    foundProcs = availableSet(cond);

                    // if there are slaves waiting
                    if (slaveQueue.length() > 0) {
                        // activate the next master in the queue to see what he
                        // can do
                        activateAsNext((SimProcess) masterQueue.succ(master));
                    }
                }
                // no OR not enough suitable slaves available
                while (foundProcs == null
                    || foundProcs.length < master.getMinLoad());
            }
        }
        masterQueue.remove(master); // remove this master from the wait queue
        master.setBlocked(false); // this master is not blocked anymore

        // determine how many slaves will be transported
        int units;
        if (foundProcs.length < master.getCapacity()) {
            units = foundProcs.length;
        } else {
            units = master.getCapacity();
        }

        // make the array of slaves which will be transported
        SimProcess[] goods = new SimProcess[units];

        // fill the array of goods to be transported
        for (int i = 0; i < units; i++) {
            // if the slave process is not O.K.
            if (!checkProcess(foundProcs[i], where)) {
                return false;
            } // just return

            // copy the slaves to the array of transported goods
            goods[i] = foundProcs[i];

            // tell in the trace for which condition the master has found which
            // slaves in which queue for which transport...
            if (currentlySendTraceNotes()) {
                sendTraceNote("finds " + cond.getQuotedName() + " "
                    + foundProcs[i].getQuotedName() + " in "
                    + slaveQueue.getQuotedName() + " for "
                    + transportation.getQuotedName());

                skipTraceNote(); // skip the trace note from the following
                // cooperate()
            }

            // prepare the slave for the transport
            foundProcs[i].prepareTransport(); // removes the slave from its
            // queue also
        }

        // start the real transport with the array of slaves
        transportation.transport(master, goods);

        // release and activate all the slaves again
        for (int i = 0; i < units; i++) {
            // the transportation is over, so no master is controlling the slave
            // anymore
            goods[i].resetMaster();

            // the master is done with the transportation
            // so get the slave activated after him
            goods[i].activateAfter(master);
        }

        return true;
    }

    /**
     * This method is inherited from the class <code>WaitQueue</code> and will be overwritten here to use the more
     * suitable method
     * <code>waitOnTransport()</code>.
     *
     * @return boolean : Is <code>true</code> if the process requesting the transportation has been transported
     *     successfully to his destination, <code>false</code> otherwise (i.e. capacity limit of the slave queue is
     *     reached).
     */
    public boolean waitOnCoop() // wait() is a final method in java.lang.Object
        throws SuspendExecution {
        // call the new method especially for transport purposes from this
        // TransportJunction
        return waitOnTransport();
    }

    /**
     * This method is called from a sim-process which wants to be transported as a slave. If no suitable master process
     * (transporter) is available at the moment, the slave process will be stored in the slave queue, until a suitable
     * master (transporter) is available. If the capacity limit of the slave queue is reached, the process will not be
     * enqueued and
     * <code>false</code> will be returned. During the transportation the
     * master process (transporter) is the only active one. The slave process is passive and will be reactivated after
     * the transportation is done.
     *
     * @return boolean : Is <code>true</code> if the process requesting the transportation has been transported
     *     successfully to his destination, <code>false</code> otherwise (i.e. capacity limit of the slave queue is
     *     reached).
     */
    public boolean waitOnTransport() // wait() is a final method in
    // java.lang.Object
        throws SuspendExecution {
        where = "boolean waitOnTransport ()";

        // the current process calling the waitOnTransport()-method is the slave
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
                    + " is trying to initiate a second transportation. The attempted second "
                    + "transportation is ignored!",
                getClass().getName() + ": " + getQuotedName()
                    + ", Method: " + where,
                "The slave process can not wait in more than one waiting-queue.",
                "Make sure that slave processes are only transported by one master "
                    + "at a time.");
            return false; // ignore the second transportation, just return
            // false.
        }

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

            sRefused++; // count the refused ones

            return false; // capacity limit is reached
        }

        slaveQueue.insert(slave); // insert the slave process in the wait
        // queue

        slave.setSlaveWaitQueue(slaveQueue); // tell the sim-process where he
        // is waiting. Will be reset in SimProcess.cooperate(), when the
        // master is leading the slave through the transportation.

        if (currentlySendTraceNotes()) // tell in the trace where the slave is waiting
        {
            sendTraceNote("waits in " + slaveQueue.getQuotedName());
        }

        // are there masters already waiting?
        if (length() > 0) {
            activateFirst(); // activate the first master in the queue
        }

        slave.setBlocked(true); // the slave process is blocked (in the wq)
        slave.skipTraceNote(); // don't tell the user, that we ...
        slave.passivate(); // passivate the slave process

        return true; // transportation performed successfully
    }
}