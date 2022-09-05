package desmoj.extensions.applicationDomains.harbour;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.advancedModellingFeatures.ProcessCoop;
import desmoj.core.dist.NumericalDist;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.SimProcess;
import desmoj.core.simulator.TimeSpan;

/**
 * Loading is the object representing the process cooperation between a
 * <code>InternalTransporter</code> or <code>Crane</code> process and the
 * Truck or an Internal Transporter he is loading. It is intended that this Loading is used with the
 * <code>WaitQueue</code> construct, where a
 * <code>InernalTransporter</code> or <code>Crane</code>>( as a master
 * process) is waiting for a truck or an internal transporter to load it. During the Loading the master is active and
 * the slaves is passive. The unloading carried out together is described in the method <code>cooperation</code>, which
 * can be overwritten by the user to build more complex models. Until now this method only models the time it takes to
 * load the truck/ an internal transporter.
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
 * @see ProcessCoop
 */
public class Loading extends ProcessCoop {

    /**
     * The random number stream determining the time it takes to load a truck or an internal transporter.
     */
    private NumericalDist<?> loadTimeStream;

    /**
     * Constructs a Loading process where a master (
     * <code>InternalTransporter</code>) or <code>Crane</code> is loading
     * slave (<code>Truck</code> or <code>InternalTransporter</code>) in a cooperate process. The time it takes to load
     * the containers (goods) is determined by the specified loadTimeStream.
     *
     * @param owner          desmoj.Model : The model this Loading is associated to.
     * @param name           java.lang.String : the name of this Loading.
     * @param loadTimeStream NumericalDist<?> : The random number stream determining the time it takes to load the
     *                       containers.
     * @param showInTrace    boolean : Flag, if this Loading should produce a trace output or not.
     */
    public Loading(Model owner, String name, NumericalDist<?> loadTimeStream,
                   boolean showInTrace) {
        super(owner, name, showInTrace); // make a ProcessCoop

        this.loadTimeStream = loadTimeStream;
    }

    /**
     * This method describes the loading process carried out by the master process (some kind of
     * <code>InternalTransporter</code> or
     * <code>Crane</code>). In this simple case only the time it takes to
     * load the containers (goods) is taken into consideration. If the user building the model has to implement a more
     * complex behavior, he has to overwrite this method in a subclass. The time it takes to load the containers (goods)
     * is obtained from the method
     * <code>getLoadTimeSample()</code>.
     *
     * @param master SimProcess : The master process which really loads the cooperation. Should be a subclass of
     *               <code>InternalTransporter</code> or <code>Crane</code>.
     * @param slave  SimProcess : The slave process which is lead through the cooperation by the master. Should be a
     *               subclass of
     *               <code>InternalTransporter</code> or <code>Truck</code>.
     */
    protected void cooperation(SimProcess master, SimProcess slave) throws SuspendExecution {

        // check if the master is an Internal Transporter and the slave is a
        // Truck
        if ((master instanceof InternalTransporter) && (slave instanceof Truck)) {
            InternalTransporter t = (InternalTransporter) master;
            Truck truck = (Truck) slave;

            // pick down a container on the truck
            t.pickDown(this.getLoadTimeSample());
            truck.setNumberOfImportGoods(truck.getNumberOfImportGoods() - 1);

        } else {
            // check if the master is a Crane and the slave is an
            // InternalTransporter
            if ((master instanceof Crane)
                && (slave instanceof InternalTransporter)) {
                Crane c = (Crane) master;
                InternalTransporter t = (InternalTransporter) slave;

                // load the transporter until he is full and the crane still has
                // to load something
                while ((t.getCurrentCapacity() < t.getCapacity())
                    && (c.getNumToLoadUnits() > 0)) {
                    c.load(this.getLoadTimeSample());
                    t.setCurrentCapacity(t.getCurrentCapacity() + 1);
                    c.setNumToLoadUnits(c.getNumToLoadUnits() - 1);
                }
            } else {
                sendWarning(
                    "The given master or slave  process  for a loading is "
                        + "not an InternalTransporter/ a Crane or Truck. The unloading will not be carried out !",
                    "Loading : "
                        + getName()
                        + " Method: void "
                        + "cooperation (SimProcess master, SimProcess slave)",
                    "The given master or slave process is not right.",
                    "It is recommended to use an InternalTransporter/ a Crane process as a master and "
                        + "Truck or an InternalTransporter as a slave to carry out a loading");
            }
        }
    }

    /**
     * Returns a <code>TimeSpan</code> object representing the time it takes to load the containers (goods) with the
     * transporter or crane. The time is taken from the given random number stream loadTimeStream.
     *
     * @return TimeSpan : The time it takes to load the containers (goods) with the transporter/crane.
     */
    protected TimeSpan getLoadTimeSample() {

        return new TimeSpan(loadTimeStream.sample().doubleValue());
    }

    /**
     * Sets the loadTimeStream to a new <code>RealDist</code> random number stream.
     *
     * @param loadTimeStream NumericalDist<?> : The new <code>RealDist</code> random number stream determining the time
     *                       it takes to load the containers (goods).
     */
    public void setLoadTimeStream(NumericalDist<?> loadTimeStream) {

        this.loadTimeStream = loadTimeStream;
    }
}