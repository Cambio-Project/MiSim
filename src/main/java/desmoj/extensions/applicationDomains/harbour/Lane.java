package desmoj.extensions.applicationDomains.harbour;

import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Model;

/**
 * A Lane represents a lane where a <code>Truck</code> waits for an internal transporter to get loaded/unloaded by him
 * at a <code>Holding Area</code>. It has a certain number that must ne not negotive.
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
public class Lane extends Entity {

    /**
     * the number of the lane.
     */
    private int number;

    /**
     * Constructs a Lane with a certain number.
     *
     * @param owner       desmoj.Model : The model this Lane is associated to.
     * @param name        java.lang.String : The name of this Lane.
     * @param number      int : The number of this Lane.
     * @param showInTrace boolean : Flag, if this Lane should produce a trace output or not.
     */
    public Lane(Model owner, String name, int number, boolean showInTrace) {

        super(owner, name, showInTrace); // make an entity

        // check the number of the lane
        if (number < 0) {
            sendWarning("The given number is " + "wrong.", getClass().getName()
                    + ": " + getQuotedName()
                    + ", Constructor: Lane(Model owner, String name, "
                    + "int number, boolean showInTrace)",
                "Tne type that is negative does not make sense.",
                "Make sure to provide a positive  value for the  number "
                    + "for the Lane to be constructed.");

            return;
        }
        // set the number
        this.number = number;

    }

    /**
     * Returns the current number of this Lane.
     *
     * @return int : The number of this Lane.
     */
    public int getNumber() {

        return this.number;
    }

    /**
     * Sets the number of this Lane to a new value.
     *
     * @param n int : The new number of this Lane.
     */
    public void setNumber(int n) {

        // check the new number for the lane
        if (n < 0) {
            sendWarning("The given number is " + "wrong.", getClass().getName()
                    + ": " + getQuotedName() + ", Method: setNumber( "
                    + "int n)",
                "Tne type that is negative does not make sense.",
                "Make sure to provide a positive  value for the  number "
                    + "for the Lane to be changed.");

            return;
        }

        this.number = n;
    }
}