package desmoj.extensions.applicationDomains.harbour;

import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.ModelComponent;

/**
 * A Job represents any kind of job that the <code>InternalTransporter</code> has to do in a container terminal. It has
 * a certain origin (the place the internal transporter has to drive to to get/leave the containers(goods) and
 * destination (the place the internal transporter has to drive to to leave/get the containers(goods). It has also a
 * certain typ: import or export. Job is derived from Entity.
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
 * @see Entity
 */
public class Job extends Entity {

    /**
     * the origin of the job
     */
    private ModelComponent origin = null;

    /**
     * the destination of the job
     */
    private ModelComponent destination = null;

    /**
     * the type of the job
     */
    private int type; // 1-import, 0-export

    /**
     * flag if the job has been already assigned to an internal transporter
     */
    private boolean selected = false;

    /**
     * Constructs a Job that must been done by an internal transporter. A Job has a certain typ. This typ must be 1
     * (import) or 0 (export), if the job is an import or export job. A Job has also certain origin and destination.
     *
     * @param owner       desmoj.Model : The model this Job is associated to.
     * @param name        java.lang.String : The name of this Job.
     * @param typ         int : The typ of this Job: 1 or 0.
     * @param origin      <code>ModelComponent</code>: The origin of this Job
     * @param destination <code>ModelComponent</code>: The destination of this Job
     * @param showInTrace boolean : Flag, if this Job should produce a trace output or not.
     */
    public Job(Model owner, String name, int type, ModelComponent origin,
               ModelComponent destination, boolean showInTrace) {

        super(owner, name, showInTrace); // make an entity

        // check the typ of the job
        if ((type != 1) && (type != 0)) {
            sendWarning(
                "The given type is " + "wrong.",
                getClass().getName()
                    + ": "
                    + getQuotedName()
                    + ", Constructor: Job(Model owner, String name, "
                    + "int type, ModelComponent origin, ModelComponent destination, boolean showInTrace)",
                "Tne type that not 1 or 0 is  does not make sense.",
                "Make sure to provide a valid  value for type "
                    + "for the Job to be constructed.");

            return;
        }

        this.type = type; // set the type of the job

        this.origin = origin; // set the origin of the job

        this.destination = destination; // set the destination of the job

    } // end of constructor

    /**
     * Returns the origin of this job.
     *
     * @return <code>ModelComponent</code>: The origin of this job.
     */
    public ModelComponent getOrigin() {

        return this.origin;
    }

    /**
     * Sets the origin of this job.
     *
     * @param o <code>ModelComponent</code>: The new origin of this Job.
     */
    public void setOrigin(ModelComponent o) {

        this.origin = o;
    }

    /**
     * Returns the destination of this job.
     *
     * @return <code>ModelComponent</code>: The destination of this job.
     */
    public ModelComponent getDestination() {

        return this.destination;
    }

    /**
     * Sets the destination of this job.
     *
     * @param d <code>ModelComponent</code>: The new destination of this
     *          Job.
     */
    public void setDestination(ModelComponent d) {

        this.destination = d;
    }

    /**
     * Returns the type of this job: 1 or 0.
     *
     * @return int : The type of this job.
     */
    public int getType() {

        return this.type;
    }

    /**
     * Sets the type of this job.
     *
     * @param i int : The new type of this Job.
     */
    public void setType(int t) {

        // check the type of the job
        if ((type != 1) && (type != 0)) {
            sendWarning("The given type is " + "wrong.",
                getClass().getName() + ": " + getQuotedName()
                    + ", Method: setType(int t " + ")",
                "Tne type that not 1 or 0 is  does not make sense.",
                "Make sure to provide a valid  value for type "
                    + "for the Job to be changed.");

            return;
        }
        this.type = t;
    }

    /**
     * Returns true if job has been already selected for an internal transporter anf false otherwise.
     *
     * @return boolean : The boolean value if the job' been already selected.
     */
    public boolean isSelected() {

        return this.selected;
    }

    /**
     * Sets the boolean value if the job' been already selected.
     *
     * @param b boolean : The new flag if the job' been already selected.
     */
    public void setSelected(boolean b) {

        this.selected = b;
    }
}