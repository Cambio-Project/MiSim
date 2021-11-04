package desmoj.extensions.applicationDomains.production;

import java.beans.PropertyChangeEvent;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.advancedModellingFeatures.Stock;
import desmoj.core.dist.NumericalDist;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.SimProcess;
import desmoj.core.simulator.TimeSpan;

/**
 * RestockProcessMS is a process restocking a <code>Stock</code> up to a maximum (M) inventory level every time a given
 * safety (S) stock level is reached.
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
 */
public class RestockProcessMS extends SimProcess implements
    java.beans.PropertyChangeListener {

    /**
     * The quantity of units the <code>Stock</code> will be replenished with. Will be recalculated in every cycle:
     * Maximum inventory level minus actual available units in the Stock (when the inventory level dropped below the
     * safety stock level).
     */
    private long orderQuantity;

    /**
     * The client <code>Stock</code> which will be supplied by this
     * <code>RestockProcess</code>.
     */
    private final Stock clientStock;

    /**
     * The random distribution stream determining the lead time. That is the time between the placement and receipt of
     * an order. If <code>null</code> lead time is zero.
     */
    private NumericalDist<?> leadTime;

    /**
     * The safety stock level. In case this level is reached a new order will be placed.
     */
    private long safetyStockLevel;

    /**
     * The maximum inventory level the <code>Stock</code> should be refilled up to.
     */
    private long maxInventoryLevel;

    /**
     * Constructs a <code>RestockProcessMS</code> which restocks a client
     * <code>Stock</code> up to a maximum inventory level every time the
     * safety stock is reached. The lead time (time gap between placement and receipt of an order) will be given as a
     * real random number distribution.
     *
     * @param owner       Model : The model this <code>RestockProcessMS</code> is associated to
     * @param name        java.lang.String : The name of the
     *                    <code>RestockProcessMS</code>
     * @param mil         long : The maximum inventory level the Stock will be filled up to with every order.
     * @param safetyStk   long : The safety stock. When this inventory level is reached an order will be placed
     * @param client      Stock : The <code>Stock</code> which will replenished.
     * @param lt          NumericalDist<?> : The lead time random number distribution to determine the time between
     *                    placement and receipt of an order. If <code>null</code> the lead time is zero.
     * @param showInTrace boolean : Flag for showing the <code>RestockProcessMS</code> in trace-files. Set it to
     *                    <code>true</code> if RestockProcessMS should show up in trace. Set it to
     *                    <code>false</code> if RestockProcessMS should not be shown
     *                    in trace.
     */
    public RestockProcessMS(Model owner, String name, long mil, long safetyStk,
                            Stock client, NumericalDist<?> lt, boolean showInTrace) {
        super(owner, name, true, showInTrace); // make a sim-process

        this.maxInventoryLevel = mil;
        this.safetyStockLevel = safetyStk;
        this.clientStock = client;
        this.leadTime = lt;

        // check the maximum inventory level parameter
        if (mil <= 0 || mil > client.getCapacity()) {
            sendWarning(
                "The given maximum inventory level parameter is zero, "
                    + "negative or greater than the capacity of the Stock. "
                    + "The maximum inventory level will be set to the capacity "
                    + "limit of the Stock!",
                "RestockProcessMS : "
                    + getName()
                    + " Constructor: RestockProcessMS(Model owner, String name, "
                    + "long mil, long safetyStk, Stock client, RealDist lt, "
                    + "boolean showInTrace)",
                "A maximum inventory level that is zero, negative or "
                    + "greater than the capacity does not make sense.",
                "Make sure to provide a maximum inventory level that is a "
                    + "valid positive integer number not greater than the capacity.");

            // set the maximum inventory level to the capacity of the Stock
            this.maxInventoryLevel = client.getCapacity(); // better than
            // nothing
        }

        // check the safety stock level parameter
        if (safetyStk <= 0 || safetyStk > client.getCapacity()) {
            sendWarning(
                "The given safety stock level parameter is zero, "
                    + "negative or greater than the capacity of the Stock. "
                    + "The safety stock level will be set to one!",
                "RestockProcessMS : "
                    + getName()
                    + " Constructor: RestockProcessMS(Model owner, String name, "
                    + "long mil, long safetyStk, Stock client, RealDist lt, "
                    + "boolean showInTrace)",
                "A safety stock level that is zero, negative or "
                    + "greater than the capacity does not make sense.",
                "Make sure to provide a safety stock level that is a "
                    + "valid positive integer number not greater than the capacity.");

            // set the safety stock level to one
            this.safetyStockLevel = 1; // better than nothing
        }

        // check the client Stock parameter
        if (client == null) {
            sendWarning(
                "The given client parameter is only a null pointer!",
                "RestockProcessMS : "
                    + getName()
                    + " Constructor: RestockProcessMS(Model owner, String name, "
                    + "long mil, long safetyStk, Stock client, RealDist lt, "
                    + "boolean showInTrace)",
                "The RestockProcessMS does not know which Stock to replenish "
                    + "and therefore is useless.",
                "Make sure to provide a valid Stock object which should "
                    + "be replenished by this RestockProcessMS.");

            return; // forget about it
        }

        // register as PropertyChangeListener at the Stock to get the news
        // about the available inventoy level
        clientStock.addPropertyChangeListener("avail", this);

    }

    /**
     * Constructs a <code>RestockProcessMS</code> which restocks a client
     * <code>Stock</code> up to a maximum inventory level every time the
     * safety stock is reached. The lead time is zero.
     *
     * @param owner       Model : The model this <code>RestockProcessMS</code> is associated to
     * @param name        java.lang.String : The name of the
     *                    <code>RestockProcessMS</code>.
     * @param mil         long : The maximum inventory level the Stock will be filled up to with every order.
     * @param safetyStk   long : The safety stock. When this inventory level is reached an order will be placed
     * @param client      Stock : The <code>Stock</code> which will replenished.
     * @param showInTrace boolean : Flag for showing the <code>RestockProcessMS</code> in trace-files. Set it to
     *                    <code>true</code> if RestockProcessMS should show up in trace. Set it to
     *                    <code>false</code> if RestockProcessMS should not be shown
     *                    in trace.
     */
    public RestockProcessMS(Model owner, String name, long mil, long safetyStk,
                            Stock client, boolean showInTrace) {
        super(owner, name, showInTrace); // make a sim-process

        this.maxInventoryLevel = mil;
        this.safetyStockLevel = safetyStk;
        this.clientStock = client;
        this.leadTime = null;

        // check the maximum inventory level parameter
        if (mil <= 0 || mil > client.getCapacity()) {
            sendWarning(
                "The given maximum inventory level parameter is zero, "
                    + "negative or greater than the capacity of the Stock. "
                    + "The maximum inventory level will be set to the capacity "
                    + "limit of the Stock!",
                "RestockProcessMS : "
                    + getName()
                    + " Constructor: RestockProcessMS(Model owner, String name, "
                    + "long mil, long safetyStk, Stock client, boolean showInTrace)",
                "A maximum inventory level that is zero, negative or "
                    + "greater than the capacity does not make sense.",
                "Make sure to provide a maximum inventory level that is a "
                    + "valid positive integer number not greater than the capacity.");

            // set the maximum inventory level to the capacity of the Stock
            this.maxInventoryLevel = client.getCapacity(); // better than
            // nothing
        }

        // check the safety stock level parameter
        if (safetyStk <= 0 || safetyStk > client.getCapacity()) {
            sendWarning(
                "The given safety stock level parameter is zero, "
                    + "negative or greater than the capacity of the Stock. "
                    + "The safety stock level will be set to one!",
                "RestockProcessMS : "
                    + getName()
                    + " Constructor: RestockProcessMS(Model owner, String name, "
                    + "long mil, long safetyStk, Stock client, boolean showInTrace)",
                "A safety stock level that is zero, negative or "
                    + "greater than the capacity does not make sense.",
                "Make sure to provide a safety stock level that is a "
                    + "valid positive integer number not greater than the capacity.");

            // set the safety stock level to one
            this.safetyStockLevel = 1; // better than nothing
        }

        // check the client Stock parameter
        if (client == null) {
            sendWarning(
                "The given client parameter is only a null pointer!",
                "RestockProcessMS : "
                    + getName()
                    + " Constructor: RestockProcessMS(Model owner, String name, "
                    + "long mil, long safetyStk, Stock client, boolean showInTrace)",
                "The RestockProcessMS does not know which Stock to replenish "
                    + "and therefore is useless.",
                "Make sure to provide a valid Stock object which should "
                    + "be replenished by this RestockProcessMS.");

            return; // forget about it
        }

        // register as PropertyChangeListener at the Stock to get the news
        // about the available inventoy level
        clientStock.addPropertyChangeListener("avail", this);

    }

    /**
     * Returns the quantity (number of units) to be stored in the Stock. Changes every time a new order is placed!
     *
     * @return long : The Stock will be replenished with this number of units.
     */
    public long getActualOrderQuantity() {

        return orderQuantity;
    }

    /**
     * Returns the random number distribution for the lead time (time between placement and receipt of an order).
     *
     * @return NumericalDist<?> : The random number distribution for the lead time (time between placement and receipt
     *     of an order).
     */
    public NumericalDist<?> getLeadTime() {

        return leadTime;
    }

    /**
     * Set the lead time to a new real random number distribution. If set to
     * <code>null</code> the lead time is zero.
     *
     * @param newLeadTime NumericalDist<?> : The new real random number distribution determining the lead time.
     */
    public void setLeadTime(NumericalDist<?> newLeadTime) {

        leadTime = newLeadTime;
    }

    /**
     * Returns the maximum inventory level up to which the Stock will be refilled in every cycle.
     *
     * @return long : The maximum inventory level up to which the Stock will be refilled in every cycle.
     */
    public long getMaxInventoryLevel() {

        return maxInventoryLevel;
    }

    /**
     * Sets the maximum inventory level to a new value. Make sure it is greater than zero and less than the capacity of
     * the Stock.
     *
     * @param newMaxInventoryLevel long : The new maximum inventory level. Make sure it is greater than zero and less
     *                             than the capacity of the Stock.
     */
    public void setMaxInventoryLevel(long newMaxInventoryLevel) {

        // check the new maximum inventory level value
        if (newMaxInventoryLevel <= 0
            || newMaxInventoryLevel > clientStock.getCapacity()) {
            sendWarning(
                "The given maximum inventory level parameter is zero, "
                    + "negative or greater than the capacity of the Stock. "
                    + "The maximum inventory level will remain unchanged!",
                "RestockProcessMS : "
                    + getName()
                    + " Method: void setMaxInventoryLevel(long newMaxInventoryLevel)",
                "A maximum inventory level that is zero, negative or "
                    + "greater than the capacity does not make sense.",
                "Make sure to provide a maximum inventory level that is a "
                    + "valid positive integer number not greater than the capacity.");

            return; // leave that rubbish alone
        }

        maxInventoryLevel = newMaxInventoryLevel;
    }

    /**
     * Returns the safety stock level. When this inventory level is reached a new order will be placed.
     *
     * @return long : The safety stock level. When this inventory level is reached a new order will be placed.
     */
    public long getSafetyStockLevel() {

        return safetyStockLevel;
    }

    /**
     * Sets the safety stock level to a new value. Make sure it is greater than zero and less than the capacity of the
     * Stock.
     *
     * @param newSafetyStk long : The new safety stock level. Make sure it is greater than zero and less than the
     *                     capacity of the Stock.
     */
    public void setSafetyStockLevel(long newSafetyStk) {

        // check the new safety stock level value
        if (newSafetyStk <= 0 || newSafetyStk > clientStock.getCapacity()) {
            sendWarning(
                "The given safety stock level parameter is zero, "
                    + "negative or greater than the capacity of the Stock. "
                    + "The safety stock level will remain unchanged!",
                "RestockProcessMS : "
                    + getName()
                    + " Method: void setSafetyStockLevel(long newSafetyStk)",
                "A safety stock level that is zero, negative or "
                    + "greater than the capacity does not make sense.",
                "Make sure to provide a safety stock level that is a "
                    + "valid positive integer number not greater than the capacity.");

            return; // leave that rubbish alone
        }

        safetyStockLevel = newSafetyStk;
    }

    /**
     * The <code>RestockProcessMS</code> replenishes the associated
     * <code>Stock</code> up to the maximum (M) inventory level every time the
     * inventory level dropped below the safety (S) stock level.
     */
    public void lifeCycle() throws SuspendExecution {

        // wait until inventory level drops below safety stock
        passivate();

        // woken up because inventory level dropped below safety stock
        // determine the order quantity for this cycle
        orderQuantity = maxInventoryLevel - clientStock.getAvail();

        // check if an order has to be placed
        if (orderQuantity > 0) {
            // place order (and tell so in the debug file)
            if (currentlySendTraceNotes()) {
                sendTraceNote("places an order over " + orderQuantity
                    + " units for " + "Stock "
                    + clientStock.getQuotedName());
            }

            // wait the lead time if necessary
            if (leadTime != null) {
                double leadDuration = leadTime.sample().doubleValue();

                // check lead duration non-negative
                if (leadDuration < 0) {

                    sendWarning(
                        "Lead duration distribution sample is negative (" + leadDuration + "). Assuming"
                            + " immediate delivery instead (i.e. duration 0).",
                        "RestockProcessMS : "
                            + getName()
                            + " lifeCycle()",
                        "The given lead time distribution " + leadTime.getName()
                            + " has returned a negative sample.",
                        "Make sure to use a non-negativ lead time distribution."
                            + " Distributions potentially yielding negative values"
                            + " (like Normal distributions) should bet set to non-negative.");

                    // set lead duration to 0
                    leadDuration = 0;
                }

                hold(new TimeSpan(leadDuration));
            }

            // store the ordered quantity in the Stock
            clientStock.store(orderQuantity);
        } // end if

    }

    /**
     * Informs the RestockProcessMS every time the inventory level of the Stock changes. If the inventory level of the
     * Stock drops below the safety stock level this RestockProcessMS must be woken up to place an order.
     *
     * @param evt java.beans.PropertyChangeEvent : Informing the event about an inventory level change of the Stock.
     */
    public void propertyChange(PropertyChangeEvent evt) {

        // if this RestockProcessMS is scheduled an order is already on its way
        // only if this RestockProcessMS is not scheduled yet it must be woken
        // up
        if (!isScheduled()) {
            // if safety stock level is reached place a new order
            if (clientStock.getAvail() <= safetyStockLevel) {
                activateAfter(currentSimProcess());
            }
        }

    }
}