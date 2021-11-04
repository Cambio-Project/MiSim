package desmoj.extensions.applicationDomains.harbour;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.report.Reporter;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.QueueBased;
import desmoj.core.simulator.QueueList;
import desmoj.core.simulator.QueueListFifo;
import desmoj.core.simulator.QueueListLifo;
import desmoj.core.simulator.QueueListRandom;
import desmoj.core.simulator.SimProcess;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeOperations;


/**
 * Berth is the place where ships are berthing until they will be unloaded and/or loaded. <code>Ship</code> can come and
 * <code>take()</code> the needed length for his berthing. Each ship has to release the same length of the berth it once
 * has acquired by calling the <code>release()</code> method of the Berth. Berth is used to implement process
 * synchronization between ships, which are using berth. The berth has a limited ccertain length. A ship can take the
 * needed for him length to berth. After his unloading and/or loading the ship must release the taken length to make her
 * available to other ships. If a ship can not get the needed length (enough free place) for his berthing, it has to
 * wait in a queue until enough length ( free palce) are released by other ships. A ship can release its berthing length
 * anytime. After the berht has <code>release()</code> the used length the waiting queue is checked for ships waiting
 * for her. The first sort criteria of the queue is always highest priorities first, the second queueing discipline of
 * the underlying queue and the capacity limit can be determined by the user (default is FIFO and unlimited capacity).
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
public class Berth extends QueueBased {

    /**
     * the berth time of the ships.
     */
    private double SumBerthTime;

    /**
     * the usage (occupation) time of this berth time.
     */
    private double SumUsageTime;

    /**
     * The inner queue for the ships.
     */
    private final QueueList<Ship> queue;

    /**
     * The number of the users (ships) of this berth.
     */
    private long users = 0;

    /**
     * The number of the current users (ships) of this berth.
     */
    private long currentUsers = 0;

    /**
     * The number of the refused ships.
     */
    private long refused = 0;

    /**
     * The length the Berth.
     */
    private int length;

    /**
     * The avail length of this berth.
     */
    private int avail;

    /**
     * Indicates the method where something has gone wrong. Is passed as a parameter to the methods
     * <code>checkProcess()</code> and
     * <code>checkCondition</code>.
     */
    private String where;

    /**
     * The shipBerth is using to define the berth time of the ships.
     */
    private final HashMap<Ship, TimeInstant> shipBerth;

    /**
     * The shipBerth is using to define the usage (occupation) time of this the berth.
     */
    private final HashMap<Ship, List<Ship>> shipPredecessors;

    /**
     * The current users (ships) of this berth.
     */
    private List<Ship> currentShips;

    /**
     * Constructor for a Berth with a certain length. The queueing discipline and the capacity limit of the underlying
     * queue can be chosen.
     *
     * @param owner        Model : The model this Berth is associated to.
     * @param name         java.lang.String : The Berth's name
     * @param sortOrder    int : determines the sort order of the underlying queue implementation. Choose a constant
     *                     from <code>QueueBased</code> like <code>QueueBased.FIFO</code> or
     *                     <code>QueueBased.LIFO</code> or ...
     * @param qCapacity    int : The capacity of the queue, that is how many processes (ships) can be enqueued. Zero (0)
     *                     means unlimited capacity.
     * @param length       int : The length of the Berth. Must be positive and greater than 0.
     * @param showInReport boolean : Flag, if Berth should produce a report or not.
     * @param showInTrace  boolean : Flag for trace to produce trace messages.
     */
    public Berth(Model owner, String name, int sortOrder, int qCapacity,
                 int length, boolean showInReport, boolean showInTrace) {
        super(owner, name, showInReport, showInTrace); // construct QueueBased

        // check if a valid sortOrder is given
        // determine the queueing strategy
        switch (sortOrder) {
            case QueueBased.FIFO:
                queue = new QueueListFifo<Ship>();
                break;
            case QueueBased.LIFO:
                queue = new QueueListLifo<Ship>();
                break;
            case QueueBased.RANDOM:
                queue = new QueueListRandom<Ship>();
                break;
            default:
                sendWarning(
                    "The given sortOrder parameter " + sortOrder + " is not valid! "
                        + "A queue with Fifo sort order will be created.",
                    "Berth : "
                        + getName()
                        + " Constructor: Berth (Model owner, String name, int "
                        + "sortOrder, long qCapacity, int length,   boolean "
                        + "showInReport, boolean showInTrace)",
                    "A valid positive integer number must be provided to "
                        + "determine the sort order of the queue.",
                    "Make sure to provide a valid positive integer number "
                        + "by using the constants in the class QueueBased, like "
                        + "QueueBased.FIFO, QueueBased.LIFO or QueueBased.RANDOM.");
                queue = new QueueListFifo<Ship>();
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
                "Berth : "
                    + getName()
                    + " Constructor: Berth (Model owner, String name, int "
                    + "sortOrder, long qCapacity, int length,	boolean "
                    + "showInReport, boolean showInTrace)",
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

        this.shipBerth = new HashMap<Ship, TimeInstant>();
        this.shipPredecessors = new HashMap<Ship, List<Ship>>();

        if (length <= 0) {
            sendWarning(
                "Attempt to construct a Berth with nothing or a negative"
                    + " length.",
                "Berth: "
                    + getName()
                    + " Constructor: Berth (Model owner, String name, int sortOrder, "
                    + "long qCapacity, int length, boolean showInReport, "
                    + "boolean showInTrace)",
                "A negative length of berth does not make sense here.",
                "Make sure to initialize the length of a Berth always with"
                    + " a positive length.");
            return;
        }

        this.length = length;
        this.avail = length;
        this.SumUsageTime = 0.0;
        this.SumBerthTime = 0.0;
        this.currentShips = new ArrayList<Ship>();
    }

    /**
     * Constructor for a Berth with a certain length. The underlying queue has a Fifo queueing discipline and unlimited
     * capacity.
     *
     * @param owner        Model : The model this Berth is associated to.
     * @param name         java.lang.String : The Berth's name
     * @param length       int : The lenght of this Berth Must be positive and greater than 0.
     * @param showInReport boolean : Flag, if Berth should produce a report or not.
     * @param showInTrace  boolean : Flag for trace to produce trace messages.
     */
    public Berth(Model owner, String name, int length, boolean showInReport,
                 boolean showInTrace) {
        super(owner, name, showInReport, showInTrace);

        // make an actual queue and give it a reference of this
        // "QueueBased"-Berth
        queue = new QueueListFifo<Ship>();
        queue.setQueueBased(this);

        this.shipBerth = new HashMap<Ship, TimeInstant>();
        this.shipPredecessors = new HashMap<Ship, List<Ship>>();
        this.currentShips = new ArrayList<Ship>();

        if (length <= 0) {
            sendWarning(
                "Attempt to construct a Berth with nothing or a negative"
                    + " length.",
                "Berth: "
                    + getName()
                    + " Constructor: Berth (Model owner, String name, int sortOrder, "
                    + "long qCapacity, int length, boolean showInReport, "
                    + "boolean showInTrace)",
                "A negative length of berth does not make sense here.",
                "Make sure to initialize the length of a Berth always with"
                    + " a positive length.");
            return;
        }

        this.length = length;
        this.avail = length;
        this.SumBerthTime = 0.0;
        this.SumUsageTime = 0.0;

    }

    /**
     * Returns a Reporter to produce a report about this Berth.
     *
     * @return desmoj.report.Reporter : The Reporter for the queue inside this Berth.
     */
    public Reporter createDefaultReporter() {

        return new desmoj.extensions.applicationDomains.harbour.report.BerthReporter(
            this);
    }

    /**
     * Changes the length of the Berth. Sets the length of the berth that is max. available to l. l must be positive.
     * This is only allowed as long as the Berth has not been used or the Berth has just been reset.
     *
     * @param l int : The new length of the Berth. Must be positive.
     */
    public void changeLength(int l) {

        if (users != 0) // if Berth is already used
        {
            sendWarning(
                "Attempt to change the length of a Berth already"
                    + " in use. The length will remain unchanged!",
                "Berth: " + this.getName()
                    + " Method: void changeLength (int l)",
                "The length of a Berth which has already be used can not"
                    + " be changed afterwards.",
                "Do not try to change the length of a Bearth which might have been"
                    + " used already. Or reset the Berth before changing its length.");

            return; // without changing the length
        }

        if (l <= 0) {
            sendWarning(
                "Attempt to construct a Berth with nothing or a negative"
                    + " length.",
                "Berth: "
                    + getName()
                    + " Constructor: Berth (Model owner, String name, int sortOrder, "
                    + "long qCapacity, int length, boolean showInReport, "
                    + "boolean showInTrace)",
                "A negative length of berth does not make sense here.",
                "Make sure to initialize the length of a Berth always with"
                    + " a positive length.");
            return;
        }
        this.length = l;
    }

    /**
     * Returns the length of the Berth.
     *
     * @return int : The length of the berth
     */
    public int getLength() {

        return length;
    }

    /**
     * Returns the average berthing time of the ships that have used this Berth.
     *
     * @return double : The average berthing time of the ships of this berth.
     */
    public double avgServTime() {

        if (this.users == 0) {
            return 0.0;
        } else {
            return this.SumBerthTime / getUsers();
        }
    }

    /**
     * Returns the average usage of the Berth. That means: in average, which percentage of the time it was in use over
     * the time?
     *
     * @return double : the average usage of the Berth
     */
    public double avgUsage() {

        TimeInstant now = presentTime(); // what is the time?

        // how long since the last reset
        double diff = now.getTimeAsDouble() - resetAt().getTimeAsDouble();

        if (diff < TimeOperations.getEpsilonSpan().getTimeAsDouble()) // diff is not long enough
        {
            sendWarning("A division by zero error occured.", "Berth: "
                    + this.getName() + " Method: double avgUsage ()",
                "The time difference between the last reset and now is shorter than "
                    + "epsilon (the shortest measurable time step).",
                "Do not reset any model component shortly before the simulation is "
                    + "over or will be stopped.");

            return UNDEFINED; // see QueueBased: UNDEFINED = -1
        }

        // Sum usage time
        double SumUT;

        // if one or more ships in the berth
        if (this.currentUsers >= 1) {
            // get first arrival ship from the current ship-users
            SimProcess s = currentShips.get(0);
            // get its arrival time
            TimeInstant t = shipBerth.get(s);

            SumUT = SumUsageTime + (now.getTimeAsDouble() - t.getTimeAsDouble());

        } else {
            SumUT = SumUsageTime;
        }

        double result = SumUT / now.getTimeAsDouble();
        return result;
    }

    /**
     * Provides the length to ships to use it for its berthing. As not enough length is available at the moment the Ship
     * has to wait in a queue until enough length is available again.
     *
     * @return boolean : Is <code>true</code> if the needed length has been provided successfully, <code>false</code>
     *     otherwise (i.e. capacity limit of the queue is reached).
     */
    public boolean take() throws SuspendExecution {

        where = "public boolean take()";

        SimProcess current = currentSimProcess();
        Ship ship;

        // check if it is a Ship
        if (current instanceof Ship) {
            ship = (Ship) current; // cast it to the right type
        } else {
            sendWarning("The sim-process using a Berth is not a "
                    + "Ship. The attempted action is ignored!", getClass()
                    .getName()
                    + ": " + getQuotedName() + ", Method: " + where,
                "A Berth is designed to give Ships the needed  length "
                    + "for  berthing.",
                "Make sure that only Ships are trying to take "
                    + "that berth.");

            return false; // ignore that rubbish
        }

        if (!checkProcess(ship, where)) // check the current process
        {
            return false;
        }

        if (ship.getBerthLength() > length) {
            sendWarning(
                "Attempt from a Berth to take more length than it has "
                    + "The attempted action is ignored!",
                "Berth: " + getName() + " Method: take ()",
                "It doesn't make sense to take more length than a Berth is large",
                "Make sure to take a right berth for this ship ");

            return false; // ignore that rubbish
        }

        if (queueLimit <= length()) // check if capac. limit of queue is reached
        {
            if (currentlySendDebugNotes()) {
                sendDebugNote("refuses to insert "
                    + ship.getQuotedName()
                    + " in waiting-queue, because the capacity limit is reached. ");
            }

            if (currentlySendTraceNotes()) {
                sendTraceNote("is refused to be enqueued in "
                    + this.getQuotedName() + "because the capacity limit ("
                    + getQueueLimit() + ") of the " + "queue is reached");
            }

            refused++; // count the refused ones

            return false; // capacity limit is reached
        }

        queue.insert(ship);

        if (ship.getBerthLength() > avail || // not enough length available
            // OR
            ship != queue.first()) // other process is first in
        // the q
        {
            // tell in the trace what the process is waiting for
            if (currentlySendTraceNotes()) {
                sendTraceNote("awaits " + ship.getBerthLength() + " of ' "
                    + this.getName() + " '");
            }

            // tell in the debug output what the process is waiting for
            if (currentlySendDebugNotes()) {
                sendDebugNote("has not enough length left to take "
                    + ship.getBerthLength() + " length to '"
                    + ship.getName() + "'");
            }
            // the process is caught in this do-while loop as long as ...see
            // while
            do {
                ship.setBlocked(true); // block the process
                ship.skipTraceNote(); // don't tell the user, that we
                // ...
                ship.passivate(); // passivate the current process
            } while (ship.getBerthLength() > avail || // not enough length
                // available OR
                ship != queue.first()); // other process is
            // first

        }

        queue.remove(ship); // get the process out of the queue
        ship.setBlocked(false);

        // give the new first process in the queue a chance
        activateFirst();

        updateStatistics(-ship.getBerthLength());

        if (currentlySendDebugNotes()) {

            sendDebugNote("gives to Ship " + ship.getName() + " "
                + ship.getBerthLength());
        }
        if (currentlySendTraceNotes()) {
            sendTraceNote("takes " + ship.getBerthLength() + " from "
                + this.getQuotedName());
        } // tell in the trace what length the process is taking from the
        // Berth

        return true;

    }

    /**
     * Updates the statistics for the Berth whenever berth are
     * <code>taken</code> or <code>"released"</code>.
     *
     * @param l int : Is positive when the Berth <code>release()</code> some length and negative when the Berth
     *          <code>take()</code> some length.
     */
    protected void updateStatistics(int l) {
        // get the current process
        SimProcess currentProcess = currentSimProcess();

        // get the current time
        TimeInstant now = presentTime();

        if (l < 0) // a ship is taking this berth
        {
            if (this.currentUsers >= 1) // if there're other users
            {
                // store all my predecessors
                ArrayList<Ship> myPred = new ArrayList<Ship>(currentShips);
                shipPredecessors.put((Ship) currentProcess, myPred);

            } // end if current users >=1

            this.currentUsers++;
            shipBerth.put((Ship) currentProcess, now); // store the time the ship is taking
            // some length

            // store this ship as current user
            currentShips.add((Ship) currentProcess);

        } else // the ship leaves this berth
        {
            // my predecessors
            List<Ship> pred = shipPredecessors.get(currentProcess);

            boolean isPred = false;
            // check if me is predecessor of other ships
            for (int i = 0; i < currentShips.size(); i++) {
                SimProcess s = currentShips.get(i);
                List<Ship> s_pred = shipPredecessors.get(s);
                if (s_pred != null) {
                    for (int j = 0; j < s_pred.size(); j++) {
                        if (s_pred.contains(s)) {
                            isPred = true;
                            break;

                        }
                    } // end of j
                } // end of i
            }
            if ((pred == null) && (!isPred)) // no predecessors && me isn't
            // predecessor of other ships
            {

                // get the arrival time (when this berth was taking)
                TimeInstant time = shipBerth.get(currentProcess);

                if (time != null)
                // add the berthing time of the ship to sum
                {
                    this.currentUsers--;
                    SumBerthTime = SumBerthTime
                        + (now.getTimeAsDouble() - time.getTimeAsDouble());
                    SumUsageTime = SumUsageTime
                        + (now.getTimeAsDouble() - time.getTimeAsDouble());

                    shipBerth.remove(currentProcess);

                    currentShips.remove(currentProcess);
                } else // when the ship tries to release the berth without it
                {
                    sendWarning(
                        "A ship attempts to release the berth without have taken"
                            + "that berth!" + currentProcess.getName()
                            + "' is releasing at the " + "moment" + l
                            + " to berth. <br>"
                            + "The attempted action is ignored!",
                        "Berth: " + this.getName()
                            + " Method: void updateStatistics (int l)",
                        "A ship can not release the berth without taking it before.",
                        "Make sure not to release a berth without taking it before.");

                    return;
                }
            } else // if there're pred.
            {
                if (pred != null) {
                    boolean allLeft = true;
                    // check if all my pred. already left this berth
                    for (int j = 0; j < pred.size(); j++) {
                        SimProcess ship = pred.get(j);

                        if (currentShips.contains(ship)) {
                            allLeft = false;
                            break;
                        }
                    } // end of j
                    if (allLeft) // if all my pred. left this berth
                    {

                        // get my first pred.
                        SimProcess firstPred = pred.get(0);
                        TimeInstant time1 = shipBerth.get(firstPred);
                        SumUsageTime = SumUsageTime
                            + (now.getTimeAsDouble() - time1.getTimeAsDouble());

                        // check the arrival time
                        TimeInstant arrivalTime = shipBerth.get(currentProcess);

                        SumBerthTime = SumBerthTime
                            + (now.getTimeAsDouble() - arrivalTime
                            .getTimeAsDouble());
                        this.currentUsers--;
                        shipBerth.remove(currentProcess);
                        shipPredecessors.remove(currentProcess);
                        currentShips.remove(currentProcess);

                    } // end of allLeft
                } // end of if there're pred.

            }
        }
        // change the avail length
        avail += l; // l can be positive or negative (remember ?!)

    }

    /**
     * Checks whether the process (ship) using the Berth is a valid process.
     *
     * @param p SimProcess : Is this SimProcess a valid one?
     * @return boolean : Returns whether the sim-process is valid or not.
     */
    protected boolean checkProcess(SimProcess p, String where) {
        if (p == null) // if p is a null pointer instead of a process
        {
            sendWarning("A non existing process is trying to use a Berth "
                    + "object. The attempted action is ignored!", "Berth: "
                    + getName() + " Method: " + where,
                "The process is only a null pointer.",
                "Make sure that only real SimProcesses are using Berth's.");
            return false;
        }

        if (!isModelCompatible(p)) // if p is not modelcompatible
        {
            sendWarning(
                "The process trying to use a Berth object does"
                    + " not belong to this model. The attempted action is ignored!",
                "Berth: " + getName() + " Method: " + where,
                "The process is not modelcompatible.",
                "Make sure that processes are using only Berth's within"
                    + " their model.");
            return false;
        }
        return true;
    }

    /**
     * Activates the first process (ship) waiting in the queue. That is a process which was trying to take the needed
     * length of the berth, but there was not enough left in the berth. Or another ship was first in the queue to be
     * served. This method is called every time a process returns length to berth or when a ship in the waiting-queue is
     * satisfied.
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
     * A Ship is using this method to release this berth and to put length it has used back to the Berth.
     */
    public boolean release() {

        where = "public boolean release()";

        // get the current process-ship
        SimProcess currentProcess = currentSimProcess();
        Ship ship;

        // check if it is a Ship
        if (currentProcess instanceof Ship) {
            ship = (Ship) currentProcess; // cast it to the right type
        } else {
            sendWarning("The sim-process using a Berth is not a "
                    + "Ship. The attempted action is ignored!", getClass()
                    .getName()
                    + ": " + getQuotedName() + ", Method: " + where,
                "A Berth is designed to give Ships the chance release  the length "
                    + "back.",
                "Make sure that only Ships are trying to release "
                    + "that berth.");

            return false; // ignore that rubbish
        }

        if (!checkProcess(currentProcess, where)) // check the current process
        {
            return false;
        } // if it is not valid just return

        if (ship.getBerthLength() <= 0) // if the process is releasing nothing
        {
            sendWarning("The  returned length  is negative or zero! "
                    + "The attempted action is ignored!", "Berth: "
                    + this.getName() + " Method: void release (int l)",
                "It makes no sense to take back nothing or a negative length of "
                    + "berth.", " ");

            return false; // go to where you came from
        }

        updateStatistics(ship.getBerthLength());
        users++;

        if (currentlySendDebugNotes()) {

            sendDebugNote("takes back from the Ship "
                + currentProcess.getName() + " " + ship.getBerthLength());
        }

        // tell in the trace what the process is releases the Berth
        if (currentlySendTraceNotes()) {
            sendTraceNote("releases " + ship.getBerthLength() + " to "
                + this.getQuotedName());
        }

        activateFirst();

        return true;
    }

    /**
     * Resets the statistics of this Berth. The number of available length at this moment and the ships waiting in the
     * queue are not changed. But all statistic counters are reset. The parent <code>QueueBased</code> is also reset.
     */
    public void reset() {
        super.reset(); // reset the QueueBased also

        users = 0;
        currentUsers = 0;
        SumBerthTime = 0.0;
        SumUsageTime = 0;
        refused = 0;

    }

    /**
     * Returns the available length of the berth at the moment.
     *
     * @return int : The length of the berth available at the moment.
     */
    public int getAvailLength() {

        return avail;
    }

    /**
     * Returns the number of users (ships).
     *
     * @return long : The number of Users. That are ships have acquired and released the needed length of the berth.
     */
    public long getUsers() {
        return this.users;
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
     * Returns the number of ships refused to be enqueued in the queue, because the capacity limit is reached.
     *
     * @return long : The number of ships refused to be enqueued in the queue.
     */
    public long getRefused() {

        return refused; // that's it
    }
}