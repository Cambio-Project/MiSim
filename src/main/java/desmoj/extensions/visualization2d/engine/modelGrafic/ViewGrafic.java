package desmoj.extensions.visualization2d.engine.modelGrafic;

import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import desmoj.extensions.visualization2d.engine.model.Model;


/**
 * The Viewer contains TabedPane with different views. Each view needs a viewGrafic.
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
public class ViewGrafic extends JLayeredPane implements Grafic {

    private static final long serialVersionUID = 1L;
    private String viewId = null;
    private Model model = null;
    private Point min, max;
    private Rectangle rectangleExtern = null;
    private Rectangle rectangleIntern = null;
    private JPanel backgroundPanel = null;
    private JComponent layerBackElem = null;
    private JComponent layerBackLine = null;
    private RouteStaticComponent layerRouteStatic = null;
    private RouteDynamicComponent layerRouteDynamic = null;
    private JComponent layerStation = null;
    private JComponent layerList = null;
    private JComponent layerProcess = null;
    private JComponent layerProcessLineList = null;
    private JComponent layerProcessNew = null;
    private JComponent layerProcessLineListNew = null;
    private JComponent layerResource = null;
    private JComponent layerStock = null;
    private JComponent layerBin = null;
    private JComponent layerWaitingQueue = null;
    private JComponent layerStatistic = null;
    private EntityStaticComponent layerEntityStatic = null;
    private ZoomMarker zoomMarker = null;
    private double zoomFactor = 1.0;
    private Point zoomPoint = null;


    public ViewGrafic(Model model, String viewId) throws ModelGraficException {
        // Initialisierung
        this.model = model;
        this.viewId = viewId;
        min = null;
        max = null;
        rectangleExtern = null;
        rectangleIntern = null;
        zoomFactor = 1.0;
        backgroundPanel = null;
        layerBackElem = null;
        layerBackLine = null;
        layerRouteStatic = null;
        layerRouteDynamic = null;
        layerStation = null;
        layerList = null;
        layerProcess = null;
        layerProcessLineList = null;
        layerProcessNew = null;
        layerProcessLineListNew = null;
        layerResource = null;
        layerStock = null;
        layerBin = null;
        layerWaitingQueue = null;
        layerStatistic = null;
        layerEntityStatic = null;
        zoomMarker = null;

        this.removeAll();

        //System.out.println("ModelGrafic: viewId   : "+viewId);


        // externe Sicht
        // Default Groesse, wenn keine darstellbaren Objekte existieren.
        this.rectangleExtern = this.getBoundsExtern();
        if (this.rectangleExtern == null) {
            this.rectangleExtern = new Rectangle(0, 0, 100, 50);
        }

        this.zoomPoint = new Point((int) Math.round(this.rectangleExtern.getCenterX()),
            (int) Math.round(this.rectangleExtern.getCenterY()));
        this.zoomFactor = 1.0;

        this.min = new Point(this.rectangleExtern.x, this.rectangleExtern.y);
        this.max = new Point(this.rectangleExtern.x + this.rectangleExtern.width,
            this.rectangleExtern.y + this.rectangleExtern.height);
        //System.out.println("ViewGrafic: min point: "+min.toString());
        //System.out.println("ViewGrafic: max point: "+max.toString());
        //System.out.println();

        // pruefe ob min punktweise kleiner max ist.
        if ((this.min.x > this.max.x) || (this.min.y > this.max.y)) {
            throw new ModelGraficException(
                "ModelGrafic Min-point is not componetwise less max-point. min: (" + this.min.x + ", " + this.min.y +
                    ")  max: (" + this.max.x + ", " + this.max.y + ") ");
        }

        // interne Sicht, verschiebe Ecke links oben nach (0,0)
        this.rectangleIntern =
            new Rectangle(0, 0, (int) (this.zoomFactor * this.rectangleExtern.width) + 2 * ModelGrafic.BOUNDARY_WIDTH,
                (int) (this.zoomFactor * this.rectangleExtern.height) + 2 * ModelGrafic.BOUNDARY_WIDTH);

        // erzeuge Layer
        this.backgroundPanel = new JPanel();
        backgroundPanel.setBounds(this.rectangleIntern);
        backgroundPanel.setDoubleBuffered(false);
        backgroundPanel.setOpaque(true);
        backgroundPanel.setBackground(Grafic.COLOR_BACKGROUND);
        this.add(backgroundPanel, Grafic.LAYER_BACKGROUND);

        this.layerBackElem = new JPanel();
        this.layerBackElem.setLayout(null);
        this.layerBackElem.setBounds(this.rectangleIntern);
        this.layerBackElem.setDoubleBuffered(true);
        this.layerBackElem.setOpaque(false);
        this.add(this.layerBackElem, Grafic.LAYER_BackGroundElement);

        this.layerBackLine = new JPanel();
        this.layerBackLine.setLayout(null);
        this.layerBackLine.setBounds(this.rectangleIntern);
        this.layerBackLine.setDoubleBuffered(true);
        this.layerBackLine.setOpaque(false);
        this.add(this.layerBackLine, Grafic.LAYER_BackGroundLine);

        this.layerRouteStatic = new RouteStaticComponent(this.model, this.viewId, this.rectangleIntern);
        this.add(this.layerRouteStatic, Grafic.LAYER_ROUTE_STATIC);

        this.layerRouteDynamic = new RouteDynamicComponent(this.model, this.viewId, this.rectangleIntern);
        this.add(this.layerRouteDynamic, Grafic.LAYER_ROUTE_DYNAMIC);

        this.layerEntityStatic = new EntityStaticComponent(this.rectangleIntern);
        this.add(this.layerEntityStatic, Grafic.LAYER_ENTITY);

        this.layerStation = new JPanel();
        this.layerStation.setLayout(null);
        this.layerStation.setBounds(this.rectangleIntern);
        this.layerStation.setDoubleBuffered(true);
        this.layerStation.setOpaque(false);
        this.add(this.layerStation, Grafic.LAYER_STATION);

        this.layerList = new JPanel();
        this.layerList.setLayout(null);
        this.layerList.setBounds(this.rectangleIntern);
        this.layerList.setDoubleBuffered(true);
        this.layerList.setOpaque(false);
        this.add(this.layerList, Grafic.LAYER_LIST);

        this.layerProcess = new JPanel();
        this.layerProcess.setLayout(null);
        this.layerProcess.setBounds(this.rectangleIntern);
        this.layerProcess.setDoubleBuffered(true);
        this.layerProcess.setOpaque(false);
        this.add(this.layerProcess, Grafic.LAYER_PROCESS);

        this.layerProcessLineList = new JPanel();
        this.layerProcessLineList.setLayout(null);
        this.layerProcessLineList.setBounds(this.rectangleIntern);
        this.layerProcessLineList.setDoubleBuffered(true);
        this.layerProcessLineList.setOpaque(false);
        this.add(this.layerProcessLineList, Grafic.LAYER_PROCESS_LINE_LIST);

        this.layerProcessNew = new JPanel();
        this.layerProcessNew.setLayout(null);
        this.layerProcessNew.setBounds(this.rectangleIntern);
        this.layerProcessNew.setDoubleBuffered(true);
        this.layerProcessNew.setOpaque(false);
        this.add(this.layerProcessNew, Grafic.LAYER_PROCESS);

        this.layerProcessLineListNew = new JPanel();
        this.layerProcessLineListNew.setLayout(null);
        this.layerProcessLineListNew.setBounds(this.rectangleIntern);
        this.layerProcessLineListNew.setDoubleBuffered(true);
        this.layerProcessLineListNew.setOpaque(false);
        this.add(this.layerProcessLineListNew, Grafic.LAYER_PROCESS_LINE_LIST);

        this.layerResource = new JPanel();
        this.layerResource.setLayout(null);
        this.layerResource.setBounds(this.rectangleIntern);
        this.layerResource.setDoubleBuffered(true);
        this.layerResource.setOpaque(false);
        this.add(this.layerResource, Grafic.LAYER_RESOURCE);

        this.layerStock = new JPanel();
        this.layerStock.setLayout(null);
        this.layerStock.setBounds(this.rectangleIntern);
        this.layerStock.setDoubleBuffered(true);
        this.layerStock.setOpaque(false);
        this.add(this.layerStock, Grafic.LAYER_STOCK);

        this.layerBin = new JPanel();
        this.layerBin.setLayout(null);
        this.layerBin.setBounds(this.rectangleIntern);
        this.layerBin.setDoubleBuffered(true);
        this.layerBin.setOpaque(false);
        this.add(this.layerBin, Grafic.LAYER_STOCK);

        this.layerWaitingQueue = new JPanel();
        this.layerWaitingQueue.setLayout(null);
        this.layerWaitingQueue.setBounds(this.rectangleIntern);
        this.layerWaitingQueue.setDoubleBuffered(true);
        this.layerWaitingQueue.setOpaque(false);
        this.add(this.layerWaitingQueue, Grafic.LAYER_WAITING_QUEUE);

        this.layerStatistic = new JPanel();
        this.layerStatistic.setLayout(null);
        this.layerStatistic.setBounds(this.rectangleIntern);
        this.layerStatistic.setDoubleBuffered(true);
        this.layerStatistic.setOpaque(false);
        this.add(this.layerStatistic, Grafic.LAYER_STATISTIC);

        this.zoomMarker = new ZoomMarker(this.model, this.viewId, this.zoomPoint);
        this.zoomMarker.setLayout(null);
        this.zoomMarker.setBounds(this.rectangleIntern);
        this.zoomMarker.setDoubleBuffered(true);
        this.zoomMarker.setOpaque(false);
        this.add(this.zoomMarker, Grafic.LAYER_MARKER);

        this.setZoomFactor(this.zoomFactor, this.zoomPoint);

        this.setVisible(true);
        this.setBounds(this.rectangleIntern);
        this.setDoubleBuffered(true);
        this.addMouseListener(this.layerRouteDynamic);
        //this.addMouseListener(this.layerEntityStatic);

        this.setBackground(Grafic.COLOR_BACKGROUND);
        this.setPreferredSize(getSize());
        //System.out.println("ModelGrafic rectExt: "+this.rectangleExtern.toString());
        //System.out.println("ModelGrafic erstellt size: "+this.getSize().toString());

    }

    /**
     * Construct the smallest rectangle, which include the positions of all entities, routes, lists, processes and
     * stations;
     *
     * @return smallest rectangle
     */
    public Rectangle getBoundsExtern() {
        int minX = Integer.MAX_VALUE / 2;
        int minY = Integer.MAX_VALUE / 2;
        int maxX = Integer.MIN_VALUE / 2;
        int maxY = Integer.MIN_VALUE / 2;
        boolean found = false;

        Rectangle painting = BackgroundElementGrafic.getBoundsExternGlobal(model, viewId);
        if (painting != null) {
            found = true;
            minX = Math.min(minX, painting.x);
            minY = Math.min(minY, painting.y);
            maxX = Math.max(maxX, painting.x + painting.width);
            maxY = Math.max(maxY, painting.y + painting.height);
        }

        Rectangle lines = BackgroundLineGrafic.getBoundsExternGlobal(model, viewId);
        if (lines != null) {
            found = true;
            minX = Math.min(minX, lines.x);
            minY = Math.min(minY, lines.y);
            maxX = Math.max(maxX, lines.x + lines.width);
            maxY = Math.max(maxY, lines.y + lines.height);
        }

        Rectangle entities = EntityGrafic.getBoundsExternGlobal(model, viewId);
        if (entities != null) {
            found = true;
            minX = Math.min(minX, entities.x);
            minY = Math.min(minY, entities.y);
            maxX = Math.max(maxX, entities.x + entities.width);
            maxY = Math.max(maxY, entities.y + entities.height);
        }

        Rectangle routes = RouteGrafic.getBoundsExternGlobal(model, viewId);
        if (routes != null) {
            found = true;
            minX = Math.min(minX, routes.x);
            minY = Math.min(minY, routes.y);
            maxX = Math.max(maxX, routes.x + routes.width);
            maxY = Math.max(maxY, routes.y + routes.height);
        }

        Rectangle lists = ListGrafic.getBoundsExternGlobal(model, viewId);
        if (lists != null) {
            found = true;
            minX = Math.min(minX, lists.x);
            minY = Math.min(minY, lists.y);
            maxX = Math.max(maxX, lists.x + lists.width);
            maxY = Math.max(maxY, lists.y + lists.height);
        }

        Rectangle procs = ProcessGrafic.getBoundsExternGlobal(model, viewId);
        if (procs != null) {
            found = true;
            minX = Math.min(minX, procs.x);
            minY = Math.min(minY, procs.y);
            maxX = Math.max(maxX, procs.x + procs.width);
            maxY = Math.max(maxY, procs.y + procs.height);
        }

        Rectangle procsNew = ProcessNewGrafic.getBoundsExternGlobal(model, viewId);
        if (procsNew != null) {
            found = true;
            minX = Math.min(minX, procsNew.x);
            minY = Math.min(minY, procsNew.y);
            maxX = Math.max(maxX, procsNew.x + procsNew.width);
            maxY = Math.max(maxY, procsNew.y + procsNew.height);
        }

        Rectangle resorces = ResourceGrafic.getBoundsExternGlobal(model, viewId);
        if (resorces != null) {
            found = true;
            minX = Math.min(minX, resorces.x);
            minY = Math.min(minY, resorces.y);
            maxX = Math.max(maxX, resorces.x + resorces.width);
            maxY = Math.max(maxY, resorces.y + resorces.height);
        }

        Rectangle stock = StockGrafic.getBoundsExternGlobal(model, viewId);
        if (stock != null) {
            found = true;
            minX = Math.min(minX, stock.x);
            minY = Math.min(minY, stock.y);
            maxX = Math.max(maxX, stock.x + stock.width);
            maxY = Math.max(maxY, stock.y + stock.height);
        }

        Rectangle bin = BinGrafic.getBoundsExternGlobal(model, viewId);
        if (bin != null) {
            found = true;
            minX = Math.min(minX, bin.x);
            minY = Math.min(minY, bin.y);
            maxX = Math.max(maxX, bin.x + bin.width);
            maxY = Math.max(maxY, bin.y + bin.height);
        }

        Rectangle waitingQueue = WaitingQueueGrafic.getBoundsExternGlobal(model, viewId);
        if (waitingQueue != null) {
            found = true;
            minX = Math.min(minX, waitingQueue.x);
            minY = Math.min(minY, waitingQueue.y);
            maxX = Math.max(maxX, waitingQueue.x + waitingQueue.width);
            maxY = Math.max(maxY, waitingQueue.y + waitingQueue.height);
        }

        Rectangle statistics = StatisticGrafic.getBoundsExternGlobal(model, viewId);
        if (statistics != null) {
            found = true;
            minX = Math.min(minX, statistics.x);
            minY = Math.min(minY, statistics.y);
            maxX = Math.max(maxX, statistics.x + statistics.width);
            maxY = Math.max(maxY, statistics.y + statistics.height);
        }

        Rectangle stations = StationGrafic.getBoundsExternGlobal(model, viewId);
        if (stations != null) {
            found = true;
            minX = Math.min(minX, stations.x);
            minY = Math.min(minY, stations.y);
            maxX = Math.max(maxX, stations.x + stations.width);
            maxY = Math.max(maxY, stations.y + stations.height);
        }

        //System.out.println("ViewGrafic: viewId   : "+viewId);
        //System.out.println("ViewGrafic: min point: "+minX+", "+minY);
        //System.out.println("ViewGrafic: max point: "+maxX+", "+maxY);

        Rectangle r = null;
        //Rectangle r = new Rectangle(0,0,1,1);
        if (found) {
            r = new Rectangle(minX, minY, maxX - minX, maxY - minY);
        }
        return r;
    }


    public Point transformToIntern(Point p) {
        //System.out.println("transformToIntern "+zoomFactor+"  "+p+"  "+min);
        Point out = null;
        out = new Point((int) (zoomFactor * (p.x - min.x) + ModelGrafic.BOUNDARY_WIDTH),
            (int) (zoomFactor * (p.y - min.y) + ModelGrafic.BOUNDARY_WIDTH));
        return out;
    }

    public Point transformToExtern(Point p) {
        Point out = null;
        out = new Point((int) Math.round((p.x - ModelGrafic.BOUNDARY_WIDTH) / zoomFactor) + min.x,
            (int) Math.round((p.y - ModelGrafic.BOUNDARY_WIDTH) / zoomFactor) + min.y);
        return out;
    }

    public String getViewId() {
        return this.viewId;
    }


    public void paintZoomMarker(Point zoomPoint, boolean shiftMode) {
        //System.out.println("ViewGrafic.paintZoomMarker   point: "+zoomPoint+" mode: "+shiftMode+"  id: "+this.viewId+"  bounds: "+this.getBoundsExtern());
        if (zoomPoint != null && this.getBoundsExtern().contains(zoomPoint)) {
            this.zoomPoint = zoomPoint;
            this.zoomMarker.setPoint(zoomPoint, shiftMode);
        }
    }

    public boolean isInZoomCenter(Point p) {
        return this.zoomMarker.isInZoomCenter(p);
    }

    public boolean setZoomFactor(double zoomFactor, Point zoomPoint) {
        boolean out = false;
        if (zoomPoint != null && this.getBoundsExtern().contains(zoomPoint)) {
            paintZoomMarker(zoomPoint, false);
            out = true;
        }
        if (zoomFactor > 0.0 && this.zoomPoint != null) {
            this.zoomFactor = zoomFactor;
            this.rectangleIntern = new Rectangle(0, 0,
                (int) (this.zoomFactor * this.rectangleExtern.width) + 2 * ModelGrafic.BOUNDARY_WIDTH,
                (int) (this.zoomFactor * this.rectangleExtern.height) + 2 * ModelGrafic.BOUNDARY_WIDTH);
            this.setBounds(this.rectangleIntern);
            this.backgroundPanel.setBounds(this.rectangleIntern);
            this.layerBackElem.setBounds(this.rectangleIntern);
            this.layerBackLine.setBounds(this.rectangleIntern);
            this.layerEntityStatic.setBounds(this.rectangleIntern);
            this.layerList.setBounds(this.rectangleIntern);
            this.layerProcess.setBounds(this.rectangleIntern);
            this.layerProcessLineList.setBounds(this.rectangleIntern);
            this.layerProcessNew.setBounds(this.rectangleIntern);
            this.layerProcessLineListNew.setBounds(this.rectangleIntern);
            this.layerStatistic.setBounds(this.rectangleIntern);
            this.layerResource.setBounds(this.rectangleIntern);
            this.layerStock.setBounds(this.rectangleIntern);
            this.layerBin.setBounds(this.rectangleIntern);
            this.layerWaitingQueue.setBounds(this.rectangleIntern);
            this.layerRouteDynamic.setBounds(this.rectangleIntern);
            this.layerRouteStatic.setBounds(this.rectangleIntern);
            this.layerStation.setBounds(this.rectangleIntern);
            this.zoomMarker.setBounds(this.rectangleIntern);
            this.zoomMarker.setPoint(zoomPoint, false);
            this.setSize(this.rectangleIntern.width, this.rectangleIntern.height);
            this.setPreferredSize(getSize());
            this.revalidate();
            this.repaint();
            //System.out.println("ModelGraficsetZoomFactor zoomFactor: "+this.zoomFactor);
            //System.out.println("ModelGrafic rectExt: "+this.rectangleExtern.toString());
            //System.out.println("ModelGrafic rectInt: "+this.rectangleIntern.toString());
            //System.out.println("ModelGrafic erstellt size: "+this.getSize().toString());
        }
        return out;
    }

    public double getZoomFactor() {
        return this.zoomFactor;
    }

    public Point getZoomPoint() {
        return this.zoomPoint;
    }

    /**
     *
     */
    public void updateInit(long time) {
        BackgroundElementGrafic.updateInit(model, this.viewId, layerBackElem);
        BackgroundLineGrafic.updateInit(model, this.viewId, layerBackLine);
        EntityGrafic.updateInit(model, this.viewId, layerEntityStatic);
        ListGrafic.updateInit(model, this.viewId, layerList);
        ProcessGrafic.updateInit(model, this.viewId, layerProcess, layerProcessLineList);
        ProcessNewGrafic.updateInit(model, this.viewId, layerProcessNew, layerProcessLineListNew);
        StatisticGrafic.updateInit(model, this.viewId, layerStatistic);
        ResourceGrafic.updateInit(model, this.viewId, layerResource);
        StockGrafic.updateInit(model, this.viewId, layerStock);
        BinGrafic.updateInit(model, this.viewId, layerBin);
        WaitingQueueGrafic.updateInit(model, this.viewId, layerWaitingQueue);
        StationGrafic.updateInit(model, this.viewId, layerStation);
        RouteGrafic.updateInit(model, this.viewId, time);
        this.setZoomFactor(this.zoomFactor, this.zoomPoint);
        this.repaint();
    }

    public void updateDynamic(long time) {
        this.layerRouteDynamic.updateEntityPositions(time);
        this.repaint();
    }

    /**
     * Paints a snapshot of this ViewGrafic in a BufferedImage. Title is printed in top left corner, when title != null
     *
     * @param title
     * @return BufferedImage with a snapshot
     */
    public BufferedImage getSnapShot(String title) {
        int w = this.getWidth();
        int h = this.getHeight();
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
        synchronized (this.getTreeLock()) {
            Graphics2D g = image.createGraphics();
            this.paint(g);
            if (title != null) {
                g.setColor(Grafic.COLOR_FOREGROUND);
                g.setFont(Grafic.FONT_DEFAULT);
                g.drawString(title, 10, 20);
            }
        }
        return image;
    }

}
