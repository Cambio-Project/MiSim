package desmoj.extensions.applicationDomains.harbour;

import java.util.HashMap;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.advancedModellingFeatures.Res;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.ModelComponent;
import desmoj.core.simulator.SimProcess;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeOperations;

/**
 * The Gates is a place on a container terminal where the <code>Truck</code> s enter and leave the terminal. It has a
 * ceratin number of the entrance and exit lanes. Both must be not negative. The lanes are implemented as
 * <code>Res</code> with FIFO-strategy and unlimited capacity. The Gates is
 * derived from ModelComponent.
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
 * @see ModelComponent
 */
public class Gates extends ModelComponent {

    /**
     * the number of the entrance lanes of this Gates.
     */
    private final int nEntranceLanes;

    /**
     * the exit lanes as Res of this Gates.
     */
    private final Res exitLanes;

    /**
     * the entrance lanes as Res of this Gates.
     */
    private final Res entranceLanes;

    /**
     * the map used to determine the turn around time of trucks.
     */
    private final HashMap<SimProcess, TimeInstant> arrivalTimeTable;

    /**
     * the sum for the turn around times of the trucks.
     */
    private double sumTurnArounTime = 0.0;

    /**
     * Constructor for a Gates of a container terminal where the trucks enter and leave the terminal.
     *
     * @param owner       desmoj.Model : The model this Gates is associated to.
     * @param name        java.lang.String : The name of this Gates.
     * @param int         nEntLanes: the number of the entrance lanes of this Gates.
     * @param int         nExitLanes: the number of the exit lanes of this Gates.
     * @param showInTrace boolean : Flag, if trace messages of this Gates should be displayed in the trace file.
     */
    public Gates(Model owner, String name, int nEntLanes, int nExitLanes,
                 boolean showInTrace) {

        super(owner, name, showInTrace); // make a ModelComponent

        // check the number of the entrance lanes
        if (nEntLanes <= 0) {
            sendWarning(
                "The given number of the entrance lanes   is "
                    + "negative or zero. The number of the entrance lanes will be set to one!",
                getClass().getName()
                    + ": "
                    + getQuotedName()
                    + ", Constructor: Gates(Model owner, String name, "
                    + "int nEntLanes, int nExitLanes, boolean showInTrace)",
                "Tne negative number or zero of the entrance lanes does not make sense.",
                "Make sure to provide a valid positive number of entrance lanes "
                    + "for the Gates to be constructed.");

            this.nEntranceLanes = 1;
        } else {
            this.nEntranceLanes = nEntLanes;
        }
        this.arrivalTimeTable = new HashMap<SimProcess, TimeInstant>();

        // check the number of the entrance lanes
        if (nExitLanes < 0) {
            sendWarning(
                "The given number of the exit lanes   is "
                    + "negative. The number of the exit lanes will be set to one!",
                getClass().getName()
                    + ": "
                    + getQuotedName()
                    + ", Constructor: Gates(Model owner, String name, "
                    + "int nEntLanes, int nExitLanes, boolean showInTrace)",
                "Tne negative number of the exit lanes does not make sense.",
                "Make sure to provide a valid positive number of exit lanes  "
                    + "for the Gates to be constructed.");
        } else {
        }

        // make the lanes as Res
        this.entranceLanes = new Res(owner, "EntranceLanes", nEntranceLanes,
            true, showInTrace);
        this.exitLanes = new Res(owner, "ExitLanes", nExitLanes, true,
            showInTrace);
    }

    /**
     * A process is using this method to enter the entrance of the Gates and get one of free lanes of this Gates.
     */
    public void enterEntrance() throws SuspendExecution {

        // get the current process
        SimProcess currentProcess = currentSimProcess();
        // get the current time
        TimeInstant now = presentTime();

        // store arrival time
        arrivalTimeTable.put(currentProcess, now);

        // trace that enters the Gates
        if (currentlySendTraceNotes()) {
            sendTraceNote("enters the entrance of " + this.getName());
        }

        // take one free entrance lane
        this.entranceLanes.provide(1);
    }

    /**
     * A process is using this method to leave the enter of the Gates and put used entrance lane back.
     */
    public void leaveEntrance() {

        // trace that leaves the entrance
        if (currentlySendTraceNotes()) {
            sendTraceNote("leaves the entrance of " + this.getName());
        }

        // take back the used entrance lane
        this.entranceLanes.takeBack(1);

    }

    /**
     * A process is using this method to enter the exit of the Gates and get one of free exit lanes of this Gates.
     */
    public void enterExit() throws SuspendExecution {

        // trace that enters the gates
        if (currentlySendTraceNotes()) {
            sendTraceNote("enters the exit of " + this.getName());
        }

        // take one free exit lane
        this.exitLanes.provide(1);
    }

    /**
     * A process is using this method to leave the exit of the Gates and put a used exit lane back.
     */
    public void leaveExit() {

        // trace that leaves the exit
        if (currentlySendTraceNotes()) {
            sendTraceNote("leaves the exit of " + this.getName());
        }

        // take one used exit lane back.
        this.exitLanes.takeBack(1);

        // get the current process
        SimProcess currentProcess = currentSimProcess();
        // get the current time
        TimeInstant now = presentTime();
        // get the arrival time of the truck
        TimeInstant arrivalTime = arrivalTimeTable.remove(currentProcess);

        this.sumTurnArounTime = this.sumTurnArounTime
            + TimeOperations.diff(now, arrivalTime).getTimeAsDouble();

    }

    /**
     * Returns the number of users (trucks) of this Gates. It means the number of the trucks that have entered and left
     * the Gates.
     *
     * @return long : The number of trucks that enterd and left the terminal through this Gates.
     */
    public long getUsers() {

        long users = this.exitLanes.getUsers();

        return users;
    }

    /**
     * Returns the turn around time of users (trucks) of this Gates. It means the time of the trucks that were at the
     * terminal: have entered and left the terminal through this Gates.
     *
     * @return double : The turn around time of trucks.
     */
    public double getTurnAroundTime() {

        return this.sumTurnArounTime / this.getUsers();

    }
}