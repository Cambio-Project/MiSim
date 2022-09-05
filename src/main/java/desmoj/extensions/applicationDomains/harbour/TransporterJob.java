package desmoj.extensions.applicationDomains.harbour;

/**
 * A TransporterJob represents a job that must be assigned to internal the transporter get it done. TransporterJob is
 * using to store the transporter and his job to do by a transport strategy.
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
 */
public class TransporterJob {

    /**
     * the internal transporter of this TransporterJob.
     */
    private InternalTransporter t = null;

    /**
     * the job of this TransporterJob.
     */
    private Job j = null;

    /**
     * Constructs a TransporterJob with the internal transporter and his job.
     *
     * @param t InternalTransporter : The InternalTransporter.
     * @param j Job : The Job.
     */
    public TransporterJob(InternalTransporter t, Job j) {
        this.t = t;
        this.j = j;

    }

    /**
     * Returns the InternalTransporter of this TransporterJob.
     *
     * @return <code>InternalTransporter</code>: The InternalTransporter of
     *     this TransporterJob.
     */
    public InternalTransporter getTransporter() {

        return t;

    }

    /**
     * Returns the Job of this TransporterJob.
     *
     * @return <code>Job</code>: The Job of this TransporterJob.
     */
    public Job getJob() {

        return j;

    }
}