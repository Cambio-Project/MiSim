package desmoj.extensions.applicationDomains.harbour;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeOperations;
import desmoj.core.simulator.TimeSpan;
import desmoj.extensions.applicationDomains.production.Transporter;

/**
 * A InternalTransporter represents any kind of vehicle (AGV, VC, chassis) which carries goods (containers) around in a
 * container terminal. It has a certain loaded and empty speed when he drives in a terminal and is loaded or empty. He
 * has also a certain transporter control which he gets his <code>Job</code> to do by. InternalTransporter is derived
 * from Transporter. Because of that he has a certain capacity (maximum number of goods which can be carried around at
 * once) and a minimum load (minimum number of goods which will be carried) that one is. Its <code>lifeCycle()</code> is
 * already implemented and spezifies the behavior of the InternalTransporter. But its methods
 * <code>importJobCycle(Job j)</code> and <code>ExportJobCycle(Job j)</code>
 * must be ipmlemented by the user in order to specify the behavior of the InternalTransporter if he does an emport or
 * export job.
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
 * @see Transporter
 * @see T_Control
 * @see Job
 */
public abstract class InternalTransporter extends Transporter {

    /**
     * the loaded speed
     */
    private double speedLoad;

    /**
     * the empty speed
     */
    private double speedEmpty;

    /**
     * the number of empty drives
     */
    private long numEmptyDrives = 0;

    /**
     * the number of loaded drives
     */
    private long numLoadDrives = 0;

    /**
     * the time of all empty drives
     */
    private double sumEmptyDrives = 0.0;

    /**
     * the time of all loaded drives
     */
    private double sumLoadDrives = 0.0;

    /**
     * the job that the InternalTransporter has to do
     */
    private Job task = null;

    /**
     * the transporter control where the InternalTransporter gets his job to do
     */
    private T_Control ts;

    /**
     * the crane (a containerbridge) which the InternalTransporter can be assigned to
     */
    private Crane crane = null;

    /**
     * the current capacity that the InternalTransporter has at the moment
     */
    private int currentCapacity = 0;

    /**
     * Constructs an Internal Transporter which will carry around containers (goods) in a container terminal. Implement
     * its
     * <code>importJobCycle(Job j)</code> method and
     * <code>exportJobCycle(Job j)</code> to specify its behavior when he gets
     * an ex- or import job. An Internal Transporter has like a Transporter a capacity (maximum number of goods which
     * can be transported) and a mimimum load ( a minimum number of goods it will carry). Both must not be zero or
     * negative. The capacity default value is one. The minimum load has always value one.
     *
     * @param owner       desmoj.Model : The model this InternalTransporter is associated to.
     * @param name        java.lang.String : The name of this InternalTransporter.
     * @param capac       int : The maximum number of goods this InternalTransporter can carry around.
     * @param speedLoad   double : The loaded speed of this InternalTransporter.
     * @param speedEmpty  double : The empty speed of this InternalTransporter.
     * @param ts          <code>T_Control</code>: The transporter control wchich this
     *                    InternalTransporter belongs to.
     * @param showInTrace boolean : Flag, if this InternalTransporter should produce a trace output or not.
     * @see Transporter It has a certain loaded and empty speed when he drives in a terminal and is loaded or empty.
     *     Both must not be zero or negative. He has also a certain transporter control which he gets his job to do by.
     */
    public InternalTransporter(Model owner, String name, int capac,
                               double speedLoad, double speedEmpty, T_Control ts,
                               boolean showInTrace) {

        super(owner, name, capac, showInTrace); // make a Transporter

        // check the loaded parameter
        if (speedLoad <= 0.0) {
            sendWarning(
                "The given loaded speed is " + "negative or zero.",
                getClass().getName()
                    + ": "
                    + getQuotedName()
                    + ", Constructor: InternalTransporter(Model owner, String name, "
                    + "int capac, double SpeedLoad, double SpeedEmpty, boolean showInTrace)",
                "Tne negative load speed or zero for it  does not make sense.",
                "Make sure to provide a valid positive value for load speed "
                    + "for the InternalTransporter to be constructed.");

            return;
        }

        this.speedLoad = speedLoad; // set the loaded speed

        // check the empty speed parameter
        if (speedEmpty <= 0.0) {
            sendWarning(
                "The given empty speed is " + "negative or zero.",
                getClass().getName()
                    + ": "
                    + getQuotedName()
                    + ", Constructor: InternalTransporter(Model owner, String name, "
                    + "int capac, double SpeedLoad, double SpeedEmpty, boolean showInTrace)",
                "Tne negative empty speed or zero for it  does not make sense.",
                "Make sure to provide a valid positive value for empty speed "
                    + "for the InternalTransporter to be constructed.");

            return;
        }
        this.speedEmpty = speedEmpty; // set the empty speed

        this.ts = ts;// set the transporter control

    } // end of constructor

    /**
     * Returns the crane (containerbridge) which this InternalTransporter is assigned to.
     *
     * @return <code>Crane</code>: The assigned crane of this
     *     InternalTransporter.
     */
    public Crane getCrane() {

        return this.crane;
    }

    /**
     * Sets the crane that this InternalTransporter must be assigned to.
     *
     * @param c <code>Crane</code>: The new assigned crane of this
     *          InternalTransporter.
     */
    public void setCrane(Crane c) {

        this.crane = c;
    }

    /**
     * This method describes the driving the certain distance of the internal transporter if he is loaded (has some
     * goods). In this simple case only the time it takes to drive the distance with his loaded speed. The number and
     * time of loaded drives were counted in this method.
     *
     * @param distance double : The distance to drive
     */
    public void driveLoad(double distance) throws SuspendExecution {

        // define the needed time to drive
        TimeSpan t = new TimeSpan(distance / this.speedLoad);

        // trace with the note that the internal transporter is driving loaded
        // now
        if (currentlySendTraceNotes()) {
            sendTraceNote("drives loaded for "
                + t
                + " until "
                + TimeOperations.add(presentTime(), t));
        }
        skipTraceNote();
        // hold for the driving time
        hold(t);

        // change all the statistics about loaded drives for this internal
        // transporter
        this.sumLoadDrives = this.sumLoadDrives + t.getTimeAsDouble();
        this.numLoadDrives = this.numLoadDrives + 1;

        // change all the statistics about empty drives by the transporter
        // control
        // this internal transporter belongs to.
        this.ts.getTS().addSumNumLoadedDrives(1);
        this.ts.getTS().addSumTimeLoadedDrives(t.getTimeAsDouble());
        this.ts.getTS().addSumDistanceLoadedDrives(distance);

    }

    /**
     * This method describes the driving the certain distance of the internal transporter if he is empty (has no goods).
     * In this simple case only the time it takes to drive the distance with his empty speed. The number and time of
     * empty drives were counted in this method.
     *
     * @param distance double : The distance to drive
     */
    public void driveEmpty(double distance) throws SuspendExecution {

        TimeSpan t = new TimeSpan(distance / this.speedEmpty);

        // trace with the note that the internal transporter is driving empty
        // now
        if (currentlySendTraceNotes()) {
            sendTraceNote("drives empty for "
                + t
                + " until "
                + TimeOperations.add(presentTime(), t));
        }

        skipTraceNote();
        // hold for the driving time
        hold(t);
        // change all the statistics about empty drives for this internal
        // transporter
        sumEmptyDrives = sumEmptyDrives + t.getTimeAsDouble();
        this.numEmptyDrives = this.numEmptyDrives + 1;

        // change all the statistics about empty drives by the transporter
        // control
        // this internal transporter belongs to.
        this.ts.getTS().addSumNumEmptyDrives(1);
        this.ts.getTS().addSumTimeEmptyDrives(t.getTimeAsDouble());
        this.ts.getTS().addSumDistanceEmptyDrives(distance);
    }

    /**
     * Returns the empty speed of this InternalTransporter.
     *
     * @return double : The empty speed of this InternalTransporter.
     */
    public double getSpeedEmpty() {

        return this.speedEmpty;
    }

    /**
     * Sets the empty speed of this InternalTransporter to a new value. The new value must not be zero or negative.
     *
     * @param s double : The new empty speed of this InternalTransporter.
     */
    public void setSpeedEmpty(double s) {

        if (s <= 0.0) {
            sendWarning(
                "The empty speed of a internal transporter should be changed to zero or "
                    + "a negative value. The empty speed will remain unchanged!",
                "InternalTransporter : " + getName()
                    + " Method: void setSpeedEmpty(double s)",
                "A speed which is zero or negative does not make sense.",
                "Make sure to provide a valid positive empty speed"
                    + "when changing this attribute.");

            return; // forget that rubbish
        }

        this.speedEmpty = s;
    }

    /**
     * Returns the loaded speed of this InternalTransporter.
     *
     * @return double : The loaded speed of this InternalTransporter.
     */
    public double getSpeedLoad() {

        return this.speedLoad;
    }

    /**
     * Sets the loaded speed of this InternalTransporter to a new value. The new value must not be zero or negative.
     *
     * @param s double : The new loaded speed of this InternalTransporter.
     */
    public void setSpeedLoad(double s) {

        if (s <= 0.0) {
            sendWarning(
                "The loaded speed of a internal transporter should be changed to zero or "
                    + "a negative value. The load speed will remain unchanged!",
                "InternalTransporter : " + getName()
                    + " Method: void setSpeedLoad(double s)",
                "A speed which is zero or negative does not make sense.",
                "Make sure to provide a valid positive load speed "
                    + "when changing this attribute.");

            return; // forget that rubbish
        }

        this.speedLoad = s;
    }

    /**
     * Returns the current job of this InternalTransporter.
     *
     * @return <code>Job</code>: The job of this InternalTransporter.
     */
    public Job getJob() {

        return this.task;
    }

    /**
     * Sets the job of this InternalTransporter that he has to do.
     *
     * @param j <code>Job</code>: The new job of this InternalTransporter.
     */
    public void setJob(Job j) {

        this.task = j;
    }

    /**
     * Returns the current capacity of this InternalTransporter. That is the number of goods (containers) he carries at
     * the moment.
     *
     * @return int : The current capacity of this InternalTransporter.
     */
    public int getCurrentCapacity() {

        return this.currentCapacity;
    }

    /**
     * Sets the current capacity of this InternalTransporter to a new value. The new value must not be negative.
     *
     * @param c int : The new current capacity of this InternalTransporter.
     */
    public void setCurrentCapacity(int c) {

        if (c < 0) {
            sendWarning(
                "The current capacity of an internal transporter should be changed to "
                    + "a negative value. The capacity will remain unchanged!",
                "InternalTransporter : " + getName()
                    + " Method: void setCurrentCapacity(int c)",
                "A current capacity which is  negative does not make sense.",
                "Make sure to provide a valid positive current capacity "
                    + "when changing this attribute.");

            return; // forget that rubbish
        }

        if (c > this.getCapacity()) {
            sendWarning(
                "The current capacity of an internal transporter should be changed to "
                    +
                    "a value that larger than the max.capacity of the transporter. The capacity will remain unchanged!",
                "InternalTransporter : " + getName()
                    + " Method: void setCurrentCapacity(int c)",
                "A current capacity that is largen than max. capacity of the transporter does not make sense.",
                "Make sure to provide a valid current capacity "
                    + "when changing this attribute.");

            return; // forget that rubbish
        }
        this.currentCapacity = c;
    }

    /**
     * Override this method in a subclass of InternalTransporter to implement that internal transporter's specific
     * behaviour if he has to do an import job in a container terminal.
     *
     * @param j <code>Job</code>: The import job of this
     *          InternalTransporter.
     */
    public abstract void importJobCycle(Job j) throws SuspendExecution;

    /**
     * Override this method in a subclass of InternalTransporter to implement that internal transporter's specific
     * behaviour if he has to do an export job in a container terminal.
     *
     * @param j <code>Job</code>: The export job of this
     *          InternalTransporter.
     */
    public abstract void exportJobCycle(Job j) throws SuspendExecution;

    /**
     * This method implements the internal transporter specific behaviour. This behavior is always the same therefore
     * there is no need to implement that in a subclass of InternalTransporter. This method starts after an internal
     * transporter has been created and activated by the scheduler.
     */
    public void lifeCycle() throws SuspendExecution {

        // neverending cycle of an internal transporter
        while (true) {
            // try to get the job from the transport control because the
            // internal transporter is idle
            ts.activateAfter(this); // activate the transport control after me
            // System.out.println("Transporter aktiviert Steuerung : " +
            // this.getName() );
            this.ts.addTransporter(this); // passivate myself

            // define the job to do
            Job j = this.getJob();

            if (j.getType() == 1)
            // do the import cycle if the job is an import job
            {
                importJobCycle(j);
            } else
            // do the export cycle if the job is an export job
            {
                exportJobCycle(j);
            }

        }
    }

    /**
     * Returns the number of empty drives that this InternalTransporter has done.
     *
     * @return long : The number of empty drives of this InternalTransporter.
     */
    public long getNumEmptyDrives() {

        return this.numEmptyDrives;
    }

    /**
     * Returns the number of loaded drives that this InternalTransporter has done.
     *
     * @return long : The number of loaded drives of this InternalTransporter.
     */
    public long getNumLoadedDrives() {

        return this.numLoadDrives;
    }

    /**
     * Returns the whole time of empty drives that this InternalTransporter has done.
     *
     * @return long : The whole time of empty drives of this InternalTransporter.
     */
    public double getTimeEmptyDrives() {

        return this.sumEmptyDrives;
    }

    /**
     * Returns the whole time of loaded drives that this InternalTransporter has done.
     *
     * @return long : The whole time of loaded drives of this InternalTransporter.
     */
    public double getTimeLoadedDrives() {

        return this.sumLoadDrives;
    }

    /**
     * This method describes the loading of the internal transporter. In this simple case only the time it takes to load
     * a container/good on itself.
     *
     * @param time TimeSpan : The needed time to load.
     */
    public void pickUp(TimeSpan time) throws SuspendExecution {

        // trace with the note that the internal transporter is picking up now
        if (currentlySendTraceNotes()) {
            sendTraceNote("picks up for "
                + time
                + " until "
                + TimeOperations.add(presentTime(), time));
        }

        skipTraceNote();
        // hold for the loading time
        hold(time);

        // change the current capacity
        this.currentCapacity++;
    }

    /**
     * This method describes the unloading of the internal transporter. In this simple case only the time it takes to
     * unload a good/container from itself.
     *
     * @param time TimeSpan : The needed time to unload.
     */
    public void pickDown(TimeSpan time) throws SuspendExecution {

        // trace with the note that the internal transporter is picking down now
        if (currentlySendTraceNotes()) {
            sendTraceNote("picks down for "
                + time
                + " until "
                + TimeOperations.add(presentTime(), time));
        }

        skipTraceNote();
        // hold for the unloading time
        hold(time);

        // change the current capacity
        this.currentCapacity--;
    }

    /**
     * To reset all the statistics about this InternalTransporter.
     */
    public void reset() {

        this.sumEmptyDrives = 0;
        this.sumLoadDrives = 0;

        this.numEmptyDrives = 0;
        this.numLoadDrives = 0;
    }
}