package desmoj.extensions.applicationDomains.production;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.advancedModellingFeatures.ProcessCoop;
import desmoj.core.dist.NumericalDist;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.SimProcess;
import desmoj.core.simulator.TimeSpan;

/**
 * Processing is the object representing the processing process taking place at a <code>WorkStation</code> where a
 * <code>Worker</code> process or a
 * <code>MachineProcess</code> are processing some parts (products) to
 * manufacture some new parts (or products). The kind and the number of parts being processed are determined by a
 * <code>PartsList</code>. The parts (or products) are represented by <code>SimProcess</code> es. The
 * <code>Worker</code> and the <code>MachineProcess</code> are also some
 * kind of <code>SimProcess</code> es. But one <code>Worker</code> or
 * <code>MachineProcess</code> is the master process and the other processes
 * are all slave processes. It is intended that that this Processing is used with the <code>WorkStation</code>
 * construct, where a master process (
 * <code>Worker</code> or <code>MachineProcess</code>) is waiting for parts
 * or products (slave processes) to process them. During the Processing the master process is active and the slave
 * processes are passive. The processing taking place at the <code>WorkStation</code> is described in the method
 * <code>cooperation</code>, which should be overwritten by the user to model
 * the manufacturing process taking place at the <code>WorkStation</code>. Until now this method only models the time it
 * takes to process the parts in some way. Note: when using the <code>WorkStation</code> construct the master (Worker or
 * MachineProcess) will be activated after the processing is done and the slave processes will be activated after the
 * master (if they have not been activated during the processing process already).
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
 * @see WorkStation
 */
public class Processing extends ProcessCoop {

    /**
     * The random number stream determining the time it takes to process the parts.
     */
    private NumericalDist<?> processingTimeStream;

    /**
     * Constructs a Processing process where a master (<code>Worker</code> or
     * <code>MachineProcess</code>) is processing slaves (
     * <code>SimPorcess</code> es) in a cooperate manufacturing process. The
     * time it takes to process the parts is determined by the specified processTimeStream.
     *
     * @param owner             desmoj.Model : The model this Processing is associated to.
     * @param name              java.lang.String : The name of this Processing.
     * @param processTimeStream NumericalDist<?> : The random number stream determining the time it takes to process the
     *                          parts.
     * @param showInTrace       boolean : Flag, if this Processing should produce a trace output or not.
     */
    public Processing(Model owner, String name, NumericalDist<?> processTimeStream,
                      boolean showInTrace) {

        super(owner, name, showInTrace); // make a ProcessCoop

        this.processingTimeStream = processTimeStream;
    }

    /**
     * This method describes the Processing process carried out by the master process (some kind of <code>Worker</code>
     * or
     * <code>MachineProcess</code>). In this simple case only the time it
     * takes to process the parts is taken into consideration. If the user building the model has to implement a more
     * complex behavior, he has to overwrite this method in a subclass. The time it takes to process the parts is
     * obtained from the method <code>getProcessingTimeSample()</code>.
     *
     * @param master SimProcess : The master process which really carries out the cooperation (and processes the slave
     *               parts). Should be a subclass of <code>Worker</code> or
     *               <code>MachineProcess</code>.
     * @param slaves SimProcess[] : The slave processes (parts) which are lead through the process cooperation by the
     *               master.
     */
    protected void cooperation(SimProcess master, SimProcess[] slaves) throws SuspendExecution {
        /*
         * The master has not to be a Worker or MachineProcess. Although in most
         * cases this would make the model more easy to understand and to build.
         * If for one reason the master is not a Worker or MachineProcess the
         * following warning (meant as a hint) can get quite annoying and fill
         * up the error and warnings file. So maybe we better leave this warning
         * ....
         */
        // check if the master is a Worker or MachineProcess
        if (!((master instanceof Worker) || (master instanceof MachineProcess))) {
            sendWarning(
                "The given master process for a processing process is "
                    + "not a Worker or MachineProcess. The process will be carried "
                    + "out anyway!",
                "Processing : "
                    + getName()
                    + " Method: void "
                    + "cooperation (SimProcess master, SimProcess[] slave)",
                "The given master process is not a Worker or MachineProcess.",
                "It is recommended to use a Worker or MachineProcess as a "
                    + "master to perform a processing process");
        }

        // hold for the time the goods are processed
        hold(getProcessingTimeSample()); // that's all for the moment

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
        cooperation(master, wrapArray);

    }

    /**
     * Returns a <code>SimTime</code> object representing the time it takes to process the parts with the worker or
     * machine. The time is taken from the given random number stream processingTimeStream.
     *
     * @return desmoj.SimTime : The time it takes to process the parts with the worker or machineProcess.
     */
    protected TimeSpan getProcessingTimeSample() {

        return processingTimeStream.sampleTimeSpan();
    }

    /**
     * Sets the processingTimeStream to a new <code>RealDist</code> random number stream.
     *
     * @param newProcessingTimeStream NumericalDist<?> : The new <code>RealDist</code> random number stream determining
     *                                the time it takes to process the parts.
     */
    public void setProcessingTimeStream(NumericalDist<?> newProcessingTimeStream) {

        this.processingTimeStream = newProcessingTimeStream;
    }
}