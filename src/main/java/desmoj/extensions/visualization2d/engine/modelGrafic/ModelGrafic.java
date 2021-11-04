package desmoj.extensions.visualization2d.engine.modelGrafic;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;

import desmoj.extensions.visualization2d.engine.model.Model;
import desmoj.extensions.visualization2d.engine.viewer.SimulationTime;
import desmoj.extensions.visualization2d.engine.viewer.ViewerPanel;


/**
 * The grafic of a model is an JPanel with all animated objects.
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
public class ModelGrafic extends JPanel implements Grafic, ChangeListener {


    private static final long serialVersionUID = 1L;
    private Model model = null;
    private Hashtable<String, ViewGrafic> viewList = null;
    private JTabbedPane tabbedPane = null;

    public ModelGrafic(Model model) throws ModelGraficException {
        //System.out.println("Konstruktor ModelGrafic");
        this.model = model;
        this.setBackground(Grafic.COLOR_BACKGROUND);
        this.reset();
    }

    public void reset() {
        this.viewList = new Hashtable<String, ViewGrafic>();
        this.init();
        this.setLayout(new GridLayout(1, 1));
        this.tabbedPane = new JTabbedPane();
        this.tabbedPane.addChangeListener(this);
        String codeFirst = "main";
        if (this.viewList.containsKey(codeFirst)) {
            this.addToTabbedPane(codeFirst);
        }
        TreeSet<String> idsorted = new TreeSet<String>();
        for (Enumeration<String> en = this.viewList.keys(); en.hasMoreElements(); ) {
            String code = en.nextElement();
            if (!code.equals(codeFirst)) {
                idsorted.add(code);
            }
        }
        for (Iterator<String> it = idsorted.iterator(); it.hasNext(); ) {
            this.addToTabbedPane(it.next());
        }
        if (this.tabbedPane.getTabCount() > 0) {
            this.tabbedPane.setSelectedIndex(0);
        }
        this.removeAll();
        this.add(tabbedPane);
    }

    /**
     * Construct the smallest rectangle, which include the positions of all entities, routes, lists, processes and
     * stations;
     *
     * @return smallest rectangle
     */
    public Rectangle getBoundsExtern() {
        ViewGrafic vg = this.getSelectedView();
        Rectangle out = null;
        if (vg != null) {
            out = vg.getBoundsExtern();
        }
        return out;
    }

    public Point transformToIntern(String viewId, Point p) {
        Point out = null;
        ViewGrafic vg = this.viewList.get(viewId);
        if (vg != null) {
            out = vg.transformToIntern(p);
        }
        return out;
    }

    public Point transformToIntern(Point p) {
        return this.transformToIntern(this.getSelectedViewId(), p);
    }

    public Point transformToExtern(String viewId, Point p) {
        Point out = null;
        ViewGrafic vg = this.viewList.get(viewId);
        if (vg != null) {
            out = vg.transformToExtern(p);
        }
        return out;
    }

    public Point transformToExtern(Point p) {
        return this.transformToExtern(this.getSelectedViewId(), p);
    }


    public ViewGrafic getSelectedView() {
        ViewGrafic out = null;
        if (!this.viewList.isEmpty()) {
            out = this.viewList.get(this.getSelectedViewId());
        }
        return out;
    }

    public String getSelectedViewId() {
        int selIndex = this.tabbedPane.getSelectedIndex();
        return this.tabbedPane.getTitleAt(selIndex);
    }

    public JScrollPane getSelectedComponent() {
        return (JScrollPane) this.tabbedPane.getSelectedComponent();
    }

    private void addToTabbedPane(String viewId) {
        ViewGrafic vg = this.viewList.get(viewId);
        vg.setZoomFactor(1.0, null);
        if (this.model.getCoordinatenListener() != null) {
            this.model.getCoordinatenListener().addRoot(vg);
            vg.addMouseListener(this.model.getCoordinatenListener());
            vg.addMouseMotionListener(this.model.getCoordinatenListener());
        }
        JScrollPane scroll = new JScrollPane(vg,
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setBackground(Grafic.COLOR_BACKGROUND);
        ImageIcon icon = new ImageIcon(model.getViewer().
            getLanguage().getInternURL("viewer_info_icon"));
        JButton info = new JButton(icon);
        info.addActionListener(ViewerPanel.getInfoPane());
        scroll.setCorner(JScrollPane.LOWER_RIGHT_CORNER, info);
        tabbedPane.add(viewId, scroll);
    }

    private void init() {
        //System.out.println("ModelGrafic.init begin");
        String[] ids = null;
        ids = BackgroundElementGrafic.getViews(model);
        for (int i = 0; i < ids.length; i++) {
            if (!this.viewList.containsKey(ids[i])) {
                this.viewList.put(ids[i], new ViewGrafic(model, ids[i]));
            }
        }
        ids = BackgroundLineGrafic.getViews(model);
        for (int i = 0; i < ids.length; i++) {
            if (!this.viewList.containsKey(ids[i])) {
                this.viewList.put(ids[i], new ViewGrafic(model, ids[i]));
            }
        }
        ids = BinGrafic.getViews(model);
        for (int i = 0; i < ids.length; i++) {
            //System.out.println("bin: "+ids[i]);
            if (!this.viewList.containsKey(ids[i])) {
                this.viewList.put(ids[i], new ViewGrafic(model, ids[i]));
            }
        }
        ids = EntityGrafic.getViews(model);
        for (int i = 0; i < ids.length; i++) {
            //System.out.println("entity: "+ids[i]);
            if (!this.viewList.containsKey(ids[i])) {
                this.viewList.put(ids[i], new ViewGrafic(model, ids[i]));
            }
        }
        ids = ListGrafic.getViews(model);
        for (int i = 0; i < ids.length; i++) {
            //System.out.println("list: "+ids[i]);
            if (!this.viewList.containsKey(ids[i])) {
                this.viewList.put(ids[i], new ViewGrafic(model, ids[i]));
            }
        }
        ids = ProcessGrafic.getViews(model);
        for (int i = 0; i < ids.length; i++) {
            //System.out.println("process: "+ids[i]);
            if (!this.viewList.containsKey(ids[i])) {
                this.viewList.put(ids[i], new ViewGrafic(model, ids[i]));
            }
        }
        ids = ProcessNewGrafic.getViews(model);
        for (int i = 0; i < ids.length; i++) {
            //System.out.println("processNew: "+ids[i]);
            if (!this.viewList.containsKey(ids[i])) {
                this.viewList.put(ids[i], new ViewGrafic(model, ids[i]));
            }
        }
        ids = ResourceGrafic.getViews(model);
        for (int i = 0; i < ids.length; i++) {
            //System.out.println("resource: "+ids[i]);
            if (!this.viewList.containsKey(ids[i])) {
                this.viewList.put(ids[i], new ViewGrafic(model, ids[i]));
            }
        }
        ids = RouteGrafic.getViews(model);
        for (int i = 0; i < ids.length; i++) {
            //System.out.println("route: "+ids[i]);
            if (!this.viewList.containsKey(ids[i])) {
                this.viewList.put(ids[i], new ViewGrafic(model, ids[i]));
            }
        }
        ids = StationGrafic.getViews(model);
        for (int i = 0; i < ids.length; i++) {
            //System.out.println("station: "+ids[i]);
            if (!this.viewList.containsKey(ids[i])) {
                this.viewList.put(ids[i], new ViewGrafic(model, ids[i]));
            }
        }
        ids = StatisticGrafic.getViews(model);
        for (int i = 0; i < ids.length; i++) {
            //System.out.println("statistic: "+ids[i]);
            if (!this.viewList.containsKey(ids[i])) {
                this.viewList.put(ids[i], new ViewGrafic(model, ids[i]));
            }
        }
        ids = StockGrafic.getViews(model);
        for (int i = 0; i < ids.length; i++) {
            //System.out.println("stock: "+ids[i]);
            if (!this.viewList.containsKey(ids[i])) {
                this.viewList.put(ids[i], new ViewGrafic(model, ids[i]));
            }
        }
        ids = WaitingQueueGrafic.getViews(model);
        for (int i = 0; i < ids.length; i++) {
            //System.out.println("wq: "+ids[i]);
            if (!this.viewList.containsKey(ids[i])) {
                this.viewList.put(ids[i], new ViewGrafic(model, ids[i]));
            }
        }
        //System.out.println("ModelGrafic.init end");
    }


    public void paintZoomMarker(Point zoomPoint, boolean shiftMode) {
        //System.out.println("ModelGrafic.paintZoomMarker   point: "+zoomPoint+" mode: "+shiftMode);
        ViewGrafic vg = this.getSelectedView();
        if (vg != null) {
            vg.paintZoomMarker(zoomPoint, shiftMode);
        }
    }

    public boolean isInZoomCenter(Point p) {
        boolean out = false;
        ViewGrafic vg = this.getSelectedView();
        if (vg != null) {
            out = vg.isInZoomCenter(p);
        }
        return out;
    }

    public void setZoomFactor(double zoomFactor, Point zoomPoint, long time) {
        //System.out.println("ModelGrafic.setZoomFactor   Factor: "+zoomFactor+"  Point: "+zoomPoint);
        ViewGrafic vg = this.getSelectedView();
        if (vg != null) {
            Rectangle r = vg.getBoundsExtern();
            if (r != null) {
                if (zoomPoint == null) {
                    zoomPoint = new Point((int) r.getCenterX(), (int) r.getCenterY());
                }
                if (!vg.setZoomFactor(zoomFactor, zoomPoint)) {
                    // zuruecksetzen auf alte Werte, wenn setZoomFactor nicht erfolgreich war
                    zoomPoint = vg.getZoomPoint();
                    zoomFactor = vg.getZoomFactor();
                }
                vg.updateInit(time);
                JScrollPane sp = (JScrollPane) this.tabbedPane.getSelectedComponent();
                Dimension d = sp.getViewport().getExtentSize();
                Point p = transformToIntern(zoomPoint);
                Rectangle r1 = new Rectangle(p.x - d.width / 2, p.y - d.height / 2, d.width, d.height);
                sp.getViewport().scrollRectToVisible(r1);
            }
        }
    }

    public double getZoomFactor(String viewId) {
        double out = 1.0;
        ViewGrafic vg = this.viewList.get(viewId);
        if (vg != null) {
            out = vg.getZoomFactor();
        }
        return out;
    }

    public Point getZoomPoint(String viewId) {
        Point out = null;
        ViewGrafic vg = this.viewList.get(viewId);
        if (vg != null) {
            out = vg.getZoomPoint();
        }
        return out;
    }


    public Hashtable<String, ZoomEntry> getZoomProperty() {
        //System.out.println("ModelGrafic.getZoomProperty");
        Hashtable<String, ZoomEntry> zoomProperty = new Hashtable<String, ZoomEntry>();
        int selIndex = this.tabbedPane.getSelectedIndex();
        for (int i = 0; i < this.tabbedPane.getTabCount(); i++) {
            String code = this.tabbedPane.getTitleAt(i);
            boolean sel = (i == selIndex);
            JScrollPane sp = (JScrollPane) this.tabbedPane.getComponentAt(i);
            Rectangle r = sp.getViewport().getViewRect();
            ViewGrafic vg = this.viewList.get(code);
            if (vg != null) {
                ZoomEntry ze = new ZoomEntry(code, sel,
                    vg.getZoomFactor(), vg.getZoomPoint(), r);
                zoomProperty.put(code, ze);
                //System.out.println("put code: "+code+"  "+r);
            }
        }
        return zoomProperty;
    }

    public void setZoomProperty(Hashtable<String, ZoomEntry> zoomProperty) {
        //System.out.println("ModelGrafic.setZoomProperty");
        int selIndex = 0;
        for (int i = 0; i < this.tabbedPane.getTabCount(); i++) {
            JScrollPane sp = (JScrollPane) this.tabbedPane.getComponentAt(i);
            String code = this.tabbedPane.getTitleAt(i);
            ZoomEntry ze = zoomProperty.get(code);
            if (ze.isSelected) {
                selIndex = i;
            }
            ViewGrafic vg = this.viewList.get(code);
            if (vg != null && ze != null) {
                vg.setZoomFactor(ze.zoomFactor, ze.zoomPoint);
                sp.getViewport().scrollRectToVisible(ze.viewRectangle);
                //System.out.println("get code: "+code+"  "+ze.viewRectangle);
            }
        }
        this.tabbedPane.setSelectedIndex(selIndex);
    }

    public void updateInit(long time) {
        for (Enumeration<String> en = this.viewList.keys(); en.hasMoreElements(); ) {
            this.viewList.get(en.nextElement()).updateInit(time);
        }
    }

    public void updateDynamic(long time) {
        for (Enumeration<String> en = this.viewList.keys(); en.hasMoreElements(); ) {
            this.viewList.get(en.nextElement()).updateDynamic(time);
        }
    }

    public void stateChanged(ChangeEvent e) {
        String vid = this.getSelectedViewId();
        double zoomFactor = this.getZoomFactor(vid);
        this.setZoomFactor(zoomFactor, this.getZoomPoint(vid),
            model.getSimulationTime().getSimulationTime());
        //System.out.println("  "+vid+"   "+this.getSelectedView().getZoomPoint());
        if (this.model.getViewer() != null) {
            this.model.getViewer().setSimulationZoomGUI(zoomFactor);
        }
        //System.out.println("ModelGrafic:  zoomFactor: "+this.getZoomFactor(vid));
    }

    /**
     * make snapshots as png image and store it into directory
     *
     * @param directory A directory
     * @param all       Make snapshots from all views or only from the actual selected view
     * @return returnCode
     * @throws IOException
     */
    public boolean makeSnapShot(File directory, boolean all) throws IOException {
        // check that directory is a directory and writable
        if (!(directory.isDirectory() && directory.canWrite())) {
            throw new IOException(directory.getAbsolutePath() + " isn't a writable directory!");
        }
        // check that viewlist isn't empty
        if (this.viewList == null || this.viewList.isEmpty()) {
            return false;
        }
        String timeStr = SimulationTime.getTimeString(model.getSimulationTime().getSimulationTime(),
            SimulationTime.SHOW_DAY_DATE_TIME_MILLIS_DST);
        String nowStr = SimulationTime.getTimeString(System.currentTimeMillis(), SimulationTime.SHOW_DATE_TIME);
        if (all) {
            // make snapshots from all views
            Enumeration<String> it = this.viewList.keys();
            while (it.hasMoreElements()) {
                long time = model.getSimulationTime().getSimulationTime();
                String id = it.nextElement();
                String title = timeStr + ", View: " + id + ", generated at " + nowStr;
                this.viewList.get(id).updateInit(time);
                double zoom = this.getZoomFactor(id);
                this.setZoomFactor(zoom, this.getZoomPoint(id), time);
                this.viewList.get(id).validate();
                BufferedImage image = this.viewList.get(id).getSnapShot(title);
                File file = new File(directory, this.adjustFileName(id) + ".png");
                ImageIO.write(image, "png", file);
            }
        } else {
            // make a snapshot from selected view
            String id = this.getSelectedViewId();
            String title = timeStr + ", View: " + id + ", generated at " + nowStr;
            BufferedImage image = this.viewList.get(id).getSnapShot(title);
            File file = new File(directory, this.adjustFileName(id) + ".png");
            ImageIO.write(image, "png", file);
        }
        return true;
    }

    private String adjustFileName(String name) {
        name = name.replace(' ', '_');
        name = name.replace(',', '_');
        name = name.replace('.', '-');
        name = name.replace(':', '-');
        name = name.replace('/', '%');
        name = name.replace('\\', '%');
        return name;
    }


    public class ZoomEntry {
        protected String viewId;
        protected boolean isSelected;
        protected double zoomFactor;
        protected Point zoomPoint;
        protected Rectangle viewRectangle;

        public ZoomEntry(String viewId, boolean isSelected, double zoomFactor, Point zoomPoint,
                         Rectangle viewRectangle) {
            this.viewId = viewId;
            this.isSelected = isSelected;
            this.zoomFactor = zoomFactor;
            this.zoomPoint = zoomPoint;
            this.viewRectangle = viewRectangle;
        }
    }


}
