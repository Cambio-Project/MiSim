package desmoj.extensions.visualization2d.animation;

import java.awt.Color;
import java.awt.Point;

import desmoj.core.simulator.TimeInstant;
import desmoj.extensions.visualization2d.animation.core.simulator.ModelAnimation;
import desmoj.extensions.visualization2d.animation.internalTools.SimulationException;
import desmoj.extensions.visualization2d.engine.command.Command;
import desmoj.extensions.visualization2d.engine.command.CommandException;
import desmoj.extensions.visualization2d.engine.command.Parameter;
import desmoj.extensions.visualization2d.engine.model.BackgroundElement;

/**
 * Background elements are rectangles. There are two types: - One has fixed corner points and - the other has a fixed
 * middle point. The size is set also or comes from the text element. It is possible to show a text in the rectangle.
 * There is a automatic word wrapping. For an explicit newline use a <br> tag. The text- position, size and style can be
 * set explicitly. The colors of text and background can be set also. When background is null, the background is
 * transparent. It is also possible to show an image on background. This image must loaded before in ModelAnimation. The
 * level value determine the order of painting. Elements with low level value are painted in front of elements with high
 * level.
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
public class BackgroundElementAnimation {

    private final boolean showInAnimation;
    private CmdGeneration cmdGen = null;
    private ModelAnimation model = null;
    private String id = null;
    private String text = null;
    private int textPosition = BackgroundElement.TEXT_POSITION_Middle;
    private int textSize = BackgroundElement.TEXT_Size_Normal;
    private int textStyle = BackgroundElement.TEXT_Style_Plain;
    private Color textColor = null;
    private Color background = null;
    private String imageId = null;

    /**
     * Build background element with fixed corners
     *
     * @param owner           Simulation model
     * @param baseId          A internal Name. The framework generate with this name an id
     * @param name            Can be null, when not null a border with the name is shown.
     * @param text            The text element, that is shown in background element, with automatic word wrapping. Use
     *                        <br> for a new line.
     * @param textPosition    For values look at BackgroundElement.TEXT_POSITION_...
     * @param textSize        For values look at BackgroundElement.TEXT_SIZE_...
     * @param textStyle       For values look at BackgroundElement.TEXT_STYLE_...
     * @param topLeft         Coordinate of topLeft corner point
     * @param bottomRight     Coordinate of bottomRight corner point
     * @param level           Elements with low level are painted in front of elements with high level
     * @param fg              Text color
     * @param bg              Background color, when null then transparent
     * @param showInAnimation
     */
    public BackgroundElementAnimation(ModelAnimation owner, String baseId, String name,
                                      String text, int textPosition, int textSize, int textStyle,
                                      Position topLeft, Position bottomRight, double level, Color fg, Color bg,
                                      boolean showInAnimation) {

        this.showInAnimation = showInAnimation;
        this.model = owner;
        this.cmdGen = owner.getCmdGen();
        this.id = this.cmdGen.createInternId(baseId);
        this.text = text;
        this.textPosition = textPosition;
        this.textSize = textSize;
        this.textStyle = textStyle;
        this.textColor = fg;
        this.background = bg;
        TimeInstant simTime = owner.presentTime();
        boolean init = this.cmdGen.isInitPhase();
        Command c;
        Point pTopLeft = topLeft.getPoint();
        Point pBottomRight = bottomRight.getPoint();

        // Consistent checks
        if (topLeft == null) {
            throw new SimulationException("topLeft is null");
        }
        if (bottomRight == null) {
            throw new SimulationException("bottomRight is null");
        }
        if (this.text != null && this.textColor == null) {
            throw new SimulationException("text has a value and foreground is null");
        }
        if ((pTopLeft.x > pBottomRight.x) || (pTopLeft.y > pBottomRight.y)) {
            throw new SimulationException("topLeft: (" + pTopLeft.x + "," + pTopLeft.y + ") and " +
                "bottomRight: (" + pBottomRight.x + "," + pBottomRight.y + ") don't build a rectangle.");
        }
        if (!topLeft.getView().equals(bottomRight.getView())) {
            throw new SimulationException("topLeft and bottomRight don't have the same view.");
        }
        if (this.text.trim().equals("")) {
            this.text = ".";
        }

        if (this.showInAnimation) {
            try {
                if (init) {
                    c = Command.getCommandInit("createBackgroundElement", this.cmdGen.getAnimationTime(simTime));
                } else {
                    c = Command.getCommandTime("createBackgroundElement", this.cmdGen.getAnimationTime(simTime));
                }
                c.addParameter("BgElemId", id);
                if (name != null) {
                    c.addParameter("Name", name);
                }
                if (text != null) {
                    String[] text1 = {this.text, Integer.toString(this.textPosition),
                        Integer.toString(this.textSize), Integer.toString(this.textStyle)};
                    c.addParameter("Text", Parameter.cat(text1));
                }
                if (topLeft != null) {
                    String[] topLeft1 = {topLeft.getView(), Integer.toString(pTopLeft.x), Integer.toString(pTopLeft.y)};
                    c.addParameter("TopLeft", Parameter.cat(topLeft1));
                }
                if (bottomRight != null) {
                    String[] bottomRight1 =
                        {bottomRight.getView(), Integer.toString(pBottomRight.x), Integer.toString(pBottomRight.y)};
                    c.addParameter("BottomRight", Parameter.cat(bottomRight1));
                }
                if (this.textColor != null) {
                    String[] foreground = {Integer.toString(this.textColor.getRed()),
                        Integer.toString(this.textColor.getGreen()), Integer.toString(this.textColor.getBlue())};
                    c.addParameter("Foreground", Parameter.cat(foreground));
                }
                if (this.background != null) {
                    String[] background = {Integer.toString(this.background.getRed()),
                        Integer.toString(this.background.getGreen()), Integer.toString(this.background.getBlue())};
                    c.addParameter("Background", Parameter.cat(background));
                }
                c.addParameter("Level", Double.toString(level));
                c.setRemark("erzeugt von BackgroundElementAnimation");
                cmdGen.checkAndLog(c);
                cmdGen.write(c);

            } catch (CommandException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * BackgroundElement with fixed middle point
     *
     * @param owner           Simulation model
     * @param baseId          A internal Name. The framework generate with this name an id
     * @param name            Can be null, when not null a border with the name is shown.
     * @param text            The text element, that is shown in background element, with automatic word wrapping. Use
     *                        <br> for a new line.
     * @param textPosition    For values look at BackgroundElement.TEXT_POSITION_...
     * @param textSize        For values look at BackgroundElement.TEXT_SIZE_...
     * @param textStyle       For values look at BackgroundElement.TEXT_STYLE_...
     * @param level           Elements with low level are painted in front of elements with high level
     * @param middle          Middle point of background element
     * @param form            Form of background element, when null minimal size of text is used.
     * @param fg              Text color
     * @param bg              Background color, when null then transparent
     * @param showInAnimation
     */
    public BackgroundElementAnimation(ModelAnimation owner, String baseId, String name,
                                      String text, int textPosition, int textSize, int textStyle,
                                      double level, Position middle, Form form, Color fg, Color bg,
                                      boolean showInAnimation) {

        this.showInAnimation = showInAnimation;
        this.model = owner;
        this.cmdGen = owner.getCmdGen();
        this.id = this.cmdGen.createInternId(baseId);
        this.text = text;
        this.textPosition = textPosition;
        this.textSize = textSize;
        this.textStyle = textStyle;
        this.textColor = fg;
        this.background = bg;
        TimeInstant simTime = owner.presentTime();
        boolean init = this.cmdGen.isInitPhase();
        Command c;

        // Consistent checks
        if (middle == null) {
            throw new SimulationException("middle is null");
        }
        if (this.text == null && form == null) {
            throw new SimulationException("text and size are null");
        }
        if (this.text != null && this.textColor == null) {
            throw new SimulationException("text has a value and foreground is null");
        }
        if (this.text == null || this.text.trim().equals("")) {
            this.text = ".";
        }

        if (this.showInAnimation) {
            try {
                if (init) {
                    c = Command.getCommandInit("createBackgroundElement", this.cmdGen.getAnimationTime(simTime));
                } else {
                    c = Command.getCommandTime("createBackgroundElement", this.cmdGen.getAnimationTime(simTime));
                }
                c.addParameter("BgElemId", id);
                if (name != null) {
                    c.addParameter("Name", name);
                }
                if (this.text != null) {
                    String[] text1 = {this.text, Integer.toString(this.textPosition),
                        Integer.toString(this.textSize), Integer.toString(this.textStyle)};
                    c.addParameter("Text", Parameter.cat(text1));
                }
                if (middle != null) {
                    String[] middle1 = {middle.getView(), Integer.toString(middle.getPoint().x),
                        Integer.toString(middle.getPoint().y)};
                    c.addParameter("Middle", Parameter.cat(middle1));
                }
                if (form != null) {
                    String[] size1 = {Integer.toString(form.getDeltaSize().width),
                        Integer.toString(form.getDeltaSize().height)};
                    c.addParameter("Size", Parameter.cat(size1));
                }
                if (this.textColor != null) {
                    String[] foreground = {Integer.toString(this.textColor.getRed()),
                        Integer.toString(this.textColor.getGreen()), Integer.toString(this.textColor.getBlue())};
                    c.addParameter("Foreground", Parameter.cat(foreground));
                }
                if (this.background != null) {
                    String[] background = {Integer.toString(this.background.getRed()),
                        Integer.toString(this.background.getGreen()), Integer.toString(this.background.getBlue())};
                    c.addParameter("Background", Parameter.cat(background));
                }
                c.addParameter("Level", Double.toString(level));
                c.setRemark("erzeugt von BackgroundElementAnimation");
                cmdGen.checkAndLog(c);
                cmdGen.write(c);

            } catch (CommandException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * Changes Text of Background Element
     *
     * @param text The text element, that is shown in background element, with automatic word wrapping. Use <br> for a
     *             new line.
     */
    public void setText(String text) {
        this.text = text.trim().equals("") ? "." : text;
        this.setData();
    }

    /**
     * Changes Position of Text in Background Element
     *
     * @param pos For values look at BackgroundElement.TEXT_POSITION_...
     */
    public void setTextPosition(int pos) {
        this.textPosition = pos;
        this.setData();
    }

    /**
     * Changes Size of Text in Background Element
     *
     * @param size For values look at BackgroundElement.TEXT_SIZE_...
     */
    public void setTextSize(int size) {
        this.textSize = size;
        this.setData();
    }

    /**
     * Changes Style of Text in Background Element
     *
     * @param style For values look at BackgroundElement.TEXT_STYLE_...
     */
    public void setTextStyle(int style) {
        this.textStyle = style;
        this.setData();
    }

    /**
     * Changes Color of Text in Background Element
     *
     * @param fg Text color
     */
    public void setTextColor(Color fg) {
        this.textColor = fg;
        this.setData();
    }

    /**
     * Changes Background Color of Background Element
     *
     * @param bg Background color, when null then transparent
     */
    public void setBackground(Color bg) {
        this.background = bg;
        this.setData();
    }

    /**
     * Changes/set imageId of background element. The imageId must defined before in ModelAnimation.addIcon(..)
     *
     * @param imageId
     */
    public void setImageId(String imageId) {
        this.imageId = imageId;
        this.setData();
    }

    /**
     * send changed data to background grafic
     */
    private void setData() {

        if (this.showInAnimation) {
            TimeInstant simTime = model.presentTime();
            boolean init = this.cmdGen.isInitPhase();
            Command c;
            try {
                if (init) {
                    c = Command.getCommandInit("setBackgroundElement", this.cmdGen.getAnimationTime(simTime));
                } else {
                    c = Command.getCommandTime("setBackgroundElement", this.cmdGen.getAnimationTime(simTime));
                }
                c.addParameter("BgElemId", this.id);
                if (text != null) {
                    String[] text1 = {this.text, Integer.toString(this.textPosition),
                        Integer.toString(this.textSize), Integer.toString(this.textStyle)};
                    c.addParameter("Text", Parameter.cat(text1));
                }
                if (this.textColor != null) {
                    String[] foreground = {Integer.toString(this.textColor.getRed()),
                        Integer.toString(this.textColor.getGreen()), Integer.toString(this.textColor.getBlue())};
                    c.addParameter("Foreground", Parameter.cat(foreground));
                }
                if (this.background != null) {
                    String[] background = {Integer.toString(this.background.getRed()),
                        Integer.toString(this.background.getGreen()), Integer.toString(this.background.getBlue())};
                    c.addParameter("Background", Parameter.cat(background));
                }
                if (this.imageId != null) {
                    c.addParameter("ImageId", this.imageId);
                }
                c.setRemark("erzeugt von BackgroundElementAnimation");
                cmdGen.checkAndLog(c);
                cmdGen.write(c);
            } catch (CommandException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
