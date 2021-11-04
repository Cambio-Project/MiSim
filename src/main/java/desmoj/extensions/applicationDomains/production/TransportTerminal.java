package desmoj.extensions.applicationDomains.production;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.dist.NumericalDist;
import desmoj.core.simulator.Condition;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.ModelComponent;
import desmoj.core.simulator.QueueBased;
import desmoj.core.simulator.SimProcess;

/**
 * A TransportTerminal is a place where a number of transporters (default are
 * <code>SimpleTransporter</code>s) are waiting for goods to transport them
 * around. The <code>SimpleTransporter</code> s are generated automatically when a TransportTerminal is initialized. It
 * is intended as an easy to use interface (facade) for the user and to hide the different classes and their more or
 * less complex interaction from the user, he normally has to set up when he wants to model transportation in his
 * model.
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
 * @see SimpleTransporter
 */
public class TransportTerminal extends ModelComponent {

    /**
     * The number of transporters which belong to this termminal.
     */
    private final int numOfTransporter;

    /**
     * The minimum number of goods to be transported by one transporter.
     */
    private int minLoad = 1;

    /**
     * The maximum number of goods which can be transported by one transporter.
     */
    private int capacity = 1;

    /**
     * The maximum number of transporters in the waiting-queue.
     */
    private final int transporterQCapac;

    /**
     * The random number stream determining the time it takes to transport the goods.
     */
    private final NumericalDist<?> transportTimeStream;

    /**
     * The random number stream determining the time it takes the transporters to return to their TransportTerminal.
     */
    private final NumericalDist<?> returnTimeStream;

    /**
     * The <code>TransportJunction</code> terminal the transporters are associated to. From there they start and to
     * that
     * <code>TransportJunction</code> they return.
     */
    private final TransportJunction homeTerminal;

    /**
     * The <code>Transportation</code> the transporters are performing. That is the joint cooperation of one transporter
     * and the goods (
     * <code>SimProcess</code> es) he is carrying.
     */
    private final Transportation transportation;

    /**
     * Constructor for a simple TransportTerminal where a given number of
     * <code>SimpleTransporter</code> s wait for goods to carry them to their
     * destination. The kind of transportation they perform can be determined by the user. The class implementing this
     * special transportation can be provided as a parameter. The sort order and the capacity of the queues for the
     * transporters and the goods can be chosen, too.
     *
     * @param owner               desmoj.Model : The model this TransportTerminal is associated to.
     * @param name                java.lang.String : The name of this TransportTerminal.
     * @param numOfTransp         int : The number of transporters belonging to this TransportTerminal.
     * @param capac               int : The maximum number of goods the transporters can carry around.
     * @param minLoad             int : The minimum number of goods the transporters will carry around.
     * @param transportTimeStream NumericalDist<?> : The random number stream determining the time it takes to transport
     *                            the goods.
     * @param returnTime          NumericalDist<?> : The time it takes the transporters to return to their
     *                            TransportTerminal after carrying the goods to their destination.
     * @param mSortOrder          int : The sort order of the transporters waiting-queue. Choose a constant from
     *                            <code>QueueBased</code> like
     *                            <code>QueueBased.FIFO</code> or <code>QueueBased.LIFO</code>
     *                            or ...
     * @param mQCapacity          int : The capacity of the transporters waiting-queue, that is how many transporters
     *                            can be enqueued. Zero (0) means unlimited capacity.
     * @param sSortOrder          int : The sort order of the goods waiting-queue. Choose a constant from
     *                            <code>QueueBased</code> like
     *                            <code>QueueBased.FIFO</code> or <code>QueueBased.LIFO</code>
     *                            or ...
     * @param sQCapacity          int : The capacity of the goods waiting-queue, that is how many goods processes can be
     *                            enqueued. Zero (0) means unlimited capacity.
     * @param transportation      Transportation : The type of transportation to be performed by the transporters.
     * @param showInReport        boolean : Flag, if this TransportTerminal should produce a report or not.
     * @param showInTrace         boolean : Flag, if trace messages of this TransportTerminal should be displayed in the
     *                            trace file.
     */
    public TransportTerminal(Model owner, String name, int numOfTransp,
                             int capac, int minLoad, NumericalDist<?> transportTimeStream,
                             NumericalDist<?> returnTime, int mSortOrder, int mQCapacity,
                             int sSortOrder, int sQCapacity, Transportation transportation,
                             boolean showInReport, boolean showInTrace) {

        super(owner, name, showInTrace); // make a ModelComponent

        // check the parameters

        // check the number of transporters
        if (numOfTransp < 1) {
            sendWarning(
                "The number of transporters belonging to the Transport"
                    + "Terminal being constructed is zero or negative. One transporter will "
                    + "be created!",
                getClass().getName()
                    + ": "
                    + getQuotedName()
                    + ", Constructor: "
                    + "TransportTerminal(Model owner, String name, int numOfTransp, "
                    + "int capac, int minLoad, RealDist transportTimeStream, "
                    + "RealDist returnTime, int mSortOrder, int mQCapacity,	int sSortOrder, "
                    + "int sQCapacity, Transportation transportation,	boolean showInReport, "
                    + "boolean showInTrace)",
                "A TransportTerminal with no transporters does not make sense.",
                "Make sure to create at least one transporter for a TransportTerminal.");

            this.numOfTransporter = 1; // make at least one transporter
        } else {
            this.numOfTransporter = numOfTransp;
        }

        // check the capacity parameter
        if (capac < 1) {
            sendWarning(
                "The given capacity of a transporter is zero or negative. "
                    + "The capacity will be set to one!",
                getClass().getName()
                    + ": "
                    + getQuotedName()
                    + ", Constructor: TransportTerminal(Model owner, String name, "
                    + "int numOfTransp, int capac, int minLoad, "
                    + "RealDist transportTimeStream, RealDist returnTime, "
                    + "int mSortOrder, int mQCapacity,	int sSortOrder, "
                    + "int sQCapacity, Transportation transportation,	"
                    + "boolean showInReport, boolean showInTrace)",
                "A transporters' capacity which is zero or negative does not "
                    + "make sense.",
                "Make sure to provide a valid positive capacity "
                    + "for the Transporter to be constructed.");
            // set the capacity to one
            this.capacity = 1;
        } else {
            this.capacity = capac;
        }

        // check the minimum load parameter
        if (minLoad < 1) {
            sendWarning(
                "The given minimum load of a transporter is zero or "
                    + "negative. The minimum load will be set to one!",
                getClass().getName()
                    + ": "
                    + getQuotedName()
                    + ", Constructor: TransportTerminal(Model owner, String name, "
                    + "int numOfTransp, int capac, int minLoad, "
                    + "RealDist transportTimeStream, RealDist returnTime, "
                    + "int mSortOrder, int mQCapacity,	int sSortOrder, "
                    + "int sQCapacity, Transportation transportation,	"
                    + "boolean showInReport, boolean showInTrace)",
                "A minimum load which is zero or negative does not make sense.",
                "Make sure to provide a valid positive minimum load "
                    + "for the Transporter to be constructed.");
            // set the minimum load to one
            this.minLoad = 1;
        } else {
            this.minLoad = minLoad;
        }

        // check if the capacity of the transporter queue does make sense
        if (0 < mQCapacity && mQCapacity < numOfTransporter) {
            sendWarning(
                "The given capacity of the transporter queue is less than "
                    + "the number of transporters belonging to this Transport"
                    + "Terminal. The capacity of the queue will be set to the "
                    + "number of transporters!",
                getClass().getName()
                    + ": "
                    + getQuotedName()
                    + ", Constructor: TransportTerminal(Model owner, String name, "
                    + "int numOfTransp, int capac, int minLoad, "
                    + "RealDist transportTimeStream, RealDist returnTime, "
                    + "int mSortOrder, int mQCapacity,	int sSortOrder, "
                    + "int sQCapacity, Transportation transportation,	"
                    + "boolean showInReport, boolean showInTrace)",
                "It does make no sense if the queue can not hold all the "
                    + "transporters.",
                "Make sure to provide a valid positive capacity for the queue "
                    + "which can hold all the transporters.");
            // set the capacity to the number of transporters
            this.transporterQCapac = numOfTransporter;
        } else {
            this.transporterQCapac = mQCapacity;
        }

        // save the random number streams for the transport and return time
        this.transportTimeStream = transportTimeStream;
        this.returnTimeStream = returnTime;

        // save the kind of transportation to be performed
        this.transportation = transportation;

        // make the TransportJunction home base
        this.homeTerminal = new TransportJunction(owner, name, mSortOrder,
            transporterQCapac, sSortOrder, sQCapacity, showInReport,
            showInTrace);

        // make the specified number of SimpleTransporters
        for (int i = 0; i < numOfTransporter; i++) {
            SimpleTransporter smpTrans = new SimpleTransporter(owner,
                "smplTransporter", minLoad, capacity, transportation,
                homeTerminal, returnTimeStream, showInTrace);
            smpTrans.activate();
        }

    }

    /**
     * Constructor for a simple TransportTerminal where a given number of
     * <code>SimpleTransporter</code> s wait for goods to carry them to their
     * destination. The <code>SimpleTransporter</code> s all have a minimum load of one (1) and they perform just a
     * simple transportation. That is, they pick up the goods carry them to their destination and release them there.
     * The queues for the transporters and the goods have a FIFO sort order and no capacity limit. The following
     * parameters must be specified by the user.
     *
     * @param owner               desmoj.Model : The model this TransportTerminal is associated to.
     * @param name                java.lang.String : The name of this TransportTerminal.
     * @param numOfTransp         int : The number of SimpleTransporters belonging to this TransportTerminal.
     * @param capac               int : The maximum number of goods the SimpleTransporters can carry around.
     * @param transportTimeStream NumericalDist<?> : The random number stream determining the time it takes to transport
     *                            the goods.
     * @param returnTime          NumericalDist<?> : The time it takes the SimpleTransporters to return to their
     *                            TransportTerminal after carrying the goods to their destination.
     * @param showInReport        boolean : Flag, if this TransportTerminal should produce a report or not.
     * @param showInTrace         boolean : Flag, if trace messages of this TransportTerminal should be displayed in the
     *                            trace file.
     */
    public TransportTerminal(Model owner, String name, int numOfTransp,
                             int capac, NumericalDist<?> transportTimeStream, NumericalDist<?> returnTime,
                             boolean showInReport, boolean showInTrace) {
        // create a TransportTerminal with the given parameters and ...
        this(owner, name, numOfTransp, capac, 1, // a minimum load of one for
            // the transporters
            transportTimeStream, returnTime, QueueBased.FIFO, // a FIFO
            // sort
            // order of
            // the
            // transporters
            0, // unlimited capacity of the transporters queue
            QueueBased.FIFO, // a FIFO sort order of the goods
            0, // unlimited capacity of the goods' queue
            // the transportation is just a simple Transportation created
            // on-the-fly
            new Transportation(owner, name + "transportation",
                transportTimeStream, showInTrace), showInReport,
            showInTrace);
    }

    /**
     * Returns an array of available slave SimProcesses which comply to a given condition at this moment.
     *
     * @param cond desmoj.Condition : The condition to which the sim-processes we are looking for must comply.
     * @return desmoj.SimProcess[] : The array of available slave SimProcesses which comply to the given condition. If
     *     no suitable SimProcess is available <code>null</code> will be returned.
     */
    public synchronized SimProcess[] availableSet(Condition cond) {

        // return that array from the home base TransportJunction
        return homeTerminal.availableSet(cond);
    }

    /**
     * Returns a transporter process waiting in the transporter (master) queue complying to the given condition. If
     * there is no such transporter waiting
     * <code>null</code> is returned. This method is passed on to the
     * underlying <code>TransportJunction</code>.
     *
     * @param cond Condition : The Condition <code>cond</code> is describing the condition to which the trasnporter must
     *             comply to. This has to be implemented by the user in the class:
     *             <code>Condition</code> in the method: <code>check()</code>.
     * @return Transporter : Returns the first transporter in the master queue which complies to the given condition.
     */
    public Transporter availTransporter(Condition cond) {
        // return the suitable transporter from the underlying TransportJunction
        return homeTerminal.availTransporter(cond);
    } // end method

    /**
     * Returns the underlying <code>TransportJunction</code>. That is the home base of all the transporters associated
     * to this TransportTerminal.
     *
     * @return desmoj.TransportJunction : The home base of the transporters associated to this TransportTerminal.
     */
    public TransportJunction getTransportJunction() {

        return homeTerminal;
    }

    /**
     * This method is to be called from a <code>Transporter</code> which wants to transport goods. If not enough
     * suitable goods (slave processes) are available at the moment, the transporter process will be stored in a
     * waiting-queue, until enough suitable slaves are available. If the capacity limit of the queue is reached, the
     * transporter will not be enqueued and <code>false</code> will be returned. When enough suitable slaves are
     * available their <code>cooperate</code> method (in the class
     * <code>SimProcess</code>) will be called. During the transportation
     * process the transporter process is the only active one. The slave processes are passive and will be reactivated
     * after the transportation is done. This method is passed on to the underlying
     * <code>TransportJunction</code>.
     *
     * @param transport Transportation : The transportation process transport is describing the joint action of the
     *                  processes. The transport to be carried out has to be implemented by the user in the class:
     *                  <code>Transportation</code> in the method:
     *                  <code>cooperation()</code>.
     * @return boolean : Is <code>true</code> if the transporter can be enqueued successfully, <code>false</code>
     *     otherwise (i.e. capacity limit of the transporter queue is reached).
     */
    public boolean transport(Transportation transport) throws SuspendExecution {
        // call the same method from the TransportJunction home terminal
        return homeTerminal.transport(transport);
    }

    /**
     * This method is to be called from a <code>Transporter</code> who wants to transport goods which comply to a
     * certain condition. The condition must be specified in the method <code>check()</code> in a class derived from
     * <code>Condition</code>. If not enough suitable goods (slave processes) are available at the moment, the
     * transporter process will be stored in the waiting-queue, until enough suitable slaves are available. If the
     * capacity limit of the queue is reached, the transporter will not be enqueued and <code>false</code> returned.
     * When enough suitable slaves are available their <code>cooperate</code> method (in the class
     * <code>SimProcess</code>) will be called. During the transportation
     * process the transporter process is the only active one. The slave process is passive and will be reactivated
     * after the transportation is done. This method is passed on to the underlying <code>TransportJunction</code>.
     *
     * @param transport Transportation : The transportation process transport is describing the joint action of the
     *                  processes. The transport to be carried out has to be implemented by the user in the class:
     *                  <code>Transportation</code> in the method:
     *                  <code>cooperation()</code>.
     * @param cond      Condition : The Condition <code>cond</code> is describing the condition to which the slave
     *                  process must comply. This has to be implemented by the user in the class:
     *                  <code>Condition</code> in the method: <code>check()</code>.
     * @return boolean : Is <code>true</code> if the transporter can be enqueued successfully, <code>false</code>
     *     otherwise (i.e. capacity limit of the master queue is reached).
     */
    public boolean transport(Transportation transport, Condition cond) throws SuspendExecution {
        // call the same method from the TransportJunction home terminal
        return homeTerminal.transport(transport, cond);
    }

    /**
     * This method is called from a sim-process which wants to be transported as a slave. If no suitable master process
     * (transporter) is available at the moment, the slave process will be stored in the slave queue, until a suitable
     * transporter is available. If the capacity limit of the slave queue is reached, the process will not be enqueued
     * and <code>false</code> will be returned. During the cooperation the master process is the only active one. The
     * slave process is passive and will be reactivated after the cooperation is done. This method is passed on to the
     * underlying
     * <code>TransportJunction</code>.
     *
     * @return boolean : Is <code>true</code> if the process requesting the transportation has been transported
     *     successfully to his destination, <code>false</code> otherwise (i.e. capacity limit of the slave queue is
     *     reached).
     */
    public boolean waitOnTransport() // wait() is a final method in
    // java.lang.Object
        throws SuspendExecution {
        // call the same method from the TransportJunction home terminal
        return homeTerminal.waitOnTransport();
    }
}