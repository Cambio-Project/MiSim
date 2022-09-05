package desmoj.extensions.applicationDomains.harbour;

import desmoj.core.simulator.Condition;
import desmoj.core.simulator.Model;

/**
 * This condition should check if the job for the internal transporter (VC) has as the origin or destination the Crane
 * which that internal transporter (VC) is assigned to. The condition is used by the
 * <code>FixedVCtoCBAssignment_Strategy</code> to check if there'is a job for
 * a VC that is assigned a certain Crane to.
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
 * @see FixedVCtoCBAssignment_Strategy
 * @see TransportStrategy
 */
public class IsJobForTransporter extends Condition<Job> {

    /**
     * the crane (a containerbridge) which the VC that must be checked is assigned to.
     */
    private Crane c = null;

    /**
     * Constructs a IsJobForTransporter condition with all the given parameters.
     *
     * @param owner       desmoj.Model : The model this IsJobForTransporter condition belongs to.
     * @param name        java.lang.String : The name of this IsJobForTransporter condition.
     * @param showInTrace boolean : Flag, if this IsJobForTransporter condition should produce a trace output or not.
     */
    public IsJobForTransporter(Model owner, String name, boolean showInTrace) {

        super(owner, name, showInTrace); // make a condition
    }

    /**
     * Returns a boolean showing whether the job for the VC has as the origin or destination the Crane which that VC is
     * assigned to.
     *
     * @param e Entity : The entity (Job) to test.
     * @return boolean : Is <code>true</code>, if the job for the VC has as the origin or destination the Crane which
     *     that VC is assigned to,
     *     <code>false</code> otherwise.
     */
    public boolean check(Job j) {

        if (j == null) {
            sendWarning(
                "Attempt to check whether the job for the internal transporter (VC) has as the origin or destination the Crane which that VC is assigned to "
                    + "empty or with a null reference to that entity."
                    + "False will be returned!",
                "IsJobForTransporter:" + this.getName()
                    + " Method: boolean " + "check(Job j). ",
                "There is only a null pointer given which could not be checked.",
                "Make sure to pass "
                    + "a suitable job instead of a null pointer.");
            return false;
        }

        return (((j.getType() == 1) && j.getOrigin().equals(this
            .getCrane())) || ((j.getType() == 0) && j
            .getDestination().equals(this.getCrane())));
    }

    /**
     * Returns the crane (containerbridge) which the VC that must be checked is assigned to.
     *
     * @return <code>Crane</code>: The assigned crane of the VC.
     */
    public Crane getCrane() {

        return this.c;
    }

    /**
     * Sets the crane that the VC that must be checked is assigned to.
     *
     * @param c <code>Crane</code>: The assigned crane of the VC.
     */
    public void setCrane(Crane c) {

        this.c = c;
    }

}