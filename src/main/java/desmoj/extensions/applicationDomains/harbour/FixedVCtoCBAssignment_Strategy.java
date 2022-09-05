package desmoj.extensions.applicationDomains.harbour;

import java.util.Vector;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.ModelComponent;
import desmoj.core.simulator.ProcessQueue;
import desmoj.core.simulator.Queue;

/**
 * The FixedVCtoCBAssignment_Strategy is the strategy that a transporter control uses by the assigning of the jobs to
 * transporters the following way: every transporter (VC) gets the <code>Job</code> that has as origin or destination
 * the crane that transporter is assigned to. The FixedVCtoCBAssignment_Strategy implements the method getJobs
 * (ProcessQueue Transporter, Queue Jobs) of the <code>TransportStategy</code>. The FixedVCtoCBAssignment_Strategy is
 * derived from ModelComponent.
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
 * @see TransporterJob
 * @see T_Control
 * @see IsJobForVC
 * @see ModelComponent
 */
public class FixedVCtoCBAssignment_Strategy extends ModelComponent implements
    TransportStrategy {
    /**
     * the condition that checks the job for a VC has as the origin or destination the Crane which that VC is assigned
     * to.
     */
    private final IsJobForTransporter cond;

    /**
     * Constructs the FixedVCtoCBAssignment_Strategy that a transporter control uses by the assigning of the jobs to
     * transporters the following way: every transporter (VC) gets the <code>Job</code> that has as origin or
     * destination the crane that transporter is assigned to.
     *
     * @param owner desmoj.Model : The model this FixedVCtoCBAssignment_Strategy is associated to.
     */
    public FixedVCtoCBAssignment_Strategy(Model owner) {

        super(owner, "FixedVCtoCBAssignmentStrategy"); // make a ModelComponent

        this.cond = new IsJobForTransporter(owner, "IsJobForVC", false); // make
        // a
        // new
        // IsJobForTransporter-condition
    }

    /**
     * This method describes the following way of the assigning the jobs to transporters: every transporter (VC) gets
     * the <code>Job</code> that has as origin or destination the crane that transporter is assigned to. This each
     * assignment will be stored in a <code>TranspoterJob</code>. All the assignments are used by the transport
     * control.
     *
     * @param Jobs <code>Queue</code>: The queue with the jobs of the
     *             transporter control.
     * @return <code>TransporterJob</code>[] : The pairs of the transporters
     *     with the their assigned jobs.
     */
    public TransporterJob[] getJobs(ProcessQueue transporters, Queue jobs) {

        // make a new Vector to store the TransporterJobs
        Vector v = new Vector();

        // get the first transporter of the queue
        InternalTransporter t = (InternalTransporter) transporters.first();

        // while there's a transporter
        while (t != null) {
            // get the crane the transporter is assigned to
            Crane c = t.getCrane();

            if (c != null) {
                // set by the condition the crane that a job must have as its
                // origin or destination
                cond.setCrane(c);

                // check if there's jobs for this transpoter and get the first
                // such job
                Job j = (Job) jobs.first(cond);

                // while there is a job and this job is already assigned to a
                // transporter
                while ((j != null) && (j.isSelected())) {
                    // get the next Job
                    j = (Job) jobs.succ(j, cond);
                }

                // if there is a job and it is still not assigned
                if (j != null) {
                    // add the new TransporterJob to the Vector
                    v.addElement(new TransporterJob(t, j));

                    // sets the job as assigned
                    j.setSelected(true);

                }
            } // end of if the crane is not null
            // get the next transporter from the queue
            t = (InternalTransporter) transporters.succ(t);
        } // end of the while with t

        // make an array of the TransporterJobs and store there all the found
        // TransporterJobs
        // from the Vector
        TransporterJob[] result = new TransporterJob[v.size()];

        for (int i = 0; i < result.length; i++) {

            result[i] = (TransporterJob) v.elementAt(i);
        }

        return result;
    }

}