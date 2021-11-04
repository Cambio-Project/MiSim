package desmoj.extensions.applicationDomains.production;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.SimProcess;

/**
 * A Worker represents any kind of human worker who is working at some
 * <code>WorkStation</code> and processing parts (products or goods) there.
 * Worker is derived from SimProcess. Its <code>lifeCycle()</code> must be implemented by the user in order to specify
 * the behavior of the Worker.
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
 * @see Processing
 * @see WorkStation
 */
public abstract class Worker extends SimProcess {
    /**
     * A standard constructor for a Worker needs a model he belongs to, a name and a flag stating if trace messages of
     * this Worker should be displayed in the trace file.
     *
     * @param owner       desmoj.Model : The model this Worker is associated to.
     * @param name        java.lang.String : The name of this Worker.
     * @param showInTrace boolean : Flag, if trace messages of this Worker should be displayed in the trace file or
     *                    not.
     */
    public Worker(Model owner, String name, boolean showInTrace) {

        super(owner, name, showInTrace); // make a sim-process
    }

    /**
     * Override this method in a subclass of Worker to implement the Workers specific behaviour. It should contain at
     * least something to let this Worker wait in a <code>WorkStation</code> for parts to process them, like <br>
     * <code> WorkStationXY.cooperate(processing);</code><br>
     * This <code>lifeCycle()</code> method starts after a Worker has been created and activated by the scheduler. It
     * describes the behavior of this special Worker when he is acting alone. All action taking place when this Worker
     * (as a master) acts together with other <code>SimProcess</code> es (as slaves) in some process cooperation is
     * described in that special
     * <code>cooperation</code> method of the class <code>Processing</code>.
     */
    public abstract void lifeCycle() throws co.paralleluniverse.fibers.SuspendExecution;
}