package desmoj.extensions.applicationDomains.production;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.SimProcess;

/**
 * A Transporter represents any kind of vehicle or conveyor belt which carries goods (products) around in a
 * manufacturing system. It has a certain capacity (maximum number of goods which can be carried around at once) and a
 * minimum load (minimum number of goods which will be carried). Transporter is derived from SimProcess. Its
 * <code>lifeCycle()</code> must be implemented by the user in order to specify the behavior of the Transporter.
 *
 * @author Soenke Claassen
 *     <p>
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 *     with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *     <p>
 *     Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *     the specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 * @see SimProcess
 * @see Transportation
 * @see TransportJunction
 */
public abstract class Transporter extends SimProcess {

    /**
     * The minimum number of goods to be transported.
     */
    private int minLoad = 1;

    /**
     * The maximum number of goods which can be transported.
     */
    private int capacity = 1;

    /**
     * Constructs a Transporter which will carry around goods in a manufacturing system. Implement its
     * <code>lifeCycle()</code> method to specify its behavior. A Transporter has a capacity (maximum number of goods
     * which can be transported) and a mimimum load ( a minimum number of goods it will carry). Both must not be zero or
     * negative. Their default value is one.
     *
     * @param owner       desmoj.Model : The model this Transporter is associated to.
     * @param name        java.lang.String : The name of this Transporter.
     * @param minLoad     int : The minimum number of goods this Transporter will carry around.
     * @param capac       int : The maximum number of goods this Transporter can carry around.
     * @param showInTrace boolean : Flag, if this Transporter should produce a trace output or not.
     */
    public Transporter(Model owner, String name, int minLoad, int capac,
                       boolean showInTrace) {
        super(owner, name, showInTrace); // make a sim-process

        // check the minimum load parameter
        if (minLoad < 0) {
            sendWarning(
                "The given minimum load of a transporter is "
                    + "negative. The minimum load will be set to one!",
                getClass().getName()
                    + ": "
                    + getQuotedName()
                    + ", Constructor: Transporter(Model owner, String name, "
                    + "int minLoad, int capac, boolean showInTrace)",
                "A minimum load which is negative does not make sense.",
                "Make sure to provide a valid positive minimum load "
                    + "for the Transporter to be constructed.");
            // set the minimum load to one
            this.minLoad = 1;
        } else {
            this.minLoad = minLoad;
        }

        // check the capacity parameter
        if (capac < 1) {
            sendWarning(
                "The given capacity of a transporter is zero or negative. "
                    + "The capacity will be set to one!",
                getClass().getName()
                    + ": "
                    + getQuotedName()
                    + ", Constructor: Transporter(Model owner, String name, "
                    + "int minLoad, int capac, boolean showInTrace)",
                "A capacity which is zero or negative does not make sense.",
                "Make sure to provide a valid positive capacity "
                    + "for the Transporter to be constructed.");
            // set the capacity to one
            this.capacity = 1;
        } else {
            this.capacity = capac;
        }

    }

    /**
     * Constructs a Transporter which will carry around goods in a manufacturing system. Implement its
     * <code>lifeCycle()</code> method to specify its behavior. A Transporter has a capacity (maximum number of goods
     * which can be transported) and a mimimum load (a minimum number of goods it will carry). Use this constructor to
     * construct a Transporter with a specified capacity and a minimum load of one.
     *
     * @param owner       desmoj.Model : The model this Transporter is associated to.
     * @param name        java.lang.String : The name of this Transporter.
     * @param capac       int : The maximum number of goods this Transporter can carry around.
     * @param showInTrace boolean : Flag, if this Transporter should produce a trace output or not.
     */
    public Transporter(Model owner, String name, int capac, boolean showInTrace) {
        // construct a Transporter with a minimum load of one
        this(owner, name, 1, capac, showInTrace);
    }

    /**
     * Returns the capacity of this Transporter. That is the maximum number of goods it can carry around the
     * manufacturing system.
     *
     * @return int : The capacity of this Transporter.
     */
    public int getCapacity() {

        return this.capacity;
    }

    /**
     * Sets the capacity of this Transporter to a new value. The new value must not be zero or negative.
     *
     * @param newCapacity int : The new capacity of this Transporter.
     */
    public void setCapacity(int newCapacity) {

        if (newCapacity < 1) {
            sendWarning(
                "The capacity of a transporter should be changed to zero or "
                    + "a negative value. The capacity will remain unchanged!",
                "Transporter : " + getName()
                    + " Method: void setCapacity(int newCapacity)",
                "A capacity which is zero or negative does not make sense.",
                "Make sure to provide a valid positive capacity "
                    + "when changing this attribute.");

            return; // forget that rubbish
        }

        this.capacity = newCapacity;
    }

    /**
     * Returns the minimum load of this Transporter. That is the minimum number of goods which must be loaded on this
     * Transporter before it starts carrying them to their destination.
     *
     * @return int : The minimum load of this Transporter
     */
    public int getMinLoad() {

        return this.minLoad;
    }

    /**
     * Sets the minimum load of this Transporter to a new value. The new value must not be negative.
     *
     * @param newMinLoad int : The new minimum load of this Transporter.
     */
    public void setMinLoad(int newMinLoad) {

        if (newMinLoad < 0) {
            sendWarning(
                "The minimum load of a transporter should be changed to a "
                    + "negative value. The minimum load will remain unchanged!",
                "Transporter : " + getName()
                    + " Method: void setMinLoad(int newMinLoad)",
                "A minimum load which is negative does not make sense.",
                "Make sure to provide a valid positive minimum load "
                    + "when changing this attribute.");

            return; // forget that rubbish
        }

        this.minLoad = newMinLoad;
    }

    /**
     * Override this method in a subclass of Transporter to implement the Transporters specific behaviour. It will at
     * least contain something to let this Transporter wait in a <code>TransportJunction</code> for goods to carry
     * around, like <br>
     * <code> TransportJunctionXY.cooperate(transportation);</code><br>
     * This method starts after a Transporter has been created and activated by the scheduler. It describes the behavior
     * of this special Transporter when he is acting alone. All action taking place when this Transporter (as a master)
     * acts together with other <code>SimProcess</code> es (as slaves) in some process cooperation is described in that
     * special
     * <code>cooperation</code> method of the class
     * <code>Transportation</code>.
     */
    public abstract void lifeCycle() throws SuspendExecution;
}