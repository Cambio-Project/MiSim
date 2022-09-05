package desmoj.extensions.visualization2d.engine.model;

import java.awt.Point;

import desmoj.extensions.visualization2d.engine.modelGrafic.Grafic;
import desmoj.extensions.visualization2d.engine.modelGrafic.StationGrafic;


/**
 * A station is a start- or end-point of a route
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
public class Station implements Basic {

    private final String id;
    private String name;
    private StationGrafic grafic;
    private final Model model;

    /**
     * @param model used animation.model.Model
     * @param id    must be unique
     */
    public Station(Model model, String id) {
        this.model = model;
        this.id = id;
        this.name = null;
        if (this.id != null) {
            model.getStations().add(this);
        }

    }

    public Model getModel() {
        return this.model;
    }

    public String getId() {
        return this.id;
    }

    /**
     * get name of station, can be null
     */
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * create stationGrafic
     *
     * @param x middlepoint x-coordinate
     * @param y middlepoint y-coordinate
     * @return StationGrafic
     */
    public Grafic createGrafic(String viewId, int x, int y) {
        this.grafic = new StationGrafic(this, viewId, new Point(x, y));
        return this.grafic;
    }

    /**
     * get stationGrafic created before
     */
    public Grafic getGrafic() {
        return this.grafic;
    }

}
