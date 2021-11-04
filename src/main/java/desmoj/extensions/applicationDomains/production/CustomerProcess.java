package desmoj.extensions.applicationDomains.production;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.ComplexSimProcess;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.SimProcess;

/**
 * CustomerProcess is a kind of process representing a customer. He is fetching a certain number of products from an
 * <code>Entrepot</code> to use them up. He is not doing anything special with them and so the products will be
 * collected by the garbage collector. To change this behavior overwrite the method <code>lifeCycle()</code> in a
 * subclass. The CustomerProcess can be viewed as the border of the simulated system, where the products disappear. It
 * is intended to be used in conjunction with the <code>DemandProcess</code>. CustomerProcess has two main parameters to
 * be specified (see the Constructor): The <code>Entrepot</code> where the products are to be fetched from and the
 * number of demanded products.
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
 * @see DemandProcess
 */
public class CustomerProcess extends SimProcess {

    /**
     * The number of demanded products.
     */
    private final long demand;

    /**
     * The <code>Entrepot</code> supplying the products this CustomerProcess is demanding.
     */
    private final Entrepot entrepot;

    /**
     * Constructor for a CustomerProcess. The CustomerProcess is fetching a given number of products from the given
     * <code>Entrepot</code>.
     *
     * @param owner       desmoj.Model : The model this CustomerProcess is associated to.
     * @param name        java.lang.String : The name of this CustomerProcess.
     * @param supplier    desmoj.Entrepot : The <code>Entrepot</code> supplying the products this CustomerProcess is
     *                    buying.
     * @param quantity    long : The number of products this CustomerProcess is fetching from the
     *                    <code>Entrepot</code>.
     * @param showInTrace boolean : Flag, if this CustomerProcess should produce a trace output or not.
     */
    public CustomerProcess(Model owner, String name, Entrepot supplier,
                           long quantity, boolean showInTrace) {
        super(owner, name, showInTrace); // make a sim-process

        // save the parameters
        this.entrepot = supplier;
        this.demand = quantity;
    }

    /**
     * The CustomerProcess is fetching the given number of products from the given <code>Entrepot</code>. As there are
     * not enough products available at the moment this CustomerProcess has to wait in a queue in the
     * <code>Entrepot</code>. The obtained products are consumed (used up), that means all references to them are
     * released and the product Sim-process is activated (as the process is not terminated yet), to end its lifeCycle
     * and terminate by itself. Then the garbage collector can delete it. Therefore the <code>ComplexSimProcess</code>
     * es are disassembled automatically.
     */
    public void lifeCycle() throws SuspendExecution {

        // fetch the given number of products from the Entrepot
        SimProcess[] fetchedProducts = entrepot.removeProducts(demand);

        // disassemble the ComplexSimProcesses to all their elements
        for (int i = 0; i < fetchedProducts.length; i++) {
            if (fetchedProducts[i] instanceof ComplexSimProcess) {
                // all elements (SimProcesses) will be removed
                // (and activated if not terminated yet)
                ((ComplexSimProcess) fetchedProducts[i]).removeAllComponents();
            }
        }

        // just to make sure the reference is really destroyed
        //fetchedProducts = null;
        // 
        // Inserted comments since not necessary:
        // Assigning null does not guarantee SimProcess[] *immediately*
        // removed by garbage collector (JG, 11.03.09)
    }
}