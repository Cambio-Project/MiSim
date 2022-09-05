package desmoj.extensions.visualization2d.engine.model;

import java.awt.Color;
import java.awt.Point;

import desmoj.extensions.visualization2d.engine.modelGrafic.BackgroundLineGrafic;
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
public class BackgroundLine implements Basic {

    public static final int LINE_Size_Normal = 2;
    public static final int LINE_Size_Small = 1;
    public static final int LINE_Size_Big = 4;

    /**
     * hashtable with all process-instances
     */
    private final String id;
    private final double level;                    // Zeichenreihenfolge
    private final int lineSize;                // siehe LINE_SIZE_...
    private Grafic grafic;
    private final Model model;

    /**
     * Background element to paint a line
     *
     * @param model
     * @param id       id of background element
     * @param lineSize
     * @param level    elements with low level are painted in front of elements with high level
     */
    public BackgroundLine(Model model, String id, int lineSize, double level) {
        this.model = model;
        this.id = id;
        this.level = level;
        this.lineSize = lineSize;
        this.grafic = null;
        if (this.id != null) {
            model.getBackgroundLines().add(this);
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

    public String getName() {
        return this.id;
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
     * Get line size of background element. for values look at LINE_SIZE_...
     *
     * @return
     */
    public int getLineSize() {
        return this.lineSize;
    }

    /**
     * Create Gragic of BackgroundLine
     *
     * @param viewId
     * @param points
     * @param color
     * @return
     */
    public Grafic createGrafic(String viewId, Point[] points, Color color) {
        this.grafic = new BackgroundLineGrafic(this, viewId, points, color);
        return this.grafic;
    }

    /**
     * get ProcessGrafic, created before
     */
    public Grafic getGrafic() {
        return grafic;
    }


}
