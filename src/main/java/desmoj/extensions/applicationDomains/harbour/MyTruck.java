package desmoj.extensions.applicationDomains.harbour;

import desmoj.core.simulator.Condition;
import desmoj.core.simulator.Model;

/**
 * This condition should check if the number of lane of the truck where he waits for an internal transporter at the
 * <code>HoldingArea</code> is equal the number of the lane of this condition. The condition is used by an internal
 * transporter to find the needed truck that it has to unload/load.
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
 * @see Condition
 */
public class MyTruck extends Condition<Truck> {

    /**
     * the lane which number must be checked with the number of the truck lane.
     */
    private Lane lane = null;

    /**
     * Constructs a MyTruck condition with all the given parameters.
     *
     * @param owner       desmoj.Model : The model this MyTruck condition belongs to.
     * @param name        java.lang.String : The name of this MyTruck condition.
     * @param lane        Lane : The lane of this MyTruck condition.
     * @param showInTrace boolean : Flag, if this MyTruck condition should produce a trace output or not.
     */
    public MyTruck(Model owner, String name, Lane lane, boolean showInTrace) {

        super(owner, name, showInTrace); // make a condition

        this.lane = lane;
    }

    /**
     * Returns a boolean showing whether the number of the truck lane is equal to the condition lane.
     *
     * @param e Entity : The entity (Truck) to test.
     * @return boolean : Is <code>true</code>, if the number of the lanes are equal, <code>false</code> otherwise.
     */
    public boolean check(Truck t) {

        if (t == null) {
            sendWarning(
                "Attempt to check whether the number of the truck lane is equal the condition lane "
                    + "empty or  with a null reference to that entity."
                    + "False will be returned!",
                "MyTruck:" + this.getName() + " Method: boolean "
                    + "check(Truck t). ",
                "There is only a null pointer given which could not be checked.",
                "Make sure to pass "
                    + "a suitable truck instead of a null pointer.");
            return false;
        }

        return t.getLane().getNumber() == this.lane.getNumber();
    }

    /**
     * Returns the lane that must be checked with the truck lane.
     *
     * @return <code>Lane</code>: The lane of this condition.
     */
    public Lane getLane() {

        return this.lane;
    }

    /**
     * Sets the lane that must be checked with the truck lane .
     *
     * @param l <code>Lane</code>: The new lane for this condition .
     */
    public void setLane(Lane l) {

        this.lane = l;

    }

}