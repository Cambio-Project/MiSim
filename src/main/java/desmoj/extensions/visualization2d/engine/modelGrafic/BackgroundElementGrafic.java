package desmoj.extensions.visualization2d.engine.modelGrafic;


import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;

import desmoj.extensions.visualization2d.engine.model.BackgroundElement;
import desmoj.extensions.visualization2d.engine.model.Model;
import desmoj.extensions.visualization2d.engine.util.VerticalLabelUI;


/**
 * The Constructor is called by BackgroundElement.createGrafic
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
public class BackgroundElementGrafic extends JComponent implements Grafic {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final Dimension defaultSize = new Dimension(10, 10);

    private final BackgroundElement backgroundElement;
    private Color background;
    private Color textColor;
    private Point topLeftIntern;
    private final Point topLeftExtern;
    private Point bottomRightIntern;
    private final Point bottomRightExtern;    // Position (1. Alternative)
    private Point middlepointIntern;
    private final Point middlepointExtern;            // Position (2. Alternative)
    private Dimension sizeExtern, sizeIntern;                    // Position (2. Alternative)
    private Font font;
    private final String viewId;
    private JLayeredPane contentLabel;

    /**
     * Constructor for fixed size rectangle without text When in backgroundElement a name is set, then a border with the
     * name is displayed.
     *
     * @param backgroundElement
     * @param viewId            Id of view
     * @param middle            middlePoint
     * @param size              fixed size of element
     * @param background        background color, when null then transparent
     */
    public BackgroundElementGrafic(BackgroundElement backgroundElement, String viewId, Point middle, Dimension size,
                                   Color background) {
        this(backgroundElement, viewId, null, null, middle, size, null, background);

    }


    /**
     * Constructor for dynamic size rectangle without text When in backgroundElement a name is set, then a border with
     * the name is displayed.
     *
     * @param backgroundElement
     * @param topLeft           topLeft corner
     * @param bottomRight       bottomRight corner
     * @param background        background color, when null then transparent
     */
    public BackgroundElementGrafic(BackgroundElement backgroundElement, String viewId, Point topLeft, Point bottomRight,
                                   Color background) {
        this(backgroundElement, viewId, topLeft, bottomRight, null, null,
            null, background);

    }

    /**
     * Constructor for fixed size rectangle with text and transparent background When in backgroundElement a name is
     * set, then a border with the name is displayed.
     *
     * @param backgroundElement
     * @param viewId            Id of view
     * @param middle            middlePoint
     * @param textColor         color of text
     */
    public BackgroundElementGrafic(BackgroundElement backgroundElement, String viewId, Point middle,
                                   Color textColor) {
        this(backgroundElement, viewId, null, null, middle, null, textColor, null);
    }

    /**
     * Constructor with all features There a 2 possible cases: (topLeft and bottomRight are both null) xor (middle and
     * size are null) When in backgroundElement a name is set, then a border with the name is displayed.
     *
     * @param backgroundElement
     * @param viewId            Id of view
     * @param topLeft           topLeft corner
     * @param bottomRight       bottomRight corner
     * @param middle            middlePoint
     * @param size              fixed size of element
     * @param textColor         color of text
     * @param background        background color, when null then transparent
     */
    public BackgroundElementGrafic(BackgroundElement backgroundElement, String viewId, Point topLeft, Point bottomRight,
                                   Point middle, Dimension size, Color textColor, Color background) {
        // TODO Auto-generated constructor stub
        if (viewId == null) {
            viewId = "main";
        }
        this.viewId = viewId;
        this.backgroundElement = backgroundElement;
        this.topLeftExtern = topLeft;
        this.bottomRightExtern = bottomRight;
        this.middlepointExtern = middle;
        if (size != null) {
            this.sizeExtern = size;
        }
        if (topLeft != null && bottomRight != null) {
            this.sizeExtern = new Dimension(bottomRight.x - topLeft.x, bottomRight.y - topLeft.y);
        }
        this.background = background;
        this.textColor = textColor;
        this.font = new Font("SansSerif",
            this.backgroundElement.getTextStyle(), this.backgroundElement.getTextSize());
        this.setOpaque(true);
        this.contentLabel = this.buildContent();
        if (this.sizeExtern == null) {
            this.sizeExtern = this.contentLabel.getPreferredSize();
        }
        if (this.sizeExtern == null) {
            this.sizeExtern = defaultSize;
        }

        boolean fallA = this.middlepointExtern != null;
        boolean fallB = this.topLeftExtern != null &&
            this.bottomRightExtern != null;
        if (!(fallA || fallB)) {
            throw new ModelGraficException("please, set a middlepoint or a topLeft- " +
                "and bottomRight- point for " + this.backgroundElement.getId());
        }
        this.transform();
        //System.out.println(backgroundElement.getText()+"  sizeExtern: "+this.sizeExtern+"  sizeIntern: "+this.sizeIntern);

        if (this.backgroundElement.getName() != null) {
            this.setBorder(BorderFactory.createTitledBorder(Grafic.Border_Default,
                this.backgroundElement.getName(), TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION, Grafic.FONT_DEFAULT,
                Grafic.COLOR_BORDER));
        }
        this.setLayout(new BorderLayout());
        this.add(this.contentLabel, BorderLayout.CENTER);
        //this.setBackground(ModelGrafic.COLOR_BACKGROUND);
        this.setBounds();
        if (this.backgroundElement.getModel().getCoordinatenListener() != null) {
            this.contentLabel.addMouseListener(this.backgroundElement.getModel().getCoordinatenListener());
            this.contentLabel.addMouseMotionListener(this.backgroundElement.getModel().getCoordinatenListener());
        }
        //System.out.println("background: "+this.background+"   id: "+this.backgroundElement.getId());
    }

    /**
     * get all views (viewId's) with BackgroundElements
     *
     * @return
     */
    public static String[] getViews(Model model) {
        TreeSet<String> views = new TreeSet<String>();
        String[] ids = model.getBackgroundElements().getAllIds();
        //BackgroundElement.classContent.getAllIds();
        for (int i = 0; i < ids.length; i++) {
            BackgroundElement bg = model.getBackgroundElements().get(ids[i]);
            BackgroundElementGrafic bgg = (BackgroundElementGrafic) bg.getGrafic();
            String viewId = bgg.getViewId();
            views.add(viewId);
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
     * Construct the smallest rectangle, which include the positions of all entities  in view
     *
     * @param viewId id of view
     * @return smallest Rectangle, when an background element exist. null otherwise
     */
    public static Rectangle getBoundsExternGlobal(Model model, String viewId) {
        boolean found = false;
        double minX = (double) Integer.MAX_VALUE / 2;
        double minY = (double) Integer.MAX_VALUE / 2;
        double maxX = (double) Integer.MIN_VALUE / 2;
        double maxY = (double) Integer.MIN_VALUE / 2;
        String[] id = model.getBackgroundElements().getAllIds();
        for (int i = 0; i < id.length; i++) {
            BackgroundElement backgroundElement = model.getBackgroundElements().get(id[i]);
            BackgroundElementGrafic backgroundElementGrafic = (BackgroundElementGrafic) backgroundElement.getGrafic();
            if (backgroundElementGrafic != null &&
                backgroundElementGrafic.getViewId().equals(viewId)) {
                found = true;
                Rectangle r = backgroundElementGrafic.getBoundsExtern();
                minX = Math.floor(Math.min(minX, r.getX()));
                minY = Math.floor(Math.min(minY, r.getY()));
                maxX = Math.ceil(Math.max(maxX, r.getX() + r.width));
                maxY = Math.ceil(Math.max(maxY, r.getY() + r.height));
                //System.out.println(backgroundElement.getId()+"   "+backgroundElementGrafic.getBoundsExtern().toString());
            }
        }
        Rectangle r = null;
        if (found) {
            r = new Rectangle((int) Math.round(minX), (int) Math.round(minY), (int) Math.round(maxX - minX),
                (int) Math.round(maxY - minY));
        }
        //System.out.println("BackgroundElementGrafic  BoundsExtern: "+r);
        return r;
    }

    /**
     * called by ViewGrafic.updateInit
     *
     * @param panel
     */
    public static void updateInit(Model model, String viewId, JComponent panel) {
        //System.out.println("in BackgroundElement.updateInit");

        // baue TreeSet tmp zur Sortierung der Abarbeitung auf
        TreeMap<Double, TreeSet<String>> tmp = new TreeMap<Double, TreeSet<String>>();
        String[] id = model.getBackgroundElements().getAllIds();
        for (int i = 0; i < id.length; i++) {
            BackgroundElement backgroundElement = model.getBackgroundElements().get(id[i]);
            Double level = new Double(backgroundElement.getLevel());
            TreeSet<String> tmpSet = tmp.get(level);
            if (tmpSet == null) {
                tmpSet = new TreeSet<String>();
            }
            tmpSet.add(id[i]);
            tmp.put(level, tmpSet);
        }

        // Abarbeitung des  TreeSet
        Iterator<Double> enLevel = tmp.keySet().iterator();
        while (enLevel.hasNext()) {
            TreeSet<String> tmpSet = tmp.get(enLevel.next());
            Iterator<String> enId = tmpSet.iterator();
            while (enId.hasNext()) {
                BackgroundElement backgroundElement = model.getBackgroundElements().get(enId.next());
                BackgroundElementGrafic backgroundElementGrafic =
                    (BackgroundElementGrafic) backgroundElement.getGrafic();
                if (backgroundElementGrafic != null &&
                    backgroundElementGrafic.getViewId().equals(viewId)) {
                    backgroundElementGrafic.transform();
                    backgroundElementGrafic.update(backgroundElementGrafic.textColor,
                        backgroundElementGrafic.background);
                    panel.add(backgroundElementGrafic);
                }
            }
        }
    }

    public String getViewId() {
        return this.viewId;
    }

    /**
     * transforms from external to internal coordinate system
     */
    public void transform() {
        //System.out.println("begin transform");
        if (this.middlepointExtern != null) {
            this.middlepointIntern =
                backgroundElement.getModel().getModelGrafic().transformToIntern(this.viewId, this.middlepointExtern);
            if (this.middlepointIntern == null) {
                this.middlepointIntern = this.middlepointExtern;
            }
            this.sizeIntern = this.sizeExtern;
        } else {
            this.topLeftIntern =
                backgroundElement.getModel().getModelGrafic().transformToIntern(this.viewId, this.topLeftExtern);
            this.bottomRightIntern =
                backgroundElement.getModel().getModelGrafic().transformToIntern(this.viewId, this.bottomRightExtern);
            if (this.topLeftIntern == null) {
                this.topLeftIntern = this.topLeftExtern;
            }
            if (this.bottomRightIntern == null) {
                this.bottomRightIntern = this.bottomRightExtern;
            }
            this.sizeExtern = new Dimension(this.bottomRightExtern.x - this.topLeftExtern.x,
                this.bottomRightExtern.y - this.topLeftExtern.y);

            this.sizeIntern = new Dimension(this.bottomRightIntern.x - this.topLeftIntern.x,
                this.bottomRightIntern.y - this.topLeftIntern.y);
        }
        this.setBounds();
        if (this.backgroundElement.getModel().getCoordinatenListener() != null) {
            this.addMouseMotionListener(this.backgroundElement.getModel().getCoordinatenListener());
        }
        //System.out.println("end transform sizeIntern: "+this.sizeIntern);
    }

    /**
     * paint background element as JLayeredPane with a JLabel for background-image and a JLabel for text Works only when
     * sizeIntern is known.
     *
     * @return
     */
    private JLayeredPane buildContent() {
        //System.out.println("begin buildContent sizeIntern: "+this.sizeIntern);
        JLayeredPane out = new JLayeredPane();
        JLabel imageLabel = new JLabel();
        JLabel textLabel = this.buildTextLabel();
        Dimension size = this.sizeIntern;
        if (size == null) {
            size = textLabel.getPreferredSize();
        }
        Rectangle internBound = new Rectangle(0, 0, size.width, size.height);

        // when a background-image exist and its area isn't 0
        if (this.backgroundElement.getImageId() != null &&
            backgroundElement.getModel().getImage(backgroundElement.getImageId()) != null &&
            (size.width != 0 && size.height != 0)) {
            Image image0 = backgroundElement.getModel().getImage(backgroundElement.getImageId());
            Image image = image0.getScaledInstance(internBound.width, internBound.height, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(image));
            imageLabel.setOpaque(false);
            imageLabel.setLayout(null);
            imageLabel.setBounds(internBound);
            imageLabel.setDoubleBuffered(true);
            out.add(imageLabel, new Integer(1));
        }
        textLabel.setLayout(null);
        textLabel.setBounds(internBound);
        textLabel.setDoubleBuffered(true);
        if (this.background != null) {
            textLabel.setBackground(background);
            textLabel.setOpaque(true);
        } else {
            textLabel.setOpaque(false);
        }
        out.add(textLabel, new Integer(2));

        out.setBounds(internBound);
        out.setOpaque(false);
        out.setVisible(true);
        out.validate();
        return out;
    }

    private JLabel buildTextLabel() {
        JLabel textLabel = new JLabel();
        if (backgroundElement.getText() != null) {
            String text = "";
            textLabel.setForeground(textColor);
            textLabel.setFont(font);
            switch (backgroundElement.getTextPosition()) {
                case BackgroundElement.TEXT_POSITION_BottomLeft:
                    text = "<html><body><p align=\"left\">" + backgroundElement.getText() + "</p></body></html>";
                    textLabel.setText(text);
                    textLabel.setHorizontalAlignment(JLabel.LEFT);
                    textLabel.setVerticalAlignment(JLabel.BOTTOM);
                    break;
                case BackgroundElement.TEXT_POSITION_BottomMiddle:
                    text = "<html><body><p align=\"center\">" + backgroundElement.getText() + "</p></body></html>";
                    textLabel.setText(text);
                    textLabel.setHorizontalAlignment(JLabel.CENTER);
                    textLabel.setVerticalAlignment(JLabel.BOTTOM);
                    break;
                case BackgroundElement.TEXT_POSITION_BottomRight:
                    text = "<html><body><p align=\"right\">" + backgroundElement.getText() + "</p></body></html>";
                    textLabel.setText(text);
                    textLabel.setHorizontalAlignment(JLabel.RIGHT);
                    textLabel.setVerticalAlignment(JLabel.BOTTOM);
                    break;
                case BackgroundElement.TEXT_POSITION_MiddleLeft:
                    text = backgroundElement.getText().replaceAll("<br>|<BR>", " ");
                    textLabel.setText(text);
                    textLabel.setUI(new VerticalLabelUI(false));
                    textLabel.setHorizontalAlignment(JLabel.CENTER);
                    textLabel.setVerticalAlignment(JLabel.TOP);
                    break;
                case BackgroundElement.TEXT_POSITION_Middle:
                    text = "<html><body><p align=\"center\">" + backgroundElement.getText() + "</p></body></html>";
                    textLabel.setText(text);
                    textLabel.setHorizontalAlignment(JLabel.CENTER);
                    textLabel.setVerticalAlignment(JLabel.CENTER);
                    break;
                case BackgroundElement.TEXT_POSITION_MiddleRight:
                    text = backgroundElement.getText().replaceAll("<br>|<BR>", " ");
                    textLabel.setText(text);
                    textLabel.setUI(new VerticalLabelUI(false));
                    textLabel.setHorizontalAlignment(JLabel.CENTER);
                    textLabel.setVerticalAlignment(JLabel.BOTTOM);
                    break;
                case BackgroundElement.TEXT_POSITION_TopLeft:
                    text = "<html><body><p align=\"left\">" + backgroundElement.getText() + "</p></body></html>";
                    textLabel.setText(text);
                    textLabel.setHorizontalAlignment(JLabel.LEFT);
                    textLabel.setVerticalAlignment(JLabel.TOP);
                    break;
                case BackgroundElement.TEXT_POSITION_TopMiddle:
                    text = "<html><body><p align=\"center\">" + backgroundElement.getText() + "</p></body></html>";
                    textLabel.setText(text);
                    textLabel.setHorizontalAlignment(JLabel.CENTER);
                    textLabel.setVerticalAlignment(JLabel.TOP);
                    break;
                case BackgroundElement.TEXT_POSITION_TopRight:
                    text = "<html><body><p align=\"right\">" + backgroundElement.getText() + "</p></body></html>";
                    textLabel.setText(text);
                    textLabel.setHorizontalAlignment(JLabel.RIGHT);
                    textLabel.setVerticalAlignment(JLabel.TOP);
                    break;
            }
        }
        return textLabel;
    }

    /**
     * sets bounds of background element
     */
    private void setBounds() {
        Insets in = new Insets(0, 0, 0, 0);
        if (this.getBorder() != null) {
            in = this.getBorder().getBorderInsets(this);
        }

        if (this.middlepointIntern != null) {
            this.setBounds(this.middlepointIntern.x - this.sizeIntern.width / 2 - in.left,
                this.middlepointIntern.y - this.sizeIntern.height / 2 - in.top,
                this.sizeIntern.width + in.left + in.right, this.sizeIntern.height + in.top + in.bottom);
        } else {
            this.setBounds(this.topLeftIntern.x - in.left, this.topLeftIntern.y - in.top,
                this.sizeIntern.width + in.left + in.right, this.sizeIntern.height + in.top + in.bottom);
        }
    }

    private void setPreferredSize() {
        Insets in = new Insets(0, 0, 0, 0);
        if (this.getBorder() != null) {
            in = this.getBorder().getBorderInsets(this);
        }

        Dimension d =
            new Dimension(this.sizeIntern.width + in.left + in.right, this.sizeIntern.height + in.top + in.bottom);
        this.setPreferredSize(d);
        this.setSize(d);
    }

    /**
     * get external size
     *
     * @return
     */
    public Rectangle getBoundsExtern() {
        Dimension d = null;
        Point p = null;
        if (this.topLeftExtern != null && this.bottomRightExtern != null) {
            p = this.topLeftExtern;
            d = new Dimension(this.bottomRightExtern.x - this.topLeftExtern.x,
                this.bottomRightExtern.y - this.topLeftExtern.y);
        }
        if (this.middlepointExtern != null) {
            if (this.sizeExtern != null) {
                d = this.sizeExtern;
            } else {
                d = this.sizeIntern;
            }
            p = new Point(this.middlepointExtern.x - d.width / 2,
                this.middlepointExtern.y - d.height / 2);
        }
        if (p == null || d == null) {
            throw new ModelGraficException("BackgroundElementGrafic.getBoundsExtern: Bounds Error");
        }
        return new Rectangle(p.x, p.y, d.width, d.height);
    }

    /**
     * Update of GraficGrafic called by BackgroundElement.setData and BackgroundElementGragic.updateInit()
     */
    public void update(Color foreground, Color background) {
        //System.out.println("begin update "+this.sizeIntern);
        this.remove(this.contentLabel);
        this.textColor = foreground;
        this.background = background;
        this.font = new Font("SansSerif", this.backgroundElement.getTextStyle(),
            this.backgroundElement.getTextSize());
        this.contentLabel = this.buildContent();
        this.add(this.contentLabel, BorderLayout.CENTER);
        this.validate();
        this.repaint();
    }

    protected void paintComponent(java.awt.Graphics g) {
        if ((this.background != null) || (this.backgroundElement.getImageId() != null)) {
            g.setColor(Grafic.COLOR_BACKGROUND);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
        super.paintComponent(g);
    }


}
