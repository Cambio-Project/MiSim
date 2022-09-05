package desmoj.extensions.applicationDomains.harbour;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.InterruptCode;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.SimProcess;

/**
 * A C_Control represents the control component for the cranes. The cranes and the transporters (external/internal) can
 * activate (interrupt) the crane control to begin with their serving. Its <code>lifeCycle()</code> is already
 * implemented and spezifies the behavior of the crane control.
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
 * @see CranesSystem
 */
public class C_Control extends SimProcess {
    /**
     * The cranes system that manages all the statistic of the all cranes of this crane control.
     */
    private final CranesSystem cs;

    /**
     * The interruptCode caused by a transporters arrival to interrupt this crane control.
     */
    private final InterruptCode transporterArrival;

    /**
     * The interruptCode caused by an idle Crane to interrupt this crane control.
     */
    private final InterruptCode craneIsFree;

    /**
     * Constructor for a crane control C_Control for the cranes. The queueing discipline and the capacity limit of the
     * underlying queues for the cranes and for the transporters (external/internal) can be chosen.
     *
     * @param owner        Model : The model this C_Control is associated to.
     * @param name         java.lang.String : The C_Control's name
     * @param csortOrder   int : determines the sort order of the underlying cranes queue implementation. Choose a
     *                     constant from <code>QueueBased</code> like <code>QueueBased.FIFO</code> or
     *                     <code>QueueBased.LIFO</code> or ...
     * @param cQCapacity   int : The capacity of the cranes queue, that is how many cranes can be enqueued. Zero (0)
     *                     means unlimited capacity.
     * @param tsortOrder   int : determines the sort order of the underlying transporters queue implementation. Choose a
     *                     constant from
     *                     <code>QueueBased</code> like <code>QueueBased.FIFO</code>
     *                     or <code>QueueBased.LIFO</code> or ...
     * @param tQCapacity   int : The capacity of the transporters queue, that is how many transporters can be enqueued.
     *                     Zero (0) means unlimited capacity.
     * @param showInReport boolean : Flag, if C_Control (its CranesSystem) should produce a report or not.
     * @param showInTrace  boolean : Flag for trace to produce trace messages.
     */
    public C_Control(Model owner, String name, int cQSortOrder, int cQCapacity,
                     int tQSortOrder, int tQCapacity, boolean showInReport,
                     boolean showInTrace) {

        super(owner, name, true, showInTrace); // make a sim-process

        // make a new CranesSystem
        this.cs = new CranesSystem(owner, "CranesSystem", cQSortOrder,
            cQCapacity, tQSortOrder, tQCapacity, true, true);

        // init the interrupts causes for this crane control
        this.craneIsFree = new InterruptCode("CraneIsIdle");
        this.transporterArrival = new InterruptCode("TransporterArrival");
    } // end of constructor

    /**
     * Returns the cranes system of this crane control.
     *
     * @return <code>CranesSystem</code>: The crane system of this crane
     *     control.
     */
    public CranesSystem getCS() {

        return this.cs;
    }

    /**
     * This method is used by a transporter (external/internal) to send the message to this crane control that it
     * arrives and waits on its serving.
     */
    public void transporterArrival() throws SuspendExecution {

        // get the current process that sends the message to the crane control
        SimProcess currentProcess = currentSimProcess();

        // add the transporter to the cranes system
        this.cs.addTransporter(currentProcess);

        // interrupt this crane control
        this.interrupt(this.transporterArrival);

        if (currentlySendTraceNotes()) {
            currentProcess.sendTraceNote("waits for his unloading and/or loading");
        }

        // skip trace note
        currentProcess.skipTraceNote();

        // passivate the transporter
        currentProcess.passivate();
    }

    /**
     * This method is used by a crane to send the message to this crane control that it is idle and waits on its
     * serving.
     */
    public void CraneIsIdle() throws SuspendExecution {

        // get the current process that sends the message to the crane control
        SimProcess currentProcess = currentSimProcess();

        // insert the crane into the cranes queue of the cranes system
        // this.getCS().getCranesQueue().insert(currentProcess);
        if (currentProcess instanceof Crane) {
            this.getCS().addCrane((Crane) currentProcess);

            // interrupt this crane control
            this.interrupt(this.craneIsFree);

            // passivate the crane
            currentProcess.passivate();
        }
    }

    /**
     * This method implements crane control specific behaviour. This behavior is always the same therefore there is no
     * need to implement that in a subclass of C_Cotrol. This method starts after a crane contrl has been created and
     * activated by the scheduler.
     */
    public void lifeCycle() throws SuspendExecution {

        // wait before a transporter or crane is here
        this.passivate();

        // if a transporter arrives
        if (this.isInterrupted()
            && this.getInterruptCode() == this.transporterArrival) {
            // serve the transporter
            serveTransporter();
            this.clearInterruptCode();
        }
        // if a crane is idle
        if (this.isInterrupted()
            && this.getInterruptCode() == this.craneIsFree) {
            // serve the crane
            serveCrane();
            this.clearInterruptCode();
        }


    }

    /**
     * This method implements the specific behaviour of the crane control if a transporter arrives and must be served by
     * this control. It's implemented for the case - a ship arrives and needs some cranes for his unloading and/or
     * loading. Override this method to specify another case or behavior.
     */
    public void serveTransporter() {

        // get the transporter from the transporter queue
        SimProcess t = this.cs.getTransporter();

        // cast the ship
        Ship s = (Ship) t;

        // get the number of cranes for the serving of this ship
        int n = s.getNumOfCranes();

        // try to get this number of cranes
        Crane[] cranes = this.cs.getCranes(n);

        // if there're enough cranes
        if (cranes != null) {
            // remove ship from the queue
            this.cs.getTransporterQueue().remove(s);

            // get the number of import containers (goods) of this ship to
            // unload
            long toUnload = s.getNumberOfImportGoods();

            // get the number of export containers (goods) of this ship to load
            long toLoad = s.getNumberOfExportGoods();

            long m = 0, m1 = 0;
            long d = 0, d1 = 0;
            long l = 0, l1 = 0;

            // define how many containers every crane has to unlaod and/or load
            // try to set by the every crane the same number of impot and/or
            // export containers to
            // unload and/or load
            if (toUnload > 0) // if the ship has some import containers
            {
                m = toUnload % n; // get the rest (mod) of this division

                // get number of import containers to unload for all the cranes
                // that can be divided for
                // all the cranes the same way
                d = toUnload - m;

                // get the min. number of import containers to unload for every
                // crane
                l = d / n;
            }

            if (toLoad > 0) // if the ship has some export containers
            {
                m1 = toLoad % n; // get the rest (mod) of this division

                // get number of export containers to load for all the cranes
                // that can be divided for
                // all the cranes the same way
                d1 = toLoad - m1;

                // get the min. number of export containers to load for every
                // crane
                l1 = d1 / n;
            }

            // set by every crane the numner of import and export containers it
            // has to unload
            // and/or load
            Crane c;
            for (int i = 0; i < n; i++) {

                c = cranes[i];
                // assign the crane to the ship
                c.setShip(s);

                // if the number of import containers can't be the same for
                // every crane
                // set by some crane a larger number of import containers to
                // unload
                if (m > 0) {
                    m = m - 1;
                    c.setNumToUnloadUnits(l + 1);
                } else {
                    c.setNumToUnloadUnits(l);
                }

                // if the number of export containers can't be the same for
                // every crane
                // set by some crane a larger number of export containers to
                // load
                if (m1 > 0) {
                    m1 = m1 - 1;
                    c.setNumToLoadUnits(l1 + 1);
                } else {
                    c.setNumToLoadUnits(l1);
                }

                // insert the crane in the queue for working cranes
                cs.getWorkingCranes().insert(c);

                // trace that crane is assigned to the ship
                if (currentlySendTraceNotes()) {
                    this.sendTraceNote("assigns the " + c.getName() + " to the "
                        + s.getName());
                }

                // activate
                c.activate();
            } // end of for

        } // end of if
    }

    /**
     * This method implements the specific behaviour of the crane control if a crane is idle must be served by this
     * control. It's implemented for the case - a crane is idle and the crane control tries to find a ship that waits
     * for his serving and this crane can be assigned to. If the crane was assigned it checks if the ship is ready and
     * can be acivated. Override this method to specify another case or behavior.
     */
    public void serveCrane() {

        // get the crane to serve
        Crane c = this.cs.getCranesQueue().last();

        // get the ship it's assigned to
        Ship ship = c.getShip();

        // if the crane is assigned a ship to
        if (ship != null) {
            // remove the crane from the working cranse queue
            cs.getWorkingCranes().remove(c);

            // get the number of loaded and unloaded units of this crane
            long unload = c.getNumUnloadedUnits();
            long load = c.getNumLoadedUnits();

            // reset the crane
            c.reset();

            // get the number of import and export containers of this ship
            long impGoods = ship.getNumberOfImportGoods();
            long expGoods = ship.getNumberOfExportGoods();

            // set the number of import and export containers by this ship to
            // the new values
            ship.setNumberOfImportGoods(impGoods - unload);
            ship.setNumberOfExportGoods(expGoods - load);

            // if some other ships wait in the queue
            if (cs.getTransporterQueue().length() > 0) {
                serveTransporter(); // try to serve one ship
            }

            // if the ship is complete served: unloaded and/or loaded
            if ((ship.getNumberOfImportGoods() == 0)
                && (ship.getNumberOfExportGoods() == 0))

            // activate this ship
            {
                ship.activateAfter(this);
            }

        }
    }

}