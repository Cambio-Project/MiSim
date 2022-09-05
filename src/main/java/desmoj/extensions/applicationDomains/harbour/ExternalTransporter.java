package desmoj.extensions.applicationDomains.harbour;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.SimProcess;

/**
 * An ExternalTransporter is represents any kind of an external transporter (
 * <code>Ship</code>,<code>Truck</code>, Train)that arrives in a
 * container terminal to deliver and/or pick up some containers (goods). It has a certain number of import/export
 * containers that must have loaded/unloaded before it can leave a container terminal. The both numbers must be not
 * negative. ExternalTransporter is derived from SimProcess. Its
 * <code>lifeCycle()</code> must be implemented by the user in order to
 * specify the behavior of the ExternalTransporter.
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
 * @see SimProcess
 */
public abstract class ExternalTransporter extends SimProcess {

    /**
     * The number of import goods (containers) to be unloaded/loaded.
     */
    private long nImportGoods;

    /**
     * The number of export goods (containers) to be loaded/unloaded.
     */
    private long nExportGoods;

    /**
     * Constructs an ExternalTransporter which arrives at a container terminal to deliver and/or pick up some containers
     * (goods). Implement its or more its derived classes <code>lifeCycle()</code> method like
     * <code>Ship</code>,<code>Truck</code> or Train to specify its
     * behavior. An External Transporter has a number of import/export containers that must have loaded/unloaded before
     * it can leave a container terminal. Both must not be negative. Their default value is one.
     *
     * @param owner        desmoj.Model : The model this ExternalTransporter is associated to.
     * @param name         java.lang.String : The name of this ExternalTransporter.
     * @param nImportGoods long : The number of import goods this ExternalTransporter has to bring /take to/from
     *                     container terminal.
     * @param nExportGoods long : The number of export goods this ExternalTransporter has to take /bring from/to
     *                     container terminal.
     * @param showInTrace  boolean : Flag, if this ExternaTransporter should produce a trace output or not.
     */
    public ExternalTransporter(Model owner, String name, long nImportGoods,
                               long nExportGoods, boolean showInTrace) {
        super(owner, name, showInTrace); // make a sim-process

        // check the number of import goods
        if (nImportGoods < 0) {
            sendWarning(
                "The given number of  import goods  is "
                    + "negative. The number of import goods will be set to one!",
                getClass().getName()
                    + ": "
                    + getQuotedName()
                    + ", Constructor: ExternalTransporter(Model owner, String name, "
                    + "long nImportGoods, long nExportGoods, boolean showInTrace)",
                "Tne negative number of import goods does not make sense.",
                "Make sure to provide a valid positive number of import goods "
                    + "for the ExternalTransporter to be constructed.");

            this.nImportGoods = 1;
        } else {
            this.nImportGoods = nImportGoods;
        }

        // check the number of export goods
        if (nExportGoods < 0) {
            sendWarning(
                "The given number of export goods  is "
                    + "negative. The number of export goods will be set to one!",
                getClass().getName()
                    + ": "
                    + getQuotedName()
                    + ", Constructor: ExternalTransporter(Model owner, String name, "
                    + "long nImportGoods, long nExportGoods, boolean showInTrace)",
                "Tne negative number of export goods does not make sense.",
                "Make sure to provide a valid positive number of export goods "
                    + "for the ExternalTransporter to be constructed.");

            this.nExportGoods = 1;
        } else {
            this.nExportGoods = nExportGoods;
        }

    }

    /**
     * Returns the number of import goods (containers) of this ExternalTransporter.
     *
     * @return long : The number of import goods of this ExternalTransporter.
     */
    public long getNumberOfImportGoods() {

        return nImportGoods;
    }

    /**
     * Sets the number of import goods (containers) of this ExternalTransporter to a new value. The new value must not
     * be negative.
     *
     * @param n long : The new number of import goods of this ExternalTransporter.
     */
    public void setNumberOfImportGoods(long n) {

        if (n < 0) {
            sendWarning("The given number of  import goods  is "
                    + "negative. The number of import goods must be positive!",
                getClass().getName() + ": " + getQuotedName()
                    + ", Method: public void  "
                    + "setNumberOfImportGoods(long n)",
                "Tne negative number of import goods does not make sense.",
                "Make sure to provide a valid positive number of import goods "
                    + "for the ExternalTransporter to be changed.");
            return;
        } else {
            nImportGoods = n;
        }

    }

    /**
     * Returns the number of export goods (containers) of this ExternalTransporter.
     *
     * @return long : The number of export goods of this ExternalTransporter.
     */
    public long getNumberOfExportGoods() {

        return nExportGoods;
    }

    /**
     * Sets the number of export goods (containers) of this ExternalTransporter to a new value. The new value must not
     * be negative.
     *
     * @param n long : The new number of export goods of this ExternalTransporter.
     */
    public void setNumberOfExportGoods(long n) {

        if (n < 0) {
            sendWarning("The given number of  export goods  is "
                    + "negative. The number of export goods must be positive!",
                getClass().getName() + ": " + getQuotedName()
                    + ", Method: public void  "
                    + "setNumberOfExportGoods(long n)",
                "Tne negative number of export goods does not make sense.",
                "Make sure to provide a valid positive number of export goods "
                    + "for the ExternalTransporter to be changed.");
            return;
        } else {
            nExportGoods = n;
        }

    }

}