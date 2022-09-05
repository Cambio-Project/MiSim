package desmoj.extensions.applicationDomains.production;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.advancedModellingFeatures.Stock;
import desmoj.core.dist.NumericalDist;
import desmoj.core.exception.DelayedInterruptException;
import desmoj.core.exception.InterruptException;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.SimProcess;
import desmoj.core.simulator.TimeSpan;

/**
 * RestockProcessQT is a process restocking a <code>Stock</code> with a fixed quantity (Q) of units on a periodic review
 * bases (fixed Time span = T).
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
public class RestockProcessQT extends SimProcess {

    /**
     * The fixed quantity of units the <code>Stock</code> will be replenished with.
     */
    private long orderQuantity;

    /**
     * The fixed time span after which the inventory will be reviewed and orders will be placed.
     */
    private TimeSpan reviewSpan;

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
     * Constructs a <code>RestockProcessQT</code> which restocks a client
     * <code>Stock</code> after a fixed time period with a fixed quantity of
     * units. The lead time (time gap between placement and receipt of an order) will be given as a real random number
     * distribution.
     *
     * @param owner       Model : The model this <code>RestockProcessQT</code> is associated to
     * @param name        java.lang.String : The name of the
     *                    <code>RestockProcessQT</code>
     * @param q           long : The quantity supplied to the Stock with every order.
     * @param t           TimeSpan : The time period between the inventory reviews (in this case also the placements of
     *                    the orders).
     * @param client      Stock : The <code>Stock</code> which will replenished.
     * @param lt          NumericalDist<?> : The lead time random number distribution to determine the time between
     *                    placement and receipt of an order. If <code>null</code> the lead time is zero.
     * @param showInTrace boolean : Flag for showing the <code>RestockProcessQT</code> in trace-files. Set it to
     *                    <code>true</code> if RestockProcessQT should show up in trace. Set it to
     *                    <code>false</code> if RestockProcessQT should not be shown
     *                    in trace.
     */
    public RestockProcessQT(Model owner, String name, long q, TimeSpan t,
                            Stock client, NumericalDist<?> lt, boolean showInTrace) {
        super(owner, name, true, showInTrace); // make a sim-process

        this.orderQuantity = q;
        this.reviewSpan = t;
        this.clientStock = client;
        this.leadTime = lt;

        // check the quantity parameter
        if (q <= 0) {
            sendWarning(
                "The given quantity parameter is negative or zero!"
                    + "The quantity will be set to one!",
                "RestockProcessQT : "
                    + getName()
                    + " Constructor: RestockProcessQT(Model owner, String name, "
                    + "long q, SimTime t, Stock client, RealDist lt, boolean "
                    + "showInTrace)",
                "A negative or zero quantity for an order does not make "
                    + "sense.",
                "Make sure to provide a valid positive integer number "
                    + "for the quantity of every order.");

            // set the quantity to at least one
            this.orderQuantity = 1; // better than nothing
        }

        // check the review period parameter
        if (t == null) {
            sendWarning(
                "The given review period parameter is only a null pointer!"
                    + "The review period will be set to 100!",
                "RestockProcessQT : "
                    + getName()
                    + " Constructor: RestockProcessQT(Model owner, String name, "
                    + "long q, SimTime t, Stock client, RealDist lt, boolean "
                    + "showInTrace)",
                "A non existing review period does not make sense.",
                "Make sure to provide a valid SimTime object as review "
                    + "period.");

            // set the review period to 100 (or 42 ?!?)
            this.reviewSpan = new TimeSpan(100); // better than nothing
        }

        // check the client Stock parameter
        if (client == null) {
            sendWarning(
                "The given client parameter is only a null pointer!",
                "RestockProcessQT : "
                    + getName()
                    + " Constructor: RestockProcessQT(Model owner, String name, "
                    + "long q, TimeSpan t, Stock client, RealDist lt, boolean "
                    + "showInTrace)",
                "The RestockProcessQT does not know which Stock to replenish "
                    + "and therefore is useless.",
                "Make sure to provide a valid Stock object which should "
                    + "be replenished by this RestockProcessQT.");

        }
    }

    /**
     * Constructs a <code>RestockProcessQT</code> which restocks a client
     * <code>Stock</code> after a fixed time period with a fixed quantity of
     * units. The lead time is zero.
     *
     * @param owner       Model : The model this <code>RestockProcessQT</code> is associated to
     * @param name        java.lang.String : The name of the
     *                    <code>RestockProcessQT</code>
     * @param q           long : The quantity supplied to the Stock with every order.
     * @param t           TimeSpan : The time period between the inventory reviews (in this case also the placements of
     *                    the orders).
     * @param client      Stock : The <code>Stock</code> which will replenished.
     * @param showInTrace boolean : Flag for showing the <code>RestockProcessQT</code> in trace-files. Set it to
     *                    <code>true</code> if RestockProcessQT should show up in trace. Set it to
     *                    <code>false</code> if RestockProcessQT should not be shown
     *                    in trace.
     */
    public RestockProcessQT(Model owner, String name, long q, TimeSpan t,
                            Stock client, boolean showInTrace) {
        super(owner, name, true, showInTrace); // make a sim-process

        this.orderQuantity = q;
        this.reviewSpan = t;
        this.clientStock = client;
        this.leadTime = null;

        // check the quantity parameter
        if (q <= 0) {
            sendWarning(
                "The given quantity parameter is negative or zero!"
                    + "The quantity will be set to one!",
                "RestockProcessQT : "
                    + getName()
                    + " Constructor: RestockProcessQT(Model owner, String name, "
                    + "long q, TimeSpan t, Stock client, boolean showInTrace)",
                "A negative or zero quantity for an order does not make "
                    + "sense.",
                "Make sure to provide a valid positive integer number "
                    + "for the quantity of every order.");

            // set the quantity to at least one
            this.orderQuantity = 1; // better than nothing
        }

        // check the review period parameter
        if (t == null) {
            sendWarning(
                "The given review period parameter is only a null pointer!"
                    + "The review period will be set to 100!",
                "RestockProcessQT : "
                    + getName()
                    + " Constructor: RestockProcessQT(Model owner, String name, "
                    + "long q, TimeSpan t, Stock client, boolean showInTrace)",
                "A non existing review period does not make sense.",
                "Make sure to provide a valid TimeSpan object as review "
                    + "period.");

            // set the review period to 100 (or 42 ?!?)
            this.reviewSpan = new TimeSpan(100); // better than nothing
        }

        // check the client Stock parameter
        if (client == null) {
            sendWarning(
                "The given client parameter is only a null pointer!",
                "RestockProcessQT : "
                    + getName()
                    + " Constructor: RestockProcessQT(Model owner, String name, "
                    + "long q, TimeSpan t, Stock client, boolean showInTrace)",
                "The RestockProcessQT does not know which Stock to replenish "
                    + "and therefore is useless.",
                "Make sure to provide a valid Stock object which should "
                    + "be replenished by this RestockProcessQT.");

        }
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
     * @param newLeadTime desmoj.dist.RealDist : The new real random number distribution determining the lead time.
     */
    public void setLeadTime(NumericalDist<Double> newLeadTime) {
        leadTime = newLeadTime;
    }

    /**
     * Returns the quantity (number of units) to be stored in the Stock.
     *
     * @return long : The Stock will be replenished with this number of units.
     */
    public long getOrderQuantity() {
        return orderQuantity;
    }

    /**
     * Sets the order quantity to a new value. Zero or negative values will be rejected.
     *
     * @param newOrderQuantity long : Choose a postitive value greater than zero as new order quantity.
     */
    public void setOrderQuantity(long newOrderQuantity) {

        if (newOrderQuantity <= 0) {
            sendWarning(
                "The given quantity parameter is negative or zero!"
                    + "The order quantity will remain unchanged!",
                "RestockProcessQT : "
                    + getName()
                    + " Method: void setOrderQuantity(long newOrderQuantity)",
                "A negative or zero quantity for an order does not make "
                    + "sense.",
                "Make sure to provide a valid positive integer number "
                    + "for the quantity of every order.");

            return; // do nothing, just return. ignore that rubbish
        }

        orderQuantity = newOrderQuantity;
    }

    /**
     * Returns the time span between every replenishment of the Stock.
     *
     * @return TimeSpan : The time (as a SimTime object) between every replenishment of the Stock.
     */
    public TimeSpan getReviewSpan() {
        return reviewSpan;
    }

    /**
     * Sets the review period to a new value.
     *
     * @param newReviewPeriod desmoj.SimTime : The new value for the review period.
     *                        <code>null</code> will be rejected.
     */
    public void setReviewSpan(TimeSpan newReviewSpan) {

        if (newReviewSpan == null) {
            sendWarning(
                "The given review period parameter is only a null pointer!"
                    + "The review period will remain unchanged!",
                "RestockProcessQT : "
                    + getName()
                    + " Method: void setReviewSpan(TimeSpan newReviewSpan)",
                "A null pointer or a time span with zero length does not "
                    + "make sense as a review period.",
                "Make sure to provide a valid TimeSpan object as parameter "
                    + "for the review period.");

            return; // do nothing, just return. ignore that rubbish
        }

        reviewSpan = newReviewSpan;
    }

    /**
     * The <code>RestockProcessQT</code> replenishes the associated
     * <code>Stock</code> with the given quantity (Q) every period (T).
     */
    public void lifeCycle() throws InterruptException, SuspendExecution {

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
                    "RestockProcessQT : "
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

        // wait until start of the next period
        hold(reviewSpan);
    }
}