package desmoj.extensions.visualization2d.engine.model;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;

import desmoj.extensions.visualization2d.engine.modelGrafic.BackgroundElementGrafic;
import desmoj.extensions.visualization2d.engine.modelGrafic.Grafic;


/**
 * The class describe background elements to give a structure of animation. A background element is a rectangle which
 * includes optional text. The text position in the rectangle and text size and style are determined in the constructor
 * of this class. With a createGrafic Method a BackgroundElementGrafic class is created. There are mainly 2 types: One
 * with dynamic size has fixed corner points of its rectangle. By zooming its showed  size is changed. And one with
 * static size. This has a fixed middle point and fixed showed size. By zooming its showed size is not changed.
 * <p>
 * When text is null, a rectangle without text is build. When background is null, the background of this element is
 * transparent. When name is set, the element has a border with its name. With level is determined which element is
 * located in front of an other.
 * <p>
 * Elements with a low level are painted in front of elements with a high level. All Background elements are located in
 * a background layer.
 * <p>
 * See also in BackgroundElementGrafic.
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
public class BackgroundElement implements Basic {

    public static final int TEXT_POSITION_TopLeft = 0;
    public static final int TEXT_POSITION_TopMiddle = 1;
    public static final int TEXT_POSITION_TopRight = 2;
    public static final int TEXT_POSITION_MiddleLeft = 3;
    public static final int TEXT_POSITION_Middle = 4;
    public static final int TEXT_POSITION_MiddleRight = 5;
    public static final int TEXT_POSITION_BottomLeft = 6;
    public static final int TEXT_POSITION_BottomMiddle = 7;
    public static final int TEXT_POSITION_BottomRight = 8;

    public static final int TEXT_Style_Plain = 0;
    public static final int TEXT_Style_Bold = 1;
    public static final int TEXT_Style_Italic = 2;

    public static final int TEXT_Size_Normal = 10;
    public static final int TEXT_Size_Small = 8;
    public static final int TEXT_Size_Big = 20;

    /**
     * hashtable with all process-instances
     */
    private final String id;
    private String name;
    private final double level;                    // Zeichenreihenfolge
    private String text;
    private String imageId;                // null when no image to paint
    private int textPosition;            // siehe TEXT_POSITION_...
    private int textSize;                // siehe TEXT_SIZE_...
    private int textStyle;                // siehe TEXT_STYLE_...
    private Grafic grafic;
    private final Model model;


    /**
     * Background element to paint a string
     *
     * @param id        id of background element
     * @param text      text to show. When null, no text is shown.
     * @param textSize  for values look at TEXT_SIZE_...
     * @param textStyle for values look at TEXT_STYLE_...
     * @param level     elements with low level are painted in front of elements with high level
     * @param imageId   image for background, maybe null
     */
    public BackgroundElement(Model model, String id, String text,
                             int textSize, int textStyle, double level, String imageId) {
        this.model = model;
        this.id = id;
        this.name = null;
        this.level = level;
        this.text = text;
        this.imageId = imageId;
        this.textPosition = BackgroundElement.TEXT_POSITION_Middle;
        this.textSize = BackgroundElement.TEXT_Size_Normal;
        this.textStyle = BackgroundElement.TEXT_Style_Plain;
        this.grafic = null;
        if (this.id != null) {
            model.getBackgroundElements().add(this);
        }

        if (imageId != null && !this.model.containsImageId(imageId)) {
            throw new ModelException("In BackgroundElement id: " + id +
                "  imageId: " + imageId + "  is unknown.");
        }
    }

    /**
     * Background element with all features
     *
     * @param model        used animation.model.Model
     * @param id           id of background element
     * @param text         text to show. When null, no text is shown.
     * @param textPosition for values look at TEXT_POSITION_...
     * @param textSize     for values look at TEXT_SIZE_...
     * @param textStyle    for values look at TEXT_STYLE_...
     * @param level        elements with low level are painted in front of elements with high level
     * @param imageId      image for background, maybe null
     */
    public BackgroundElement(Model model, String id, String text,
                             int textPosition, int textSize, int textStyle, double level, String imageId) {
        this.model = model;
        this.id = id;
        this.name = null;
        this.level = level;
        this.text = text;
        this.imageId = imageId;
        this.textPosition = textPosition;
        this.textSize = textSize;
        this.textStyle = textStyle;
        this.grafic = null;
        if (this.id != null) {
            model.getBackgroundElements().add(this);
        }

        if (imageId != null && !this.model.containsImageId(imageId)) {
            throw new ModelException("In BackgroundElement id: " + id +
                "  imageId: " + imageId + "  is unknown.");
        }
    }

    public Model getModel() {
        return this.model;
    }


    /**
     * get id of background element
     */
    public String getId() {
        return this.id;
    }

    /**
     * get name of background element
     */
    public String getName() {
        return this.name;
    }

    /**
     * set name of background element
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get level of background element. Elements with low level are painted in front of elements with high level
     *
     * @return
     */
    public double getLevel() {
        return this.level;
    }

    /**
     * Get text of background element.
     *
     * @return
     */
    public String getText() {
        return this.text;
    }

    /**
     * Get ImageId, may be null This id is a key in this.getModel().getImage(id)
     *
     * @return
     */
    public String getImageId() {
        return this.imageId;
    }

    /**
     * Get text position of background element. for values look at TEXT_POSITION_...
     *
     * @return
     */
    public int getTextPosition() {
        return this.textPosition;
    }

    /**
     * Get text size of background element. for values look at TEXT_SIZE_...
     *
     * @return
     */
    public int getTextSize() {
        return this.textSize;
    }

    /**
     * Get text style of background element. for values look at TEXT_STYLE_...
     *
     * @return
     */
    public int getTextStyle() {
        return this.textStyle;
    }

    /**
     * Updates and repaints text properties
     *
     * @param text
     * @param textPosition
     * @param textSize
     * @param textStyle
     * @param foreground
     * @param background
     * @param imageId
     */
    public void setData(String text, int textPosition, int textSize,
                        int textStyle, Color foreground, Color background, String imageId) {
        this.text = text;
        this.textPosition = textPosition;
        this.textSize = textSize;
        this.textStyle = textStyle;
        this.imageId = imageId;
        if (imageId != null && !this.model.containsImageId(imageId)) {
            throw new ModelException("In BackgroundElement id: " + id +
                "  imageId: " + imageId + "  is unknown.");
        }

        if (this.getGrafic() != null) {
            ((BackgroundElementGrafic) this.getGrafic()).update(foreground, background);
        }
    }

    /**
     * create grafic of background element with fixed corners
     *
     * @param x0 x coordinate of top left corner
     * @param y0 y coordinate of top left corner
     * @param x1 x coordinate of bottom right corner
     * @param y1 y coordinate of bottom right corner
     * @param fg text color
     * @param bg background color. When null, the background is transparent
     * @return
     */
    public Grafic createGrafic(String viewId, Point topLeft, Point bottomRight, Color fg, Color bg) {
        this.grafic = new BackgroundElementGrafic(this, viewId, topLeft, bottomRight,
            null, null, fg, bg);
        return this.grafic;
    }

    /**
     * create grafic of background element with fixed middle point and fixed size
     *
     * @param x      x coordinate of the middle point
     * @param y      y coordinate of the middle point
     * @param fg     text color
     * @param bg     background color. When null, the background is transparent
     * @param width  width of background element.
     * @param height heigth of background element.
     * @return
     */
    public Grafic createGrafic(String viewId, Point middle, Color fg, Color bg, Dimension size) {
        this.grafic = new BackgroundElementGrafic(this, viewId, null, null, middle,
            size, fg, bg);
        return this.grafic;
    }

    /**
     * create grafic of background element with fixed middle point The size is the size, that the text need to display
     *
     * @param viewId
     * @param middle
     * @param fg
     * @param bg
     * @return
     */
    public Grafic createGrafic(String viewId, Point middle, Color fg, Color bg) {
        this.grafic = new BackgroundElementGrafic(this, viewId, null, null, middle,
            null, fg, bg);
        return this.grafic;
    }

    /**
     * get ProcessGrafic, created before
     */
    public Grafic getGrafic() {
        return grafic;
    }


}
