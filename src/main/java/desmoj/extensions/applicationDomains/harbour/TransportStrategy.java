package desmoj.extensions.applicationDomains.harbour;

import desmoj.core.simulator.ProcessQueue;
import desmoj.core.simulator.Queue;

/**
 * A TransportStrategy is an interface and presents the strategy that a transporter control uses by the assigning of the
 * jobs to transporters. The method getJobs (ProcessQueue Transporter, Queue Jobs) that gives the transport control the
 * pairs of the assigned jobs with their transporters -
 * <code>TransporterJobs</code> must be implemented by the user in a class
 * that implements TransportStrategy to define the strategy which will be used by the transporter control.
 * TransportStrategy is part of the strategy design pattern as described in [Gamm97] page 333 in which it represents the
 * strategy interface.
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
 * @see TransporterJob
 * @see T_Control
 */
public interface TransportStrategy {

    /**
     * Implement this method in a class that implements this interface to define the algorithm of the strategy that
     * finds the jobs for the transporters and is used by transporter control.
     *
     * @param Transporter <code>ProcessQueue</code>: The queue with the transporters
     *                    of the transporter control.
     * @param Jobs        <code>Queue</code>: The queue with the jobs of the
     *                    transporter control.
     * @return <code>TransporterJob</code>[] : The pairs of the transporters
     *     with the their assigned jobs.
     */
    TransporterJob[] getJobs(ProcessQueue Transporter, Queue Jobs);
}