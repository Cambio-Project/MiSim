package desmoj.extensions.visualization2d.animation;

import java.awt.Point;

import desmoj.extensions.visualization2d.animation.internalTools.SimulationException;


/**
 * Describes the middle point of an simulation object. View is at moment unused.
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
public class Position {

    private String view;
    private final Point point;

    /**
     * describe a point in a plane "view"
     *
     * @param view  name of view plane, not used in moment
     * @param point point in a plane
     */
    public Position(String view, Point point) {
        this.view = view;
        this.point = point;
        if (this.view == null) {
            this.view = "main";
        }
        if (this.point == null) {
            throw new SimulationException("Position: Point is null");
        }
    }

    public Position(String view, int x, int y) {
        this(view, new Point(x, y));
    }

    public Position(int x, int y) {
        this(null, new Point(x, y));
    }

    public String getView() {
        return this.view;
    }

    public Point getPoint() {
        return this.point;
    }
}
