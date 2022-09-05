package desmoj.extensions.applicationDomains.production;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.advancedModellingFeatures.ProcessCoop;
import desmoj.core.dist.NumericalDist;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.SimProcess;
import desmoj.core.simulator.TimeSpan;

/**
 * Transportation is the object representing the process cooperation between a
 * <code>Transporter</code> process and the goods (products represented by
 * Sim-processes) he is transporting. It is intended that that this Transportation is used with the
 * <code>TransportJunction</code> construct, where a <code>Transporter</code>( as a master process) is waiting for goods
 * (slave processes) to carry them around in the manufacturing system. During the Transportation the master is active
 * and the slaves are passive. The transportation carried out together is described in the method
 * <code>cooperation</code>, which can be overwritten by the user to build
 * more complex models. Until now this method only models the time it takes to transport the goods. Note: when using the
 * <code>TransportJunction</code> construct the master (Transporter) will be activated after the transportation is done
 * and the slaves will be activated after the master (if they have not been activated during the transportation
 * already).
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
 * @see TransportJunction
 */
public class Transportation extends ProcessCoop {

    /**
     * The random number stream determining the time it takes to transport the goods.
     */
    private NumericalDist<?> transportTimeStream;

    /**
     * Constructs a Transportation process where a master (
     * <code>Transporter</code>) is transporting slaves (
     * <code>SimPorcess</code> es) in a cooperate process. The time it takes
     * to transport the goods is determined by the specified transportTimeStream.
     *
     * @param owner               desmoj.Model : The model this Transportation is associated to.
     * @param name                java.lang.String : the name of this Transportation.
     * @param transportTimeStream NumericalDist<?> : The random number stream determining the time it takes to transport
     *                            the goods.
     * @param showInTrace         boolean : Flag, if this Transportation should produce a trace output or not.
     */
    public Transportation(Model owner, String name,
                          NumericalDist<?> transportTimeStream, boolean showInTrace) {
        super(owner, name, showInTrace); // make a ProcessCoop

        this.transportTimeStream = transportTimeStream;
    }


    /**
     * The <code>cooperation()</code> method with only one master and one slave is not needed here. So we pass it to the
     * more general method with an array of slaves as parameter.
     */
    protected void cooperation(SimProcess master, SimProcess slave) throws SuspendExecution {
        // make an array for the slave to be wrapped up
        SimProcess[] wrapArray = new SimProcess[1];

        wrapArray[0] = slave;

        // hand it over to the more general cooperation method
        transport(master, wrapArray);
    }

    /**
     * Returns a <code>TimeSpan</code> object representing the time it takes to transport the goods with the
     * transporter. The time is taken from the given random number stream transportTimeStream.
     *
     * @return TimeSpan : The time it takes to transport the goods with the transporter.
     */
    protected TimeSpan getTransportTimeSample() {

        return transportTimeStream.sampleTimeSpan();
    }

    /**
     * Sets the transportTimeStream to a new <code>RealDist</code> random number stream.
     *
     * @param newTransportTimeStream NumericalDist<?> : The new <code>RealDist</code> random number stream determining
     *                               the time it takes to transport the goods.
     */
    public void setTransportTimeStream(NumericalDist<?> newTransportTimeStream) {

        this.transportTimeStream = newTransportTimeStream;
    }

    /**
     * This method describes the transportation process carried out by the master process (some kind of
     * <code>Transporter</code>). In this simple case only the time it takes to transport the goods is taken into
     * consideration. If the user building the model has to implement a more complex behavior, he has to overwrite this
     * method in a subclass. The time it takes to transport the goods is obtained from the method
     * <code>getTransportTimeSample()</code>.
     *
     * @param master SimProcess : The master process which really carries out the cooperation. Should be a subclass of
     *               <code>Transporter</code>.
     * @param slaves SimProcess[] : The slave processes which are lead through the cooperation by the master.
     */
    protected void transport(SimProcess master, SimProcess[] slaves) throws SuspendExecution {
        // check if the master is a Transporter
        if (!(master instanceof Transporter)) {
            sendWarning(
                "The given master process for a transportation is "
                    + "not a Transporter. The transport will be carried out anyway!",
                "Transportation : "
                    + getName()
                    + " Method: void "
                    + "cooperation (SimProcess master, SimProcess[] slave)",
                "The given master process is not a Transporter.",
                "It is recommended to use a Transporter process as a master "
                    + "to carry out a transportation");
        }

        // hold for the time the goods are transported
        hold(getTransportTimeSample());

    }

    /**
     * The <code>transport()</code> method with only one master and one slave is not needed here. So we pass it to the
     * more general method with an array of slaves as parameter.
     */
    protected void transport(SimProcess master, SimProcess slave) throws SuspendExecution {
        // make an array for the slave to be wrapped up
        SimProcess[] wrapArray = new SimProcess[1];

        wrapArray[0] = slave;

        // hand it over to the more general cooperation method
        transport(master, wrapArray);
    }
}