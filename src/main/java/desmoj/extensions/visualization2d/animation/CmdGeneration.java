package desmoj.extensions.visualization2d.animation;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.TimeInstant;
import desmoj.extensions.visualization2d.animation.internalTools.SimulationException;
import desmoj.extensions.visualization2d.engine.command.Command;
import desmoj.extensions.visualization2d.engine.command.CommandException;
import desmoj.extensions.visualization2d.engine.command.WriteCmds;

/**
 * CmdGeneration manages cmd- and log-file generation writing simTimeBound command and converts Simtime to
 * animationTime
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
public class CmdGeneration extends WriteCmds {

    /**
     * A short cut to the default time zone (thanks to Marcin Kawelski).
     */
    private final static TimeZone DEFAULT_PREFERRED_TIMEZONE = TimeZone.getTimeZone("UTC");
    private URL iconPathURL = null;
    private Experiment experiment = null;
    private TimeInstant experimentBegin = null;
    private TimeInstant experimentEnd = null;
    private TimeZone timezone = null;
    private boolean initPhase = true;
    private Map<String, Long> nameMap = null;
    private boolean checkAndLogOn = false;

    /**
     * Supportmethods of cmds-file-generation. Opens cmds- and log-file and
     *
     * @param cmdFileName
     * @param logFileName
     * @param iconPathURL
     * @throws UnsupportedEncodingException
     */
    public CmdGeneration(String cmdFileName, String logFileName, URL iconPathURL) {
        super(cmdFileName, logFileName, iconPathURL);
        this.cmdFileName = cmdFileName;
        this.logFileName = logFileName;
        this.iconPathURL = iconPathURL;
        this.nameMap = new HashMap<String, Long>();
        this.checkAndLogOn = false;    //Default
    }

    /**
     * Defines simulation start- and stop-time. Must be between experiment constructor and model-experiment connect.<br>
     * Attention: It makes no sence to define a TimeInstant before experiment constructor.
     *
     * @param begin    Begin of simulation
     * @param end      End of simulation
     * @param timezone Timezone of simulation, UTC when null
     */
    public void setStartStopTime(TimeInstant begin, TimeInstant end, TimeZone timezone) {
        this.initPhase = true;
        this.experimentBegin = begin;
        if (this.experimentBegin == null) {
            throw new SimulationException("ExperimentBegin-Parameter is null");
        }
        this.experimentEnd = end;
        this.timezone = timezone;
        if (this.timezone == null) {
            this.timezone = DEFAULT_PREFERRED_TIMEZONE;
        }
    }

    /**
     * close cmds- and log-file
     */
    public void close() {
        super.close();
    }

    public String getCmdFileName() {
        return this.cmdFileName;
    }

    public String getLogFileName() {
        return this.logFileName;
    }

    /**
     * Only when checkAndLog is set the Method CheckAndLog is active. Default is false. The Method checkAndLog makes a
     * syntax and semantic check of the generated animation commands. The commands are also logged in logFileName from
     * constructor. For semantic check the whole animation environment is build in heap. This consumes a lot of time and
     * heap space. Normally the CheckAndLog Method is switched off.
     */
    public void setCheckAndLog() {
        this.checkAndLogOn = true;
    }

    /**
     * sets the number of comands, when CommandFrame.writeCmdBuffer is flushed in cmdFile. Default is 1000 A small value
     * reduces the uses buffer space and may be increases the running time.
     *
     * @param flushSeq
     */
    public void setFlushSeq(int flushSeq) {
        super.setFlushSeq(flushSeq);
    }

    /**
     * get CmdFileName as File-URL
     *
     * @return
     */
    public URL getCmdFileURL() {
        URL url = null;
        try {
            url = (new File(this.cmdFileName)).toURI().toURL();
        } catch (MalformedURLException e) {
            throw new SimulationException(
                "Fehler bei Umwandlung von cmdFileName in File-URL: " + this.cmdFileName);
        }
        return url;
    }

    public URL getIconPathURL() {
        return this.iconPathURL;
    }

    /**
     * write a command in CommandFrame.writeCmdBuffer. When the buffer has more than this.flushSeq comands, the buffer
     * is flushed in cmdFile
     *
     * @param c Command to write
     */
    public void write(Command c) {
        super.write(c);
    }

    /**
     * The Method checkAndLog makes a syntax and semantic check of the generated animation commands. The commands are
     * also logged in logFileName from constructor. For semantic check the whole animation environment is build in heap.
     * This consumes a lot of time and heap space. This Method works only when its switched on with setCheckAndLog()
     * Method. Normally the CheckAndLog Method is switched off.
     *
     * @param c Command to write
     */
    public void checkAndLog(Command c) {
        if (this.checkAndLogOn) {
            super.checkAndLog(c);
        }
    }

    /**
     * say true when simulation isn't started
     *
     * @return
     */
    public boolean isInitPhase() {
        return this.initPhase;
    }

    public TimeInstant getInitTime() {
        return this.experimentBegin;
    }

    /**
     * sets begin and end time of experiment, create cmds command "createSimTimeBounds" and starts experiment
     *
     * @param exp
     * @param begin
     * @param end
     * @param timezone
     * @param initSpeed
     */
    public void experimentStart(Experiment exp, double initSpeed) {

        this.experiment = exp;
        if (this.experiment == null) {
            throw new SimulationException("Experiment-Parameter is null");
        }
        if (this.experimentEnd != null) {
            this.experiment.stop(this.experimentEnd);
        }

        Command c;
        try {
            c = Command.getCommandInit("createSimTimeBounds", this.getAnimationTimeInit());
            c.addParameter("Begin", Long.toString(this.getAnimationTimeInit()));
            if (exp.getStopTime() != null) {
                c.addParameter("End", Long.toString(this.getAnimationTime(exp.getStopTime())));
            }
            c.addParameter("TimeZone", this.timezone.getID());
            c.addParameter("Speed", Double.toString(initSpeed));
            c.setRemark("erzeugt in CmdGeneration.experimentStart");
            this.checkAndLog(c);
            this.write(c);
        } catch (CommandException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        this.initPhase = false;
        System.out.println("cmdGen: begin of Experiment");
        this.experiment.start(this.experimentBegin);
        System.out.println("cmdGen: End of Experiment");
    }

    /**
     * get Unix TimeStamp of animation begin
     *
     * @return
     */
    public long getAnimationTimeInit() {
        return this.experimentBegin.getTimeRounded(TimeUnit.MILLISECONDS);
    }


    /**
     * get Unix TimeStamp of Time t, shown in viewer
     *
     * @param t TimeInstant, used in desmoj
     * @return computed Time-stamp
     */
    public long getAnimationTime(TimeInstant t) {
        long time = t.getTimeRounded(TimeUnit.MILLISECONDS);
        return time;
    }

    /**
     * distance between actual simTime and initTime as UnixTimeStamp
     *
     * @param t SimTime distance
     * @return computed AnimationTime distance
     */
    public long getAnimationRunTime(TimeInstant t) {
        return getAnimationTime(t) - getAnimationTimeInit();
    }

    public TimeZone getTimeZone() {
        return this.timezone;
    }

    public void setTimeZone(TimeZone timezone) {
        this.timezone = timezone;
    }

    /**
     * create an internal Id based on name. This id is used as an internal id by building animation objects.
     *
     * @param name
     * @return
     */
    public String createInternId(String name) {
        Long value;
        if (this.nameMap.containsKey(name)) {
            value = this.nameMap.get(name) + 1;
            this.nameMap.put(name, value);
        } else {
            value = new Long(0);
            this.nameMap.put(name, value);
        }
        return name + "@" + value;
    }

}
