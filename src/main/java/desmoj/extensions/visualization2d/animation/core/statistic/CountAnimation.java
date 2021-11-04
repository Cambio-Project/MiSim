package desmoj.extensions.visualization2d.animation.core.statistic;

import java.awt.Dimension;
import java.awt.Point;

import desmoj.core.simulator.TimeInstant;
import desmoj.core.statistic.Count;
import desmoj.extensions.visualization2d.animation.CmdGeneration;
import desmoj.extensions.visualization2d.animation.Form;
import desmoj.extensions.visualization2d.animation.Position;
import desmoj.extensions.visualization2d.animation.core.simulator.ModelAnimation;
import desmoj.extensions.visualization2d.engine.command.Command;
import desmoj.extensions.visualization2d.engine.command.CommandException;
import desmoj.extensions.visualization2d.engine.command.Parameter;
import desmoj.extensions.visualization2d.engine.model.Statistic;
import desmoj.extensions.visualization2d.engine.modelGrafic.StatisticGrafic;


/**
 * Animation of Count Integer values will be aggregated TypeData: 	Statistic.DATA_TimeSeries TypeIndex:
 * Statistic.INDEX_Mean_StdDev
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
public class CountAnimation extends Count {

    private boolean showInAnimation;
    private CmdGeneration cmdGen = null;
    private final String id;

    /**
     * Constructor with the same parameters as in Count
     *
     * @param owner        used model
     * @param name         name of process-station
     * @param showInReport as in Count
     * @param showInTrace  as in Count
     */
    public CountAnimation(ModelAnimation owner, String name,
                          boolean showInReport, boolean showInTrace) {

        super(owner, name, showInReport, showInTrace);
        this.showInAnimation = false;
        this.cmdGen = owner.getCmdGen();
        this.id = this.cmdGen.createInternId(name);
    }

    /**
     * create standard animation
     *
     * @param pos             middle point of animation object
     * @param form            form of animation object
     * @param showInAnimation switch animation on or off
     */
    public void createAnimation(Position pos, Form form, boolean showInAnimation) {
        this.createAnimation(new TimeInstant(1.0), 0.0, 1.0, 10,
            pos, form, showInAnimation);
    }

    /**
     * create animation with full parameterization
     *
     * @param highTime        initial time line from now until highTime
     * @param valMin          initial value line from valMin to valMax
     * @param valMax          initial value line from valMin to valMax
     * @param histogramCells  no of histogram cells between valMin and valMax 0 means, there is no histogram support.
     * @param pos             middle point of animation object
     * @param form            form of animation object
     * @param showInAnimation switch animation on or off
     */
    public void createAnimation(TimeInstant highTime, double valMin, double valMax,
                                int histogramCells, Position pos, Form form, boolean showInAnimation) {

        long timeMin, timeMax;
        this.showInAnimation = showInAnimation;
        TimeInstant simTime = this.getModel().presentTime();
        boolean init = this.cmdGen.isInitPhase();
        Command c;
        Point p = pos.getPoint();
        Dimension deltaSize = form.getDeltaSize();

        timeMin = this.cmdGen.getAnimationTime(simTime);
        timeMax = this.cmdGen.getAnimationTime(highTime);
        String[] timeBounds = {Long.toString(timeMin), Long.toString(timeMax)};
        String[] valueBounds = {Double.toString(valMin), Double.toString(valMax)};
        String[] point = {pos.getView(), Integer.toString(p.x), Integer.toString(p.y)};
        if (deltaSize == null) {
            deltaSize = new Dimension(0, 0);
        }
        String[] deltaSize1 = {Integer.toString(deltaSize.width), Integer.toString(deltaSize.height)};
        if (this.showInAnimation) {
            try {
                if (init) {
                    c = Command.getCommandInit("createStatistic", this.cmdGen.getAnimationTime(simTime));
                } else {
                    c = Command.getCommandTime("createStatistic", this.cmdGen.getAnimationTime(simTime));
                }
                c.addParameter("StatisticId", this.id);
                c.addParameter("Name", this.getName());
                c.addParameter("TypeData", Statistic.DATA_TimeSeries + "");
                c.addParameter("TypeIndex", Statistic.INDEX_Mean_StdDev + "");
                c.addParameter("Aggregate", "");
                c.addParameter("TimeBounds", Parameter.cat(timeBounds));
                c.addParameter("ValueBounds", Parameter.cat(valueBounds));
                c.addParameter("HistogramCells", histogramCells + "");
                c.addParameter("Point", Parameter.cat(point));
                c.addParameter("TypeAnimation", StatisticGrafic.ANIMATION_LastValue + "");
                c.addParameter("IsIntValue", "");
                c.addParameter("DeltaSize", Parameter.cat(deltaSize1));
                c.setRemark(this.getGeneratedBy(CountAnimation.class.getSimpleName()));
                cmdGen.checkAndLog(c);
                cmdGen.write(c);

            } catch (CommandException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            update(0);
        }
    }

    /**
     * Add a value to statistical object. The simTime is picked up automatically.
     *
     * @param value add a value
     */
    public void update(long value) {
        super.update(value);
        TimeInstant simTime = this.getModel().presentTime();
        boolean init = this.cmdGen.isInitPhase();
        Command c;
        if (this.showInAnimation) {
            try {
                if (init) {
                    c = Command.getCommandInit("setStatistic", this.cmdGen.getAnimationTime(simTime));
                } else {
                    c = Command.getCommandTime("setStatistic", this.cmdGen.getAnimationTime(simTime));
                }
                c.addParameter("StatisticId", this.id);
                c.addParameter("Value", (double) value + "");
                c.setRemark(this.getGeneratedBy(CountAnimation.class.getSimpleName()));
                cmdGen.checkAndLog(c);
                cmdGen.write(c);
            } catch (CommandException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }

    /**
     *
     */
    public void reset() {
        super.reset();
        TimeInstant simTime = this.getModel().presentTime();
        boolean init = this.cmdGen.isInitPhase();
        Command c;
        if (this.showInAnimation) {
            try {
                if (init) {
                    c = Command.getCommandInit("resetStatistic", this.cmdGen.getAnimationTime(simTime));
                } else {
                    c = Command.getCommandTime("resetStatistic", this.cmdGen.getAnimationTime(simTime));
                }
                c.addParameter("StatisticId", this.id);
                c.setRemark(this.getGeneratedBy(CountAnimation.class.getSimpleName()));
                cmdGen.checkAndLog(c);
                cmdGen.write(c);
            } catch (CommandException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }

    private String getGeneratedBy(String name) {
        String out = "generated by " + name + " and called by ";
        if (this.currentSimProcess() != null) {
            out += this.currentSimProcess().getName();
        } else {
            out += this.currentModel().getName();
        }
        return out;
    }

}
