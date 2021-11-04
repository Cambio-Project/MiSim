package desmoj.extensions.applicationDomains.production;

import java.util.Enumeration;
import java.util.Vector;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.advancedModellingFeatures.Bin;
import desmoj.core.report.Reporter;
import desmoj.core.simulator.Condition;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.QueueBased;
import desmoj.core.simulator.QueueList;
import desmoj.core.simulator.QueueListFifo;
import desmoj.core.simulator.QueueListLifo;
import desmoj.core.simulator.QueueListRandom;
import desmoj.core.simulator.SimProcess;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeOperations;
import desmoj.core.statistic.StatisticObject;

/**
 * Entrepot is some kind of storage where products (in the form of
 * <code>SimProcess</code> es) can be stored. They will be stored in a kind of
 * FIFO order, that means the products arriving first at the Entrepot will be the first ones to be removed. One can
 * remove products from the Entrepot in order to sell (or dispatch) them or to process them in any other way. Note:
 * while products can enter the Entrepot themselves, another process (
 * <code>SimProcess</code>) is needed to remove the products from the
 * Entrepot. Because the product processes are passivated while they are stored in the Entepot! The Entrepot has no
 * capacity limit, so it can store (almost) an endless number of products. The maximum number of available products in
 * the Entrepot will be shown in the report and can be used to judge what a reasonable capacity limit for the Entrepot
 * in the real world might be. In contrast the queue for the waiting customers can have a certain limit. If the
 * customers' queue capacity is reached any further customer will be rejected (refused to service). The default number
 * of products an Entrepot starts with is zero. It will be filled during simulation by some manufacturing process (or
 * the product process itself) using the <code>storeProduct()</code> method. Products can be retrieved (removed) from
 * the Entrepot using one of the <code>removeProduct()</code> methods. If no or not enough products are available the
 * costumers have to wait in a queue until new products are stored in the Entrepot. The first sort criteria of the
 * customer queue is always highest priorities first. The second queueing discipline and the capacity limit of the
 * customer queue can be determined by the user (default is FIFO and unlimited capacity). As long as the products
 * (SimProcesses) are stored in the Entrepot they are passivated and blocked. Entrepot is derived from QueueBased, which
 * provides all the statistical functionality for the customer queue.
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
 * @see Bin
 */
public class Entrepot extends QueueBased {

    // ****** attributes ******

    /**
     * The Vector holding all the products stored in this Entrepot
     */
    private final Vector<SimProcess> products;

    /**
     * The queue actually storing the processes waiting for products
     */
    private final QueueList<SimProcess> queue;

    /**
     * The maximum number of products in the Entrepot
     */
    private long maximum;

    /**
     * Number of customers having obtained products from the Entrepot
     */
    private long customers;

    /**
     * Number of sold products from the Entrepot
     */
    private long soldProducts;

    /**
     * Weighted sum of available products in the Entrepot over the time (must be divided by the total time to get the
     * average available units!)
     */
    private double wSumAvail;

    /**
     * The last time the Entrepot has been used
     */
    private TimeInstant lastUsage;

    /**
     * Indicates the method where something has gone wrong. Is passed as a parameter to the method
     * <code>checkProcess()</code>.
     */
    private String where;

    /**
     * Counter for the sim-processes which are refused to be enqueued, because the queue capacity is full.
     */
    private long refused;

    /**
     * Flag to indicate whether an entity can pass by other entities in the queue which are enqueued before that entity
     * in the queue. Is
     * <code>false</code> as default value.
     */
    private boolean passBy = false;

    /**
     * Constructs an empty Entrepot where products (SimProcesses) can be stored and retrieved. The queueing discipline
     * and the capacity of the underlying customer queue can be chosen.
     *
     * @param owner        desmoj.Model : The model this Entrepot is associated to.
     * @param name         java.lang.String : The name of this Entrepot.
     * @param sortOrder    int : determines the sort order of the underlying queue implementation. Choose a constant
     *                     from <code>QueueBased</code> like <code>QueueBased.FIFO</code> or
     *                     <code>QueueBased.LIFO</code> or ...
     * @param qCapacity    int : The capacity of the queue, that is how many processes can be enqueued. Zero (0) means
     *                     unlimited capacity.
     * @param showInReport boolean : Flag, if this Entrepot should produce a report or not.
     * @param showInTrace  boolean : Flag, if this Entrepot should produce trace messages or not.
     */
    public Entrepot(Model owner, String name, int sortOrder, int qCapacity,
                    boolean showInReport, boolean showInTrace) {
        super(owner, name, showInReport, showInTrace); // make a QueueBased

        // make a Vector to store all the products (SimProcesses)
        products = new Vector<SimProcess>();

        reset();

        // check if a valid sortOrder is given
        switch (sortOrder) {
            case QueueBased.FIFO:
                queue = new QueueListFifo<SimProcess>();
                break;
            case QueueBased.LIFO:
                queue = new QueueListLifo<SimProcess>();
                break;
            case QueueBased.RANDOM:
                queue = new QueueListRandom<SimProcess>();
                break;
            default:
                sendWarning(
                    "The given sortOrder parameter " + sortOrder + " is not valid! "
                        + "A queue with Fifo sort order will be created.",
                    "Entrepot : "
                        + getName()
                        + " Constructor: Entrepot (desmoj.Model owner, String name, "
                        + "int sortOrder, long qCapacity, "
                        + "boolean showInReport, boolean showInTrace)",
                    "A valid positive integer number must be provided to "
                        + "determine the sort order of the queue.",
                    "Make sure to provide a valid positive integer number "
                        + "by using the constants in the class QueueBased, like "
                        + "QueueBased.FIFO, QueueBased.LIFO or QueueBased.RANDOM.");
                queue = new QueueListFifo<SimProcess>();
        }

        // give the QueueList a reference to this QueueBased
        queue.setQueueBased(this);


        // set the capacity of the queue
        queueLimit = qCapacity;

        // check if it the capacity does make sense
        if (qCapacity < 0) {
            sendWarning(
                "The given capacity of the queue is negative! "
                    + "A queue with unlimited capacity will be created instead.",
                "Entrepot : "
                    + getName()
                    + " Constructor: Entrepot (desmoj.Model owner, String name, "
                    + "int sortOrder, long qCapacity, "
                    + "boolean showInReport, boolean showInTrace)",
                "A negative capacity for a queue does not make sense.",
                "Make sure to provide a valid positive capacity "
                    + "for the underlying queue.");
            // set the capacity to the maximum value
            queueLimit = Integer.MAX_VALUE;
        }

        // check if qCapacity is zero (that means unlimited capacity)
        if (qCapacity == 0) {
            // set the capacity to the maximum value
            queueLimit = Integer.MAX_VALUE;
        }
    }

    /**
     * Constructs an empty Entrepot where products (SimProcesses) can be stored and retrieved. The underlying queue has
     * a FIFO queueing discipline and unlimited capacity.
     *
     * @param owner        desmoj.Model : The model this Entrepot is associated to.
     * @param name         java.lang.String : The name of this Entrepot.
     * @param showInReport boolean : Flag, if this Entrepot should produce a report or not.
     * @param showInTrace  boolean : Flag, if this Entrepot should produce trace messages or not.
     */
    public Entrepot(Model owner, String name, boolean showInReport,
                    boolean showInTrace) {
        super(owner, name, showInReport, showInTrace); // make a QueueBased

        // make a Vector to store all the products (SimProcesses)
        products = new Vector<SimProcess>();

        reset();

        queue = new QueueListFifo<SimProcess>(); // make an actual queue and give it a
        queue.setQueueBased(this); // reference of this "QueueBased"-Entrepot

    }

    /**
     * Activates the sim-process <code>process</code>, given as a parameter of this method, as the next process. This
     * process should be a sim-process waiting in the queue for some products.
     *
     * @param process SimProcess : The process that is to be activated as next.
     */
    protected void activateAsNext(SimProcess process) {
        where = "protected void activateAsNext(SimProcess process)";

        if (process != null) {
            // if the given process is not valid just return
            if (!checkProcess(process, where)) {
                return;
            }

            // if the process is scheduled (on the event-list) already
            if (process.isScheduled()) {
                process.skipTraceNote(); // don't tell the user, that we ...
                process.cancel(); // get the process from the event-list
            }

            // remember if the process is blocked at the moment
            boolean wasBlocked = process.isBlocked();

            // unblock the process to be able to activate him
            if (wasBlocked) {
                process.setBlocked(false); // the process is not blocked
                // anymore
                // and
            } // ready to become activated

            // don't tell the user, that we activate the process after the
            // current process
            process.skipTraceNote();
            process.activateAfter(current());

            // the process status is still "blocked"
            if (wasBlocked) {
                process.setBlocked(true);
            }
        } // end outer if
    }

    /**
     * Activates the first process waiting in the customers' queue. That is a process which was trying to get products,
     * but it could not get any because there were not enough products for it or another process was first in the queue
     * to be served. This method is called every time new products have arrived in the Entrepot or a customer in the
     * waiting-queue is satisfied.
     */
    protected void activateFirst() {
        where = "protected void activateFirst()";

        // first is the first process in the queue (or null if none is in the
        // queue)
        SimProcess first = queue.first();

        if (first != null) {
            // if first is not modelcompatible just return
            if (!checkProcess(first, where)) {
                return;
            }

            // if first is scheduled (on the event-list) already
            if (first.isScheduled()) {
                first.skipTraceNote(); // don't tell the user, that we ...
                first.cancel(); // get the process from the event-list
            }

            // remember if first is blocked at the moment
            boolean wasBlocked = first.isBlocked();

            // unblock the process to be able to activate him
            if (wasBlocked) {
                first.setBlocked(false);
            }

            // don't tell the user, that we activate first after the current
            // process
            first.skipTraceNote();
            first.activateAfter(current());

            // the status of first is still "blocked"
            if (wasBlocked) {
                first.setBlocked(true);
            }
        } // end outer if
    }

    /**
     * Returns the average number of products available in the Entrepot over the time since the last reset of the
     * Entrepot.
     *
     * @return double : The average number of products available in the Entrepot over the time since the last reset of
     *     the Entrepot.
     */
    public double avgAvail() {
        TimeInstant now = presentTime(); // what is the time?
        // how long since the last reset
        double diff = now.getTimeAsDouble() - resetAt().getTimeAsDouble();

        // the number of available products at the moment
        int avail = getAvail();

        // update the weighted sum of available products
        double wSumAvl = wSumAvail
            + (avail * (now.getTimeAsDouble() - lastUsage.getTimeAsDouble()));

        if (diff < TimeOperations.getEpsilonSpan().getTimeAsDouble()) // diff is not long enough
        {
            sendWarning("A Division-by-Zero error occured in a calculation. "
                    + "The UNDEFINED Value: -1.0 is returned as result.",
                "Entrepot: " + getName() + " Method: double avgAvail ()",
                "The Time difference is shorter than epsilon.",
                "Make sure not to use avgAvail() right after a reset.");
            return UNDEFINED; // see QueueBased: UNDEFINED = -1
        }
        // return the rounded average
        return StatisticObject.round(wSumAvl / diff);
    }

    /**
     * Checks whether the sim-process using the Entrepot is a valid process.
     *
     * @param p     SimProcess : Is this SimProcess a valid one?
     * @param where String : The method having called <code>checkProcess()</code> as a String.
     * @return boolean :<code>true</code> if and only if the given SimProcess is valid and model compatible;
     *     <code>false</code> otherwise.
     */
    protected boolean checkProcess(SimProcess p, String where) {
        if (p == null) // if p is a null pointer instead of a process
        {
            sendWarning(
                "A non existing process is trying to use an Entrepot object."
                    + " The attempted action is ignored!", "Entrepot: "
                    + getName() + " Method: " + where,
                "The given process is only a null pointer.",
                "Make sure that only real SimProcesses are using Entrepots.");
            return false;
        }

        if (!isModelCompatible(p)) // if p is not modelcompatible
        {
            sendWarning("The process trying to use a Entrepot object does not "
                    + "belong to this model. The attempted action is ignored!",
                "Entrepot: " + getName() + " Method: " + where,
                "The process is not modelcompatible.",
                "Make sure that processes are using only Entrepot within their model.");
            return false;
        }

        return true;
    }

    /**
     * Checks if the given product (SimProcess) is contained in the Entrepot.
     *
     * @param product desmoj.SimProcess : The product which might be in the Entrepot.
     * @return boolean :<code>true</code> if and only if the specified SimProcess is the same as a component in this
     *     Entrepot, as determined by the equals method; <code>false</code> otherwise.
     */
    public boolean contains(SimProcess product) {

        return products.contains(product); // have a look in the Vector
    }

    /**
     * Returns a Reporter to produce a report about this Entrepot.
     *
     * @return desmoj.report.Reporter : The Reporter for the queue inside this Entrepot.
     */
    public Reporter createDefaultReporter() {

        return new desmoj.extensions.applicationDomains.production.report.EntrepotReporter(
            this);
    }

    /**
     * Returns the number of products available at the moment.
     *
     * @return int : The number of products available at the moment.
     */
    public int getAvail() {
        return products.size();
    }

    /**
     * Returns the number of products available in the Entrepot at the moment which are in compliance with the given
     * <code>Condition</code>. This method is useful to test if enough products are available which are in compliance
     * with a given condition (i.e. before you use the method
     * <code>removeProducts(n, condition)</code>).
     *
     * @param condition desmoj.Condition : The <code>Condition</code> the products have to be in compliance with.
     * @return int : The number of products available in the Entrepot at the moment which are in compliance with the
     *     given
     *     <code>Condition</code>.
     */
    public int getAvailComplyWith(Condition condition) {

        // the number of products found to be in compliance with the given
        // condition
        int j = 0;

        // loop throught the whole Vector of products
        for (Enumeration e = products.elements(); e.hasMoreElements(); ) {
            // check if the product is in compliance with the condition
            if (condition.check((SimProcess) e.nextElement())) {
                // increment the number of complying products
                j++;
            }
        }

        return j;
    }

    /**
     * Returns the number of customers having obtained products from the Entrepot.
     *
     * @return long : The number of customers having obtained products from the Entrepot.
     */
    public long getCustomers() {

        return customers;
    }

    /**
     * Returns the maximum number of products in the Entrepot.
     *
     * @return long : The maximum number of products in the Entrepot.
     */
    public long getMaximum() {
        return this.maximum;
    }

    /**
     * Returns whether entities can pass by other entities which are enqueued before them in the queue.
     *
     * @return boolean : Indicates whether entities can pass by other entities which are enqueued before them in the
     *     queue.
     */
    public boolean getPassBy() {
        return passBy;
    }

    /**
     * Sets the flag passBy to a new value. PassBy is indicating whether entities can pass by other entities which are
     * enqueued before them in the queue.
     *
     * @param newPassBy boolean : The new value of passBy. Set it to <code>true</code> if you want entities to pass by
     *                  other entities which are enqueued before them in the queue. Set it to
     *                  <code>false</code> if you don't want entities to overtake
     *                  other entities in the queue.
     */
    public void setPassBy(boolean newPassBy) {
        this.passBy = newPassBy; // that's all!
    }

    /**
     * Returns the implemented queueing discipline of the underlying queue as a String, so it can be displayed in the
     * report.
     *
     * @return String : The String indicating the queueing discipline.
     */
    public String getQueueStrategy() {

        return queue.getAbbreviation(); // that's it

    }

    /**
     * Returns the number of SimProcesses refused to be enqueued in the queue, because the capacity limit is reached.
     *
     * @return long : The number of SimProcesses refused to be enqueued in the queue.
     */
    public long getRefused() {

        return refused; // that's it
    }

    /**
     * Returns the number of products which already have been sold (passed through this Entrepot).
     *
     * @return long : The number of products which already have been sold (passed through this Entrepot).
     */
    public long getSoldProducts() {

        return soldProducts;
    }

    /**
     * Tests if the Entrepot is empty (has no products stored).
     *
     * @return boolean :<code>true</code> if and only if this Entrepot has no products stored (it is empty);
     *     <code>false</code> otherwise.
     */
    public boolean isEmpty() {

        return products.isEmpty(); // ask the Vector
    }

    /**
     * Returns an enumeration of the products stored in this
     * <code>Entrepot</code>. The returned <code>Enumeration</code> object
     * will generate all items contained in the vector <code>products</code>.
     *
     * @return java.util.Enumeration : An enumeration of the products in this Entrepot.
     */
    public Enumeration products() {

        return products.elements();
    }

    /**
     * Removes (and returns) all products (SimProcesses) from the Entrepot which are contained in there at the moment,
     * even if other processes are waiting in the queue. Note, that as long as the lifeCycles of the products
     * (SimProcesses) leaving the Entrepot are not terminated they will automatically be activated after the current
     * SimProcess! If no products are available at the moment <code>null</code> will be returned. The products will be
     * retrieved in a kind of FIFO order, that means the product which arrived first in the Entrepot is the first to be
     * removed.
     *
     * @return desmoj.SimProcess[] : All the products contained in the Entrepot at the moment will be removed. The
     *     lifeCycles of their SimProcesses will be activated as long as they are not terminated. Is <code>null</code>
     *     if no products are available at the moment.
     */
    public SimProcess[] removeAllProducts() {

        where = "SimProcess[] removeAllProducts()";

        SimProcess currentProcess = currentSimProcess();

        // if the current process is not valid: just return
        if (!checkProcess(currentProcess, where)) {
            return null;
        }

        queue.insert(currentProcess); // insert every process in the queue for
        // statistic reasons
        queue.remove(currentProcess); // get the process out of the queue

        // if nothing is in the Entrepot at the moment
        if (isEmpty()) {
            // trace output
            if (currentlySendTraceNotes()) {
                sendTraceNote("removes no products from " + getQuotedName()
                    + ", because there are no available at the moment");
            } // tell in the trace how many products are removed from the
            // Entrepot

            // for debugging purposes
            if (currentlySendDebugNotes()) {
                sendDebugNote("no products are removed, because there are no at "
                    + "the moment.");
            }

            return null;
        } else // if something is in the Entrepot at the moment
        {
            // the number of products in the Entrepot
            int n = products.size();

            // make a Stringbuffer to store all the products removed from the
            // Entrepot
            StringBuffer rmvdProdBuff = new StringBuffer();

            // make the array of removed products
            SimProcess[] removedProducts = new SimProcess[n];

            // loop throught the whole Vector of products
            for (Enumeration e = products.elements(); e.hasMoreElements(); ) {
                SimProcess nextSP = (SimProcess) e.nextElement();

                // add the product to the Stringbuffer (needed for the debug
                // note)
                rmvdProdBuff.append(nextSP.getQuotedName() + " ");

                // the sim-process (product) is not blocked (anymore)
                nextSP.setBlocked(false);

                // activate the removed SimProcess (if it is not terminated yet)
                if (!nextSP.isTerminated()) {
                    nextSP.skipTraceNote();
                    nextSP.activateAfter(current());
                }
            } // end for loop

            // copy the whole Vector into the array to be returned
            products.copyInto(removedProducts);

            // clear the whole Vector
            products.clear();

            updateStatistics(-n); // statistics will be updated
            // with a negative n for remove(), remember?!
            // trace output
            if (currentlySendTraceNotes()) {
                sendTraceNote("removes all " + n + " products from "
                    + getQuotedName());
            } // tell in the trace how many products are removed from the
            // Entrepot

            // for debugging purposes
            if (currentlySendDebugNotes()) {
                sendDebugNote("all the following products are removed: "
                    + rmvdProdBuff);
            }

            return removedProducts;
        }
    }

    /**
     * Removes (and returns) all products (SimProcesses) which are in compliance with the given condition from the
     * Entrepot, no matter if there are other processes waiting in the queue. Note, that as long as the lifeCycles of
     * the products (SimProcesses) leaving the Entrepot are not terminated they will automatically be activated after
     * the current SimProcess! If no products complying with the given condition are available at the moment
     * <code>null</code> will be returned. The products will be retrieved in a
     * kind of FIFO order, that means the product which arrived first in the Entrepot is the first to be removed. To
     * check if there are any products available which are in compliance with the given condition use the method
     * <code>getAvailComplyWith()</code>.
     *
     * @param condition desmoj.Condition : The condition the products to be removed must comply with.
     * @return desmoj.SimProcess[] : All the products which are in compliance with the given condition and are removed
     *     from the Entrepot. The lifeCycles of their SimProcesses will be activated as long as they are not terminated.
     *     Is <code>null</code> if no such products are available at the moment.
     */
    public SimProcess[] removeAllProducts(Condition condition) {

        where = "SimProcess[] removeAllProducts(Condition condition)";

        SimProcess currentProcess = currentSimProcess();

        // if the current process is not valid: just return
        if (!checkProcess(currentProcess, where)) {
            return null;
        }

        queue.insert(currentProcess); // insert every process in the queue for
        // statistic reasons

        // the number of available products complying with the condition at the
        // moment
        int n = getAvailComplyWith(condition);

        queue.remove(currentProcess); // get the process out of the queue

        if (n > 0) {
            // make a Stringbuffer to store all the products removed from the
            // Entrepot
            StringBuffer rmvdProdBuff = new StringBuffer();

            // make the array of removed products
            SimProcess[] removedProducts = new SimProcess[n];

            // the number of actually removed products
            int i = 0;

            // loop throught the whole Vector of products
            for (Enumeration e = products.elements(); e.hasMoreElements(); ) {
                SimProcess nextSP = (SimProcess) e.nextElement();

                // check if the next product is in compliance with the condition
                if (condition.check(nextSP)) {
                    // remove the next SimProcess (product) from the Vector
                    products.remove(nextSP);

                    // add the removed product (SimProcess) to the array to be
                    // returned
                    removedProducts[i] = nextSP;

                    // add the product to the Stringbuffer (needed for the debug
                    // note)
                    rmvdProdBuff.append(nextSP.getQuotedName() + " ");

                    // the sim-process (product) is not blocked (anymore)
                    nextSP.setBlocked(false);

                    // activate the removed SimProcess (if it is not terminated
                    // yet)
                    if (!nextSP.isTerminated()) {
                        nextSP.skipTraceNote();
                        nextSP.activateAfter(current());
                    }

                    // increment the number of actually removed products
                    i++;
                }
            } // end for loop

            updateStatistics(-i); // statistics will be updated
            // with a negative i for remove(), remember?!
            // trace output
            if (currentlySendTraceNotes()) {
                sendTraceNote("removes " + n + " products from "
                    + getQuotedName() + " which are in compliance with "
                    + condition.getQuotedName());
            } // tell in the trace how many products are removed from the
            // Entrepot

            // for debugging purposes
            if (currentlySendDebugNotes()) {
                sendDebugNote("the following products are removed: "
                    + rmvdProdBuff + " they are all in compliance with "
                    + condition.getQuotedName());
            }

            return removedProducts;
        } else // nothing to return
        {
            // trace output
            if (currentlySendTraceNotes()) {
                sendTraceNote("removes nothing from " + getQuotedName()
                    + " because there are no products in compliance with "
                    + condition.getQuotedName());
            } // tell in the trace that no products are removed from the
            // Entrepot

            // for debugging purposes
            if (currentlySendDebugNotes()) {
                sendDebugNote("no products in compliance with "
                    + condition.getQuotedName()
                    + " were found at the moment.");
            }

            return null;
        }
    }

    /**
     * Removes (and returns) one product (SimProcess) from the Entrepot. Note, that as long as the lifeCycle of the
     * product (SimProcess) leaving the Entrepot is not terminated it will automatically be activated after the current
     * SimProcess! If no products are available at the moment the requesting SimProcess will be enqueued in the wait
     * queue until a product becomes available. In case the capacity limit of the wait queue is reached the current
     * SimProcess will be rejected and not get any product (
     * <code>null</code> will be returned). The product will be retrieved in a
     * kind of FIFO order, that means the product which arrived first in the Entrepot is the first to be removed.
     *
     * @return desmoj.SimProcess : The product which is removed from the Entrepot. The lifeCycles of this SimProcess
     *     will be activated as long as he is not terminated. Is <code>null</code> if the capacity limit of the wait
     *     queue is reached and therefore the SimProcess is refused to be serviced or another failure has occured.
     */
    public SimProcess removeProduct() throws SuspendExecution {
        where = "SimProcess removeProduct()";

        SimProcess currentProcess = currentSimProcess();

        // if the current process is not valid: just return
        if (!checkProcess(currentProcess, where)) {
            return null;
        }

        // check if capac. limit of queue is reached
        if (queueLimit <= length()) {
            if (currentlySendDebugNotes()) {
                sendDebugNote("refuses to insert "
                    + currentProcess.getQuotedName()
                    + " in waiting-queue, because the capacity limit is reached. ");
            }

            if (currentlySendTraceNotes()) {
                sendTraceNote("is refused to be enqueued in "
                    + this.getQuotedName() + "because the capacity limit ("
                    + getQueueLimit() + ") of the " + "queue is reached");
            }

            refused++; // count the refused ones

            return null; // capacity limit is reached
        }

        // insert every process in the queue for statistic reasons
        queue.insert(currentProcess);

        // is it possible for this process to pass by?
        if (passBy == false) // no bypass possible
        {
            // no product is available OR another process is first in the queue
            if (isEmpty() || currentProcess != queue.first()) {
                if (currentlySendTraceNotes()) {
                    // tell in the trace what the process is waiting for
                    sendTraceNote("is waiting for a product of '"
                        + this.getName() + "'");
                }

                // for debugging purposes
                if (currentlySendDebugNotes()) {
                    sendDebugNote("can not remove a product for "
                        + currentProcess.getQuotedName() + "<br>"
                        + " because it is not his turn or no units are "
                        + " available right now.");
                }

                do { // the process is stuck in here
                    currentProcess.setBlocked(true); // as long as ...see
                    // while
                    currentProcess.skipTraceNote(); // don't tell the user, that
                    // we ...
                    currentProcess.passivate(); // passivate the current process
                }
                // while no product is available OR another process is first in
                // the queue
                while (isEmpty() || currentProcess != queue.first());

            } // end if
        } else // it is possible to pass by, passBy = true
        {
            // no product is available OR another process is first in the queue
            if (isEmpty() || currentProcess != queue.first()) {
                // if this process is not the first in the queue
                if (currentProcess != queue.first()) {
                    // we have to make sure that no other process in front of
                    // this current
                    // process in the wait queue could be satisfied, so activate
                    // the first Process in the queue to see what he can do. He
                    // will pass
                    // the activation on to his successors until this process
                    // will be
                    // activated again to get his products. (hopefully)
                    activateFirst();
                }

                // only if no product is available right now, the process has to
                // wait
                if (isEmpty()) {
                    // tell in the trace where the current process is waiting
                    // and what for
                    if (currentlySendTraceNotes()) {
                        sendTraceNote("is waiting for a product of '"
                            + this.getName() + "'");
                    }

                    // for debugging purposes
                    if (currentlySendDebugNotes()) {
                        sendDebugNote("can not remove a product for "
                            + currentProcess.getQuotedName() + "<br>"
                            + " because no units are available right now.");
                    }
                } // end if no product is available right now

                // block and passivate the process until enough products are
                // available
                do { // the process is stuck in here
                    currentProcess.setBlocked(true); // as long as ...see
                    // while
                    currentProcess.skipTraceNote(); // don't tell the user, that
                    // we ...
                    currentProcess.passivate(); // passivate the current process
                } while (isEmpty()); // no product is available

            } // end if
        } // end else (passBy = true)

        // the current process has got the product he wanted ...

        // we left the do while loop because we can get the product desired
        // remove the first SimProcess (product) from the Vector
        SimProcess rmvdProduct = products.remove(0);

        // the sim-process (product) is not blocked (anymore)
        rmvdProduct.setBlocked(false);

        // activate the removed SimProcess (if it is not terminated yet)
        if (!rmvdProduct.isTerminated()) {
            rmvdProduct.skipTraceNote();
            rmvdProduct.activateAfter(current());
        }

        queue.remove(currentProcess); // get the process out of the queue
        currentProcess.setBlocked(false); // we are not blocked (anymore),
        // yeah!

        activateFirst(); // give waiting successors a chance

        updateStatistics(-1); // statistics will be updated
        // with a negative n for remove(), remember?!
        // trace output
        if (currentlySendTraceNotes()) {
            sendTraceNote("removes a product from " + getQuotedName());
        } // tell in the trace how many products are removed from the Entrepot

        // for debugging purposes
        if (currentlySendDebugNotes()) {
            sendDebugNote("the following product is removed: "
                + rmvdProduct.getQuotedName());
        }

        return rmvdProduct;
    }

    /**
     * Removes a certain product (SimProcess) from the Entrepot, if the given product is stored in the Entrepot, no
     * matter if other processes are waiting in the queue already. Note that as long as the lifeCycle of the products
     * (SimProcesses) leaving the Entrepot is not terminated they will automatically be activated after the current
     * SimProcess! To use this method one must know which product(s) are in the Entrepot. To check which products are
     * contained use methods like <code>products()</code> or
     * <code>contains()</code>. The products will be retrieved in a kind of
     * FIFO order that means the product which arrived first in the Entrepot is the first to be removed. Users of this
     * method will not be enqueued in the waiting-queue, because no one can ensure that the requested SimProcess will
     * ever arrive in this Entrepot.
     *
     * @param product desmoj.SimProcess : The product to be removed from the Entrepot. Note that this SimProcess will be
     *                activated after the current SimProcess as long as its lifeCycle is not terminated.
     */
    public void removeProduct(SimProcess product) {

        where = "void removeProduct(SimProcess product)";

        SimProcess currentProcess = currentSimProcess();

        if (!checkProcess(currentProcess, where)) // if the current process
        {
            return;
        } // is not valid: just return

        // check the sim-process to be removed
        if (!checkProcess(product, where)) // if the sim-process to be removed
        {
            return;
        } // is not valid: just return

        // check if the given SimProcess is contained in the Entrepot
        if (!contains(product)) {
            sendWarning(
                "The product to be removed from the Entrepot is not "
                    + "contained in the Entrepot. The attempted action is ignored!",
                "Entrepot: " + getName() + " Method: " + where,
                "A product can only be removed from an Entrepot as it is contained "
                    + "in there.",
                "Make sure that every product to be removed is contained in the "
                    + "Entrepot. You can use methods like <code>contains()</code> or "
                    + "<code>products()</code> to check that.");
            return;
        }

        queue.insert(currentProcess); // insert every process in the queue for
        // statistic reasons
        queue.remove(currentProcess); // get the process out of the queue

        // remove the sim-process (product) from the Vector
        products.remove(product);

        // the sim-process (product) is not blocked (anymore)
        product.setBlocked(false);

        // activate the removed SimProcess (if it is not terminated yet)
        if (!product.isTerminated()) {
            product.skipTraceNote();
            product.activateAfter(current());
        }

        updateStatistics(-1);

        // trace output
        if (currentlySendTraceNotes()) {
            sendTraceNote("removes " + product.getQuotedName() + " from "
                + getQuotedName());
        } // tell in the trace which product is removed from the Entrepot

        // for debugging purposes
        if (currentlySendDebugNotes()) {
            sendDebugNote("the product " + product.getQuotedName()
                + " is removed.");
        }
    }

    /**
     * Removes (and returns) a certain number of products (SimProcesses) from the Entrepot. Note, that as long as the
     * lifeCycles of the products (SimProcesses) leaving the Entrepot are not terminated they will automatically be
     * activated after the current SimProcess! If not enough products are available at the moment the requesting
     * SimProcess will be enqueued in the wait queue until enough products become available. In case the capacity limit
     * of the wait queue is reached the current Sim-process will be rejected and not get any products (<code>null</code>
     * will be returned). The products will be retrieved in a kind of FIFO order, that means the product which arrived
     * first in the Entrepot is the first to be removed.
     *
     * @param n int : The number of products to be removed from the Entrepot.
     * @return desmoj.SimProcess[] : All the products which are removed from the Entrepot. The lifeCycles of their
     *     SimProcesses will be activated as long as they are not terminated. Is <code>null</code> if the capacity limit
     *     of the wait queue is reached and therefore the SimProcess is refused to be serviced or another failure has
     *     occured.
     */
    public SimProcess[] removeProducts(long n) throws SuspendExecution {

        where = "SimProcess[] removeProducts(long n)";

        SimProcess currentProcess = currentSimProcess();

        // if the current process is not valid: just return
        if (!checkProcess(currentProcess, where)) {
            return null;
        }

        if (n <= 0) // if nothing or less should be removed
        {
            sendWarning(
                "Attempt to remove nothing or a negative number of products"
                    + " from an Entrepot. The attempted action is ignored!",
                "Entrepot: " + getName() + " Method: " + where,
                "It does not make sense to remove nothing or less from an Entrepot. "
                    + "The statistic will be corrupted with negative numbers!",
                "Make sure to remove at least one product from the Entrepot.");
            return null; // go to where you came from; ignore that rubbish
        }

        if (queueLimit <= length()) // check if capac. limit of queue is reached
        {
            if (currentlySendDebugNotes()) {
                sendDebugNote("refuses to insert "
                    + currentProcess.getQuotedName()
                    + " in waiting-queue, because the capacity limit is reached. ");
            }

            if (currentlySendTraceNotes()) {
                sendTraceNote("is refused to be enqueued in "
                    + this.getQuotedName() + "because the capacity limit ("
                    + getQueueLimit() + ") of the " + "queue is reached");
            }

            refused++; // count the refused ones

            return null; // capacity limit is reached
        }

        // insert every process in the queue for statistic reasons
        queue.insert(currentProcess);

        // is it possible for this process to pass by?
        if (passBy == false) // no bypass possible
        {
            if (n > getAvail() || // not enough products available OR
                currentProcess != queue.first()) // other process is
            // first
            // in the q
            {
                // tell in the trace what the process is waiting for
                if (currentlySendTraceNotes()) {
                    sendTraceNote("is waiting for " + n + " products of "
                        + this.getQuotedName());
                }

                // for debugging purposes
                if (currentlySendDebugNotes()) {
                    sendDebugNote("can not remove " + n + " products for "
                        + currentProcess.getQuotedName() + "<br>"
                        + "because there are only " + getAvail()
                        + " units " + "right now.");
                }

                do { // the process is stuck in here
                    currentProcess.setBlocked(true); // as long as ...see
                    // while
                    currentProcess.skipTraceNote(); // don't tell the user, that
                    // we ...
                    currentProcess.passivate(); // passivate the current process
                } while (n > getAvail() || // not enough products available OR
                    currentProcess != queue.first()); // other process is
                // first

            } // end if
        } else // it is possible to pass by, passBy = true
        {
            if (n > getAvail() || // not enough products available OR
                currentProcess != queue.first()) // other process is
            // first
            // in the q
            {
                // if this process is not the first in the queue
                if (currentProcess != queue.first()) {
                    // we have to make sure that no other process in front of
                    // this current
                    // process in the wait queue could be satisfied, so activate
                    // the first Process in the queue to see what he can do. He
                    // will pass
                    // the activation on to his successors until this process
                    // will be
                    // activated again to get his products. (hopefully)
                    activateFirst();
                }

                // only if not enough products are available, the process has to
                // wait
                if (n > getAvail()) {
                    // tell in the trace where the current process is waiting
                    // and what for
                    if (currentlySendTraceNotes()) {
                        sendTraceNote("is waiting for " + n + " products of "
                            + this.getQuotedName());
                    }

                    // for debugging purposes
                    if (currentlySendDebugNotes()) {
                        sendDebugNote("can not remove " + n + " products for "
                            + currentProcess.getQuotedName() + "<br>"
                            + "because there are only " + getAvail()
                            + " units " + "right now.");
                    }
                } // end if not enough products are available

                // block and passivate the process until enough products are
                // available
                do { // the process is stuck in here
                    currentProcess.setBlocked(true); // as long as ...see
                    // while
                    currentProcess.skipTraceNote(); // don't tell the user, that
                    // we ...
                    currentProcess.passivate(); // passivate the current process
                } while (n > getAvail()); // not enough products available

            } // end if
        } // end else (passBy = true)

        // the current process has got the products he wanted ...

        // we left the do while loop because we can get the products desired
        // make a Stringbuffer to store all the products removed from the
        // Entrepot
        StringBuffer rmvdProdBuff = new StringBuffer();
        // make the array of removed products
        SimProcess[] removedProducts = new SimProcess[(int) n];
        // and fill it
        for (int i = 0; i < n; i++) {
            // remove the first SimProcess (product) from the Vector
            SimProcess rmvdProduct = products.remove(0);

            // add the removed product (SimProcess) to the array to be returned
            removedProducts[i] = rmvdProduct;

            // add the product to the Stringbuffer (needed for the debug note)
            rmvdProdBuff.append(rmvdProduct.getQuotedName());
            if (i < (n - 1)) {
                rmvdProdBuff.append(", ");
            }

            // the sim-process (product) is not blocked (anymore)
            rmvdProduct.setBlocked(false);

            // activate the removed SimProcess (if it is not terminated yet)
            if (!rmvdProduct.isTerminated()) {
                rmvdProduct.skipTraceNote();
                rmvdProduct.activateAfter(current());
            }
        } // end for loop

        queue.remove(currentProcess); // get the process out of the queue
        currentProcess.setBlocked(false); // we are not blocked (anymore),
        // yeah!

        activateFirst(); // give waiting successors a chance

        updateStatistics(-n); // statistics will be updated
        // with a negative n for remove(), remember?!
        // trace output
        if (currentlySendTraceNotes()) {
            sendTraceNote("removes " + n + " products from " + getQuotedName());
        } // tell in the trace how many products are removed from the Entrepot

        // for debugging purposes
        if (currentlySendDebugNotes()) {
            sendDebugNote("the following products are removed: " + rmvdProdBuff);
        }

        return removedProducts;
    }

    /**
     * Removes (and returns) a certain number of products (SimProcesses) from the Entrepot which are in compliance with
     * the given condition. Note, that as long as the lifeCycles of the products (SimProcesses) leaving the Entrepot are
     * not terminated they will automatically be activated after the current SimProcess! If not enough products are
     * available at the moment the requesting SimProcess will be enqueued in the waiting-queue until enough products
     * become available. In case the capacity limit of the wait queue is reached the current SimProcess will be rejected
     * and not get any products (<code>null</code> will be returned). The products will be retrieved in a kind of FIFO
     * order, that means the product which arrived first in the Entrepot is the first to be removed. To make sure there
     * are enough products available which are in compliance with the given condition use the method
     * <code>getAvailComplyWith()</code>.
     *
     * @param n         int : The number of products (which are in compliance with the given condition) to be removed
     *                  from the Entrepot.
     * @param condition desmoj.Condition : The condition the products to be removed must comply with.
     * @return desmoj.SimProcess[] : All the products which are in compliance with the given condition and are removed
     *     from the Entrepot. The lifeCycles of their SimProcesses will be activated as long as they are not terminated.
     *     Is <code>null</code> if the capacity limit of the wait queue is reached and therefore the sim-process is
     *     refused to be serviced or another failure has occured.
     */
    public SimProcess[] removeProducts(int n, Condition condition) throws SuspendExecution {

        where = "SimProcess[] removeProducts(int n, Condition condition)";

        SimProcess currentProcess = currentSimProcess();

        // if the current process is not valid: just return
        if (!checkProcess(currentProcess, where)) {
            return null;
        }

        if (n <= 0) // if nothing or less should be removed
        {
            sendWarning(
                "Attempt to remove nothing or a negative number of products"
                    + " from an Entrepot. The attempted action is ignored!",
                "Entrepot: " + getName() + " Method: " + where,
                "It does not make sense to remove nothing or less from an Entrepot. "
                    + "The statistic will be corrupted with negative numbers!",
                "Make sure to remove at least one product from the Entrepot.");
            return null; // go to where you came from; ignore that rubbish
        }

        if (queueLimit <= length()) // check if capac. limit of queue is reached
        {
            if (currentlySendDebugNotes()) {
                sendDebugNote("refuses to insert "
                    + currentProcess.getQuotedName()
                    + " in waiting-queue, because the capacity limit is reached. ");
            }

            if (currentlySendTraceNotes()) {
                sendTraceNote("is refused to be enqueued in "
                    + this.getQuotedName() + "because the capacity limit ("
                    + getQueueLimit() + ") of the " + "queue is reached");
            }

            refused++; // count the refused ones

            return null; // capacity limit is reached
        }

        queue.insert(currentProcess); // insert every process in the queue for
        // statistic reasons

        // is it possible for this process to pass by?
        if (passBy == false) // no bypass possible
        {
            // not enough products complying with the given condition are
            // available OR
            if (n > getAvailComplyWith(condition)
                || currentProcess != queue.first()) // other process is
            // first in the q
            {
                // tell in the trace what the process is waiting for
                if (currentlySendTraceNotes()) {
                    sendTraceNote("is waiting for " + n + " products of '"
                        + this.getName()
                        + "' which are in compliance with "
                        + condition.getQuotedName());
                }

                // for debugging purposes
                if (currentlySendDebugNotes()) {
                    sendDebugNote("can not remove " + n + " products for "
                        + currentProcess.getQuotedName() + "<br>"
                        + " which are in compliance with "
                        + condition.getQuotedName()
                        + " because there are only "
                        + getAvailComplyWith(condition)
                        + " units in compliance " + "with "
                        + condition.getQuotedName() + " right now.");
                }

                do { // the process is stuck in here
                    currentProcess.setBlocked(true); // as long as ...see
                    // while
                    currentProcess.skipTraceNote(); // don't tell the user, that
                    // we ...
                    currentProcess.passivate(); // passivate the current process
                } while (n > getAvailComplyWith(condition) || // not enough
                    // products
                    // available OR
                    currentProcess != queue.first()); // other process is
                // first

            } // end if
        } else // it is possible to pass by (passBy = true)
        {
            // not enough products complying with the given condition are
            // available OR
            if (n > getAvailComplyWith(condition)
                || currentProcess != queue.first()) // other process is
            // first in the q
            {
                // if this process is not the first in the queue
                if (currentProcess != queue.first()) {
                    // we have to make sure that no other process in front of
                    // this current
                    // process in the wait queue could be satisfied, so activate
                    // the first Process in the queue to see what he can do. He
                    // will pass
                    // the activation on to his successors until this process
                    // will be
                    // activated again to get his products. (hopefully)
                    activateFirst();
                }

                // only if not enough suitable products are available,
                // the process has to wait
                if (n > getAvailComplyWith(condition)) {
                    // tell in the trace what the process is waiting for
                    if (currentlySendTraceNotes()) {
                        sendTraceNote("is waiting for " + n + " products of '"
                            + this.getName()
                            + "' which are in compliance with"
                            + condition.getQuotedName());
                    }

                    // for debugging purposes
                    if (currentlySendDebugNotes()) {
                        sendDebugNote("can not remove " + n + " products for "
                            + currentProcess.getQuotedName() + "<br>"
                            + " which are in compliance with "
                            + condition.getQuotedName()
                            + " because there are only "
                            + getAvailComplyWith(condition)
                            + " units in compliance " + "with "
                            + condition.getQuotedName() + " right now.");
                    }
                } // end if not enough suitable products are available

                // block and passivate the process until enough suitable prods
                // are available
                do { // the process is stuck in here
                    currentProcess.setBlocked(true); // as long as ...see
                    // while
                    currentProcess.skipTraceNote(); // don't tell the user, that
                    // we ...
                    currentProcess.passivate(); // passivate the current process
                } while (n > getAvailComplyWith(condition)); // not enough
                // suitable prods
                // avail.

            } // end if
        } // end else (passBy = true)

        // the current process has got all the products he wanted ...

        // we left the do-while-loop because we can get the products desired
        // make a Stringbuffer to store all the products removed from the
        // Entrepot
        StringBuffer rmvdProdBuff = new StringBuffer();
        // make the array of removed products
        SimProcess[] removedProducts = new SimProcess[n];

        // the number of actually removed products
        int i = 0;

        // loop throught the whole Vector of products
        for (Enumeration e = products.elements(); e.hasMoreElements(); ) {
            SimProcess nextSP = (SimProcess) e.nextElement();

            // check if the next product is in compliance with the condition AND
            // we don't
            // have removed enough products yet
            if (condition.check(nextSP) && (i < n)) {
                // remove the next SimProcess (product) from the Vector
                products.remove(nextSP);

                // add the removed product (SimProcess) to the array to be
                // returned
                removedProducts[i] = nextSP;

                // add the product to the Stringbuffer (needed for the debug
                // note)
                rmvdProdBuff.append(nextSP.getQuotedName() + " ");

                // the sim-process (product) is not blocked (anymore)
                nextSP.setBlocked(false);

                // activate the removed SimProcess (if it is not terminated yet)
                if (!nextSP.isTerminated()) {
                    nextSP.skipTraceNote();
                    nextSP.activateAfter(current());
                }

                // increment the number of actually removed products
                i++;
            }
        } // end for loop

        queue.remove(currentProcess); // get the process out of the queue
        currentProcess.setBlocked(false); // we are not blocked (anymore),
        // yeah!

        // activate the new first process in the queue
        activateFirst(); // give waiting successors a chance

        updateStatistics(-i); // statistics will be updated
        // with a negative i for remove(), remember?!
        // trace output
        if (currentlySendTraceNotes()) {
            sendTraceNote("removes " + n + " products from " + getQuotedName()
                + " which are in compliance with "
                + condition.getQuotedName());
        } // tell in the trace how many products are removed from the Entrepot

        // for debugging purposes
        if (currentlySendDebugNotes()) {
            sendDebugNote("the following products are removed: " + rmvdProdBuff
                + " they are all in compliance with "
                + condition.getQuotedName());
        }

        return removedProducts;
    }

    /**
     * To reset the statistics of this Entrepot. The number of available products at this moment and the processes
     * waiting in the queue are not changed. But all statistic counters are reset. The
     * <code>QueueBased</code> is also reset.
     */
    public void reset() {
        super.reset(); // reset the QueueBased also

        maximum = getAvail();
        customers = 0;
        soldProducts = 0;
        wSumAvail = 0.0;
        lastUsage = presentTime();
        refused = 0;
    }

    /**
     * Stores a sim-process as a product in the Entrepot. As there is no capacity limit for the Entrepot, SimProcesses
     * can always be stored. The products will be kept in a Vector and retrieved in a kind of FIFO order (as long as no
     * other conditions for retrieval have to be met). As long as the Sim-process is kept in the Entrepot it is
     * passivated and blocked.
     *
     * @param product desmoj.SimProcess : The sim-process (product) to be stored in the Entrepot.
     */
    public void storeProduct(SimProcess product) throws SuspendExecution {

        where = "void storeProduct(SimProcess product)";

        SimProcess currentProcess = currentSimProcess();

        if (!checkProcess(currentProcess, where)) // if the current process
        {
            return;
        } // is not valid: just return

        // check the sim-process to be stored
        if (!checkProcess(product, where)) // if the sim-process to be stored
        {
            return;
        } // is not valid: just return

        // check if the given SimProcess is contained in the Entrepot already
        if (contains(product)) {
            sendWarning(
                "The product to be stored in the Entrepot is already "
                    + "contained in the Entrepot. The attempted action is ignored!",
                "Entrepot: " + getName() + " Method: " + where,
                "A product can not be stored in an Entrepot more than one time.",
                "Make sure that every product is stored in an Entrepot only once.");
            return;
        }

        // the sim-process to be stored in this Entrepot should be active or
        // passivated but not scheduled. Just in case it is scheduled, ignore
        // this
        // attempt to store the sim-process
        if (product.isScheduled()) {
            sendWarning(
                "The sim-process to be stored in an Entrepot is scheduled! "
                    + "The attempt to store the sim-process is ignored! ",
                "Entrepot: " + getName() + " Method: " + where,
                "A sim-process which is scheduled is currently busy with "
                    + "something and therefore should not be stored in an Entrepot.",
                "Make sure that the sim-process is either storing itself in "
                    + "an Entrepot or that the sim-process is passive.");
            return;
        }

        // make sure the sim-process to be stored is passive and blocked
        product.setBlocked(true); // the product process is blocked

        // put the sim-process (product) in the Vector
        products.add(product);

        // tell in the trace which product is stored in the Entrepot
        if (currentlySendTraceNotes()) {
            sendTraceNote("stores " + product.getQuotedName() + " in "
                + getQuotedName());
        }

        // for debugging purposes
        if (currentlySendDebugNotes()) {
            sendDebugNote("stores " + product.getQuotedName());
        }

        updateStatistics(1);

        // see if someone is in the queue waiting for products
        activateFirst();

        // either the sim-process is storing itself to the Entrepot
        // or it is passive (or terminated) already
        if (currentProcess == product) // adds itself to the Entrepot
        {
            product.skipTraceNote(); // don't tell the user, that we ...
            product.passivate(); // passivate the product process
        }

    }

    /**
     * Stores an array of SimProcesses as products in the Entrepot. Make sure that the current SimProcess is not in the
     * array of products to be stored. Because when the current SimProcess gets passivated the execution of this method
     * will stop! As there is no capacity limit for the Entrepot, Sim-processes can always be stored. The products will
     * be kept in a Vector and retrieved in a kind of FIFO order (as long as no other conditions for retrieval have to
     * be met). As long as the sim-processes are kept in the Entrepot they are passivated and blocked.
     *
     * @param finishedProds desmoj.SimProcess[] : The array of SimProcesses (products) to be stored in the Entrepot.
     */
    public void storeProducts(SimProcess[] finishedProds) throws SuspendExecution {

        where = "void storeProducts(SimProcess[] finishedProds)";

        // make a Stringbuffer to store all the products added to the Entrepot
        StringBuffer storedProducts = new StringBuffer();

        // counter for the products being stored
        int n = 0;

        SimProcess currentProcess = currentSimProcess();

        if (!checkProcess(currentProcess, where)) // if the current process
        {
            return;
        } // is not valid: just return

        // for every single SimProcess to be stored
        for (int i = 0; i < finishedProds.length; i++) {
            // flag if this product is okay to be stored
            boolean productIsOkay = checkProcess(finishedProds[i], where);

            // check if the sim-process to be stored is valid

            // check if the given SimProcess is contained in the Entrepot
            // already
            if (contains(finishedProds[i])) {
                sendWarning(
                    "The product "
                        + finishedProds[i].getQuotedName()
                        + " to be stored in the Entrepot is already contained "
                        + "in the Entrepot. The attempted action is ignored!",
                    "Entrepot: " + getName() + " Method: " + where,
                    "A product can not be stored in an Entrepot more than one time.",
                    "Make sure that every product is stored in an Entrepot only once.");

                productIsOkay = false;
            }

            // the sim-process to be stored in this Entrepot should be active or
            // passivated but not scheduled. Just in case it is scheduled,
            // ignore this
            // attempt to store the sim-process
            if (finishedProds[i].isScheduled()) {
                sendWarning(
                    "The sim-process to be stored in an Entrepot is scheduled! "
                        + "The attempt to store the sim-process is ignored! ",
                    "Entrepot: " + getName() + " Method: " + where,
                    "A sim-process which is scheduled is currently busy with "
                        + "something and therefore should not be stored in an Entrepot.",
                    "Make sure that the sim-process is either storing itself in "
                        + "an Entrepot or that the sim-process is passive.");

                productIsOkay = false;
            }

            // store the product if it is okay
            if (productIsOkay) {
                // make sure the sim-process to be stored is passive and blocked
                finishedProds[i].setBlocked(true); // the product process is
                // blocked

                // put the sim-process (product) in the Vector
                products.add(finishedProds[i]);

                // add the product to the Stringbuffer (needed for the trace
                // note)
                storedProducts.append(finishedProds[i].getQuotedName());
                if (i < (finishedProds.length - 1)) {
                    storedProducts.append(", ");
                }

                // update the number of stored products
                n++;

                // either the sim-process is storing itself to the Entrepot
                // or it is passive (or terminated) already
                if (currentProcess == finishedProds[i]) // adds itself to the
                // Entrepot
                {
                    finishedProds[i].skipTraceNote(); // don't tell the user,
                    // that we ...
                    finishedProds[i].passivate(); // passivate the product
                    // process
                }
            }
        }

        // tell in the trace which products are stored in the Entrepot
        if (currentlySendTraceNotes()) {
            sendTraceNote("stores: " + storedProducts + "in " + getQuotedName());
        }

        // for debugging purposes
        if (currentlySendDebugNotes()) {
            sendDebugNote("stores " + storedProducts);
        }

        updateStatistics(n);

        // see if someone is in the queue waiting for products
        activateFirst();
    }

    /**
     * Updates the statistics every time a product (SimProcess) is stored in the Entrepot or removed from the Entrepot.
     *
     * @param n long : The number of products stored in or removed from the Entrepot. Is positive when products are
     *          stored in the Entrepot and negative when products are removed from the Entrepot.
     */
    protected void updateStatistics(long n) {
        // get the current time
        TimeInstant now = presentTime();

        // update the weighted sum of available products
        wSumAvail = wSumAvail
            + (((getAvail() - n)) * (now.getTimeAsDouble() - lastUsage.getTimeAsDouble()));

        // update the last usage of this Entrepot
        lastUsage = now;

        if (n > 0) // a product is stored
        {
            if (getAvail() > maximum) {
                maximum = getAvail();
            }
        } else // a product is removed (sold)
        {
            soldProducts += Math.abs(n);
            customers++;
        }
    }
}