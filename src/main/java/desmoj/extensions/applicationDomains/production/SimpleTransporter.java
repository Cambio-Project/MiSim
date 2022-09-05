package desmoj.extensions.applicationDomains.production;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.dist.NumericalDist;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;

/**
 * A SimpleTransporter is a simple transporter (vehicle) associated to a
 * <code>TransportJunction</code>. There it waits for goods to transport
 * them. The goods are transported for a certain time and than released. The SimpleTransporter returns to his
 * <code>TranspsortJunction</code> where it waits again for some other goods to transport. The time it takes the
 * transporter to return to its <code>TransportJunction</code> must be specified in the parameter
 * <code>returnTime</code>. It must be some kind of <code>desmoj.core.dist.NumericalDist</code> random number stream.
 * The SimpleTransporter has a certain capacity (maximum number of goods which can be carried around at once) and a
 * minimum load (minimum number of goods which will be carried). The minimum load is one (1) unless something different
 * is specified. The SimpleTransporters lifeCycle is kept simple, as it only waits in a <code>TransportJunction</code>
 * for goods to be transported. Than, after having transported the goods it returns to its
 * <code>TransportJunction</code>. The user can overwrite the method
 * <code>lifeCycle()</code> in a subclass in order to implement a different
 * behavior.
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
 * @see Transporter
 */
public class SimpleTransporter extends Transporter {

    /**
     * The random number stream determining the time it takes this SimpleTransporter to return to its
     * <code>TransportJunction</code>.
     */
    private NumericalDist<?> returnTimeStream;

    /**
     * The <code>TransportJunction</code> this SimpleTransporter is associated to. From there it starts and to that
     * <code>TransportJunction</code> it returns.
     */
    private TransportJunction homeBase;

    /**
     * The <code>Transportation</code> this SimpleTransporter is performing. That is the joint cooperation of this
     * SimpleTransporter and the goods (
     * <code>SimProcess</code> es).
     */
    private Transportation transportation;

    /**
     * Constructs a SimpleTransporter which will carry around goods in a manufacturing system, from and to a certain
     * <code>TransportJunction</code>. A SimpleTransporter is associated to a
     * certain <code>TransportJunction</code> as his home base. He will perform the transportation of the goods as
     * described in the Transportation object. He needs a certain time to return to his home base and has a capacity
     * (maximum number of goods which can be transported) and a mimimum load (a minimum number of goods it will carry).
     * Use this constructor to construct a Transporter with a certain minimum load, a specified capacity, a given
     * transportation order, a certain home base (
     * <code>TransportJunction</code>) and a given return time it takes him
     * to return to his base. The minimum load and the capacity must not be zero or negative.
     *
     * @param owner       desmoj.Model : The model this SimpleTransporter is associated to.
     * @param name        java.lang.String : The name of this SimpleTransporter.
     * @param minLoad     int : The minimum number of goods this SimpleTransporter will carry around.
     * @param capac       int : The maximum number of goods this SimpleTransporter can carry around.
     * @param transport   Transportation : The transportation to be carried out by this SimpleTransporter.
     * @param homeBase    TransportJunction : The home base of this SimpleTransporter; where he comes from and where he
     *                    returns to.
     * @param returnTime  NumericalDist<?> : The time it takes the SimpleTransporter to return to his home base after
     *                    transporting the goods to a certain place.
     * @param showInTrace boolean : Flag, if this SimpleTransporter should produce a trace output or not.
     */
    public SimpleTransporter(Model owner, String name, int minLoad, int capac,
                             Transportation transport, TransportJunction homeBase,
                             NumericalDist<?> returnTime, boolean showInTrace) {
        super(owner, name, minLoad, capac, showInTrace); // make a
        // Transporter
        // the minLoad and the capacity parameter will be checked there

        // check the transport parameter
        if (transport == null) {
            sendWarning(
                "The given Transportation this SimpleTransporter should carry "
                    + "out is only a null pointer. The SimpleTransporter can not "
                    + "be constructed!",
                getClass().getName()
                    + ": "
                    + getQuotedName()
                    + ", Constructor: SimpleTransporter(Model owner, String name, "
                    + "int minLoad, int capac, Transportation transport, "
                    + "TransportJunction homeBase, RealDist returnTime, "
                    + "boolean showInTrace) ",
                "A SimpleTransporter needs a reference to a Transportation "
                    + "he is supposed to carry out.",
                "Make sure to provide a valid Transportation object for the "
                    + "SimpleTransporter to carry out.");

            return; // ignore that rubbish and just return
        } else {
            this.transportation = transport;
        }

        // check the home base parameter
        if (homeBase == null) {
            sendWarning(
                "The given TransportJunction homeBase for a SimpleTransporter"
                    + " is only a null pointer. The SimpleTransporter can not be "
                    + "constructed!",
                getClass().getName()
                    + ": "
                    + getQuotedName()
                    + ", Constructor: SimpleTransporter(Model owner, String name, "
                    + "int minLoad, int capac, Transportation transport, "
                    + "TransportJunction homeBase, RealDist returnTime, "
                    + "boolean showInTrace) ",
                "A SimpleTransporter needs a certain TransportJunction as "
                    + "its home base.",
                "Make sure to provide a valid TransportJunction as a home "
                    + "base for the SimpleTransporter to be constructed.");

            return; // ignore that rubbish and just return
        } else {
            this.homeBase = homeBase;
        }

        // check the return time stream parameter
        if (returnTime == null) {
            sendWarning(
                "The given return time for a SimpleTransporter is only a "
                    + "null pointer. The return time will be set to zero!",
                getClass().getName()
                    + ": "
                    + getQuotedName()
                    + ", Constructor: SimpleTransporter(Model owner, String name, "
                    + "int minLoad, int capac, Transportation transport, "
                    + "TransportJunction homeBase, RealDist returnTime, "
                    + "boolean showInTrace) ",
                "A SimpleTransporter needs a certain time to return to his "
                    + "home TransportJunction.",
                "Make sure to provide a valid desmoj.dist.RealDist* random "
                    + "number stream as the return time for the SimpleTransporter "
                    + "to be constructed.");
            // set the return time to zero
            this.returnTimeStream = new desmoj.core.dist.DiscreteDistConstant<Double>(owner,
                "simpleTransReturnTime", 0.0, true, false);
        } else {
            this.returnTimeStream = returnTime;
        }
    }

    /**
     * Constructs a SimpleTransporter which will carry around goods in a manufacturing system, from and to a certain
     * <code>TransportJunction</code>. A SimpleTransporter is associated to a
     * certain <code>TransportJunction</code> as his home base. He will perform the transportation of the goods as
     * described in the Transportation object. He needs a certain time to return to his home base and has a capacity
     * (maximum number of goods which can be transported). The mimimum load (a minimum number of goods it will carry) is
     * one. Use this constructor to construct a Transporter with a specified capacity, a given transportation order, a
     * certain home base (
     * <code>TransportJunction</code>), a given return time it takes him to
     * return to his base and a minimum load of one.
     *
     * @param owner       desmoj.Model : The model this SimpleTransporter is associated to.
     * @param name        java.lang.String : The name of this SimpleTransporter.
     * @param capac       int : The maximum number of goods this SimpleTransporter can carry around.
     * @param transport   Transportation : The transportation to be carried out by this SimpleTransporter.
     * @param homeBase    TransportJunction : The home base of this SimpleTransporter; where he comes from and where he
     *                    returns to.
     * @param returnTime  NumericalDist<?> : The time it takes the SimpleTransporter to return to his home base after
     *                    transporting the goods to a certain place.
     * @param showInTrace boolean : Flag, if this SimpleTransporter should produce a trace output or not.
     */
    public SimpleTransporter(Model owner, String name, int capac,
                             Transportation transport, TransportJunction homeBase,
                             NumericalDist<?> returnTime, boolean showInTrace) {
        // construct a SimpleTransporter with a minimum load of one
        this(owner, name, 1, capac, transport, homeBase, returnTime,
            showInTrace);
    }

    /**
     * Returns the home base <code>TransportJunction</code> this SimpleTransporter is associated to.
     *
     * @return desmoj.TransportJunction : The home base
     *     <code>TransportJunction</code> this SimpleTransporter is
     *     associated to.
     */
    public TransportJunction getHomeBase() {

        return this.homeBase;
    }

    /**
     * Sets a new <code>TransportJunction</code> as the home base this SimpleTransporter is associated to. Must not be
     * <code>null</code>!
     *
     * @param newHomeBase desmoj.TransportJunction : The new
     *                    <code>TransportJunction</code> as the home base of this
     *                    SimpleTransporter.
     */
    public void setHomeBase(TransportJunction newHomeBase) {

        // check the home base parameter
        if (newHomeBase == null) {
            sendWarning(
                "The given TransportJunction homeBase for a SimpleTransporter"
                    + " is only a null pointer. The attempted action is ignored!",
                getClass().getName()
                    + ": "
                    + getQuotedName()
                    + ", Mehtod: void setHomeBase(TransportJunction newHomeBase)",
                "A SimpleTransporter needs a certain TransportJunction as "
                    + "its home base.",
                "Make sure to provide a valid TransportJunction as a home "
                    + "base for this SimpleTransporter.");

            return; // ignore that rubbish and just return
        } else {
            this.homeBase = newHomeBase;
        }

    }

    /**
     * Returns a <code>TimeSpan</code> object representing the time it takes the SimpleTransporter to return to his home
     * base (
     * <code>TransportJunction</code>) after having transported the goods to
     * some place. The time is taken from the given random number stream returnTimeStream.
     *
     * @return TimeSpan : The time it takes the SimpleTransporter to return to his home base
     *     (<code>TransportJunction</code>) after having transported the goods to some place.
     */
    public TimeSpan getReturnTimeSample() {

        return returnTimeStream.sampleTimeSpan();
    }

    /**
     * Returns the <code>Transportation</code> order this SimpleTransporter is supposed to execute.
     *
     * @return desmoj.Transportation : The <code>Transportation</code> order this SimpleTransporter is supposed to
     *     execute.
     */
    public Transportation getTransportation() {

        return this.transportation;
    }

    /**
     * Sets a new <code>Transportation</code> order this SimpleTransporter is supposed to carry out. Must not be
     * <code>null</code>!
     *
     * @param newTransportation desmoj.Transportation : The new <code>Transportation</code> order this SimpleTransporter
     *                          is supposed to carry out.
     */
    public void setTransportation(Transportation newTransportation) {

        // check the home base parameter
        if (newTransportation == null) {
            sendWarning(
                "The given Transportation this SimpleTransporter should carry "
                    + "out is only a null pointer. The attempted action is ignored!",
                getClass().getName()
                    + ": "
                    + getQuotedName()
                    + ", Mehtod: void setTransportation(Transportation "
                    + "newTransportation)",
                "A SimpleTransporter needs a reference to a Transportation "
                    + "he is supposed to carry out.",
                "Make sure to provide a valid Transportation object for the "
                    + "SimpleTransporter to carry out.");

            return; // ignore that rubbish and just return
        } else {
            this.transportation = newTransportation;
        }

    }

    /**
     * This SimpleTransporter has a very simple lifeCycle. He waits in his home base (<code>TransportJunction</code>)
     * for goods to transport. As soon as goods arrive at the <code>TransportJunction</code> they will be transported to
     * their destination and the SimpleTransporter returns to his home base.
     */
    public void lifeCycle() throws SuspendExecution {
        // neverending cycle of transportation work
        while (true) {
            // wait in home base for goods to transport
            homeBase.transport(transportation);

            // transportation will be carried out as described in transportation

            // return to home base
            hold(getReturnTimeSample());
        }

    }
}