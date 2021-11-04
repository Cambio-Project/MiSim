package desmoj.extensions.applicationDomains.harbour;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeOperations;
import desmoj.core.simulator.TimeSpan;

/**
 * A Truck is an <code>ExternalTransporter</code> which arrives at a container terminal to deliver and /or pick up one
 * some containers (goods). It has a certain number of import/export containers that must have loaded/unloaded before it
 * can leave a container terminal. The both numbers must be not negative. It also has a certain speed that he drives in
 * a container terminal with. the speed must be not negative or zero. Truck is derived from ExternalTrasnporter. Its
 * <code>lifeCycle()</code> must be implemented by the user in order to specify the behavior of the Truck.
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
 * @see ExternalTransporter
 */
public abstract class Truck extends ExternalTransporter {

    /**
     * the speed of the Truck
     */
    private double speed;

    /**
     * the whole time that the Truck has driven
     */
    private double driveTime = 0.0;

    /**
     * the lane of the Truck where he waits at he holding area for an internal transporter to get unloaded/loaded.
     */
    private Lane lane = null;

    /**
     * Constructs a Truck which arrives at a container terminal to deliver and/or pick up some containers (goods).
     * Implement its
     * <code>lifeCycle()</code> method to specify its behavior. A Truck has a
     * number of import/export containers that must have loaded/unloaded before it can leave a container terminal. Both
     * must not be negative. Their default value is one. A Truck has also a certain speed that he drives in a container
     * terminal with.
     *
     * @param owner        desmoj.Model : The model this Truck is associated to.
     * @param name         java.lang.String : The name of this Truck.
     * @param nImportGoods long : The number of import goods this Truck has to pick up from a container terminal.
     * @param nExportGoods long : The number of export goods this Truck delivers to a container terminal.
     * @param speed        double : The speed of this Truck.
     * @param showInTrace  boolean : Flag, if this Truck should produce a trace output or not.
     */
    public Truck(Model owner, String name, long nImportGoods,
                 long nExportGoods, double speed, boolean showInTrace) {

        super(owner, name, nImportGoods, nExportGoods, showInTrace); // make
        // an
        // ExternalTransporter

        // check the speed of the Truck
        if (speed <= 0.0) {
            sendWarning(
                "The given  speed is " + "negative or zero.",
                getClass().getName()
                    + ": "
                    + getQuotedName()
                    + ", Constructor: Truck(Model owner, String name, "
                    + "long nImportGoods, long nExportGoods, double speed, boolean showInTrace)",
                "Tne negative speed or zero for it  does not make sense.",
                "Make sure to provide a valid positive value for speed "
                    + "for the Truck to be constructed.");

            return;
        }

        this.speed = speed;

    } // end of constuctor

    /**
     * Returns the speed of this Truck he drives with.
     *
     * @return double : The speed of this Truck.
     */
    public double getSpeed() {

        return this.speed;
    }

    /**
     * Sets the speed of this Truck it drives with. to a new value. The new value must not be negative or zero.
     *
     * @param s double : The new speed of this Truck.
     */
    public void setSpeed(double s) {

        if (s <= 0.0) {
            sendWarning("The given  speed is " + "negative or zero.",
                getClass().getName() + ": " + getQuotedName()
                    + ", Method: public void " + "setSpeed(double s)",
                "Tne negative speed or zero for it  does not make sense.",
                "Make sure to provide a valid positive value for speed "
                    + "for the Truck to be changed.");

            return;
        }
        this.speed = s;

    }

    /**
     * This method describes the driving of the certain distance of this Truck. In this simple case only the time it
     * takes to drive the distance with his speed. The time of drives of this Truck were counted in this method.
     *
     * @param distance double : The distance to drive.
     */
    public void drive(double distance) throws SuspendExecution {

        // define the SimSpan time to drive
        TimeSpan t = new TimeSpan(distance / this.speed);

        // trace with the note that the Truck is driving now
        if (currentlySendTraceNotes()) {
            sendTraceNote("drives for "
                + t
                + " until "
                + TimeOperations.add(presentTime(), t));
        }
        skipTraceNote();
        // hold for the driving time
        hold(t);
        // change the statistic about driving time for this truck
        this.driveTime = this.driveTime + t.getTimeAsDouble();
    }

    /**
     * Returns the whole time of this Truck he has driven.
     *
     * @return double : The driving time of this Truck.
     */
    public double getDrivingTime() {

        return this.driveTime;
    }

    /**
     * Returns the current lane this Truck stands in by the HO.
     *
     * @return <code>Lane</code>: The lane of this Truck.
     */
    public Lane getLane() {

        return this.lane;
    }

    /**
     * Sets the lane where the truck waits by the holding area.
     *
     * @param l <code>Lane</code>: The new lane of this Truck.
     */
    public void setLane(Lane l) {

        this.lane = l;

    }
}