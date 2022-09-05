package desmoj.extensions.visualization2d.animation;

import java.awt.Point;

/**
 * Extension of Position, used for static entities, that can be rotated or mirrored
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
public class PositionExt extends Position {

    private final double angle;
    private final boolean direction;

    /**
     * constructor with full parameterization
     *
     * @param view      name of view plane, not used in moment
     * @param point     point in a plane
     * @param angle     rotation angle [radiant]
     * @param direction true: normal, false: mirrored
     */
    public PositionExt(String view, Point point, double angle, boolean direction) {
        super(view, point);
        this.angle = angle;
        this.direction = direction;
    }

    /**
     * constructor with default plane
     *
     * @param point     point in a plane
     * @param angle     rotation angle [radiant]
     * @param direction true: normal, false: mirrored
     */
    public PositionExt(Point point, double angle, boolean direction) {
        this(null, point, angle, direction);
    }

    /**
     * constructor with default plane
     *
     * @param x         x coordinate of point
     * @param y         y coordinate of point
     * @param angle     rotation angle [radiant]
     * @param direction true: normal, false: mirrored
     */
    public PositionExt(int x, int y, double angle, boolean direction) {
        this(null, new Point(x, y), angle, direction);
    }

    public double getAngle() {
        return this.angle;
    }

    public boolean getDirection() {
        return this.direction;
    }
}
