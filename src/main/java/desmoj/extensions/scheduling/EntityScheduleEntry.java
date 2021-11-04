package desmoj.extensions.scheduling;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.PatternBasedTimeFormatter;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeOperations;
import desmoj.core.simulator.TimeSpan;


/**
 * EntityScheduleEntry is an entry of EntitySchedule. A EntityScheduleEntry is a time region (e.g. every workingday from
 * 8:00 until 16:00) All EntityScheduleEntry of a EntitySchedule have the same timezone, set by EntitySchedule.
 *
 * @author christian.mueller@th-wildau.de
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
public class EntityScheduleEntry {

    private TimeInstant validFrom, validUntil;       // Range of validity
    private boolean[] dayOfWeek;
    private TimeSpan fromBeginOfDay = null;
    private TimeSpan duration = null;
    private Model model = null;
    private TimeZone timezone = null;
    private Object[] values;

    /**
     * Create an schedule entry with daily time border and an entry value. Default values of this entry: validFrom and
     * validUntil are not set (null) day of week (working days of this schedule) are MON..FRI values is {value} Defaults
     * can be changed by approbate methods.
     *
     * @param model
     * @param fromBeginOfDay  TimeSpan from 0:00 until begin of entry. Must be smaller than 24h. Null is not allowed.
     * @param untilBeginOfDay TimeSpan from 0:00 until end of entry. Null is not allowed.
     * @param value
     */
    public EntityScheduleEntry(Model model, TimeSpan fromBeginOfDay, TimeSpan untilBeginOfDay, Object value) {
        this.model = model;
        this.validFrom = null;
        this.validUntil = null;
        this.dayOfWeek = EntitySchedule.workingDays;
        this.timezone = TimeZone.getDefault();
        this.fromBeginOfDay = fromBeginOfDay;
        this.duration = TimeOperations.diff(untilBeginOfDay, fromBeginOfDay);
        Object[] cap = {value};
        this.values = cap;

        if (fromBeginOfDay == null) {
            throw (new desmoj.core.exception.SimAbortedException(
                new desmoj.core.report.ErrorMessage(
                    null,
                    "fromBeginOfDay is null!! Simulation aborted.",
                    "Class : EntitySchedule  Constructor: fromBeginOfDay is null!",
                    "fromBeginOfDay is null!",
                    "fromBeginOfDay is null!", null)));
        }

        if (TimeSpan.isLongerOrEqual(fromBeginOfDay, new TimeSpan(24, TimeUnit.HOURS))) {
            throw (new desmoj.core.exception.SimAbortedException(
                new desmoj.core.report.ErrorMessage(
                    null,
                    "fromBeginOfDay is longer as 24 hours! Simulation aborted.",
                    "Class : EntitySchedule  Constructor: fromBeginOfDay is longer as 24 hours!",
                    "fromBeginOfDay is " + fromBeginOfDay.getTimeAsDouble(TimeUnit.HOURS),
                    "fromBeginOfDay is longer as 24 hours!", null)));
        }

        if (untilBeginOfDay == null) {
            throw (new desmoj.core.exception.SimAbortedException(
                new desmoj.core.report.ErrorMessage(
                    null,
                    "untilBeginOfDay is null!! Simulation aborted.",
                    "Class : EntitySchedule  Constructor: untilBeginOfDay is null!",
                    "untilBeginOfDay is null!",
                    "untilBeginOfDay is null!", null)));
        }

        if (value == null) {
            throw (new desmoj.core.exception.SimAbortedException(
                new desmoj.core.report.ErrorMessage(
                    null,
                    "value is null!! Simulation aborted.",
                    "Class : EntitySchedule  Constructor: value is null!",
                    "value is null!",
                    "value is null!", null)));
        }
    }

    public EntityScheduleEntry(Model model, TimeSpan fromBeginOfDay, TimeSpan untilBeginOfDay, double value) {
        this(model, fromBeginOfDay, untilBeginOfDay, new Double(value));
    }

    /**
     * Create an schedule entry with daily time border. Default values of this entry: fromDate and untilDate are not set
     * (null) day of week (working days of this schedule) are MON..FRI values is {1.0} Defaults can be changed by
     * approbate methods. from/until Hour/Minutes are times of the same day
     *
     * @param model
     * @param fromHourOfDay
     * @param fromMinute
     * @param untilHourOfDay
     * @param untilMinute
     */
    public EntityScheduleEntry(Model model, TimeSpan fromBeginOfDay, TimeSpan untilBeginOfDay) {
        this(model, fromBeginOfDay, untilBeginOfDay, 1.0);
    }

    /**
     * Global range of this schedule entry Default is null
     *
     * @param from
     * @param until
     */
    public void setValidTimeRange(TimeInstant from, TimeInstant until) {
        if (TimeInstant.isBeforeOrEqual(from, until)) {
            this.validFrom = from;
            this.validUntil = until;
        }
    }

    /**
     * active daysOfWeek in this schedule entry (SUN, MON, .., FRI, SAT)
     *
     * @param dayOfWeek
     */
    public void setDayOfWeek(boolean[] dayOfWeek) {
        this.dayOfWeek = new boolean[7];
        int m = Math.max(7, dayOfWeek.length);
        for (int i = 0; i < m; i++) {
            this.dayOfWeek[i] = dayOfWeek[i];
        }
        for (int i = m; i < 7; i++) {
            this.dayOfWeek[i] = false;
        }
    }

    /**
     * Get values array of entry
     *
     * @return
     */
    public Object[] getValues() {
        return this.values;
    }

    /**
     * Each entry has a values array. Default is {1.0}
     *
     * @param capacity
     */
    public void setValues(Object[] values) {
        this.values = values;
    }

    protected TimeInstant getBeginOfDay(TimeInstant time) {
        Calendar calBeginOfDay = GregorianCalendar.getInstance(this.timezone);
        calBeginOfDay.setTimeInMillis(time.getTimeRounded(TimeUnit.MILLISECONDS));
        calBeginOfDay.set(Calendar.HOUR_OF_DAY, 0);
        calBeginOfDay.set(Calendar.MINUTE, 0);
        calBeginOfDay.set(Calendar.SECOND, 0);
        calBeginOfDay.set(Calendar.MILLISECOND, 0);
        return new TimeInstant(calBeginOfDay);
    }

    protected boolean isValidDayOfWeek(TimeInstant time) {
        Calendar cal = GregorianCalendar.getInstance(this.timezone);
        cal.setTimeInMillis(time.getTimeRounded(TimeUnit.MILLISECONDS));
        return this.dayOfWeek[cal.get(Calendar.DAY_OF_WEEK) - 1];
    }

    /**
     * checks whether time is in the entry The end time is not part of the entry ????? Used by EntitySchedule
     *
     * @param time
     * @return
     */
    protected boolean isInEntry(TimeInstant time) {
        //System.out.println("EntityScheduleEntry.isInEntry() Begin");
        boolean out = false;
        TimeSpan untilBeginOfDay = TimeOperations.add(this.fromBeginOfDay, duration);

        long days = untilBeginOfDay.getTimeTruncated(TimeUnit.DAYS);
        TimeInstant beginToday = this.getBeginOfDay(time);
        for (int i = 0; i < days + 1; i++) {
            //System.out.println("Datumswechsel i: "+i);
            TimeInstant beginDay = TimeOperations.subtract(beginToday, new TimeSpan(i, TimeUnit.DAYS));
            out |= this.isInEntry(time, beginDay);
        }
        //System.out.println("EntityScheduleEntry.isInEntry() End "+out);
        return out;
    }

    /**
     * used by isInEntry(TimeInstant time)
     *
     * @param time
     * @param beginOfDay
     * @return
     */
    private boolean isInEntry(TimeInstant time, TimeInstant beginOfDay) {
        if (time == null) {
            return false;
        }
        TimeInstant timeFrom = TimeOperations.add(beginOfDay, this.fromBeginOfDay);
        TimeInstant timeUntil = TimeOperations.add(timeFrom, this.duration);

        //System.out.println("time:       "+time);
        //System.out.println("beginOfDay: "+beginOfDay);
        //System.out.println("timeFrom:   "+timeFrom);
        //System.out.println("timeUntil:  "+timeUntil);

        // Test ob date zwischen validFrom und validUntil liegt.
        if (this.validFrom != null && TimeInstant.isBefore(time, this.validFrom)) {
            //System.out.println("before date: "+time+" from: "+this.validFrom);
            return false;
        }
        if (this.validUntil != null && TimeInstant.isAfter(time, this.validUntil)) {
            //System.out.println("after date: "+time+" until "+this.validUntil);
            return false;
        }
        // Test ob time ein gueltiger Wochentag ist
        if (!this.isValidDayOfWeek(beginOfDay)) {
            //System.out.println("ungueltiger Tag");
            return false;
        }

        // Test ob time vor timeFrom liegt
        if (TimeInstant.isBefore(time, timeFrom)) {
            //System.out.println(time+" is Before "+timeFrom);
            return false;
        }

        // Test ob time >= EntryEnd
        //System.out.println(time+" is AfterEqual "+timeUntil);
        return !TimeInstant.isAfterOrEqual(time, timeUntil);

        // alle tests positiv
        //System.out.println("alles ok");
    }

    protected TimeInstant getNextBegin(TimeInstant time) {
        TimeInstant nextBegin = null;

        // Entry ist abgelaufen
        if (this.isFinished(time)) {
            return nextBegin;
        }

        TimeInstant time1 = this.startLater(time);
        if (time1 == null) {
            time1 = time;
        }
        TimeInstant beginOfDay = this.getBeginOfDay(time1);

        // Schleife ueber 1 Woche
        for (int i = 0; i < 8; i++) {
            TimeInstant from = TimeOperations.add(beginOfDay, fromBeginOfDay);
            // pruefe ob Entry abgelaufen
            if (validUntil != null && TimeInstant.isAfterOrEqual(from, validUntil)) {
                break;
            }
            // pruefe on zulaessiger Tag und from in Zukunft liegt
            if (this.isValidDayOfWeek(from) &&
                TimeInstant.isAfterOrEqual(from, time)) {
                nextBegin = from;
                break;
            }
            // next day
            beginOfDay = TimeOperations.add(beginOfDay, new TimeSpan(24, TimeUnit.HOURS));
        }
        return nextBegin;
    }

    /**
     * Begin of entry at date, null when date is a no valid day in this entry.
     *
     * @param date
     * @return
     */
    protected TimeInstant getBegin(TimeInstant time) {
        TimeInstant timeFrom = null;
        if (this.isInEntry(time)) {
            TimeInstant beginOfDay = this.getBeginOfDay(time);
            timeFrom = TimeOperations.add(beginOfDay, this.fromBeginOfDay);
        }
        return timeFrom;
    }

    /**
     * Get end time of this Entry for given date.
     *
     * @param date
     * @return
     */
    public TimeInstant getEnd(TimeInstant time) {
        TimeInstant timeUntil = null;
        TimeInstant timeFrom = this.getBegin(time);
        if (timeFrom != null) {
            timeUntil = TimeOperations.add(timeFrom, this.duration);
        }
        return timeUntil;
    }

    /**
     * Entry is finished before date
     *
     * @param date
     * @return
     */
    protected boolean isFinished(TimeInstant time) {
        return this.validUntil != null && TimeInstant.isBefore(validUntil, time);
    }

    /**
     * Entry starts after given date
     *
     * @param date
     * @return start time of entry
     */
    protected TimeInstant startLater(TimeInstant time) {
        TimeInstant out = null;
        if (this.validFrom != null && TimeInstant.isAfter(validFrom, time)) {
            out = this.validFrom;
        }
        return out;
    }

    protected boolean isNotValid(TimeInstant time) {
        return this.isFinished(time) || this.startLater(time) != null;
    }

    /**
     * Only for internal use. All entrys of a schedule have the same timezone.
     *
     * @param timezone
     */
    protected void setTimeZone(TimeZone timezone) {
        this.timezone = timezone;
    }

    public String toString() {
        String out = "";
        PatternBasedTimeFormatter timeFormat = new PatternBasedTimeFormatter(
            new SimpleDateFormat("dd.MM.yyyy HH:mm:ss:SSS", Locale.GERMANY), TimeUnit.DAYS, TimeUnit.MILLISECONDS);
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb, Locale.US);
        f.format("%5s", "values:(");
        if (this.values instanceof Number[]) {
            for (int i = 0; i < this.values.length; i++) {
                f.format("%3.1f", this.values[i]);
                if (i < this.values.length - 1) {
                    f.format("%1s", " ");
                }
            }
        }
        f.format("%1s", ")");
        out += sb + "\n";
        sb = new StringBuilder();
        f = new Formatter(sb, Locale.US);
        f.format("%7s", " time: ");
        f.format("%10s", timeFormat.buildTimeString(this.fromBeginOfDay));
        f.format("%2s", "..");
        f.format("%10s", timeFormat.buildTimeString(
            TimeOperations.add(this.fromBeginOfDay, duration)));
        out += sb + "\n";
        sb = new StringBuilder();
        f = new Formatter(sb, Locale.US);
        f.format("%3s", this.dayOfWeek[0] ? "So" : "--");
        f.format("%2s", this.dayOfWeek[1] ? "Mo" : "--");
        f.format("%2s", this.dayOfWeek[2] ? "Di" : "--");
        f.format("%2s", this.dayOfWeek[3] ? "Mi" : "--");
        f.format("%2s", this.dayOfWeek[4] ? "Do" : "--");
        f.format("%2s", this.dayOfWeek[5] ? "Fr" : "--");
        f.format("%2s", this.dayOfWeek[6] ? "Sa" : "--");
        if (this.validFrom != null && this.validUntil != null) {
            out += sb + "\n";
            sb = new StringBuilder();
            f = new Formatter(sb, Locale.US);
            f.format("%10s", timeFormat.buildTimeString(this.validFrom));
            f.format("%2s", "..");
            f.format("%10s", timeFormat.buildTimeString(this.validUntil));
        }
        out += sb + "\n";
        return out;
    }


}

