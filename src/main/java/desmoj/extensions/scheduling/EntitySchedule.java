package desmoj.extensions.scheduling;

import java.util.Enumeration;
import java.util.TimeZone;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.SimProcess;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeOperations;
import desmoj.core.simulator.TimeSpan;


/**
 * EntitySchedule is a list of EntityScheduleEntry's. A EntityScheduleEntry is a time region (e.g. every workingday from
 * 8:00 until 16:00) All EntityScheduleEntry's have the same timezone, stored in EntitySchedule.
 * <p>
 * EntitySchedule is a SimProcess. With setProcess is a process specified. The lifecycle of EntitySchedule will activate
 * the with setProcess specified process at begin and end of each schedule entry.
 *
 * @author christian.mueller@th-wildau.de
 *     <p>
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 *     with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *     <p>
 *     Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *     the specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 */
public class EntitySchedule<E extends SimProcess> extends SimProcess {

    public static boolean[] workingDays =
        //                               So ,   Mo ,  Di ,  Mi ,  Do ,  Fr ,  Sa
        {false, true, true, true, true, true, false};
    public static boolean[] weekend =
        //                               So ,   Mo ,  Di ,  Mi ,  Do ,  Fr ,  Sa
        {true, false, false, false, false, false, true};
    public static boolean[] allDays =
        //                               So ,   Mo ,  Di ,  Mi ,  Do ,  Fr ,  Sa
        {true, true, true, true, true, true, true};

    private Vector<EntityScheduleEntry> entryList = null;
    private TimeZone timezone = null;
    private Model model = null;
    private Vector<E> processList = null;  // Processes to schedule
    private boolean processActivate = true;
    private boolean processPassiviate = false;

    /**
     * Erzeugt leeren EntitySchedule mit default Zeitzone Zu begin jedes ScheduleEntry wird process aktiviert
     *
     * @param owner
     * @param process
     */
    public EntitySchedule(Model owner, String name, E process) {
        super(owner, name, owner.traceIsOn());
        this.model = owner;
        this.timezone = TimeZone.getTimeZone("UTC");
        this.entryList = new Vector<EntityScheduleEntry>();
        this.processList = new Vector<E>();
        if (process != null) {
            this.processList.add(process);
        }
        this.processActivate = true;
        this.processPassiviate = false;
    }

    /**
     * Erzeugt leeren EntitySchedule mit default Zeitzone
     *
     * @param owner
     */
    public EntitySchedule(Model owner) {
        this(owner, "", null);
    }

    /**
     * Default Schedule ohne Zeitbeschraenkung, werktags mit default Zeitzone
     *
     * @param fromHourOfDay
     * @param untilHourOfDay
     */
    public EntitySchedule(Model owner, TimeSpan fromBeginOfDay, TimeSpan untilBeginOfDay) {
        this(owner);
        this.add(new EntityScheduleEntry(owner, fromBeginOfDay, untilBeginOfDay));
    }

    public void setProcessManagement(boolean activate, boolean passiviate) {
        this.processActivate = activate;
        this.processPassiviate = passiviate;
    }

    public void setTimeZone(TimeZone timezone) {
        this.timezone = timezone;
    }

    public void clearProcessList() {
        this.processList.clear();
    }

    public void addProcess(E process) {
        this.processList.add(process);
    }

    public void removeProcess(E process) {
        this.processList.remove(process);
    }

    /**
     * fuegt dem Schedule ein EntityScheduleEntry hinzu
     *
     * @param entry
     */
    public void add(EntityScheduleEntry entry) {
        entry.setTimeZone(this.timezone);
        this.entryList.add(entry);
    }

    /**
     * prueft ob date in dem Schedule enthalten ist.
     *
     * @param date
     * @return Values des ersten Entry, der date enthaelt. null wenn kein Entry gefunden
     */
    public Object[] isInSchedule(TimeInstant time) {
        //System.out.println("isInSchedule "+ time);
        Enumeration<EntityScheduleEntry> e = this.entryList.elements();
        while (e.hasMoreElements()) {
            EntityScheduleEntry entry = e.nextElement();
            if (entry.isInEntry(time)) {
                return entry.getValues();
            }
        }
        return null;
    }

    /**
     * prueft ob currentTime in dem Schedule enthalten ist. Die Endzeit gehoert nicht zum ScheduleEntry
     *
     * @return Capacity des ersten Entry, der date enthaelt. null wenn kein Entry gefunden
     */
    public Object[] isInSchedule() {
        return this.isInSchedule(model.presentTime());
    }

    public TimeInstant getEndOfActualEntry(TimeInstant now) {
        TimeInstant end = null;
        for (int i = 0; i < this.entryList.size(); i++) {
            EntityScheduleEntry entry = this.entryList.get(i);
            end = entry.getEnd(now);
            if (end != null) {
                break;
            }
        }
        return end;
    }

    /**
     * compute begin of next valid scheduleEntry.
     *
     * @param now actual simulation time
     * @return null when no next entry exist.
     */
    public TimeInstant getBeginOfNextEntry(TimeInstant now) {
        TimeUnit epsUnit = this.model.getExperiment().getEpsilonUnit();
        TimeInstant big = new TimeInstant(Long.MAX_VALUE - 1, epsUnit);
        TimeInstant min = big;
        for (int i = 0; i < this.entryList.size(); i++) {
            EntityScheduleEntry entry = this.entryList.get(i);
            TimeInstant nextBegin = entry.getNextBegin(now);
            if (nextBegin != null && TimeInstant.isBefore(nextBegin, min)) {
                min = nextBegin;
            }
        }
        if (min.equals(big)) {
            min = null;
        }
        return min;
    }

    public String toString() {
        String out = "";
        for (int i = 0; i < this.entryList.size(); i++) {
            out += this.entryList.get(i).toString() + "<br>";
        }
        out += this.timezone.getDisplayName();
        return out;
    }

    /**
     * Das values-array der schedule entries beschreiben Eigenschaften eines entries. Angenommen in jedem schedule
     * entity steht eine unterschiedliche Anzahl der Resource e zur Verfuegung und diese Anzahl ist in der Komponente
     * indes des values-array abgespeichert. Die Namen der Resourcen Entities enthalten am Ende eine laufende Nr. Diese
     * Methode prueft, ob diese Nr <= der in dem aktuellen Schedule zulaessigen Anzahl ist.
     *
     * @param e
     * @param index
     * @return
     */
    public boolean checkAvailability(Entity e, int index) {
        long verfuegbar = 0;
        if (this.isInSchedule() != null) {
            Object tmp = this.isInSchedule()[index];
            if (tmp instanceof Double) {
                verfuegbar = Math.round((Double) tmp);
            } else if (tmp instanceof Long) {
                verfuegbar = (Long) tmp;
            } else {
                throw (new desmoj.core.exception.SimAbortedException(
                    new desmoj.core.report.ErrorMessage(
                        null,
                        "Can't checkAvailability! Simulation aborted.",
                        "Class : EntitySchedule  checkAvailability : value[" + index +
                            "] is not of type Double or Long",
                        "its class is : " + tmp.getClass().getSimpleName(),
                        "In method checkAvailability of class EntitySchedule " +
                            "value[" + index + "] is not of type Double or Long.",
                        null)));
            }
        }
        String name = e.getClass().getSimpleName() + ":" + e.getName();
        int entityIndex = Integer.parseInt(name.substring(name.indexOf('#') + 1).trim());
        //System.out.println("checkAvailability: "+name+"  "+index+" < "+verfuegbar);
        return (entityIndex <= verfuegbar);

    }

    /**
     * Wie oben, jedoch mit index = 0 als Default Wert.
     *
     * @param e
     * @return
     */
    public boolean checkAvailability(Entity e) {
        return this.checkAvailability(e, 0);
    }

    public void lifeCycle() throws SuspendExecution {
        TimeInstant entryBegin, entryEnd;

        // vorlauf
        entryEnd = this.getEndOfActualEntry(model.presentTime());
        if (entryEnd != null) {
            this.processActivate();
            hold(TimeOperations.diff(entryEnd, model.presentTime()));
            this.processPassiviate();
        }
        while (!this.entryList.isEmpty()) {
            entryBegin = this.getBeginOfNextEntry(model.presentTime());
            if (entryBegin == null) {
                break;
            }
            hold(TimeOperations.diff(entryBegin, model.presentTime()));
            this.processActivate();

            entryEnd = this.getEndOfActualEntry(model.presentTime());
            hold(TimeOperations.diff(entryEnd, model.presentTime()));
            this.processPassiviate();
        }
    }

    private void processActivate() {
        if (this.processActivate) {
            System.out.println("activate: " + model.presentTime());
            Enumeration<E> en = this.processList.elements();
            while (en.hasMoreElements()) {
                SimProcess process = en.nextElement();
                // Nur wenn der Prozess nicht bereits geplant ist, wird er aktiviert
                if (!process.isScheduled()) {
                    process.activate();
                }
            }
        }
    }

    private void processPassiviate() throws SuspendExecution {
        if (this.processPassiviate) {
            System.out.println("passiviate: " + model.presentTime());
            Enumeration<E> en = this.processList.elements();
            while (en.hasMoreElements()) {
                SimProcess process = en.nextElement();
                process.passivate();
            }
        }
    }

}
