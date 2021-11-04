package desmoj.extensions.applicationDomains.harbour;

import java.util.Vector;

import desmoj.core.report.Reporter;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.ProcessQueue;
import desmoj.core.simulator.Queue;
import desmoj.core.simulator.QueueBased;
import desmoj.core.simulator.SimProcess;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.statistic.StatisticObject;

/**
 * A TransporterSystem represents the system of the transporters that manages the queues of the transporter and jobs,
 * give the statistics about the transporters and both queues.
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
public class TransporterSystem extends QueueBased {

    /**
     * The queue, actually storing the transporter processes waiting for jobs to do them.
     */
    protected ProcessQueue transporterQueue;

    /**
     * The queue, actually storing the jobs entities waiting to be assigned to the transporters.
     */
    protected Queue jobsQueue;

    /**
     * Counter for the whole number of the empty drives of all the transporters of this TransporterSystems.
     */
    protected long SumNumEmptyDrives;

    /**
     * Counter for the whole number of the loaded drives of all the transporters of this TransporterSystems.
     */
    protected long SumNumLoadedDrives;

    /**
     * Counter for the whole time of the empty drives of all the transporters of this TransporterSystems.
     */
    protected double SumTimeEmptyDrives;

    /**
     * Counter for the whole time of the loaded drives of all the transporters of this TransporterSystems.
     */
    protected double SumTimeLoadedDrives;

    /**
     * Counter for the whole distance of the empty drives of all the transporters of this TransporterSystems.
     */
    protected double SumDistanceEmptyDrives = 0.0;

    /**
     * Counter for the whole distance of the loaded drives of all the transporters of this TransporterSystems.
     */
    protected double SumDistanceLoadDrives = 0.0;

    /**
     * Indicates the method where something has gone wrong. Is passed as a parameter to the methods
     * <code>checkProcess()</code> and
     * <code>checkCondition</code>.
     */
    protected String where;

    /**
     * Constructor for a TransporterSystem. The queueing discipline and the capacity limit of the underlying queues for
     * the jobs and for the transporters can be chosen.
     *
     * @param owner        Model : The model this TransporterSystem is associated to.
     * @param name         java.lang.String : The TransporterSystem's name
     * @param tsortOrder   int : determines the sort order of the underlying transporter queue implementation. Choose a
     *                     constant from
     *                     <code>QueueBased</code> like <code>QueueBased.FIFO</code>
     *                     or <code>QueueBased.LIFO</code> or ...
     * @param tQCapacity   int : The capacity of the transporter queue, that is how many transporters can be enqueued.
     *                     Zero (0) means unlimited capacity.
     * @param jsortOrder   int : determines the sort order of the underlying jobs queue implementation. Choose a
     *                     constant from <code>QueueBased</code> like <code>QueueBased.FIFO</code> or
     *                     <code>QueueBased.LIFO</code> or ...
     * @param jQCapacity   int : The capacity of the jobs queue, that is how many transporters can be enqueued. Zero (0)
     *                     means unlimited capacity.
     * @param showInReport boolean : Flag, if this TransporterSystem should produce a report or not.
     * @param showInTrace  boolean : Flag for trace to produce trace messages.
     */
    public TransporterSystem(Model owner, String name, int tSortOrder,
                             int tQCapacity, int jSortOrder, int jQCapacity,
                             boolean showInReport, boolean showInTrace) {

        // construct QueueBased
        super(owner, name, showInReport, showInTrace);

        // TRANSPORTER queue

        // the sort order of the transporter queue
        int transporterQSortOrder = jSortOrder;

        // check if a valid sortOrder is given for the transporter queue
        if (tSortOrder < 0 || tSortOrder >= 3) {
            sendWarning(
                "The given tSortOrder parameter is negative! "
                    + "A transporter queue with Fifo sort order will be created "
                    + "instead.",
                " Constructor of " + getClass().getName() + " : "
                    + getQuotedName() + ".",
                "A valid positive integer number must be provided to "
                    + "determine the sort order of the underlying queue.",
                "Make sure to provide a valid positive integer number "
                    + "by using the constants in the class QueueBased, like "
                    + "QueueBased.FIFO or QueueBased.LIFO.");
            // make a Fifo queue
            transporterQSortOrder = QueueBased.FIFO; // better than nothing

        }

        // set the capacity of the transporter queues
        int transporterQLimit = tQCapacity;

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
            transporterQLimit = Integer.MAX_VALUE;
        }

        // create the queue for the transporters
        transporterQueue = new ProcessQueue(owner, name + "_T",
            transporterQSortOrder, transporterQLimit, false, false);

        // JOBS queue

        // the sort order of the jobs queue
        int jobsQSortOrder = jSortOrder;

        // check if a valid sortOrder is given for the slave queues
        if (jSortOrder < 0 || jSortOrder >= 3) {
            sendWarning(
                "The given jSortOrder parameter is negative or too big! "
                    + "Jobs queue with Fifo sort order will be created "
                    + "instead.",
                " Constructor of " + getClass().getName() + " : "
                    + getQuotedName() + ".",
                "A valid positive integer number must be provided to "
                    + "determine the sort order of the underlying queues.",
                "Make sure to provide a valid positive integer number "
                    + "by using the constants in the class QueueBased, like "
                    + "QueueBased.FIFO or QueueBased.LIFO.");

            jobsQSortOrder = QueueBased.FIFO;
        }

        // set the capacity of the jobs queues
        int jobsQLimit = jQCapacity;

        // check if the capacity does make sense
        if (jQCapacity < 0) {
            sendWarning("The given capacity of the jobs queue is negative! "
                    + "Jobs queue with unlimited capacity will be created "
                    + "instead.", " Constructor of " + getClass().getName()
                    + " : " + getQuotedName() + ".",
                "A negative capacity for a queue does not make sense.",
                "Make sure to provide a valid positive capacity "
                    + "for the underlying jobs queue.");
            // set the capacity to the maximum value
            jobsQLimit = Integer.MAX_VALUE;
        }

        // create the queue for the jobs
        jobsQueue = new Queue(owner, name + "_J", jobsQSortOrder, jobsQLimit,
            false, false);

        // reset the transporter system
        reset();

    }

    /**
     * Returns a Reporter to produce a report about this TransporterSystem.
     *
     * @return desmoj.report.Reporter : The Reporter for the queues inside this TransporterSystem.
     */
    public Reporter createDefaultReporter() {

        return new desmoj.extensions.applicationDomains.harbour.report.TSReporter(
            this);
    }

    /**
     * This method is used to add the job to the jobs queue of this TransporterSystem.
     *
     * @param t <code>Job</code>: The Job that me be enqueued.
     */
    public boolean addJob(Job j) {

        where = " boolean addJob (Job j )";

        // get the process that wants to add a job
        SimProcess currentProcess = currentSimProcess();

        if (!checkProcess(currentProcess, where)) // check the current process
        {
            return false;
        }

        if (jobsQueue.getQueueLimit() <= jobsQueue.length()) // check if
        // capac.
        // limit of queue
        // is reached
        {
            if (currentlySendDebugNotes()) {
                sendDebugNote("refuses to insert job generated of "
                    + currentProcess.getQuotedName()
                    + " in jobs queue, because the capacity limit is reached. ");
            }

            if (currentlySendTraceNotes()) {
                sendTraceNote("is refused to be enqueued in "
                    + this.getQuotedName() + "because the capacity limit ("
                    + jobsQueue.getQueueLimit() + ") of the "
                    + "queue is reached");
            }

        }

        return this.jobsQueue.insert(j); // insert the job in the jobs queue

    }

    /**
     * This method is used to add an internal transporter to the transporter queue of this TransporterSystem.
     *
     * @param t <code>Job</code>: The InternalTransporter that must be
     *          enqueued.
     */
    public boolean addTransporter(InternalTransporter t) {

        where = " boolean addTransporter (InternalTransporter t )";

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
     * Checks whether the process using the TransporterSystem is a valid process.
     *
     * @param p SimProcess : Is this SimProcess a valid one?
     * @return boolean : Returns whether the sim-process is valid or not.
     */
    protected boolean checkProcess(SimProcess p, String where) {
        if (p == null) // if p is a null pointer instead of a process
        {
            sendWarning(
                "A non existing process is trying to use a TransporterSystem "
                    + "object. The attempted action is ignored!",
                "TransporterSystem: " + getName() + " Method: " + where,
                "The process is only a null pointer.",
                "Make sure that only real SimProcesses are using TransporterSystem.");
            return false;
        }

        if (!isModelCompatible(p)) // if p is not modelcompatible
        {
            sendWarning(
                "The process trying to use a TransporterSystem object does"
                    + " not belong to this model. The attempted action is ignored!",
                "TransporterSystem: " + getName() + " Method: " + where,
                "The process is not modelcompatible.",
                "Make sure that processes are using only TransporterSystem within"
                    + " their model.");
            return false;
        }
        return true;
    }

    /**
     * Resets all statistical counters to their default values. Both, transporter queue and job queue are reset. The
     * mininum and maximum length of the queues are set to the current number of queued objects.
     */
    public void reset() {
        super.reset(); // reset Queue Based

        transporterQueue.reset(); // reset of the transporter queue

        jobsQueue.reset(); // reset of the jobs queue

        // reset all other transporter statistic values
        SumNumEmptyDrives = 0;

        SumNumLoadedDrives = 0;

        SumTimeEmptyDrives = 0.0;

        SumTimeLoadedDrives = 0.0;

        this.SumDistanceEmptyDrives = 0.0;

        this.SumDistanceLoadDrives = 0.0;
    }

    /**
     * Returns the number of the empty drives of all the transporters.
     *
     * @return long : The number of the empty drives.
     */
    public long getSumNumEmptyDrives() {

        return this.SumNumEmptyDrives;
    }

    /**
     * Change the number of the empty drives of all the transporters to a new value.
     *
     * @param n long : The number of empty drives of this TransporterSystem that must be added to the old value.
     */
    public void addSumNumEmptyDrives(long n) {

        this.SumNumEmptyDrives = this.SumNumEmptyDrives + n;
    }

    /**
     * Change the whole time of the empty drives of all the transporters to a new value.
     *
     * @param t double : The time of empty drives of this TransporterSystem that must be added to the old value.
     */
    public void addSumTimeEmptyDrives(double t) {

        this.SumTimeEmptyDrives = this.SumTimeEmptyDrives + t;
    }

    /**
     * Returns the whole time of the empty drives of all the transporters.
     *
     * @return double : The whole time of the empty drives.
     */
    public double getSumTimeEmptyDrives() {

        return StatisticObject.round(this.SumTimeEmptyDrives);
    }

    /**
     * Change the whole distance of the empty drives of all the transporters to a new value.
     *
     * @param d double : The whole distance of the empty drives of this TransporterSystem that must be added to the old
     *          value.
     */
    public void addSumDistanceEmptyDrives(double d) {

        this.SumDistanceEmptyDrives = this.SumDistanceEmptyDrives + d;
    }

    /**
     * Returns the whole distance of the empty drives of all the transporters.
     *
     * @return double : The whole distance of the empty drives.
     */
    public double getSumDistanceEmptyDrives() {

        return StatisticObject.round(this.SumDistanceEmptyDrives);
    }

    /**
     * Change the whole distance of the loaded drives of all the transporters to a new value.
     *
     * @param d double : The whole distance of the loaded drives of this TransporterSystem that must be added to the old
     *          value.
     */
    public void addSumDistanceLoadedDrives(double d) {

        this.SumDistanceLoadDrives = this.SumDistanceLoadDrives + d;
    }

    /**
     * Returns the whole distance of the loaded drives of all the transporters.
     *
     * @return double : The whole distance of the loaded drives.
     */
    public double getSumDistanceLoadedDrives() {

        return StatisticObject.round(this.SumDistanceLoadDrives);
    }

    /**
     * Returns the number of the loaded drives of all the transporters.
     *
     * @return long : The number of the loaded drives.
     */
    public long getSumNumLoadedDrives() {

        return this.SumNumLoadedDrives;
    }

    /**
     * Change the number of the loaded drives of all the transporters to a new value.
     *
     * @param n long : The number of loaded drives of this TransporterSystem that must be added to the old value.
     */
    public void addSumNumLoadedDrives(long n) {

        this.SumNumLoadedDrives = this.SumNumLoadedDrives + n;
    }

    /**
     * Change the whole time of the loaded drives of all the transporters to a new value.
     *
     * @param t double : The whole time of loaded drives of this TransporterSystem that must be added to the old value.
     */
    public void addSumTimeLoadedDrives(double t) {

        this.SumTimeLoadedDrives = this.SumTimeLoadedDrives + t;
    }

    /**
     * Returns the whole time of the loaded drives of all the transporters.
     *
     * @return double : The whole time of the loaded drives.
     */
    public double getSumTimeLoadedDrives() {

        return StatisticObject.round(this.SumTimeLoadedDrives);
    }

    /**
     * Returns the average utilization of a transporter of this transporter system.
     *
     * @return double : The average utilisation.
     */
    public double avgUsage() {

        // get the current time
        TimeInstant now = presentTime();

        // get avg. utilization
        double result = (this.getSumTimeLoadedDrives() + this
            .getSumTimeLoadedDrives())
            / (this.transporterQueue.getQueueLimit() * now.getTimeAsDouble());
        return result;
    }

    /**
     * Returns the transporter queue of this transporter system.
     *
     * @return <code>ProcessQueue</code>: The transporter queue.
     */
    public ProcessQueue getTransporter() {

        return this.transporterQueue;
    }

    /**
     * Returns the jobs queue of this transporter system.
     *
     * @return <code>Queue</code>: The jobs queue.
     */
    public Queue getJobs() {

        return this.jobsQueue;
    }

    /**
     * Returns the implemented queueing discipline of the underlying queue for transporters as a String, so it can be
     * displayed in the report.
     *
     * @return String : The String indicating the queueing discipline.
     */
    public String getTransporterQueueStrategy() {

        return this.getTransporter().getQueueStrategy(); // ask the
        // transporter
        // queue

    }

    /**
     * Returns the implemented queueing discipline of the underlying queue for jobs as a String, so it can be displayed
     * in the report.
     *
     * @return String : The String indicating the queueing discipline.
     */
    public String getJobsQueueStrategy() {

        return this.getJobs().getQueueStrategy(); // ask the jobs queue
    }

    /**
     * Returns n idle transporters of this transporter system that have still no crane that are assigned to.
     *
     * @return <code>InternalTransporter</code>[] : The idle internal
     *     transporters.
     */
    public InternalTransporter[] getFreeTransporter(int n) {

        if (n <= 0) {
            sendWarning(
                "Attempt to take negative number of transporters or nothing from a Transporter system "
                    + "The attempted action is ignored!",
                "TransporterSystem: " + getName()
                    + " Method:getFreeTransporter (int n)",
                "It doesn't make sense to take a number of transporters this way  ",
                "Make sure to take only positive number of transporter (or at least one transporter) of the TransporterSystem");

            return null; // ignore that rubbish
        }

        // if the number of the needed transporters is larger than that
        // transport system has
        if (n > transporterQueue.getQueueLimit()) {
            sendWarning(
                "Attempt to take more transporter from a Transporter System than it has "
                    + "The attempted action is ignored!",
                "TransporterSystem: " + getName()
                    + " Method:getFreeTransporter (int n)",
                "It doesn't make sense to take a number of transporters this way ",
                "Make sure to take only positive number of transporters (or at least one transporter) of the TransporterSystem");

            return null; // ignore that rubbish
        }

        // get the first transporter of the queue
        InternalTransporter t = (InternalTransporter) this.transporterQueue
            .first();

        // check how many are there such transporters
        int tnumber = 0;

        // while there's a transporter
        while (t != null) {
            // get the crane the transporter is assigned to
            if (t.getCrane() == null)
            // add the new TransporterJob to the Vector
            {
                tnumber++;
            }
            t = (InternalTransporter) this.transporterQueue.succ(t);
        }

        // if there aren't enough transporters
        if (tnumber < n) {
            return null;
        }

        // if there're enough such transporters
        InternalTransporter[] transporter = new InternalTransporter[tnumber];

        int i = 0;

        // get the first transporter of the queue
        InternalTransporter tr = (InternalTransporter) this.transporterQueue
            .first();
        while (tr != null) {
            // get the crane the transporter is assigned to
            if (tr.getCrane() == null)
            // add the new TransporterJob to the Vector
            {
                transporter[i] = tr;
                i++;
            }
            tr = (InternalTransporter) this.transporterQueue.succ(tr);
        }

        return transporter;
    }

    /**
     * Returns all the at the moment idle transporters of a certain crane they are assigned to.
     *
     * @param c <code>Crane</code>: The crane that these transportes are
     *          assigned to.
     * @return <code>InternalTransporter</code>[] : The idle internal
     *     transporters of a certain crane.
     */
    public InternalTransporter[] getAssignedTransporter(Crane c) {

        // make a new Vector to store such transporters
        Vector v = new Vector();

        // get the first transporter of the queue
        InternalTransporter t = (InternalTransporter) this.transporterQueue
            .first();

        // while there's a transporter
        while (t != null) {
            // get the crane the transporter is assigned to
            if ((t.getCrane() != null) && (t.getCrane().equals(c))) {
                v.addElement(t);
            }
            t = (InternalTransporter) this.transporterQueue.succ(t);
        }

        // if there aren't any such transporters
        if (v.size() == 0) {
            return null;
        }

        // return all found transporters
        InternalTransporter[] result = new InternalTransporter[v.size()];

        for (int i = 0; i < result.length; i++) {

            result[i] = (InternalTransporter) v.elementAt(i);
        }

        return result;
    }
}