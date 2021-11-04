package desmoj.extensions.visualization2d.engine.model;

import java.awt.Color;
import java.awt.Point;
import java.text.DecimalFormat;

import desmoj.extensions.visualization2d.engine.modelGrafic.Grafic;
import desmoj.extensions.visualization2d.engine.modelGrafic.StationGrafic;


/**
 * A route is a polyline between a start- and a end-station. The line has a length and an entity can move on it with a
 * velocity stored in its velocity-attribute
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
public class Route extends Container<Long> {

    public static final int SHOW_NOTHING = 0;
    public static final int SHOW_NAME = 1;
    public static final int SHOW_LENGTH = 2;
    public static final int SHOW_DIRECTION = 4;
    public static final int SHOW_LINE = 8;

    public static final int LINE_Size_Normal = 2;
    public static final int LINE_Size_Small = 1;
    public static final int LINE_Size_Big = 4;

    private desmoj.extensions.visualization2d.engine.modelGrafic.RouteGrafic grafic;
    private final double length;
    private final String sourceId;
    private final String sinkId;
    private String name;
    private int show;
    private Color color;
    private int lineSize;

    /**
     * Route from sourceId to sinkId with length
     *
     * @param model    used animation.model.Model
     * @param id       routeId
     * @param sourceId station with sourceId
     * @param sinkId   station with sinkId
     * @param length   length of route
     */
    public Route(Model model, String id, String sourceId, String sinkId, double length) {
        super(model, id);
        this.sourceId = sourceId;
        this.sinkId = sinkId;
        this.length = length;
        this.name = null;
        this.show = Route.SHOW_NAME | Route.SHOW_LENGTH | Route.SHOW_DIRECTION | Route.SHOW_LINE;
        this.color = Grafic.COLOR_FOREGROUND;
        this.lineSize = Route.LINE_Size_Small;
        // Route wird in Routes-Liste aufgenommen
        if (this.id != null) {
            model.getRoutes().add(this);
        }
    }

    /**
     * name of route, can be null
     */
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSourceId() {
        return this.sourceId;
    }

    public String getSinkId() {
        return this.sinkId;
    }

    public double getLength() {
        return this.length;
    }

    /**
     * Show is the sum of Route.SHOW_ Flags
     *
     * @return
     */
    public int getShow() {
        return this.show;
    }

    /**
     * Show is the sum of Route.SHOW_ Flags
     *
     * @param show
     */
    public void setShow(int show) {
        this.show = show;
    }

    /**
     * get color of route line
     *
     * @return
     */
    public Color getColor() {
        return this.color;
    }

    /**
     * set color of route line and description
     *
     * @param color
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * get size of route line
     *
     * @return
     */
    public int getLineSize() {
        return this.lineSize;
    }

    /**
     * set the line size, for values look at Route.LINE_Size flags
     *
     * @param size
     */
    public void setLineSize(int size) {
        this.lineSize = size;
    }

    /**
     * create RouteGrafic as polyline with innerpoints points. The endpoints are given from source- and sink- station
     *
     * @param points
     * @return RouteGrafic
     */
    public Grafic createGrafic(Point[] points) {
        if (points == null) {
            points = new Point[0];
        }
        int l = points.length;
        Point[] extPoints = new Point[l + 2];
        int i = 0;
        if (model.getStations().exist(sourceId)) {
            extPoints[i++] = ((StationGrafic) (model.getStations().get(sourceId).getGrafic())).getPosition();
        } else {
            extPoints[i++] = new Point(10, 10);
        }
        for (int j = 0; j < points.length; j++) {
            extPoints[i++] = points[j];
        }
        if (model.getStations().exist(sinkId)) {
            extPoints[i++] = ((StationGrafic) (model.getStations().get(sinkId).getGrafic())).getPosition();
        } else {
            extPoints[i++] = new Point(300, 300);
        }
        this.grafic = new desmoj.extensions.visualization2d.engine.modelGrafic.RouteGrafic(this, extPoints,
            this.getModel().getSimulationTime().getSimulationTime());
        return this.grafic;
    }

    /**
     * get stationGrafic, which was ceated before
     */
    public Grafic getGrafic() {
        return this.grafic;
    }

    /**
     * add entityId at sourceId on route
     *
     * @param entityId
     * @param time     time of operation
     * @return true, when successful
     * @throws ModelException
     */
    public boolean addToContainer(String entityId, long time) throws ModelException {
        boolean out;
        out = super.addToContainer(entityId, time);
        if (out) {
            model.getEntities().get(entityId).changeContainer("Route", this.getId(), "add", time);

        } else {
            throw new ModelException(
                "model.Route.addToContainer It was not possible to add entity: " + entityId + " to Route: " +
                    this.getId());
        }
        //if(this.grafic != null) this.grafic.update();
        return out;
    }

    /**
     * remove entityId from route entityId time				time of operation
     */
    public boolean removeFromContainer(String entityId, long time) throws ModelException {
        boolean out;
        double relPos = this.getRelPosition(entityId, time);
        DecimalFormat df = new DecimalFormat("0.00");
        out = super.removeFromContainer(entityId, time);
        if (out) {
            model.getEntities().get(entityId)
                .changeContainer("Route", this.getId(), "remove at " + df.format(relPos), time);
            model.getEntities().get(entityId).changeContainer(" ", "free", "", time);
        } else {
            throw new ModelException(
                "model.Route.removeFromContainer It was not possible to remove entity: " + entityId + " from Route: " +
                    this.getId());
        }
        if (this.grafic != null) {
            this.grafic.update(time);
        }
        return out;
    }

    /**
     * get rel position of entityId on route. 0.0 means entity is on source and 1.0 means entits is on sink.
     *
     * @param entityId
     * @param time     time of operation
     * @return rel position
     */
    private double getRelPosition(String entityId, long time) {
        long startTime = this.getFromContainer(entityId).longValue();
        double velocity = model.getEntities().get(entityId).getVelocity();
        double reldist = (velocity * (time - startTime)) / (this.length * 1000.0);
        //System.out.println("time: "+time+"   start: "+startTime);
        //System.out.println("reldist: "+reldist+"   time - start: "+(time - startTime)+"  velocity: "+velocity+"   length: "+length);
        if (reldist >= 1.0) {
            //this.remove(key);
            reldist = 1.0;
        }
        return reldist;
    }

    /**
     * Compute the relative position of every entity in this route (container). This is a value between 0 (start of
     * route) and 1 (end of route).
     *
     * @param time actual simulation time
     * @return getRelPosition(long time)[i][0]: key of i.th entity in route (String). getRelPosition(long time)[i][1]:
     *     relative Position of i.th entity in route (Double).
     */
    public Object[][] getRelPosition(long time) {
        Object[][] out = new Object[this.container.size()][2];
        java.util.Iterator<String> en = this.container.keySet().iterator();
        int i = 0;
        while (en.hasNext()) {
            String key = en.next();
            out[i][0] = key;
            out[i][1] = new Double(getRelPosition(key, time));
            //System.out.println("route.getContent "+i+"  "+key+"  "+out[i][1]);
            i++;

        }
        return out;
    }
    // override and additional methods for Container ------- end ----------------


}
