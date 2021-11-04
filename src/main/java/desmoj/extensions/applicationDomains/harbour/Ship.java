package desmoj.extensions.applicationDomains.harbour;

import desmoj.core.simulator.Model;

/**
 * A Ship is an <code>ExternalTransporter</code> wich arrives in a container terminal to deliver and /or pick up some
 * containers (goods). It has a certain number of import/export containers that must have loaded/unloaded before it can
 * leave a container terminal. The both numbers must be not negative. It also has a certain length that he needs to take
 * from a berth for his berthing and a ceratin number of cranes (containerbridges) that he needs for his unloading
 * and/or loading. The ship can leave the terminal after his whole unloading and/or loading. These both parameters must
 * be not negative or zero. Ship is derived from ExternalTrasnporter. Its <code>lifeCycle()</code> must be implemented
 * by the user in order to specify the behavior of the Ship.
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
 * @see ExternalTransporter
 */
public abstract class Ship extends ExternalTransporter {

    /**
     * The length that this Ship needs to take from a berth for his berthing.
     */
    private int berthLength;

    /**
     * The number of cranes (containerbridges) that the ship needs fpr its complete unloading and/or loading.
     */
    private int nCranes;

    /**
     * The berth that can be assigned to a Ship.
     */
    private Berth berth;

    /**
     * Constructs a Ship which arrives at a container terminal to deliver and/or pick up some containers (goods).
     * Implement its <code>lifeCycle()</code> method to specify its behavior. A Ship has a number of import/export
     * containers that must have loaded/unloaded before it can leave a container terminal. Both must not be negative.
     * Their default value is one. A Ship has also a certain length that he needs to take from a berth for his berthing
     * and a ceratin number of cranes (containerbridges) that he needs for his unloading and /or loading. The default
     * value for the number of the cranes is one.
     *
     * @param owner        desmoj.Model : The model this Ship is associated to.
     * @param name         java.lang.String : The name of this Ship.
     * @param nImportGoods long : The number of import goods this Ship delivers to a container terminal.
     * @param nExportGoods long : The number of export goods this Ship has to pick up from a container terminal.
     * @param berthLength  int : The length of the berth that this Ship needs for its berthing.
     * @param nCranes      int : The number of cranes (containerbridges) that this Ship needs for its complete unloading
     *                     and /or loading.
     * @param showInTrace  boolean : Flag, if this Ship should produce a trace output or not.
     */
    public Ship(Model owner, String name, long nImportGoods, long nExportGoods,
                int berthLength, int nCranes, boolean showInTrace) {

        super(owner, name, nImportGoods, nExportGoods, showInTrace); // make
        // an
        // ExternalTransporter

        // check the needed length of the berth
        if (berthLength <= 0) {
            sendWarning(
                "The given length for  a berth  is " + "negative or zero. ",
                getClass().getName()
                    + ": "
                    + getQuotedName()
                    + ", Constructor: Ship(Model owner, String name, "
                    + "long nImportGoods, long nExportGoods, int berthLength, int nCranes, boolean showInTrace)",
                "Tne negative length or zero for a berth does not make sense.",
                "Make sure to provide a valid positive length for a berth "
                    + "for the Ship to be constructed.");

            return;
        } else {
            this.berthLength = berthLength; // set the length of the berth
        }

        // check the number of the cranes
        if (nCranes <= 0) {
            sendWarning(
                "The given number of cranes for the ship  is "
                    + "negative or zero. The number of cranes will be set to one!",
                getClass().getName()
                    + ": "
                    + getQuotedName()
                    + ", Constructor: Ship(Model owner, String name, "
                    + "long nImportGoods, long nExportGoods, int berthLength, int nCranes, boolean showInTrace)",
                "Tne negative number of cranes  or zero for a ship does not make sense.",
                "Make sure to provide a valid positive number of cranes  for a ship "
                    + "for the Ship to be constructed.");

            this.nCranes = 1;
        } else {
            this.nCranes = nCranes; // set number of the cranes
        }
    } // end of constructor

    /**
     * Returns the length of a berth that this Ship needs for its berthing there.
     *
     * @return int : The needed length of a berth for this Ship to berth there.
     */
    public int getBerthLength() {

        return berthLength;

    }

    /**
     * Sets the length that this Ship needs from a berth to berth there to a new value.
     *
     * @param l int : The new needed length of the berth for this Ship.
     */
    public void setBerthLength(int l) {

        berthLength = l;

    }

    /**
     * Returns the number of the cranes (containerbridges)that this Ship needs for his unloading and/or loading.
     *
     * @return int : The number of the cranes of this Ship.
     */
    public int getNumOfCranes() {

        return nCranes;

    }

    /**
     * Sets the number of the cranes that this Ship needs for its unloading and /or loading to a new value. The new
     * value must not be negative or zero.
     *
     * @param n int : The new number of cranes (containerbridges) of this Ship.
     */
    public void setNumOfCranes(int n) {

        // check the number of the cranes
        if (nCranes <= 0) {
            sendWarning(
                "The given number of cranes for the ship  is "
                    + "negative or zero. The number must be positive!",
                getClass().getName() + ": " + getQuotedName()
                    + ", Method: public void "
                    + "setNumOfCranes (int n)",
                "Tne negative number of cranes  or zero for a ship does not make sense.",
                "Make sure to provide a valid positive number of cranes  for a ship "
                    + "for the Ship to be changed.");

            return;
        }

        this.nCranes = n;

    }

    /**
     * Returns the berth of this Ship where it's berthing.
     *
     * @return <code>Berth</code>: The berth of this Ship.
     */
    public Berth getBerth() {

        return this.berth;

    }

    /**
     * Sets (assings) the berth of this Ship to a new value.
     *
     * @param b Berth : The new berth of this Ship.
     */
    public void setBerth(Berth b) {

        this.berth = b;

    }
}