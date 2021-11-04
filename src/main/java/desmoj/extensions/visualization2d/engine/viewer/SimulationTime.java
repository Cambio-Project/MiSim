package desmoj.extensions.visualization2d.engine.viewer;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * The simulationtime is allways computed from real system-time. The simulation-time-value is given by a long an can be
 * interpreted in a java-typical way as a time-value.
 * <p>
 * Normally the time runs with a specified speed. It's can started, halted (pause), continued and stopped. In a
 * trace-mode the time can step from one value to the next.
 *
 * @author christian.mueller@th-wildau.de For information about subproject: desmoj.extensions.visualization2d please
 *     have a look at: http://www.th-wildau.de/cmueller/Desmo-J/Visualization2d/
 *     <p>
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 *     with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *     <p>
 *     Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *     the specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 */
public class SimulationTime {

    public static final int SHOW_DATE = 1;
    public static final int SHOW_TIME = 2;
    public static final int SHOW_MILLIS = 4;
    public static final int SHOW_DATE_TIME = 3;
    public static final int SHOW_DATE_TIME_MILLIS = 7;
    public static final int SHOW_TIME_MILLIS = 6;
    public static final int SHOW_DAY = 8;
    public static final int SHOW_DST = 16;
    public static final int SHOW_TIMEZONE = 32;
    public static final int SHOW_DAY_DATE_TIME_MILLIS_DST = 31;

    private static TimeZone timezone = TimeZone.getTimeZone("UTC");
    private static Locale locale = Locale.ENGLISH;

    private final long simulationStart;
    private final long simulationEnd;
    private long simulationTime;
    private double speed;
    private boolean running = false; // between start and stop
    private boolean pause = false; // between pause and continue

    /**
     * Constructor for SimulationTime
     *
     * @param start    start-time
     * @param end      end-time
     * @param speed    SimulationTime runs speed times fast as natural time
     * @param timezone default is UTC
     * @param locale   default is Locale.ENGLISH
     */
    public SimulationTime(long start, long end, double speed, TimeZone timezone, Locale locale) {
        if (end == Long.MIN_VALUE) {
            this.simulationStart = start;
            this.simulationEnd = end;
        } else {
            this.simulationStart = Math.min(start, end);
            this.simulationEnd = Math.max(start, end);
        }
        if (timezone != null) {
            SimulationTime.timezone = timezone;
        }
        if (locale != null) {
            SimulationTime.locale = locale;
        }
        this.speed = Math.abs(speed);
        this.running = false;
        this.pause = false;
    }

    /**
     * convert the time value to human-readable format. time == Long.MIN_VALUE is written as "".
     *
     * @param time   internal time as long
     * @param format timeformat for output
     * @return time value to human-readable format
     */
    public static String getTimeString(long time, int format) {
        if (time == Long.MIN_VALUE) {
            return "";
        }
        Calendar cal = Calendar.getInstance(SimulationTime.timezone, SimulationTime.locale);
        cal.setTimeInMillis(time);
        String out = "";
        int v = 0;
        if ((format & 8) == 8) {
            out += getDayName(cal) + ", ";
        }
        if ((format & 1) == 1) {
            v = cal.get(Calendar.DATE);
            out += String.format("%02d", v) + ".";
            v = cal.get(Calendar.MONTH) + 1;
            out += String.format("%02d", v) + ".";
            v = cal.get(Calendar.YEAR);
            out += String.format("%04d", v);
            out += "  ";
        }
        if ((format & 2) == 2) {
            v = cal.get(Calendar.HOUR_OF_DAY);
            out += String.format("%02d", v);
            v = cal.get(Calendar.MINUTE);
            out += ":" + String.format("%02d", v);
            v = cal.get(Calendar.SECOND);
            out += ":" + String.format("%02d", v);
        }
        if ((format & 4) == 4) {
            v = cal.get(Calendar.MILLISECOND);
            out += "." + String.format("%03d", v);
        }
        if ((format & 16) == 16) {
            if (cal.getTimeZone().inDaylightTime(cal.getTime())) {
                out += " DST";
            }
        }
        if ((format & 32) == 32) {
            out += " " + cal.getTimeZone().getDisplayName();
        }
        return out.trim();
    }

    public static String getTimeZoneString() {
        return SimulationTime.timezone.getDisplayName(locale);
    }

    private static String getDayName(Calendar cal) {
        String dayName = "";
        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
            dayName = "Mon";
        }
        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY) {
            dayName = "Tue";
        }
        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY) {
            dayName = "Wed";
        }
        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY) {
            dayName = "Thu";
        }
        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
            dayName = "Fri";
        }
        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
            dayName = "Sat";
        }
        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            dayName = "Sun";
        }
        return dayName;
    }

    /**
     * starts SimulationTime running by start-time
     */
    public void start() {
        //System.out.println("SimulationTime.start");
        this.running = true;
        this.pause = false;
    }

    /**
     * stops SimulationTime
     */
    public void stop() {
        //System.out.println("SimulationTime.stop");
        this.running = false;
        this.pause = false;
    }

    /**
     * lets pause SimulationTime
     */
    public void pause() {
        //System.out.println("SimulationTime.pause");
        this.pause = true;
    }

    /**
     * when SimulationTime is on pause, then lets it go running. In trace-mode it jumps to next step, set by
     * setNextStep().
     */
    public void cont() {
        //System.out.println("SimulationTime.cont");
        if (this.running && this.pause) {
            this.pause = false;
        }
    }

    /**
     * get simulation-speed
     *
     * @return speed, stored in SimulationTime
     */
    public double getSpeed() {
        return this.speed;
    }

    /**
     * updates simulation-speed
     *
     * @param speed
     */
    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public long getSimulationTime() {
        return this.simulationTime;
    }

    /**
     * set simulation-time
     *
     * @param time
     */
    public void setSimulationTime(long time) {
        this.simulationTime = time;
    }

    /**
     * means SimulationTime is started, not stopped and simulationEnd inn't reached.
     *
     * @return true, when running
     */
    public boolean isRunning() {
        return this.running;
    }

    public boolean isPause() {
        return this.pause;
    }

    public long getSimulationStart() {
        return this.simulationStart;
    }

    public long getSimulationEnd() {
        return this.simulationEnd;
    }
}
