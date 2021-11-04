package desmoj.extensions.applicationDomains.harbour;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.advancedModellingFeatures.Res;
import desmoj.core.advancedModellingFeatures.WaitQueue;
import desmoj.core.simulator.Queue;

/**
 * The HoldingArea is the place where <code>InternalTransporter</code>s serve (load/unload) <code>Truck</code> s at a
 * container terminal. So what happens at the HoldingArea is some kind of Rendezvous synchronisation, where
 * <code>InternalTransporter</code> s meet the <code>Truck</code>s to
 * load/unload them. There is one wait queue for the masters (<code>InternalTransporter</code>s) and one queue for the
 * slaves (
 * <code>Truck</code> s), where they have to wait for each other to cooperate.
 * The <code>InternalTransporter</code> s are the masters which perform the loading/unloading operation. The corporate
 * loading/unloading operation is described in the method <code>cooperation</code> in a subclass of
 * <code>Loading</code> or <code>Unloading</code>. The <code>Truck</code>
 * s loaded/unloaded as slaves keep still during the loading/unloading operation and will be reactivated thereafter. The
 * first sort criteria for the queues is always highest priorities first, the second queueing discipline of the
 * underlying queues and the capacity limit can be determined by the user (default is FIFO and unlimited capacity).
 * HoldingArea is derived from
 * <code>WaitQueue</code> which in turn is derived from
 * <code>QueueBased</code>, which provides all the statistical functionality
 * for the queues.
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
 * @see WaitQueue
 * @see desmoj.core.simulator.QueueBased
 * @see desmoj.core.advancedModellingFeatures.ProcessCoop
 */
public class HoldingArea extends WaitQueue {

    /**
     * the number of the lanes of this HoldingArea .
     */
    private int nLanes;

    /**
     * the lanes as Res of this Holding Area.
     */
    private Res lanes;

    /**
     * the queue of the lanes of this HoldingArea .
     */
    private Queue<Lane> laneQueue;

    /**
     * Constructor for a HoldingArea. There are two waiting-queues constructed, one internal <code>QueueList</code> for
     * the
     * <code>InternalTransporter</code> s (masters) and one separate
     * <code>ProcessQueue</code> for the <code>Truck</code> s (slave)
     * processes . The queueing discipline and the capacity limit of the underlying queues can be chosen. Highest
     * priority are always first in the queues.
     *
     * @param owner        desmoj.Model : The model this HoldingArea is associated to.
     * @param name         java.lang.String : The name of this HoldingArea.
     * @param mSortOrder   int : determines the sort order of the underlying master queue implementation. Choose a
     *                     constant from <code>QueueBased</code> like <code>QueueBased.FIFO</code> or
     *                     <code>QueueBased.LIFO</code> or ...
     * @param mQCapacity   int : The capacity of the master queue, that is how many processes can be enqueued. Zero (0)
     *                     means unlimited capacity.
     * @param sSortOrder   int : determines the sort order of the underlying slave queue implementation. Choose a
     *                     constant from <code>QueueBased</code> like <code>QueueBased.FIFO</code> or
     *                     <code>QueueBased.LIFO</code> or ...
     * @param sQCapacity   int : The capacity of the slave queue, that is how many processes can be enqueued. Zero (0)
     *                     means unlimited capacity.
     * @param int          nLanes: the number of the lanes of this HoldingArea.
     * @param showInReport boolean : Flag, if this HoldingArea should produce a report or not.
     * @param showInTrace  boolean : Flag, if trace messages of this HoldingArea should be displayed in the trace file.
     */
    public HoldingArea(desmoj.core.simulator.Model owner, String name,
                       int mSortOrder, int mQCapacity, int sSortOrder, int sQCapacity,
                       int nLanes, boolean showInReport, boolean showInTrace) {
        // make a WaitQueue
        super(owner, name, mSortOrder, mQCapacity, sSortOrder, sQCapacity,
            showInReport, showInTrace);

        // check the number of the lanes
        if (nLanes <= 0) {
            sendWarning(
                "The given number of the lanes is " + "wrong.",
                getClass().getName()
                    + ": "
                    + getQuotedName()
                    + ", Constructor: Holding Area(Model owner, String name, "
                    +
                    "int mSortOrder, int mQCapacity, int sSortOrder, int sQCapacity, int nLanes, boolean showInTrace)",
                "Tne number of the lanes that is negative or zero does not make sense.",
                "Make sure to provide a valid  value for the  number of the lanes "
                    + "for the Holding Area to be constructed.");

            return;
        }
        this.nLanes = nLanes;
        // make lanes as Res
        this.lanes = new Res(owner, "Lanes", sSortOrder, sQCapacity, nLanes,
            true, false);

        // make the Queue of the lanes
        this.laneQueue = new Queue<Lane>(owner, "LaneQueue", 0, nLanes, false, false);

        // make lanes and insert them in the laneQueue
        Lane lane;
        for (int i = 0; i < this.nLanes; i++) {
            lane = new Lane(owner, "Lane", i + 1, false);
            this.laneQueue.insert(lane);
        }
    } // end of constructor

    /**
     * Gets a Line from the HoldingArea and provides it to the sim-process to use it. As not enough lanes are available
     * at the moment the sim-process has to wait in a queue until a lane is available again.
     *
     * @return <code>Lane</code>: The available lane of this HoldingArea.
     */
    public Lane getLane() throws SuspendExecution {

        // check if there're available lanes
        if (!this.lanes.provide(1)) {
            return null;
        }

        // get the lane from the queue of the lanes
        Lane lane = this.laneQueue.first();

        // trace that a lane was taken from this HO
        if (currentlySendTraceNotes()) {
            sendTraceNote("takes " + lane.getName() + " from the " + this.getName());
        }

        // remove lane from the Queue
        this.laneQueue.remove(lane);

        return lane;
    }

    /**
     * A process is using this method to put lane it has used back in the HoldingArea.
     *
     * @param l <code>Lane</code>: The lane which should be returned to the
     *          HoldingArea.
     */
    public void takeBack(Lane l) {

        // take back the lane as Res
        this.lanes.takeBack(1);

        // insert the line to the lanes queue
        this.laneQueue.insert(l);
        if (currentlySendTraceNotes()) {
            sendTraceNote("releases " + l.getName() + " to the " + this.getName());
        }
    }
}