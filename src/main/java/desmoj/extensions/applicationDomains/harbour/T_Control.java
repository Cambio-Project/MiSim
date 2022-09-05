package desmoj.extensions.applicationDomains.harbour;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.SimProcess;

/**
 * A T_Control represents the control component for the transporters. The transporters get the <code>Jobs</code> from
 * this control that assignes the job to them by using a certain <code>TransportStrategy</code>. Its
 * <code>lifeCycle()</code> is already implemented and spezifies the behavior
 * of the transport control.
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
 * @see TransportStrategy
 * @see TransporterSystem
 */
public class T_Control extends SimProcess {

    /**
     * The transport strategy that this transport control uses by the assigning jobs to the transporters.
     */
    private TransportStrategy s = null;

    /**
     * The transporter system that manages all the statistic of the all transporters of this transporter control.
     */
    private TransporterSystem ts = null;

    /**
     * Constructor for a transport control T_Control for the transporters. The queueing discipline and the capacity
     * limit of the underlying queues for the jobs and for the transporters can be chosen.
     *
     * @param owner        Model : The model this T_Control is associated to.
     * @param name         java.lang.String : The T_Control's name
     * @param tsortOrder   int : determines the sort order of the underlying transporter queue implementation. Choose a
     *                     constant from
     *                     <code>QueueBased</code> like <code>QueueBased.FIFO</code>
     *                     or <code>QueueBased.LIFO</code> or ...
     * @param tQCapacity   int : The capacity of the transporter queue, that is how many transporters can be enqueued.
     *                     Zero (0) means unlimited capacity.
     * @param jsortOrder   int : determines the sort order of the underlying jobs queue implementation. Choose a
     *                     constant from <code>QueueBased</code> like <code>QueueBased.FIFO</code> or
     *                     <code>QueueBased.LIFO</code> or ...
     * @param jQCapacity   int : The capacity of the jobs queue, that is how many jobs can be enqueued. Zero (0) means
     *                     unlimited capacity.
     * @param s            TransportStrategy : The strategy that this T_Control uses to assign the jobs to the
     *                     transporters.
     * @param showInReport boolean : Flag, if T_Control (its TransporterSystem) should produce a report or not.
     * @param showInTrace  boolean : Flag for trace to produce trace messages.
     */
    public T_Control(Model owner, String name, int tSortOrder, int tQCapacity,
                     int jSortOrder, int jQCapacity, TransportStrategy s,
                     boolean showInReport, boolean showInTrace) {
        super(owner, name, true, showInTrace); // make a sim-process

        // make a new TransportSystem
        this.ts = new TransporterSystem(owner, "TransporterSystem", tSortOrder,
            tQCapacity, jSortOrder, jQCapacity, true, true);

        // set the transport strategy
        this.s = s;
    }

    /**
     * This method is used to add the job to the transport control to get assigned to a transporter.
     *
     * @param t <code>Job</code>: The Job that must be assigned to a
     *          transporter.
     */
    public void addJob(Job j) {

        // add the job in jobs queue by the transport system
        this.ts.addJob(j);

        // if the transporter control is still not scheduled
        if (!this.isScheduled()) {

            // activate the transport control to get the job assigned
            this.activate();
        }
    }

    /**
     * This method is used by an internal transporter to inform his transporter control that he is idle now (without a
     * job).
     *
     * @param t <code>InternalTranspoter</code>: The InternalTransporter
     *          that is idle.
     */
    public void addTransporter(InternalTransporter t) throws SuspendExecution {

        // add the transporter in the transporter queue by the transport system
        this.ts.addTransporter(t);

        // passivate the transporter
        t.passivate();
    }

    /**
     * This method implements the transporter control specific behaviour. This behavior is always the same therefore
     * there is no need to implement that in a subclass of T_Control. This method starts after a transporter control has
     * been created and activated by the scheduler.
     */
    public void lifeCycle() throws SuspendExecution {

        // if there's jobs and transporters
        if ((this.ts.getJobs().length() > 0)
            && (this.ts.getTransporter().length() > 0)) {
            // get from the strategy all the TransporterJobs
            TransporterJob[] jobs = s.getJobs(this.ts.getTransporter(),
                this.ts.getJobs());

            // for every TransporterJob
            for (int i = 0; i < jobs.length; i++) {
                // get the job
                Job j = jobs[i].getJob();

                // get the transporter
                InternalTransporter t = jobs[i].getTransporter();

                // remove the job from the jobs queue
                this.ts.getJobs().remove(j);

                // remove the transporter from the transporter queue
                this.ts.getTransporter().remove(t);

                // assign the the job to the transporter
                t.setJob(j);

                // activate the transporter after the transport control
                t.activate();

            }

        } // end of if

        passivate(); // passivate itself


    }

    /**
     * Returns the transporter system of this transport control.
     *
     * @return <code>TransporterSystem</code>: The transporter system of this
     *     transporter control.
     */
    public TransporterSystem getTS() {

        return this.ts;
    }

    /**
     * Sets the transport system of this transporter control to a new value.
     *
     * @param ts TransporterSystem : The new transport system of this transporter control.
     */
    public void setTS(TransporterSystem ts) {

        // if the transporter system has been already used
        if ((this.ts.getJobs().getObservations() > 0)
            || (this.ts.getTransporter().getObservations() > 0)) {
            sendWarning(
                "Attempt to change the  transporter system by a transporter control that already"
                    + " in use. The transporter system  remain unchanged!",
                "T_Control: " + this.getName()
                    + " Method: void setTS (TransporterSystem ts)",
                "The transporter system which has already be used can not"
                    + " be changed afterwards.",
                "Do not try to change the transporter system which might have been"
                    + " used already. Or reset the transporter system before changing it.");

            return; // without setting of the new transporter system

        }

        this.ts = ts; // set the new transporter system
    }

    /**
     * Returns the transport strategy of this transport control.
     *
     * @return <code>TransportStrategy</code>: The transport strategy of this
     *     transporter control.
     */
    public TransportStrategy getTransportStrategy() {

        return this.s;
    }

    /**
     * Sets the transport strategy of this transporter control to a new value.
     *
     * @param s TransportStrategy : The new transport strategy of this transporter control.
     */
    public void setTransportStrategy(TransportStrategy s) {

        this.s = s;

    }
}