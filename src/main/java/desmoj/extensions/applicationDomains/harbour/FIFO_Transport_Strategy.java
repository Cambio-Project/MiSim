package desmoj.extensions.applicationDomains.harbour;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.ModelComponent;
import desmoj.core.simulator.ProcessQueue;
import desmoj.core.simulator.Queue;

/**
 * The FIFO_Transport_Strategy is the strategy that a transporter control uses by the assigning of the jobs to
 * transporters the following way: the first
 * <code>Job</code> in the queue of jobs is assigned to the first transporter
 * in the queue of transportes. The FIFO_Transport_Strategy implements the method getJobs (ProcessQueue Transporter,
 * Queue Jobs) of the
 * <code>TransportStategy</code>. The FIFO_Transport_Strategy is derived from
 * ModelComponent.
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
 * @see ModelComponent
 */
public class FIFO_Transport_Strategy extends ModelComponent implements
    TransportStrategy {
    /**
     * Constructs the FIFO_Transport_Strategy that a transporter control uses by the assigning of the jobs to
     * transporters the following way: the first
     * <code>Job</code> in the queue of jobs is assigned to the first
     * transporter in the queue of transportes.
     *
     * @param owner desmoj.Model : The model this FIFO_Transport_Strategy is associated to.
     */
    public FIFO_Transport_Strategy(Model owner) {

        super(owner, "FIFOTransportStrategy"); // make a ModelComponent
    }

    /**
     * This method describes the following way of the assigning the jobs to transporters: the first job in the queue of
     * the jobs will be assigned to the first transporter in the queue of the transpoters, this each assignment will be
     * stored in a <code>TranspoterJob</code>. All the assignments are used by the transport control.
     *
     * @param Jobs <code>Queue</code>: The queue with the jobs of the
     *             transporter control.
     * @return <code>TransporterJob</code>[] : The pairs of the transporters
     *     with the their assigned jobs.
     */
    public TransporterJob[] getJobs(ProcessQueue transporters, Queue jobs) {

        // get the number of the Jobs
        int size_jobs = jobs.length();

        // get the number of the transportes
        int size_transporters = transporters.length();

        // get the number of the TransporterJobs
        int size_min = Math.min(size_jobs, size_transporters);

        TransporterJob[] result = new TransporterJob[size_min];
        int current = 0;

        // get the first job of the queue
        Job j = (Job) jobs.first();

        // get the first transporter of the queue
        InternalTransporter t = (InternalTransporter) transporters.first();

        // while there's a job and a transporter
        while ((t != null) && (j != null)) {
            // make a new TransporterJob
            result[current] = new TransporterJob(t, j);

            current++;

            // get next Job of the Job's queue
            j = (Job) jobs.succ(j);

            // get next internal transporter of the transporter queue
            t = (InternalTransporter) transporters.succ(t);

        }
        // get all the TransporterJobs
        return result;
    }

}