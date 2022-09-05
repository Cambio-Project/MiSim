package desmoj.extensions.visualization2d.animation;

import java.awt.Color;
import java.awt.Point;

import desmoj.core.simulator.TimeInstant;
import desmoj.extensions.visualization2d.animation.core.simulator.ModelAnimation;
import desmoj.extensions.visualization2d.animation.internalTools.SimulationException;
import desmoj.extensions.visualization2d.engine.command.Command;
import desmoj.extensions.visualization2d.engine.command.CommandException;
import desmoj.extensions.visualization2d.engine.command.Parameter;
import desmoj.extensions.visualization2d.engine.model.BackgroundLine;

/**
 * Background elements are rectangles. There are two types: - One has fixed corner points and - the other has a fixed
 * middle point. The size is set also or comes from the text element. It is possible to show a text in the rectangle.
 * There is a automatic word wrapping. For an explicit newline use a <br> tag. The text- position, size and style can be
 * set explicitly. The colors of text and background can be set also. When background is null, the background is
 * transparent. The level value determine the order of painting. Elements with low level value are painted in front of
 * elements with high level.
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
public class BackgroundLineAnimation {

    private final boolean showInAnimation;
    private CmdGeneration cmdGen = null;
    private ModelAnimation model = null;
    private String id = null;
    private int lineSize = BackgroundLine.LINE_Size_Normal;
    private Color lineColor = null;
    private Position startPoint = null;
    private Point[] addPoints = null;

    /**
     * paint a line in background
     *
     * @param owner           Simulation model
     * @param baseId          A internal Name. The framework generate with this name an id
     * @param lineSize        Size of line in pixel. Look at BackgroundLine.LineSize....
     * @param startPoint      Startpoint of Line
     * @param addPoints       Additional points of line
     * @param level           Elements with low level are painted in front of elements with high level
     * @param lineColor       Color of line
     * @param showInAnimation
     */
    public BackgroundLineAnimation(ModelAnimation owner, String baseId, int lineSize,
                                   Position startPoint, Point[] addPoints, double level,
                                   Color lineColor, boolean showInAnimation) {

        this.showInAnimation = showInAnimation;
        this.model = owner;
        this.cmdGen = owner.getCmdGen();
        this.id = this.cmdGen.createInternId(baseId);
        this.lineColor = lineColor;
        this.lineSize = lineSize;
        this.startPoint = startPoint;
        this.addPoints = addPoints;
        TimeInstant simTime = owner.presentTime();
        boolean init = this.cmdGen.isInitPhase();
        Command c;

        if (this.addPoints == null || this.addPoints.length == 0) {
            throw new SimulationException("Here are no additional points!!!");
        }
        if (this.startPoint == null) {
            throw new SimulationException("Here is no startpoint!!!");
        }
        if (this.lineColor == null) {
            throw new SimulationException("Here are no line color!!!");
        }

        if (this.showInAnimation) {
            try {
                if (init) {
                    c = Command.getCommandInit("createBackgroundLine", this.cmdGen.getAnimationTime(simTime));
                } else {
                    c = Command.getCommandTime("createBackgroundLine", this.cmdGen.getAnimationTime(simTime));
                }
                c.addParameter("BgLineId", id);
                c.addParameter("LineSize", Integer.toString(this.lineSize));
                c.addParameter("StartPoint", this.startPoint.getView() + "|" + this.startPoint.getPoint().x + "|" +
                    this.startPoint.getPoint().y);
                if (this.addPoints != null) {
                    for (int i = 0; i < this.addPoints.length; i++) {
                        Point p = this.addPoints[i];
                        c.addParameter("AddPoint", p.x + "|" + p.y);
                    }
                }
                if (this.lineColor != null) {
                    String[] lineColor1 = {Integer.toString(this.lineColor.getRed()),
                        Integer.toString(this.lineColor.getGreen()), Integer.toString(this.lineColor.getBlue())};
                    c.addParameter("Color", Parameter.cat(lineColor1));
                }
                c.addParameter("Level", Double.toString(level));
                c.setRemark("erzeugt von BackgroundLineAnimation");
                cmdGen.checkAndLog(c);
                cmdGen.write(c);

            } catch (CommandException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}
