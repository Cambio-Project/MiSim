package desmoj.extensions.visualization2d.engine.modelGrafic;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.util.Iterator;
import java.util.TreeSet;

import desmoj.extensions.visualization2d.engine.model.Entity;
import desmoj.extensions.visualization2d.engine.model.Model;
import desmoj.extensions.visualization2d.engine.model.Route;
import desmoj.extensions.visualization2d.engine.model.Station;


/**
 * Grafic of a route. That contains static and dynamic components
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
public class RouteGrafic extends Object implements Grafic {

    private final Point[] pointsExtern;
    private Point[] pointsIntern;
    private boolean printable;
    private final Route route;
    private final double[] routeRatio;
    // routeRatio[middleIndex-1] <= 0.5 < routeRatio[middleIndex]
    private int middleIndex;
    private RouteStaticGrafic staticGrafic;
    private RouteDynamicGrafic dynamicGrafic;
    private final String code;
    private String viewId;


    /**
     * called by Route
     *
     * @param route
     * @param pointsExtern
     * @param time
     */
    public RouteGrafic(Route route, Point[] pointsExtern, long time) {
        this.route = route;
        this.pointsExtern = pointsExtern;
        this.setViewId(route);
        this.transform(time);
        if (this.route.getName() != null) {
            this.code = this.route.getName();
        } else {
            this.code = "id: " + this.route.getId();
        }

        this.routeRatio = new double[this.pointsExtern.length];
        double l = 0.0;
        this.routeRatio[0] = 0.0;
        for (int i = 1; i < this.pointsExtern.length; i++) {
            Point p0 = this.pointsExtern[i - 1];
            Point p1 = this.pointsExtern[i];
            this.routeRatio[i] = Math.sqrt(((p0.x - p1.x) * (p0.x - p1.x)) + ((p0.y - p1.y) * (p0.y - p1.y)));
            l += this.routeRatio[i];
        }
        for (int i = 0; i < this.pointsExtern.length; i++) {
            this.routeRatio[i] /= l;
        }
        for (int i = 1; i < this.pointsExtern.length; i++) {
            this.routeRatio[i] = this.routeRatio[i - 1] + this.routeRatio[i];
        }
        this.routeRatio[this.pointsExtern.length - 1] = 1.0;

        for (int i = 1; i < this.pointsExtern.length; i++) {
            this.middleIndex = i;
            if (this.routeRatio[i] > 0.5) {
                break;
            }
        }

        this.staticGrafic = new RouteStaticGrafic();
        this.dynamicGrafic = new RouteDynamicGrafic(time);
    }

    /**
     * get all views (viewId's) with Route
     *
     * @param model
     * @return
     */
    public static String[] getViews(Model model) {
        TreeSet<String> views = new TreeSet<String>();
        String[] ids = model.getRoutes().getAllIds();
        for (int i = 0; i < ids.length; i++) {
            Route route = model.getRoutes().get(ids[i]);
            RouteGrafic routeGrafic = (RouteGrafic) route.getGrafic();
            if (routeGrafic != null) {
                String viewId = routeGrafic.getViewId();
                views.add(viewId);
            }
        }
        String[] out = new String[views.size()];
        int i = 0;
        for (Iterator<String> it = views.iterator(); it.hasNext(); ) {
            out[i] = it.next();
            i++;
        }
        return out;

    }

    /**
     * Construct the smallest rectangle, which include the positions of all routes  in view
     *
     * @param model
     * @param viewId id of view
     * @return smallest rectangle when an routeGrafic exist, null otherwise
     */
    public static Rectangle getBoundsExternGlobal(Model model, String viewId) {
        boolean found = false;
        int minX = Integer.MAX_VALUE / 2;
        int minY = Integer.MAX_VALUE / 2;
        int maxX = Integer.MIN_VALUE / 2;
        int maxY = Integer.MIN_VALUE / 2;
        String[] id = model.getRoutes().getAllIds();
        for (int i = 0; i < id.length; i++) {
            Route route = model.getRoutes().get(id[i]);
            RouteGrafic routeGrafic = (RouteGrafic) route.getGrafic();
            if (routeGrafic != null &&
                routeGrafic.getViewId().equals(viewId)) {
                found = true;
                Rectangle r = routeGrafic.getBoundsExtern();
                minX = Math.min(minX, r.x);
                minY = Math.min(minY, r.y);
                maxX = Math.max(maxX, r.x + r.width);
                maxY = Math.max(maxY, r.y + r.height);
                //System.out.println(route.getId()+"   "+r.toString());
            }
        }
        //System.out.println("RouteGrafic: min point: "+minX+", "+minY);
        //System.out.println("RouteGrafic: max point: "+maxX+", "+maxY);
        Rectangle r = null;
        if (found) {
            r = new Rectangle(minX, minY, maxX - minX, maxY - minY);
        }
        //System.out.println("RouteGrafic: BoundsExtern: "+r);
        return r;
    }

    /**
     * called by ViewGrafic by view or zoom change
     *
     * @param model
     * @param viewId
     * @param time
     */
    public static void updateInit(Model model, String viewId, long time) {
        String[] id = model.getRoutes().getAllIds();
        for (int i = 0; i < id.length; i++) {
            Route route = model.getRoutes().get(id[i]);
            RouteGrafic routeGrafic = (RouteGrafic) route.getGrafic();
            if (routeGrafic != null &&
                routeGrafic.getViewId().equals(viewId)) {
                routeGrafic.transform(time);
                //System.out.println("RouteGrafic.updateInit   "+route.getId());
            }
        }
    }

    public String getViewId() {
        return this.viewId;
    }

    /**
     * only for internal use, called by constructor determine viewId by view of source and sink station
     *
     * @param route
     */
    private void setViewId(Route route) {
        Station source = route.getModel().getStations().get(route.getSourceId());
        if (source == null) {
            throw new ModelGraficException("RouteGrafic: SourceStation is null RouteId: " + route.getId());
        }
        StationGrafic sourceGrafic = (StationGrafic) source.getGrafic();
        if (sourceGrafic == null) {
            throw new ModelGraficException("RouteGrafic: SourceGrafic is null RouteId: " + route.getId());
        }
        String sourceView = sourceGrafic.getViewId();
        if (sourceView == null) {
            throw new ModelGraficException("RouteGrafic: SourceView is null RouteId: " + route.getId());
        }
        Station sink = route.getModel().getStations().get(route.getSinkId());
        if (sink == null) {
            throw new ModelGraficException("RouteGrafic: SinkStation is null RouteId: " + route.getId());
        }
        StationGrafic sinkGrafic = (StationGrafic) sink.getGrafic();
        if (sinkGrafic == null) {
            throw new ModelGraficException("RouteGrafic: SinkGrafic is null RouteId: " + route.getId());
        }
        String sinkView = sinkGrafic.getViewId();
        if (sinkView == null) {
            throw new ModelGraficException("RouteGrafic: SinkView is null RouteId: " + route.getId());
        }
        if (sourceView.equals(sinkView)) {
            this.viewId = sourceView;
        } else {
            throw new ModelGraficException("RouteGrafic: source and sink haven't the same view. " +
                "RouteId: " + route.getId() + "  SourceView: " + sourceView + "   SinkView: " + sinkView);
        }
    }

    /**
     * determine internal points for painting route
     *
     * @param time actual simulationTime
     */
    public void transform(long time) {

        Point p = route.getModel().getModelGrafic().
            transformToIntern(this.viewId, new Point(0, 0));

        if (p == null) {
            this.pointsIntern = this.pointsExtern;
            this.printable = false;
        } else {
            this.pointsIntern = new Point[pointsExtern.length];
            for (int i = 0; i < pointsExtern.length; i++) {
                this.pointsIntern[i] = route.getModel().getModelGrafic().
                    transformToIntern(this.viewId, pointsExtern[i]);
            }
            this.printable = true;
        }
        this.staticGrafic = new RouteStaticGrafic();
        this.dynamicGrafic = new RouteDynamicGrafic(time);
    }

    public Rectangle getBoundsExtern() {
        int minX = 0, maxX = 0, minY = 0, maxY = 0;
        for (int i = 0; i < this.pointsExtern.length; i++) {
            minX = Math.min(minX, this.pointsExtern[i].x);
            maxX = Math.max(maxX, this.pointsExtern[i].x);
            minY = Math.min(minY, this.pointsExtern[i].y);
            maxY = Math.max(minY, this.pointsExtern[i].y);
        }
        return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }


    public RouteStaticGrafic getRouteStaticGrafic() {
        return this.staticGrafic;
    }

    public RouteDynamicGrafic getRouteDynamicGrafic() {
        return this.dynamicGrafic;
    }

    /**
     * update entities on route
     *
     * @param time actual simulation time
     */
    public void update(long time) {
        this.dynamicGrafic.updateEntityPositions(time);
        //this.repaint();
    }


    class RouteStaticGrafic extends Object {

        private final int[] xPoints;
        private final int[] yPoints;

        public RouteStaticGrafic() {
            xPoints = new int[pointsIntern.length];
            yPoints = new int[pointsIntern.length];
            for (int i = 0; i < pointsIntern.length; i++) {
                xPoints[i] = pointsIntern[i].x;
                yPoints[i] = pointsIntern[i].y;
            }
        }

        /**
         * Paint the line of this route. This method will be called from RouteStaticGraficComponent.paintComponent()
         *
         * @param g
         */
        public void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            AffineTransform t = g2.getTransform();
            g2.setFont(Grafic.FONT_DEFAULT);
            FontMetrics fm = g2.getFontMetrics(Grafic.FONT_DEFAULT);
            g2.setRenderingHints(
                new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON));
            g2.setColor(route.getColor());
            g2.setStroke(new BasicStroke(route.getLineSize()));
            if ((route.getShow() & Route.SHOW_LINE) != 0) {
                g2.drawPolyline(xPoints, yPoints, pointsIntern.length);
            }

            // schreibe RouteName
            String text = "";
            if ((route.getShow() & Route.SHOW_NAME) != 0) {
                text += code;
            }
            if ((route.getShow() & Route.SHOW_LENGTH) != 0) {
                text += "  l=" + route.getLength();
            }
            int middleX = (pointsIntern[middleIndex].x - pointsIntern[middleIndex - 1].x) / 2;
            int middleY = (pointsIntern[middleIndex].y - pointsIntern[middleIndex - 1].y) / 2;
            double angle = -Math.PI / 2.0;
            if (middleX != 0) {
                angle = Math.atan((double) middleY / (double) middleX);
            }
            //System.out.println("angle: "+angle+" mx:"+middleX+" my:"+middleY);
            if ((route.getShow() & Route.SHOW_DIRECTION) != 0) {
                if (middleX > 0) {
                    text = text + " >";
                } else if (middleX < 0) {
                    text = "< " + text;
                } else if (middleY > 0) {
                    text = "< " + text;
                } else {
                    text = text + " >";
                }
            }
            middleX = pointsIntern[middleIndex - 1].x + middleX;
            middleY = pointsIntern[middleIndex - 1].y + middleY;
            g2.rotate(angle, middleX, middleY);
            g2.setColor(ModelGrafic.COLOR_BACKGROUND);
            g2.fillRect(middleX - fm.stringWidth(text) / 2, middleY - fm.getAscent() / 2, fm.stringWidth(text),
                fm.getAscent());
            g2.setColor(route.getColor());
            g2.drawString(text, middleX - fm.stringWidth(text) / 2, middleY + fm.getAscent() / 2);
            g2.setTransform(t);

            if (!route.getModel().getStations().exist(route.getSourceId())) {
                // schreibe SourceName
                text = route.getSourceId();
                middleX = (pointsIntern[1].x - pointsIntern[0].x);
                middleY = (pointsIntern[1].y - pointsIntern[0].y);
                angle = -Math.PI / 2.0;
                if (middleX != 0) {
                    angle = Math.atan((double) middleY / (double) middleX);
                }
                g2.rotate(angle, pointsIntern[0].x, pointsIntern[0].y);
                if (middleX >= 0) {
                    g2.setColor(Color.yellow);
                    g2.fillRect(pointsIntern[0].x, pointsIntern[0].y - fm.getAscent() / 2, fm.stringWidth(text),
                        fm.getAscent());
                    g2.setColor(Color.red);
                    g2.drawString(text, pointsIntern[0].x, pointsIntern[0].y + fm.getAscent() / 2);
                } else {
                    g2.setColor(Color.yellow);
                    g2.fillRect(pointsIntern[0].x - fm.stringWidth(text), pointsIntern[0].y - fm.getAscent() / 2,
                        fm.stringWidth(text), fm.getAscent());
                    g2.setColor(Color.red);
                    g2.drawString(text, pointsIntern[0].x - fm.stringWidth(text),
                        pointsIntern[0].y + fm.getAscent() / 2);
                }
                g2.setTransform(t);
            }

            if (!route.getModel().getStations().exist(route.getSinkId())) {
                // schreibe SinkName
                text = route.getSinkId();
                middleX = (pointsIntern[pointsIntern.length - 1].x - pointsIntern[pointsIntern.length - 2].x);
                middleY = (pointsIntern[pointsIntern.length - 1].y - pointsIntern[pointsIntern.length - 2].y);
                angle = -Math.PI / 2.0;
                if (middleX != 0) {
                    angle = Math.atan((double) middleY / (double) middleX);
                }
                g2.rotate(angle, pointsIntern[pointsIntern.length - 1].x, pointsIntern[pointsIntern.length - 1].y);
                if (middleX >= 0) {
                    g2.setColor(Color.yellow);
                    g2.fillRect(pointsIntern[pointsIntern.length - 1].x - fm.stringWidth(text),
                        pointsIntern[pointsIntern.length - 1].y - fm.getAscent() / 2, fm.stringWidth(text),
                        fm.getAscent());
                    g2.setColor(Color.red);
                    g2.drawString(text, pointsIntern[pointsIntern.length - 1].x - fm.stringWidth(text),
                        pointsIntern[pointsIntern.length - 1].y + fm.getAscent() / 2);
                } else {
                    g2.setColor(Color.yellow);
                    g2.fillRect(pointsIntern[pointsIntern.length - 1].x,
                        pointsIntern[pointsIntern.length - 1].y - fm.getAscent() / 2, fm.stringWidth(text),
                        fm.getAscent());
                    g2.setColor(Color.red);
                    g2.drawString(text, pointsIntern[pointsIntern.length - 1].x,
                        pointsIntern[pointsIntern.length - 1].y + fm.getAscent() / 2);
                }
                g2.setTransform(t);
            }
        }

    }

    class RouteDynamicGrafic extends Object implements MouseListener {

        public RouteDynamicGrafic(long time) {
            this.updateEntityPositions(time);
        }

        /**
         * Paint the entities in this route. This method will be called from RouteDynamicGraficComponent.paintComponent()
         *
         * @param g
         */
        public void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHints(
                new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON));
            AffineTransform t = g2.getTransform();
            String[] entityIds = route.getAllContentFromContainer();
            for (int i = 0; i < entityIds.length; i++) {
                Entity entity = route.getModel().getEntities().get(entityIds[i]);
                EntityGrafic entityGrafic = (EntityGrafic) entity.getGrafic();
                if (entityGrafic.getPositionIntern() != null) {
                    EntityPosition epI = entityGrafic.getPositionIntern();
                    EntityPosition epE = entityGrafic.getPositionExtern();
                    g2.translate(entityGrafic.getPositionIntern().getX(), entityGrafic.getPositionIntern().getY());
                    g2.rotate(entityGrafic.getPositionIntern().getAngle());
                    g2.translate(-entityGrafic.getSize().width / 2.0, -entityGrafic.getSize().height / 2.0);
                    entityGrafic.paint(g2);
                    g2.setTransform(t);
                } else {
                    //System.out.println("entityGrafic.getPositionIntern() ist null  i="+i+"  max"+entityIds.length);
                }
            }

        }

        /**
         * Update the position of all entities is this route.
         *
         * @param time actual simulation time
         */
        public void updateEntityPositions(long time) {
            //System.out.println("updateEntityPositions  time: "+time);
            double eps = 0.00001;
            Object[][] content = route.getRelPosition(time);
            for (int i = 0; i < content.length; i++) {
                EntityPosition pos = null;
                double deltaX = 0.0;
                double deltaY = 0.0;
                String entityKey = (String) content[i][0];
                Double relDist = (Double) content[i][1];
                //System.out.println("RouteDynamicGrafic.updateEntityPositions  entityKey: "+entityKey+"   relDist"+relDist);
                for (int j = 1; j < routeRatio.length; j++) {
                    if (routeRatio[j] > relDist.doubleValue()) {
                        double t = (relDist.doubleValue() - routeRatio[j - 1]) / (routeRatio[j] - routeRatio[j - 1]);
                        pos = new EntityPosition();
                        pos.setX(t * pointsIntern[j].x + (1 - t) * pointsIntern[j - 1].x);
                        pos.setY(t * pointsIntern[j].y + (1 - t) * pointsIntern[j - 1].y);
                        deltaX = pointsIntern[j].x - pointsIntern[j - 1].x;
                        deltaY = pointsIntern[j].y - pointsIntern[j - 1].y;
                        pos.setAngle(Math.atan(deltaY / deltaX));
                        pos.setDirection((deltaX >= eps) || ((Math.abs(deltaX) < eps) && (deltaY < 0.0)));
                        break;
                    }
                }
                if (Math.abs(relDist.doubleValue() - 1.0) < eps) {
                    pos = new EntityPosition();
                    pos.setX(pointsIntern[pointsIntern.length - 1].x);
                    pos.setY(pointsIntern[pointsIntern.length - 1].y);
                    deltaX =
                        pointsIntern[pointsIntern.length - 1].x - pointsIntern[pointsIntern.length - 2].x;
                    deltaY =
                        pointsIntern[pointsIntern.length - 1].y - pointsIntern[pointsIntern.length - 2].y;
                    pos.setAngle(Math.atan(deltaY / deltaX));
                    pos.setDirection((deltaX >= eps) || ((Math.abs(deltaX) < eps) && (deltaY < 0.0)));
                }
                if (Math.abs(pos.getAngle() - Math.PI / 2.0) < eps) {
                    pos.setAngle(-Math.PI / 2.0);
                }
                if (pos != null) {
                    ((EntityGrafic) route.getModel().getEntities().get(entityKey).getGrafic()).setLocation(pos);
                    //System.out.println("RouteDynamicGrafic.updateEntityPositions   Key: "+entityKey+"  relPos: "+relDist+"  Pos: "+pos.getX()+","+pos.getY()+"  t: "+time);
                } else {
                    //System.out.println("RouteDynamicGrafic.updateEntityPositions  pos == null");
                }
            }
        }

        /**
         * Check MouseEvents again the entities of this route. By success the event will dispached to the entity.
         *
         * @param e MouseEvent
         */
        protected void mouseEventHandler(MouseEvent e) {
            //System.out.println("RouteDynamicGrafic.mouseEventHandler x:"+e.getPoint());
            String[] ids = route.getAllContentFromContainer();
            for (int i = 0; i < ids.length; i++) {
                //System.out.println("RouteDynamicGrafic.mouseEventHandler length:"+ids.length);
                EntityGrafic en = (EntityGrafic) route.getModel().getEntities().get(ids[i]).getGrafic();
                if (en != null) {
                    Point center = new Point(en.getX() + en.getWidth() / 2, en.getY() + en.getHeight() / 2);
                    int dist = (en.getWidth() + en.getHeight()) / 4;
                    if (Point.distance(e.getX(), e.getY(), center.x, center.y) < dist) {
                        en.dispatchEvent(e);
                        break;
                    }
                }
            }
        }

        public void mousePressed(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
        }

        public void mouseClicked(MouseEvent e) {
            mouseEventHandler(e);
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }


    }


}
