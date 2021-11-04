package desmoj.extensions.visualization2d.engine.modelGrafic;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.util.Iterator;
import java.util.TreeSet;

import desmoj.extensions.visualization2d.engine.model.Model;
import desmoj.extensions.visualization2d.engine.model.Station;


/**
 * Grafic of a station, thats a start- or end- point of route.
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
public class StationGrafic extends JComponent implements Grafic {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private final Station station;
    private String code;
    private Point pointIntern;
    private final Point pointExtern;
    private boolean printable;
    private final String viewId;


    public StationGrafic(Station station, String viewId, Point pointExtern) {
        if (viewId == null) {
            viewId = "main";
        }
        this.viewId = viewId;
        this.station = station;
        this.pointExtern = pointExtern;
        this.transform();

        this.code = this.station.getId();
        if (this.station.getName() != null) {
            this.code = this.station.getName();
        } else {
            this.code = "id: " + this.station.getId();
        }

        FontMetrics fm = this.getFontMetrics(Grafic.FONT_DEFAULT);
        int width = Math.max(Grafic.STATION_DEFAULT_DIMENSION.width, fm.stringWidth(code));
        int height = Math.max(Grafic.STATION_DEFAULT_DIMENSION.height, fm.getHeight());
        this.setPreferredSize(new Dimension(width, height));
        this.setSize(new Dimension(width, height));
        this.setLocation(pointIntern.x - width / 2, pointIntern.y - height / 2);
        this.setBorder(BorderFactory.createLineBorder(Color.black));

        if (this.station.getModel().getCoordinatenListener() != null) {
            this.addMouseMotionListener(this.station.getModel().getCoordinatenListener());
            this.addMouseListener(this.station.getModel().getCoordinatenListener());
        }

    }

    /**
     * get all views (viewId's) with Stations
     *
     * @return
     */
    public static String[] getViews(Model model) {
        TreeSet<String> views = new TreeSet<String>();
        String[] ids = model.getStations().getAllIds();
        for (int i = 0; i < ids.length; i++) {
            Station station = model.getStations().get(ids[i]);
            StationGrafic stationGrafic = (StationGrafic) station.getGrafic();
            if (stationGrafic != null) {
                String viewId = stationGrafic.getViewId();
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
     * Construct the smallest rectangle, which include the positions of all stations in view
     *
     * @param viewId id of view
     * @return smallest rectangle when an StationGrafic exist, null otherwise
     */
    public static Rectangle getBoundsExternGlobal(Model model, String viewId) {
        boolean found = false;
        int minX = Integer.MAX_VALUE / 2;
        int minY = Integer.MAX_VALUE / 2;
        int maxX = Integer.MIN_VALUE / 2;
        int maxY = Integer.MIN_VALUE / 2;
        String[] id = model.getStations().getAllIds();
        for (int i = 0; i < id.length; i++) {
            Station station = model.getStations().get(id[i]);
            StationGrafic stationGrafic = (StationGrafic) station.getGrafic();
            if (stationGrafic != null &&
                stationGrafic.getViewId().equals(viewId)) {
                found = true;
                Rectangle r = stationGrafic.getBoundsExtern();
                minX = Math.min(minX, r.x);
                minY = Math.min(minY, r.y);
                maxX = Math.max(maxX, r.x + r.width);
                maxY = Math.max(maxY, r.y + r.height);
                //System.out.println(station.getId()+"  "+r.toString());
            }
        }
        //System.out.println("StationGrafic: min point: "+minX+", "+minY);
        //System.out.println("StationGrafic: max point: "+maxX+", "+maxY);
        Rectangle r = null;
        if (found) {
            r = new Rectangle(minX, minY, maxX - minX, maxY - minY);
        }
        //System.out.println("StationGrafic: BoundsExtern: "+r);
        return r;
    }

    public static void updateInit(Model model, String viewId, JComponent panel) {
        String[] id = model.getStations().getAllIds();
        for (int i = 0; i < id.length; i++) {
            Station station = model.getStations().get(id[i]);
            StationGrafic stationGrafic = (StationGrafic) station.getGrafic();
            if (stationGrafic != null &&
                stationGrafic.getViewId().equals(viewId)) {
                stationGrafic.transform();
                panel.add(stationGrafic);
                //System.out.println("StationGrafic.updateInit   "+station.getId());
            }
        }
    }

    public String getViewId() {
        return this.viewId;
    }

    public void transform() {
        Point p = station.getModel().getModelGrafic().
            transformToIntern(this.viewId, this.pointExtern);
        if (p == null) {
            this.pointIntern = this.pointExtern;
            this.printable = false;
        } else {
            this.pointIntern = p;
            this.printable = true;
        }
        this.setLocation(this.pointIntern.x - this.getWidth() / 2, this.pointIntern.y - this.getHeight() / 2);
    }

    public Rectangle getBoundsExtern() {
        return new Rectangle(this.pointExtern.x, this.pointExtern.y, this.getWidth(), this.getHeight());
    }


    public Point getPosition() {
        return this.pointExtern;
    }


    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        AffineTransform t = g2.getTransform();
        int width = this.getWidth();
        int height = this.getHeight();
        FontMetrics fm = this.getFontMetrics(Grafic.FONT_DEFAULT);
        g2.setFont(Grafic.FONT_DEFAULT);
        g2.setColor(Color.white);
        g2.fillRect(0, 0, width, height);
        g2.setColor(Color.black);
        g2.drawString(this.code, (width - fm.stringWidth(code)) / 2, height - fm.getDescent());
        g2.setTransform(t);
    }

}
