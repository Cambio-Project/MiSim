package desmoj.extensions.visualization2d.engine.model;

import javax.swing.ImageIcon;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Point;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TimeZone;

import desmoj.extensions.visualization2d.engine.command.Parameter;
import desmoj.extensions.visualization2d.engine.modelGrafic.ModelGrafic;
import desmoj.extensions.visualization2d.engine.modelGrafic.ModelGraficException;
import desmoj.extensions.visualization2d.engine.orga.ClassContent;
import desmoj.extensions.visualization2d.engine.viewer.CoordinatenListener;
import desmoj.extensions.visualization2d.engine.viewer.SimulationTime;
import desmoj.extensions.visualization2d.engine.viewer.ViewerPanel;


/**
 * Model manages the creation and update of every animation-object. the animation-objects are accessable over a static
 * animation.orga.ClassContent hashtable.
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
public class Model {

    private String modelName;
    private String modelAuthor;
    private String modelCreatedAt;
    private String modelDescription;
    private String modelLicence;
    private String modelRemark;
    private String modelProjectName;
    private String modelProjectURL;
    private String modelProjectIconId;
    private String desmojVersion;
    private String desmojLicense;
    private String desmojLicenseURL;

    private URL simulationIconDir;
    private long simulationBegin = Long.MIN_VALUE;
    private long simulationEnd = Long.MIN_VALUE;
    private TimeZone timeZone = TimeZone.getDefault();
    private double simulationSpeed = 1.0;
    private ModelGrafic modelGrafic = null;
    private boolean runPhase = false;
    private boolean valid = false;
    private SimulationTime simulationTime = null;
    private CoordinatenListener coordinatenListener = null;
    private ViewerPanel viewer = null;

    private Hashtable<String, ModelImage> images = null;

    private ClassContent<BackgroundElement> backgroundElements;
    private ClassContent<BackgroundLine> backgroundLines;
    private ClassContent<Bin> bins;
    private ClassContent<Entity> entities;
    private ClassContent<EntityType> entityTyps;
    private ClassContent<List> lists;
    private ClassContent<Process> processes;
    private ClassContent<ProcessNew> processNewes;
    private ClassContent<Resource> resources;
    private ClassContent<Route> routes;
    private ClassContent<Station> stations;
    private ClassContent<Statistic> statistics;
    private ClassContent<Stock> stocks;
    private ClassContent<WaitingQueue> waitingQueues;


    /**
     * @param simulationIconDir   may be null
     * @param coordinatenListener may be null
     * @throws ModelException
     */
    public Model(URL simulationIconDir, CoordinatenListener coordinatenListener, ViewerPanel viewer)
        throws ModelException {
        //System.out.println("Konstruktor Model");
        this.simulationIconDir = simulationIconDir;
        this.viewer = viewer;
        this.images = new Hashtable<String, ModelImage>();
        this.runPhase = false;
        this.valid = false;
        this.coordinatenListener = coordinatenListener;
        this.reset();
    }

    public ViewerPanel getViewer() {
        return this.viewer;
    }

    /**
     * makes a model-reset, must be used before a new cmds-file is read.
     */
    public void reset() {
        this.valid = false;
        this.runPhase = false;
        this.modelName = "";
        this.modelRemark = "";
        this.simulationBegin = Long.MIN_VALUE;
        this.simulationEnd = Long.MIN_VALUE;
        this.simulationSpeed = 1.0;
        this.modelGrafic = null;
        this.images.clear();

        this.backgroundElements = new ClassContent<BackgroundElement>();
        this.backgroundLines = new ClassContent<BackgroundLine>();
        this.bins = new ClassContent<Bin>();
        this.entities = new ClassContent<Entity>();
        this.entityTyps = new ClassContent<EntityType>();
        this.lists = new ClassContent<List>();
        this.processes = new ClassContent<Process>();
        this.processNewes = new ClassContent<ProcessNew>();
        this.resources = new ClassContent<Resource>();
        this.routes = new ClassContent<Route>();
        this.stations = new ClassContent<Station>();
        this.statistics = new ClassContent<Statistic>();
        this.stocks = new ClassContent<Stock>();
        this.waitingQueues = new ClassContent<WaitingQueue>();

        this.createModelGrafic();
    }

    /**
     * create the assoziated ModelGrafic instance.
     *
     * @return ModelGrafic
     * @throws ModelGraficException
     */
    public ModelGrafic createModelGrafic() throws ModelGraficException {
        this.modelGrafic = new ModelGrafic(this);
        //System.out.println("createModelGrafic   viewer: "+this.viewer);
        return this.modelGrafic;
    }

    /**
     * get associated ModelGrafic instance, when it's created before.
     *
     * @return ModelGrafic
     * @throws ModelGraficException
     */
    public ModelGrafic getModelGrafic() throws ModelGraficException {
        if (this.modelGrafic == null) {
            System.out.println("Model.getModelGrafic() is null");
        }
        return this.modelGrafic;
    }

    public void setSimulationIconDir(URL iconDir) {
        this.simulationIconDir = iconDir;
    }

    public SimulationTime getSimulationTime() {
        return this.simulationTime;
    }

    public void setSimulationTime(SimulationTime simulationTime) {
        this.simulationTime = simulationTime;
    }

    /**
     * set Model to run-phase, otherwise its init-phase
     *
     * @param run
     */
    public void setRunPhase(boolean run) {
        this.runPhase = run;
    }

    public boolean isValid() {
        return valid;
    }

    /**
     * sets a Model to valid or not. What means valid ?????????????????????
     *
     * @param valid
     */
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public CoordinatenListener getCoordinatenListener() {
        return this.coordinatenListener;
    }

    public ClassContent<BackgroundElement> getBackgroundElements() {
        return this.backgroundElements;
    }

    public ClassContent<BackgroundLine> getBackgroundLines() {
        return this.backgroundLines;
    }

    public ClassContent<Bin> getBins() {
        return this.bins;
    }

    public ClassContent<Entity> getEntities() {
        return this.entities;
    }

    public ClassContent<EntityType> getEntityTyps() {
        return this.entityTyps;
    }

    public ClassContent<List> getLists() {
        return this.lists;
    }

    public ClassContent<Process> getProcesses() {
        return this.processes;
    }

    public ClassContent<ProcessNew> getProcessNewes() {
        return this.processNewes;
    }

    public ClassContent<Resource> getResources() {
        return this.resources;
    }

    public ClassContent<Route> getRoutes() {
        return this.routes;
    }

    public ClassContent<Station> getStations() {
        return this.stations;
    }

    public ClassContent<Statistic> getStatistics() {
        return this.statistics;
    }

    public ClassContent<Stock> getStocks() {
        return this.stocks;
    }

    public ClassContent<WaitingQueue> getWaitingQueues() {
        return this.waitingQueues;
    }

    // -- create-methods ----------------------------------------------

    /**
     * used for model-creation by animation.command.Command.execute()
     */
    public void createModelBasisData(String[] projectName, String[] projectURL,
                                     String[] projectIconId, String modelName, String modelAuthor,
                                     String[] modelDate, String[] modelDescription, String[] modelRemark,
                                     String[] modelLicense, String[] desmojVersion, String[] desmojLicense,
                                     String[] desmojLicenseURL, boolean isInit, long time) throws ModelException {
        String nl = System.getProperty("line.separator");

        this.modelProjectName = "Simulation Project";
        if (projectName.length > 0) {
            this.modelProjectName = projectName[0];
        }

        this.modelProjectURL = null;
        if (projectURL.length > 0) {
            this.modelProjectURL = projectURL[0];
        }

        this.modelProjectIconId = null;
        if (projectIconId.length > 0) {
            this.modelProjectIconId = projectIconId[0];
        }

        this.modelName = modelName;
        this.modelAuthor = modelAuthor;

        this.modelCreatedAt = "";
        if (modelDate.length > 0) {
            this.modelCreatedAt = modelDate[0];
        }

        this.modelDescription = "";
        if (modelDescription.length > 0) {
            this.modelDescription = modelDescription[0];
        }

        this.modelRemark = "";
        for (int i = 0; i < modelRemark.length; i++) {
            this.modelRemark += modelRemark[i] + nl;
        }

        this.modelLicence = "";
        if (modelLicense.length > 0) {
            this.modelLicence = modelLicense[0];
        }

        this.desmojVersion = "";
        if (desmojVersion.length > 0) {
            this.desmojVersion = desmojVersion[0];
        }

        this.desmojLicense = "";
        if (desmojLicense.length > 0) {
            this.desmojLicense = desmojLicense[0];
        }

        this.desmojLicenseURL = null;
        if (desmojLicenseURL.length > 0) {
            this.desmojLicenseURL = desmojLicenseURL[0];
        }
    }

    /**
     * used for model-creation by animation.command.Command.execute()
     *
     * @param begin
     * @param end
     * @param defaultSpeed
     * @throws ModelException
     */
    public void createSimulationTimeBounds(String begin, String[] endArray, String[] timeZone, String defaultSpeed,
                                           boolean isInit, long time) throws ModelException {
        try {
            this.simulationBegin = Long.parseLong(begin);
        } catch (NumberFormatException e) {
            throw new ModelException("Model.createSimulationTimeBounds: SimulationBegin is no long: " + begin);
        }
        if (endArray.length >= 1) {
            try {
                this.simulationEnd = Long.parseLong(endArray[0]);
            } catch (NumberFormatException e) {
                throw new ModelException("Model.createSimulationTimeBounds: SimulationEnd is no long: " + endArray[0]);
            }
        } else {
            // Ende der Simulation ist nicht durch Zeit
            // sondern durch ein Stop Kriterium gegeben.
            this.simulationEnd = Long.MIN_VALUE;
        }
        if (timeZone.length >= 1) {
            this.timeZone = TimeZone.getTimeZone(timeZone[0]);
        } else {
            this.timeZone = TimeZone.getDefault();
        }
        try {
            this.simulationSpeed = Double.parseDouble(defaultSpeed);
        } catch (NumberFormatException e) {
            throw new ModelException(
                "Model.createSimulationTimeBounds: SimulationDefaultSpeed is no double: " + defaultSpeed);
        }
    }


    /**
     * used for model-creation by animation.command.Command.execute()
     *
     * @param id
     * @param datei
     * @throws ModelException
     */
    public void createImage(String id, String datei, boolean isInit, long time) throws ModelException {
        URL imageURL = null;
        try {
            imageURL = new URL(this.simulationIconDir.toString() + datei);
        } catch (MalformedURLException e) {
            throw new ModelException("MalformedURLException on URL: this.iconPath.toString()+datei");
        }
        //imageURL.
        ImageIcon ii = new ImageIcon(imageURL, id);
        if (ii.getImageLoadStatus() == MediaTracker.ERRORED) {
            throw new ModelException("Error on loading ImageIcon URL: " + imageURL);
        }
        ModelImage mi = new ModelImage();
        mi.id = id;
        mi.datei = datei;
        mi.image = ii.getImage();
        images.put(mi.id, mi);
    }


    /**
     * used for model-creation by animation.command.Command.execute()
     *
     * @param id
     * @param width
     * @param height
     * @param posStates
     * @param posAttributes
     * @throws ModelException
     */
    public void createEntityTyp(String id, String width, String height,
                                String[] posStates, String[] posAttributes, String show, boolean isInit, long time)
        throws ModelException {

        int w = 30, h = 30;
        int show1 = EntityType.SHOW_NAME | EntityType.SHOW_ICON;

        try {
            w = Integer.parseInt(width);
            h = Integer.parseInt(height);
        } catch (NumberFormatException e) {
            throw new ModelException(
                "Model.createEntityType: iconSize is no integer pair: (" + width + ", " + height + ")");
        }

        Attribute[] posStates1 = new Attribute[posStates.length];
        for (int i = 0; i < posStates.length; i++) {
            String[] split = Parameter.split(posStates[i]);
            if (split.length == 2) {
                posStates1[i] = new Attribute(split[0], split[1]);
            } else {
                throw new ModelException(
                    "Model.createEntityTyp The posibleStates attribute must have 2 parts, state and imageId. attribute:" +
                        posStates[i]);
            }
        }

        try {
            show1 = Integer.parseInt(show);
        } catch (NumberFormatException e) {
            show1 = EntityType.SHOW_NAME | EntityType.SHOW_ICON;
        }

        EntityType et = new EntityType(id, this, w, h, posStates1, posAttributes, show1);
    }

    public void createBackgroundElement(String id, String[] name, String[] text,
                                        String[] topLeft, String[] bottomRight, String[] middle,
                                        String[] size, String[] foreground, String[] background,
                                        String level, String[] imageId, boolean isInit, long time) {

        int textLocation = BackgroundElement.TEXT_POSITION_Middle;
        int textSize = BackgroundElement.TEXT_Size_Normal;
        int textStyle = BackgroundElement.TEXT_Style_Plain;
        double level1;
        String[] tmp;
        String textStr = null, viewId = "";
        Point topLeft1 = null, bottomRight1 = null, middle1 = null;
        Dimension size1 = null;
        Color foreground1 = Color.black, background1 = null;
        BackgroundElement backgroundElement = null;
        String imageId1 = null;

        if (imageId.length > 0) {
            imageId1 = imageId[0];
        }

        try {
            level1 = Double.parseDouble(level);
        } catch (NumberFormatException e) {
            throw new ModelException("Model.createBackgroundElement: level parameter is no double: " + level);
        }

        if (text.length > 0) {
            tmp = Parameter.split(text[0]);
            if (tmp.length != 4) {
                throw new ModelException("text parameter hasn't 4 parts! Text parameter: " + text[0]);
            }
            try {
                textStr = tmp[0];
                textLocation = Integer.parseInt(tmp[1]);
                textSize = Integer.parseInt(tmp[2]);
                textStyle = Integer.parseInt(tmp[3]);
            } catch (NumberFormatException e) {
                throw new ModelException("Model.createBackgroundElement: text parameter has wrong form: " + text);
            }
        }

        if (topLeft.length > 0) {
            tmp = Parameter.split(topLeft[0]);
            if (tmp.length != 3) {
                throw new ModelException(
                    "Model.createBackgroundElement: topLeft parameter has wrong form: " + topLeft[0] + "  id: " + id);
            }
            try {
                viewId = tmp[0];
                topLeft1 = new Point(Integer.parseInt(tmp[1]), Integer.parseInt(tmp[2]));
            } catch (NumberFormatException e) {
                throw new ModelException(
                    "Model.createBackgroundElement: topLeft parameter has wrong form: " + topLeft[0] + "  id: " + id);
            }
        }

        if (bottomRight.length > 0) {
            tmp = Parameter.split(bottomRight[0]);
            if (tmp.length != 3) {
                throw new ModelException(
                    "Model.createBackgroundElement: bottomRight parameter has wrong form: " + bottomRight[0] +
                        "  id: " + id);
            }
            try {
                viewId = tmp[0];
                bottomRight1 = new Point(Integer.parseInt(tmp[1]), Integer.parseInt(tmp[2]));
            } catch (NumberFormatException e) {
                throw new ModelException(
                    "Model.createBackgroundElement: bottomRight parameter has wrong form: " + bottomRight[0] +
                        "  id: " + id);
            }
        }

        if (middle.length > 0) {
            tmp = Parameter.split(middle[0]);
            if (tmp.length != 3) {
                throw new ModelException(
                    "Model.createBackgroundElement: middle parameter has wrong form: " + middle[0] + "  id: " + id);
            }
            try {
                viewId = tmp[0];
                middle1 = new Point(Integer.parseInt(tmp[1]), Integer.parseInt(tmp[2]));
            } catch (NumberFormatException e) {
                throw new ModelException(
                    "Model.createBackgroundElement: middle parameter has wrong form: " + middle[0] + "  id: " + id);
            }
        }

        if (size.length > 0) {
            tmp = Parameter.split(size[0]);
            if (tmp.length != 2) {
                throw new ModelException(
                    "Model.createBackgroundElement: size parameter has wrong form: " + size[0] + "  id: " + id);
            }
            try {
                size1 = new Dimension(Integer.parseInt(tmp[0]), Integer.parseInt(tmp[1]));
            } catch (NumberFormatException e) {
                throw new ModelException(
                    "Model.createBackgroundElement: size parameter has wrong form: " + size[0] + "  id: " + id);
            }
        }

        if (foreground.length > 0) {
            tmp = Parameter.split(foreground[0]);
            if (tmp.length != 3) {
                throw new ModelException(
                    "Model.createBackgroundElement: foreground parameter has wrong form: " + foreground[0] + "  id: " +
                        id);
            }
            try {
                foreground1 = new Color(Integer.parseInt(tmp[0]), Integer.parseInt(tmp[1]), Integer.parseInt(tmp[2]));
            } catch (NumberFormatException e) {
                throw new ModelException(
                    "Model.createBackgroundElement: foreground parameter has wrong form: " + foreground[0] + "  id: " +
                        id);
            }
        }

        if (background.length > 0) {
            tmp = Parameter.split(background[0]);
            if (tmp.length != 3) {
                throw new ModelException(
                    "Model.createBackgroundElement: background parameter has wrong form: " + background[0] + "  id: " +
                        id);
            }
            try {
                background1 = new Color(Integer.parseInt(tmp[0]), Integer.parseInt(tmp[1]), Integer.parseInt(tmp[2]));
            } catch (NumberFormatException e) {
                throw new ModelException(
                    "Model.createBackgroundElement: background parameter has wrong form: " + background[0] + "  id: " +
                        id);
            }
        }
        backgroundElement =
            new BackgroundElement(this, id, textStr, textLocation, textSize, textStyle, level1, imageId1);
        if (name.length > 0) {
            backgroundElement.setName(name[0]);
        }
        if (middle1 == null) {
            backgroundElement.createGrafic(viewId, topLeft1, bottomRight1, foreground1, background1);
        } else {
            backgroundElement.createGrafic(viewId, middle1, foreground1, background1, size1);
        }
    }

    public void setBackgroundElement(String id, String[] name, String[] text, String[] foreground, String[] background,
                                     String[] imageId, boolean isInit, long time) {

        int textLocation = BackgroundElement.TEXT_POSITION_Middle;
        int textSize = BackgroundElement.TEXT_Size_Normal;
        int textStyle = BackgroundElement.TEXT_Style_Plain;
        String[] tmp;
        String textStr = null;
        Color foreground1 = Color.black, background1 = null;
        String imageId1 = null;

        if (imageId.length > 0) {
            imageId1 = imageId[0];
        }

        if (isInit) {
            time = this.getSimulationBegin();
        }

        BackgroundElement backgroundElement = this.getBackgroundElements().get(id);
        if (backgroundElement != null) {
            if (text.length > 0) {
                tmp = Parameter.split(text[0]);
                if (tmp.length != 4) {
                    throw new ModelException(
                        "Model.setBackgroundElement: text parameter hasn't 4 parts! Text parameter: " + text[0]);
                }
                try {
                    textStr = tmp[0];
                    textLocation = Integer.parseInt(tmp[1]);
                    textSize = Integer.parseInt(tmp[2]);
                    textStyle = Integer.parseInt(tmp[3]);
                } catch (NumberFormatException e) {
                    throw new ModelException("Model.setBackgroundElement: text parameter has wrong form: " + text);
                }
                if (foreground.length > 0) {
                    tmp = Parameter.split(foreground[0]);
                    if (tmp.length != 3) {
                        throw new ModelException(
                            "Model.setBackgroundElement: foreground parameter has wrong form: " + foreground[0] +
                                "  id: " + id);
                    }
                    try {
                        foreground1 =
                            new Color(Integer.parseInt(tmp[0]), Integer.parseInt(tmp[1]), Integer.parseInt(tmp[2]));
                    } catch (NumberFormatException e) {
                        throw new ModelException(
                            "Model.setBackgroundElement: foreground parameter has wrong form: " + foreground[0] +
                                "  id: " + id);
                    }
                }
                if (background.length > 0) {
                    tmp = Parameter.split(background[0]);
                    if (tmp.length != 3) {
                        throw new ModelException(
                            "Model.setBackgroundElement: background parameter has wrong form: " + background[0] +
                                "  id: " + id);
                    }
                    try {
                        background1 =
                            new Color(Integer.parseInt(tmp[0]), Integer.parseInt(tmp[1]), Integer.parseInt(tmp[2]));
                    } catch (NumberFormatException e) {
                        throw new ModelException(
                            "Model.setBackgroundElement: background parameter has wrong form: " + background[0] +
                                "  id: " + id);
                    }
                }

            }
            if (name.length > 0) {
                backgroundElement.setName(name[0]);
            }
            if (text.length > 0) {
                backgroundElement.setData(textStr, textLocation, textSize, textStyle, foreground1, background1,
                    imageId1);
            }
        } else {
            throw new ModelException(
                "Model.setBackgroundElement BackgroundElementId does not exist. BackgroundElementId: " + id);
        }
    }

    public void createBackgroundLine(String id, String[] lineSize, String[] color, String startPoint, String[] addPoint,
                                     String level, boolean isInit, long time) {

        double level1;
        Color color1 = Color.black;
        int lineSize1 = BackgroundLine.LINE_Size_Normal;
        Point[] p = new Point[addPoint.length + 1];
        String viewId = "";
        String[] tmp;
        BackgroundLine backgroundLine;

        try {
            level1 = Double.parseDouble(level);
        } catch (NumberFormatException e) {
            throw new ModelException("Model.createBackgroundLine: level parameter is no double: " + level);
        }

        if (lineSize.length > 0) {
            try {
                lineSize1 = Integer.parseInt(lineSize[0]);
            } catch (NumberFormatException e) {
                throw new ModelException("Model.createBackgroundLine: lineSize parameter is no int: " + level);
            }
        }

        if (color.length > 0) {
            tmp = Parameter.split(color[0]);
            if (tmp.length != 3) {
                throw new ModelException(
                    "Model.createBackgroundLine: color parameter has wrong form: " + color[0] + "  id: " + id);
            }
            try {
                color1 = new Color(Integer.parseInt(tmp[0]), Integer.parseInt(tmp[1]), Integer.parseInt(tmp[2]));
            } catch (NumberFormatException e) {
                throw new ModelException(
                    "Model.createBackgroundLine: foreground parameter has wrong form: " + color[0] + "  id: " + id);
            }
        }

        if (startPoint != null) {
            tmp = Parameter.split(startPoint);
            viewId = tmp[0];
            try {
                int x = Integer.parseInt(tmp[1]);
                int y = Integer.parseInt(tmp[2]);
                p[0] = new Point(x, y);
            } catch (NumberFormatException e) {
                throw new ModelException(
                    "Model.createBackgroundLine Id: " + id + "  parse Error startPoint: " + startPoint);
            }
        } else {
            throw new ModelException("Model.createBackgroundLine Id: " + id + "  Here is no startPoint");
        }

        for (int i = 0; i < addPoint.length; i++) {
            tmp = Parameter.split(addPoint[i]);
            try {
                int x = Integer.parseInt(tmp[0]);
                int y = Integer.parseInt(tmp[1]);
                p[i + 1] = new Point(x, y);
            } catch (NumberFormatException e) {
                throw new ModelException(
                    "Model.createBackgroundLine Id: " + id + "  parse Error addPoint: " + addPoint[i]);
            }
        }
        backgroundLine = new BackgroundLine(this, id, lineSize1, level1);
        backgroundLine.createGrafic(viewId, p, color1);
    }


    /**
     * used for model-creation by animation.command.Command.execute()
     *
     * @param id
     * @param name
     * @param entityTypeId
     * @param velocity
     * @param state
     * @param attribute
     * @param position
     * @throws ModelException
     */
    public void createEntity(String id, String[] name, String entityTypeId,
                             String[] velocity, String state, String[] attribute, String[] position, boolean isInit,
                             long time) throws ModelException {

        if (isInit) {
            time = this.getSimulationBegin();
        }

        double x = 0.0, y = 0.0, an = 0.0;
        String viewId = "";
        boolean dir = true;
        String[] tmp;
        if (position.length > 0) {
            try {
                tmp = Parameter.split(position[0]);
                viewId = tmp[0];
                x = Double.parseDouble(tmp[1]);
                y = Double.parseDouble(tmp[2]);
                an = Double.parseDouble(tmp[3]);
                dir = Boolean.parseBoolean(tmp[4]);
            } catch (NumberFormatException e) {
                throw new ModelException(
                    "Model.createEntity  EntityId: " + id + "  parse Error position: " + position[0]);
            }
        }
        if (this.getEntities().exist(id)) {
            throw new ModelException("Model.createEntity EntityId already exist. EntityId: " + id);
        }
        Entity e = new Entity(this, id, entityTypeId, state, time);
        if (name.length > 0) {
            e.setNameAttribute(name[0], time);
        }
        if (velocity.length > 0) {
            e.setVelocityAttribute(velocity[0], time);
        }
        if (position.length == 0) {
            e.createGraficFree(time);
        } else {
            e.createGraficStatic(viewId, x, y, an, dir, time);
        }

        EntityType entityType = this.getEntityTyps().get(e.getEntityTypeId());
        for (int i = 0; i < attribute.length; i++) {
            tmp = Parameter.split(attribute[i]);
            if (entityType.existPossibleAttribut(tmp[0])) {
                e.setAttribute(tmp[0], tmp[1], time);
            } else {
                throw new ModelException(
                    "Model.setEntity  EntityId: " + id + "  Attribute " + tmp[0] + " is not possible.");
            }
        }
    }

    /**
     * used for model-creation by animation.command.Command.execute()
     *
     * @param id
     * @param velocity
     * @param state
     * @param attribute
     * @throws ModelException
     */
    public void setEntity(String id, String[] velocity, String[] state, String[] attribute, boolean isInit, long time)
        throws ModelException {
        if (isInit) {
            time = this.getSimulationBegin();
        }

        Entity entity = this.getEntities().get(id);
        if (entity != null) {
            EntityType entityType = this.getEntityTyps().get(entity.getEntityTypeId());
            String[] tmp;
            if (velocity.length > 0) {
                entity.setVelocityAttribute(velocity[0], time);
            }
            if (state.length > 0) {
                entity.setState(state[0], time);
            }
            for (int i = 0; i < attribute.length; i++) {
                tmp = Parameter.split(attribute[i]);
                if (entityType.existPossibleAttribut(tmp[0])) {
                    entity.setAttribute(tmp[0], tmp[1], time);
                } else {
                    throw new ModelException(
                        "Model.setEntity  EntityId: " + id + "  Attribute " + tmp[0] + " is not possible.");
                }
            }
        } else {
            throw new ModelException("Model.setEntity EntityId does not exist. EntityId: " + id);
        }
    }

    /**
     * Dispose an entity
     *
     * @param id
     * @param isInit
     * @param time
     * @throws ModelException
     */
    public void disposeEntity(String id, boolean isInit, long time) throws ModelException {
        if (isInit) {
            time = this.getSimulationBegin();
        }

        Entity entity = this.getEntities().get(id);
        if (entity != null) {
            entity.dispose();
        } else {
            throw new ModelException("Model.disposeEntity EntityId does not exist. EntityId: " + id);
        }
    }

    /**
     * used for model-creation by animation.command.Command.execute()
     *
     * @param id
     * @param name
     * @param defaultEntityType
     * @param numberOfVisible
     * @param form
     * @param point
     * @param deltaSize
     * @throws ModelException
     */
    public void createList(String id, String[] name, String defaultEntityType,
                           String numberOfVisible, String form, String point, String[] deltaSize, String[] comment,
                           boolean isInit, long time) throws ModelException {

        int anzVisible = 1, x = 0, y = 0, dx = 0, dy = 0;
        boolean horizontal = true;
        String viewId = "";
        String[] tmp;

        try {
            anzVisible = Integer.parseInt(numberOfVisible);
        } catch (NumberFormatException e) {
            throw new ModelException(
                "Model.createList  ListId: " + id + "  parse Error numberOfVisible: " + numberOfVisible);
        }

        if (form.trim().equals("horizontal")) {
            horizontal = true;
        } else horizontal = !form.trim().equals("vertikal");

        tmp = Parameter.split(point);
        try {
            viewId = tmp[0];
            x = Integer.parseInt(tmp[1]);
            y = Integer.parseInt(tmp[2]);
        } catch (NumberFormatException e) {
            throw new ModelException("Model.createList  ListId: " + id + "  parse Error point: " + point);
        }

        tmp[0] = "0";
        tmp[1] = "0";
        if (deltaSize.length > 0) {
            tmp = Parameter.split(deltaSize[0]);
        }
        try {
            dx = Integer.parseInt(tmp[0]);
            dy = Integer.parseInt(tmp[1]);
        } catch (NumberFormatException e) {
            throw new ModelException("Model.createList  ListId: " + id + "  parse Error DeltaSize: " + deltaSize);
        }

        if (!this.getEntityTyps().exist(defaultEntityType)) {
            throw new ModelException(
                "Model.createList  ListId: " + id + "  DefaultEntityType does not exist: " + defaultEntityType);
        }

        if (this.getLists().exist(id)) {
            throw new ModelException("Model.createList ListId already exist. ListId: " + id);
        }
        List list = new List(this, List.PREFIX_QUEUE, id);
        if (name.length > 0) {
            list.setName(name[0]);
        }

        if (comment.length > 0) {
            try {
                tmp = Parameter.split(comment[0]);
                list.setCommentTest(tmp[0]);
                list.setCommentFont(Integer.parseInt(tmp[1]), Integer.parseInt(tmp[2]));
                list.setCommentColor(
                    new Color(Integer.parseInt(tmp[3]), Integer.parseInt(tmp[4]), Integer.parseInt(tmp[5])));
                list.setCommentSizeExt(Boolean.parseBoolean(tmp[6]));
            } catch (NumberFormatException e) {
                throw new ModelException("Model.createList  ListId: " + id + "  parse Error Comment: " + comment);
            }
        }
        list.createGrafic(viewId, x, y, defaultEntityType, anzVisible, horizontal, new Dimension(dx, dy));

    }

    /**
     * used for model-creation by animation.command.Command.execute()
     *
     * @param id
     * @param addEntity
     * @param addBetween
     * @param removeEntity
     * @param removeAll
     * @param setRank
     * @param isInit
     * @param time
     * @throws ModelException
     */
    public void setList(String id, String[] addEntity, String[] addEntityAfter, String[] addEntityBefore,
                        String[] removeEntity, String[] removeAll, boolean isInit, long time) throws ModelException {
        if (isInit) {
            time = this.getSimulationBegin();
        }

        String[] tmp, tmp1;
        tmp1 = new String[5];
        int prio;
        if (!this.getLists().exist(List.PREFIX_QUEUE + id)) {
            throw new ModelException("Model.setList ListId does not exist. ListId: " + id);
        }
        List list = this.getLists().get(List.PREFIX_QUEUE + id);
        // remove special Entities
        for (int i = 0; i < removeEntity.length; i++) {
            if (list.containsInContainer(removeEntity[i])) {
                list.removeFromContainer(removeEntity[i], time);
            }
        }
        // remove all Entities
        if (removeAll.length > 0) {
            String[] ids = list.getAllContentFromContainer();
            for (int i = 0; i < ids.length; i++) {
                list.removeFromContainer(ids[i], time);
            }
        }
        // add Entity
        for (int i = 0; i < addEntity.length; i++) {
            tmp = Parameter.split(addEntity[i]);
            if (tmp.length == 3) {
                if (list.containsInContainer(tmp[0])) {
                    throw new ModelException(
                        "Model.setList addEntity Entity is alredy in list. ListId: " + id + "  Entity: " + tmp[0]);
                }
                try {
                    prio = Integer.parseInt(tmp[1]);
                } catch (NumberFormatException e) {
                    throw new ModelException(
                        "Model.setList addEntity prio is no Integer. ListId: " + id + "   prio: " + tmp[1]);
                }
                list.addToContainer(tmp[0], prio, tmp[2], time);
            } else {
                throw new ModelException("Model.setList addEntity needs 3 Parameter. ListId: " + id);
            }
        }

        // add EntityAfter
        for (int i = 0; i < addEntityAfter.length; i++) {
            tmp = Parameter.split(addEntityAfter[i]);
            if (tmp.length == 3) {
                if (list.containsInContainer(tmp[0])) {
                    throw new ModelException(
                        "Model.setList addEntityAfter Entity is alredy in list. ListId: " + id + "  Entity: " + tmp[0]);
                }
                try {
                    prio = Integer.parseInt(tmp[1]);
                } catch (NumberFormatException e) {
                    throw new ModelException(
                        "Model.setList addEntityAfter prio is no Integer. ListId: " + id + "   prio: " + tmp[1]);
                }
                if (!list.containsInContainer(tmp[2])) {
                    throw new ModelException(
                        "Model.setList addEntityAfter EntityAfter is not in list. ListId: " + id + "  EntityAfter: " +
                            tmp[0]);
                }
                list.addToContainerAfter(tmp[0], prio, tmp[2], time);
            } else {
                throw new ModelException("Model.setList addEntityAfter needs 3 Parameter. ListId: " + id);
            }
        }

        // add EntityBefore
        for (int i = 0; i < addEntityBefore.length; i++) {
            tmp = Parameter.split(addEntityBefore[i]);
            if (tmp.length == 3) {
                if (list.containsInContainer(tmp[0])) {
                    throw new ModelException(
                        "Model.setList addEntityBefore Entity is alredy in list. ListId: " + id + "  Entity: " +
                            tmp[0]);
                }
                try {
                    prio = Integer.parseInt(tmp[1]);
                } catch (NumberFormatException e) {
                    throw new ModelException(
                        "Model.setList addEntityBefore prio is no Integer. ListId: " + id + "   prio: " + tmp[1]);
                }
                if (!list.containsInContainer(tmp[2])) {
                    throw new ModelException(
                        "Model.setList addEntityBefore EntityBefore is not in list. ListId: " + id +
                            "  EntityBefore: " + tmp[0]);
                }
                list.addToContainerBefore(tmp[0], prio, tmp[2], time);
            } else {
                throw new ModelException("Model.setList addEntityBefore needs 3 Parameter. ListId: " + id);
            }
        }
    }

    /**
     * used for model-creation by animation.command.Command.execute()
     *
     * @param id
     * @param name
     * @param point
     * @throws ModelException
     */
    public void createStation(String id, String[] name, String point, boolean isInit, long time) throws ModelException {
        int x = 0, y = 0;
        String viewId = "";
        String[] tmp;

        tmp = Parameter.split(point);
        try {
            viewId = tmp[0];
            x = Integer.parseInt(tmp[1]);
            y = Integer.parseInt(tmp[2]);
        } catch (NumberFormatException e) {
            throw new ModelException("Model.createStation  StationId: " + id + "  parse Error point: " + point);
        }

        if (this.getStations().exist(id)) {
            throw new ModelException("Model.createStation StationId already exist. StationId: " + id);
        }
        Station station = new Station(this, id);
        if (name.length > 0) {
            station.setName(name[0]);
        }
        station.createGrafic(viewId, x, y);
    }

    /**
     * used for model-creation by animation.command.Command.execute()
     *
     * @param id
     * @param name
     * @param length
     * @param sourceStationId
     * @param sinkStationId
     * @param point
     * @param show
     * @param color
     * @param lineSize
     * @param isInit
     * @param time
     * @throws ModelException
     */
    public void createRoute(String id, String[] name, String length,
                            String sourceStationId, String sinkStationId, String[] point, String show, String[] color,
                            String lineSize, boolean isInit, long time) throws ModelException {

        double l = 1.0;
        String[] tmp;
        int x, y;
        Point[] p = new Point[point.length];
        int lineSize1 = Route.LINE_Size_Small;
        int show1 = Route.SHOW_NAME | Route.SHOW_LENGTH | Route.SHOW_DIRECTION;
        Color color1 = Color.BLACK;

        try {
            l = Double.parseDouble(length);
        } catch (NumberFormatException e) {
            throw new ModelException("Model.createRoute  RouteId: " + id + "  parse Error length: " + length);
        }
        for (int i = 0; i < point.length; i++) {
            tmp = Parameter.split(point[i]);
            try {
                x = Integer.parseInt(tmp[0]);
                y = Integer.parseInt(tmp[1]);
                p[i] = new Point(x, y);
            } catch (NumberFormatException e) {
                throw new ModelException("Model.createRoute  RouteId: " + id + "  parse Error point: " + point[i]);
            }
        }

        if (!this.getStations().exist(sourceStationId)) {
            throw new ModelException(
                "Model.createRoute  RouteId: " + id + "  SourceStation does not exist: " + sourceStationId);
        }
        if (!this.getStations().exist(sinkStationId)) {
            throw new ModelException(
                "Model.createRoute  RouteId: " + id + "  SinkStation does not exist: " + sinkStationId);
        }

        try {
            show1 = Integer.parseInt(show);
        } catch (NumberFormatException e) {
            show1 = Route.SHOW_NAME | Route.SHOW_LENGTH | Route.SHOW_DIRECTION;
        }

        try {
            lineSize1 = Integer.parseInt(lineSize);
        } catch (NumberFormatException e) {
            lineSize1 = Route.LINE_Size_Small;
        }

        if (color.length > 0) {
            tmp = Parameter.split(color[0]);
            if (tmp.length != 3) {
                throw new ModelException("Model.Route: color parameter has wrong form: " + color[0] + "  id: " + id);
            }
            try {
                color1 = new Color(Integer.parseInt(tmp[0]), Integer.parseInt(tmp[1]), Integer.parseInt(tmp[2]));
            } catch (NumberFormatException e) {
                throw new ModelException(
                    "Model.createRoute: foreground parameter has wrong form: " + color[0] + "  id: " + id);
            }
        }


        if (this.getRoutes().exist(id)) {
            throw new ModelException("Model.createRoute RouteId already exist. RouteId: " + id);
        }
        Route route = new Route(this, id, sourceStationId, sinkStationId, l);
        if (name.length > 0) {
            route.setName(name[0]);
        }
        route.createGrafic(p);
        route.setShow(show1);
        route.setColor(color1);
        route.setLineSize(lineSize1);

    }

    /**
     * used for model-creation by animation.command.Command.execute()
     *
     * @param id
     * @param addEntity
     * @param removeEntity
     * @param removeAll
     * @throws ModelException
     */
    public void setRoute(String id, String[] addEntity, String[] removeEntity,
                         String[] removeAll, String[] show, String[] color, String[] lineSize,
                         boolean isInit, long time) throws ModelException {

        int show1, lineSize1;
        String[] tmp;
        Color color1;

        if (isInit) {
            time = this.getSimulationBegin();
        }


        if (!this.getRoutes().exist(id)) {
            throw new ModelException("Model.setRoute RouteId does not exist. RouteId: " + id);
        }
        Route route = this.getRoutes().get(id);
        // remove special Entities
        for (int i = 0; i < removeEntity.length; i++) {
            if (route.containsInContainer(removeEntity[i])) {
                route.removeFromContainer(removeEntity[i], time);
            }
        }
        // remove all Entities
        if (removeAll.length > 0) {
            String[] ids = route.getAllContentFromContainer();
            for (int i = 0; i < ids.length; i++) {
                route.removeFromContainer(ids[i], time);
            }
        }
        // add special Entities
        for (int i = 0; i < addEntity.length; i++) {
            if (!route.containsInContainer(addEntity[i])) {
                route.addToContainer(addEntity[i], time);
            }
        }

        if (show.length > 0) {
            try {
                show1 = Integer.parseInt(show[0]);
            } catch (NumberFormatException e) {
                show1 = Route.SHOW_NAME | Route.SHOW_LENGTH | Route.SHOW_DIRECTION;
            }
            route.setShow(show1);
        }

        if (lineSize.length > 0) {
            try {
                lineSize1 = Integer.parseInt(lineSize[0]);
            } catch (NumberFormatException e) {
                lineSize1 = Route.LINE_Size_Small;
            }
            route.setLineSize(lineSize1);
        }

        if (color.length > 0) {
            tmp = Parameter.split(color[0]);
            if (tmp.length != 3) {
                throw new ModelException("Model.setRoute: color parameter has wrong form: " + color[0] + "  id: " + id);
            }
            try {
                color1 = new Color(Integer.parseInt(tmp[0]), Integer.parseInt(tmp[1]), Integer.parseInt(tmp[2]));
            } catch (NumberFormatException e) {
                throw new ModelException(
                    "Model.setRoute: foreground parameter has wrong form: " + color[0] + "  id: " + id);
            }
            route.setColor(color1);
        }


    }

    /**
     * used for model-creation by animation.command.Command.execute()
     *
     * @param id
     * @param name
     * @param numberOfResEntity
     * @param numberOfProcEntity
     * @param defaultResType
     * @param defaultProcType
     * @param listId
     * @param form
     * @param point
     * @throws ModelException
     */
    public void createProcess(String id, String[] name, String numberOfResEntity, String numberOfProcEntity,
                              String defaultResType, String defaultProcType, String[] listId, String form, String point,
                              boolean isInit, long time) throws ModelException {

        int anzResEntity = 1, anzProcEntity = 1, x = 0, y = 0;
        String viewId = "";
        String[] tmp;
        String listValue = null;

        try {
            anzResEntity = Integer.parseInt(numberOfResEntity);
        } catch (NumberFormatException e) {
            throw new ModelException(
                "Model.createProcess  ProcessId: " + id + "  parse Error numberOfResEntity: " + numberOfResEntity);
        }
        try {
            anzProcEntity = Integer.parseInt(numberOfProcEntity);
        } catch (NumberFormatException e) {
            throw new ModelException(
                "Model.createProcess  ProcessId: " + id + "  parse Error numberOfProcEntity: " + numberOfProcEntity);
        }
        tmp = Parameter.split(point);
        try {
            viewId = tmp[0];
            x = Integer.parseInt(tmp[1]);
            y = Integer.parseInt(tmp[2]);
        } catch (NumberFormatException e) {
            throw new ModelException("Model.createProcess  ProcessId: " + id + "  parse Error point: " + point);
        }


        if (!this.getEntityTyps().exist(defaultResType)) {
            throw new ModelException(
                "Model.createProcess  ProcessId: " + id + "  DefaultResEntityType does not exist: " + defaultResType);
        }
        if (!this.getEntityTyps().exist(defaultProcType)) {
            throw new ModelException(
                "Model.createProcess  ProcessId: " + id + "  DefaultProcEntityType does not exist: " + defaultProcType);
        }

        if (this.getProcesses().exist(id)) {
            throw new ModelException("Model.createProcess ProcessId already exist. ProcessId: " + id);
        }

        if (listId.length > 0) {
            listValue = List.PREFIX_QUEUE + listId[0];
        } else {
            listValue = null;
        }

        Process process = new Process(this, id, anzResEntity, anzProcEntity, listValue);
        if (name.length > 0) {
            process.setName(name[0]);
        }
        process.createGrafic(viewId, x, y, defaultResType);
    }

    /**
     * used for model-creation by animation.command.Command.execute()
     *
     * @param id
     * @param addResEntity
     * @param removeResEntity
     * @param addProcEntity
     * @param removeProcEntity
     * @throws ModelException
     */
    public void setProcess(String id, String[] addResEntity, String[] removeResEntity,
                           String[] addProcEntity, String[] removeProcEntity, boolean isInit, long time)
        throws ModelException {
        if (isInit) {
            time = this.getSimulationBegin();
        }

        if (!this.getProcesses().exist(id)) {
            throw new ModelException("Model.setProcess ProcessId does not exist. ProcessId: " + id);
        }
        Process process = this.getProcesses().get(id);
        // remove Process-Entities
        for (int i = 0; i < removeProcEntity.length; i++) {
            process.unsetProzessEntity(removeProcEntity[i], time);
        }
        // add Process-Entities
        for (int i = 0; i < addProcEntity.length; i++) {
            process.setProzessEntity(addProcEntity[i], time);
        }
        // remove Ressource-Entities
        for (int i = 0; i < removeResEntity.length; i++) {
            process.unsetResourceEntity(removeResEntity[i], time);
        }
        // add Ressource-Entities
        for (int i = 0; i < addResEntity.length; i++) {
            process.setResourceEntity(addResEntity[i], time);
        }

    }

    public void createProcessNew(String id, String[] name, String[] abstractProc,
                                 String[] resourceType, String[] resourceTotal, String[] listId,
                                 String point, String defaultEntityType, String anzVisible,
                                 String form, String[] showResources, String[] deltaSize, String[] comment,
                                 boolean isInit, long time) throws ModelException {

        int x, y, dx, dy, resourceTotalValue, anzVisibleValue;
        String viewId = "";
        boolean horizontal, showRes;
        String[] tmp;
        String listIdValue = null;
        ProcessNew process = null;

        if (this.getProcessNewes().exist(id)) {
            throw new ModelException("Model.createProcessNew ProcessId already exist. ProcessId: " + id);
        }

        if (listId.length > 0) {
            listIdValue = listId[0];
            if (!this.getLists().exist(listIdValue)) {
                throw new ModelException("Model.createProcessNew ListId does not exist. ListId: " + listIdValue);
            }
        } else {
            listIdValue = null;
        }

        tmp = Parameter.split(point);
        try {
            viewId = tmp[0];
            x = Integer.parseInt(tmp[1]);
            y = Integer.parseInt(tmp[2]);
        } catch (NumberFormatException e) {
            throw new ModelException("Model.createProcessNew  ProcessId: " + id + "  parse Error point: " + point);
        }

        if (!this.getEntityTyps().exist(defaultEntityType)) {
            throw new ModelException(
                "Model.createProcessNew  ProcessId: " + id + "  DefaultEntityType does not exist: " +
                    defaultEntityType);
        }

        try {
            anzVisibleValue = Integer.parseInt(anzVisible);
        } catch (NumberFormatException e) {
            throw new ModelException("Model.createProcessNew  anzVisible isn't a Integer  anzVisible: " + anzVisible);
        }

        if (form.trim().equals("horizontal")) {
            horizontal = true;
        } else horizontal = !form.trim().equals("vertikal");

        showRes = (showResources.length > 0);

        tmp[0] = "0";
        tmp[1] = "0";
        if (deltaSize.length > 0) {
            tmp = Parameter.split(deltaSize[0]);
        }
        try {
            dx = Integer.parseInt(tmp[0]);
            dy = Integer.parseInt(tmp[1]);
        } catch (NumberFormatException e) {
            throw new ModelException(
                "Model.createProcessNew  ProcessId: " + id + "  parse Error deltaSize: " + deltaSize);
        }

        if (abstractProc.length == 0) {
            // non abstact process
            process = new ProcessNew(this, ProcessNew.PREFIX_PROCESS, id, listIdValue);

        } else {
            // abstact process
            if (resourceType.length == 0) {
                throw new ModelException(
                    "Model.createProcessNew  It's a abstact Process. Id: " + id + "  There is no resourceType");
            }
            if (resourceTotal.length == 0) {
                throw new ModelException(
                    "Model.createProcessNew  It's a abstact Process. Id: " + id + "  There is no resourceTotal");
            }
            try {
                resourceTotalValue = Integer.parseInt(resourceTotal[0]);
            } catch (NumberFormatException e) {
                throw new ModelException("Model.createProcessNew  ResourceTotal is't a integer.");
            }
            process =
                new ProcessNew(this, ProcessNew.PREFIX_PROCESS, id, resourceType[0], resourceTotalValue, listIdValue);
        }
        if (name.length > 0) {
            process.setName(name[0]);
        }

        if (comment.length > 0) {
            try {
                tmp = Parameter.split(comment[0]);
                process.setCommentText(tmp[0]);
                process.setCommentFont(Integer.parseInt(tmp[1]), Integer.parseInt(tmp[2]));
                process.setCommentColor(
                    new Color(Integer.parseInt(tmp[3]), Integer.parseInt(tmp[4]), Integer.parseInt(tmp[5])));
                process.setCommentSizeExt(Boolean.parseBoolean(tmp[6]));
            } catch (NumberFormatException e) {
                throw new ModelException(
                    "Model.createProcessNew  ProcessId: " + id + "  parse Error Comment: " + comment);
            }
        }


        process.createGrafic(viewId, x, y, defaultEntityType, anzVisibleValue,
            horizontal, showRes, new Dimension(dx, dy));
    }


    public void setProcessNew(String id, String[] addProcEntity, String[] addResEntity,
                              String[] addResAnz, String[] removeEntity, boolean isInit, long time)
        throws ModelException {

        int[] addResAnzValue = new int[0];
        if (isInit) {
            time = this.getSimulationBegin();
        }

        if (!this.getProcessNewes().exist(ProcessNew.PREFIX_PROCESS + id)) {
            throw new ModelException("Model.setProcessNew ProcessId does not exist. ProcessId: " + id);
        }

        ProcessNew process = this.getProcessNewes().get(ProcessNew.PREFIX_PROCESS + id);

        for (int i = 0; i < addProcEntity.length; i++) {
            String[] ids = Parameter.split(addProcEntity[i]);
            for (int j = 0; j < ids.length; j++) {
                if (!this.getEntities().exist(ids[j])) {
                    throw new ModelException(
                        "Model.setProcessNew - addProcEntity EntityId does not exist. EntityId: " + ids[j]);
                }
            }
        }

        for (int i = 0; i < addResEntity.length; i++) {
            String[] ids = Parameter.split(addResEntity[i]);
            for (int j = 0; j < ids.length; j++) {
                if (!this.getEntities().exist(ids[j])) {
                    throw new ModelException(
                        "Model.setProcessNew - addResEntity EntityId does not exist. EntityId: " + ids[j]);
                }
            }
        }

        for (int i = 0; i < removeEntity.length; i++) {
            if (!this.getEntities().exist(removeEntity[i])) {
                throw new ModelException(
                    "Model.setProcessNew - removeEntity EntityId does not exist. EntityId: " + removeEntity[i]);
            }
        }

        // remove Entities
        if (removeEntity.length > 0) {
            process.removeEntry(removeEntity[0], time);
        }

        // add Process-Entities
        if (process.isAbstractResource() && addProcEntity.length > 0) {
            // addProcEntity in abstract process
            addResAnzValue = new int[addResAnz.length];
            for (int i = 0; i < addResAnz.length; i++) {
                try {
                    addResAnzValue[i] = Integer.parseInt(addResAnz[i]);
                } catch (NumberFormatException e) {
                    throw new ModelException("Model.setProcessNew  AddResAnz is't a integer.");
                }
            }
            if (addProcEntity.length == addResAnzValue.length) {
                for (int i = 0; i < addProcEntity.length; i++) {
                    process.addEntry(Parameter.split(addProcEntity[i]), addResAnzValue[i], time);
                }
            } else {
                throw new ModelException("Model.setProcessNew  It's a abstact Process. Id: " + id +
                    "  There are " + addProcEntity.length + " process Entries and "
                    + addResAnzValue.length + " resource counts These numbers must be equal");
            }
        } else {
            // non abstract process
            if (addProcEntity.length == addResEntity.length) {
                for (int i = 0; i < addProcEntity.length; i++) {
                    process.addEntry(Parameter.split(addProcEntity[i]), Parameter.split(addResEntity[i]), time);
                }
            } else {
                throw new ModelException("Model.setProcessNew  It's a non abstact Process. Id: " + id +
                    "  There are " + addProcEntity.length + " process Entries and "
                    + addResEntity.length + " resource Entries These numbers must be equal");
            }
        }
    }

    public void createResource(String id, String[] name, String[] resourceType,
                               String resourceTotal, String point, String defaultEntityType,
                               String anzVisible, String form, String[] deltaSize, boolean isInit, long time)
        throws ModelException {

        int x, y, dx, dy, anzVisibleValue, resourceTotalValue;
        String viewId = "";
        String[] tmp;
        boolean horizontal;

        if (this.getResources().exist(id)) {
            throw new ModelException("Model.createResource ProcessId already exist. ResourceId: " + id);
        }

        if (resourceType.length == 0) {
            throw new ModelException("Model.createResource  ResourceId: " + id + "  There is no resourceType");
        }

        try {
            resourceTotalValue = Integer.parseInt(resourceTotal);
        } catch (NumberFormatException e) {
            throw new ModelException("Model.createResource  ResourceTotal: \"" + resourceTotal + "\" is't a integer.");
        }


        tmp = Parameter.split(point);
        try {
            viewId = tmp[0];
            x = Integer.parseInt(tmp[1]);
            y = Integer.parseInt(tmp[2]);
        } catch (NumberFormatException e) {
            throw new ModelException("Model.createResource  ResourceId: " + id + "  parse Error point: " + point);
        }

        if (!this.getEntityTyps().exist(defaultEntityType)) {
            throw new ModelException(
                "Model.createResource  ResourceId: " + id + "  DefaultEntityType does not exist: " + defaultEntityType);
        }

        try {
            anzVisibleValue = Integer.parseInt(anzVisible);
        } catch (NumberFormatException e) {
            throw new ModelException("Model.createProcessNew  anzVisible isn't a Integer  anzVisible: " + anzVisible);
        }

        if (form.trim().equals("horizontal")) {
            horizontal = true;
        } else horizontal = !form.trim().equals("vertikal");

        tmp[0] = "0";
        tmp[1] = "0";
        if (deltaSize.length > 0) {
            tmp = Parameter.split(deltaSize[0]);
        }
        try {
            dx = Integer.parseInt(tmp[0]);
            dy = Integer.parseInt(tmp[1]);
        } catch (NumberFormatException e) {
            throw new ModelException(
                "Model.createProcessNew  ProcessId: " + id + "  parse Error deltaSize: " + deltaSize);
        }

        Resource resource = new Resource(this, id, resourceType[0], resourceTotalValue);
        if (name.length > 0) {
            resource.setName(name[0]);
        }
        resource.createGrafic(viewId, x, y, defaultEntityType, anzVisibleValue, horizontal, new Dimension(dx, dy));
    }

    public void setResource(String id, String[] provide, String[] takeProcess,
                            String[] takeBackProcess, boolean isInit, long time) throws ModelException {

        int resourceAnzValue, prio;
        String provideId;
        String[] tmp;
        if (isInit) {
            time = this.getSimulationBegin();
        }

        if (!this.getResources().exist(id)) {
            throw new ModelException("Model.setResource ResourceId does not exist. ResourceId: " + id);
        }
        Resource resource = this.getResources().get(id);

        if (provide.length > 0) {
            tmp = Parameter.split(provide[0]);
            if (tmp.length != 4) {
                throw new ModelException(
                    "Model.setResource - provide  Provide must have the form: entityId|priority|resourceAnz|sortorder. Provide: " +
                        provide[0]);
            }
            provideId = tmp[0];
            if (!this.getEntities().exist(provideId)) {
                throw new ModelException(
                    "Model.setResource - provide  EntityId does not exist. EntityId: " + provideId);
            }
            try {
                prio = Integer.parseInt(tmp[1]);
            } catch (NumberFormatException e) {
                throw new ModelException("Model.setResource - provide  priority isn't a Integer  priority: " + tmp[1]);
            }
            try {
                resourceAnzValue = Integer.parseInt(tmp[2]);
            } catch (NumberFormatException e) {
                throw new ModelException(
                    "Model.setResource - provide  resourceAnz isn't a Integer  anzVisible: " + tmp[1]);
            }
            resource.provide(provideId, prio, resourceAnzValue, tmp[3], time);
        }

        if (takeProcess.length > 0) {
            if (!this.getEntities().exist(takeProcess[0])) {
                throw new ModelException(
                    "Model.setResource - takeProcess  EntityId does not exist. EntityId: " + takeProcess[0]);
            }
            resource.takeProcess(takeProcess[0], time);
        }

        if (takeBackProcess.length > 0) {
            tmp = Parameter.split(takeBackProcess[0]);
            if (tmp.length != 2) {
                throw new ModelException(
                    "Model.setResource - takeBackProcess  TakeBackProcess must have the form: entityId|resourceAnz. TakeBackProcess: " +
                        takeBackProcess[0]);
            }
            if (!this.getEntities().exist(tmp[0])) {
                throw new ModelException(
                    "Model.setResource - takeBackProcess  EntityId does not exist. EntityId: " + tmp[0]);
            }
            try {
                resourceAnzValue = Integer.parseInt(tmp[1]);
            } catch (NumberFormatException e) {
                throw new ModelException(
                    "Model.setResource - takeBackProcess  resourceAnz isn't a Integer  anzVisible: " + tmp[1]);
            }
            resource.takeBack(tmp[0], resourceAnzValue, time);
        }
    }

    public void createStock(String id, String[] name, String capacity, String initialUnits,
                            String point, String defaultEntityType, String anzVisible,
                            String form, String[] deltaSize, boolean isInit, long time) throws ModelException {

        int x, y, dx, dy, anzVisibleValue;
        String viewId = "";
        long capacityValue, initialValue;
        String[] tmp;
        boolean horizontal;

        if (this.getStocks().exist(id)) {
            throw new ModelException("Model.createStock StockId already exist. StockId: " + id);
        }

        if (capacity == null) {
            throw new ModelException("Model.createStock  StockId: " + id + "  There is no capacity");
        }

        try {
            capacityValue = Long.parseLong(capacity);
        } catch (NumberFormatException e) {
            throw new ModelException("Model.createStock  Capacity: \"" + capacity + "\" is't a long.");
        }

        if (initialUnits == null) {
            throw new ModelException("Model.createStock  StockId: " + id + "  There are no initialUnits");
        }

        try {
            initialValue = Long.parseLong(initialUnits);
        } catch (NumberFormatException e) {
            throw new ModelException("Model.createStock  initialUnits: \"" + initialUnits + "\" is't a long.");
        }


        tmp = Parameter.split(point);
        try {
            viewId = tmp[0];
            x = Integer.parseInt(tmp[1]);
            y = Integer.parseInt(tmp[2]);
        } catch (NumberFormatException e) {
            throw new ModelException("Model.createStock  StockId: " + id + "  parse Error point: " + point);
        }

        if (!this.getEntityTyps().exist(defaultEntityType)) {
            throw new ModelException(
                "Model.createStock  StockId: " + id + "  DefaultEntityType does not exist: " + defaultEntityType);
        }

        try {
            anzVisibleValue = Integer.parseInt(anzVisible);
        } catch (NumberFormatException e) {
            throw new ModelException("Model.createStock  anzVisible isn't a Integer  anzVisible: " + anzVisible);
        }

        if (form.trim().equals("horizontal")) {
            horizontal = true;
        } else horizontal = !form.trim().equals("vertikal");

        tmp[0] = "0";
        tmp[1] = "0";
        if (deltaSize.length > 0) {
            tmp = Parameter.split(deltaSize[0]);
        }
        try {
            dx = Integer.parseInt(tmp[0]);
            dy = Integer.parseInt(tmp[1]);
        } catch (NumberFormatException e) {
            throw new ModelException("Model.createStock  StockId: " + id + "  parse Error deltaSize: " + deltaSize);
        }

        Stock stock = new Stock(this, id, capacityValue, initialValue);
        if (name.length > 0) {
            stock.setName(name[0]);
        }
        stock.createGrafic(viewId, x, y, defaultEntityType, anzVisibleValue, horizontal, new Dimension(dx, dy));
    }

    public void setStock(String id, String[] retrieveBegin, String[] retrieveEnd,
                         String[] storeBegin, String[] storeEnd, boolean isInit, long time) throws ModelException {

        int produkteAnz, prio;
        String retrieveId, storeId;
        String[] tmp;
        if (isInit) {
            time = this.getSimulationBegin();
        }

        if (!this.getStocks().exist(id)) {
            throw new ModelException("Model.setStock StockId does not exist. StockId: " + id);
        }
        Stock stock = this.getStocks().get(id);

        if (retrieveBegin.length > 0) {
            tmp = Parameter.split(retrieveBegin[0]);
            if (tmp.length != 4) {
                throw new ModelException(
                    "Model.setStock - retrieveBegin  RetrieveBegin must have the form: entityId|priority|resourceAnz|sortorder. RetrieveBegin: " +
                        retrieveBegin[0]);
            }
            retrieveId = tmp[0];
            if (!this.getEntities().exist(retrieveId)) {
                throw new ModelException("Model.setStock - retrieve  EntityId does not exist. EntityId: " + retrieveId);
            }
            try {
                prio = Integer.parseInt(tmp[1]);
            } catch (NumberFormatException e) {
                throw new ModelException("Model.setStock - retrieve priority isn't a Integer  priority: " + tmp[1]);
            }
            try {
                produkteAnz = Integer.parseInt(tmp[2]);
            } catch (NumberFormatException e) {
                throw new ModelException(
                    "Model.setStock - retrieve produkteAnz isn't a Integer  produkteAnz: " + tmp[1]);
            }
            stock.retrieveBegin(retrieveId, prio, produkteAnz, tmp[3], time);
        }

        if (retrieveEnd.length > 0) {
            if (!this.getEntities().exist(retrieveEnd[0])) {
                throw new ModelException(
                    "Model.setStore - retrieveEnd  EntityId does not exist. EntityId: " + retrieveEnd[0]);
            }
            stock.retrieveEnd(retrieveEnd[0], time);
        }

        if (storeBegin.length > 0) {
            tmp = Parameter.split(storeBegin[0]);
            if (tmp.length != 4) {
                throw new ModelException(
                    "Model.setStock - storeBegin  StoreBegin must have the form: entityId|priority|resourceAnz|sortorder. StoreBegin: " +
                        storeBegin[0]);
            }
            storeId = tmp[0];
            if (!this.getEntities().exist(storeId)) {
                throw new ModelException("Model.setStock - store  EntityId does not exist. EntityId: " + storeId);
            }
            try {
                prio = Integer.parseInt(tmp[1]);
            } catch (NumberFormatException e) {
                throw new ModelException("Model.setStock - store priority isn't a Integer  priority: " + tmp[1]);
            }
            try {
                produkteAnz = Integer.parseInt(tmp[2]);
            } catch (NumberFormatException e) {
                throw new ModelException("Model.setStock - store produkteAnz isn't a Integer  produkteAnz: " + tmp[1]);
            }
            stock.storeBegin(storeId, prio, produkteAnz, tmp[3], time);
        }

        if (storeEnd.length > 0) {
            if (!this.getEntities().exist(storeEnd[0])) {
                throw new ModelException(
                    "Model.setStore - storeEnd  EntityId does not exist. EntityId: " + storeEnd[0]);
            }
            stock.storeEnd(storeEnd[0], time);
        }
    }

    public void createBin(String id, String[] name, String initialUnits,
                          String point, String defaultEntityType, String anzVisible,
                          String form, String[] deltaSize, boolean isInit, long time) throws ModelException {

        int x, y, dx, dy, anzVisibleValue;
        String viewId = "";
        long initialValue;
        String[] tmp;
        boolean horizontal;

        if (this.getBins().exist(id)) {
            throw new ModelException("Model.createBin BinId already exist. BinId: " + id);
        }

        if (initialUnits == null) {
            throw new ModelException("Model.createBin  BinId: " + id + "  There are no initialUnits");
        }

        try {
            initialValue = Long.parseLong(initialUnits);
        } catch (NumberFormatException e) {
            throw new ModelException("Model.createBin  initialUnits: \"" + initialUnits + "\" is't a long.");
        }


        tmp = Parameter.split(point);
        try {
            viewId = tmp[0];
            x = Integer.parseInt(tmp[1]);
            y = Integer.parseInt(tmp[2]);
        } catch (NumberFormatException e) {
            throw new ModelException("Model.createBin  BinId: " + id + "  parse Error point: " + point);
        }

        if (!this.getEntityTyps().exist(defaultEntityType)) {
            throw new ModelException(
                "Model.createBin  BinId: " + id + "  DefaultEntityType does not exist: " + defaultEntityType);
        }

        try {
            anzVisibleValue = Integer.parseInt(anzVisible);
        } catch (NumberFormatException e) {
            throw new ModelException("Model.createBin  anzVisible isn't a Integer  anzVisible: " + anzVisible);
        }

        if (form.trim().equals("horizontal")) {
            horizontal = true;
        } else horizontal = !form.trim().equals("vertikal");

        tmp[0] = "0";
        tmp[1] = "0";
        if (deltaSize.length > 0) {
            tmp = Parameter.split(deltaSize[0]);
        }
        try {
            dx = Integer.parseInt(tmp[0]);
            dy = Integer.parseInt(tmp[1]);
        } catch (NumberFormatException e) {
            throw new ModelException("Model.createBin  BinId: " + id + "  parse Error deltaSize: " + deltaSize);
        }

        Bin bin = new Bin(this, id, initialValue);
        if (name.length > 0) {
            bin.setName(name[0]);
        }
        bin.createGrafic(viewId, x, y, defaultEntityType, anzVisibleValue, horizontal, new Dimension(dx, dy));
    }

    public void setBin(String id, String[] retrieveBegin, String[] retrieveEnd,
                       String[] store, boolean isInit, long time) throws ModelException {

        long produkteAnz;
        int prio;
        String retrieveId, storeId;
        String[] tmp;
        if (isInit) {
            time = this.getSimulationBegin();
        }

        if (!this.getBins().exist(id)) {
            throw new ModelException("Model.setBin BinId does not exist. BinId: " + id);
        }
        Bin bin = this.getBins().get(id);

        if (retrieveBegin.length > 0) {
            tmp = Parameter.split(retrieveBegin[0]);
            if (tmp.length != 4) {
                throw new ModelException(
                    "Model.setBin - retrieveBegin  RetrieveBegin must have the form: entityId|priority|resourceAnz|sortorder. RetrieveBegin: " +
                        retrieveBegin[0]);
            }
            retrieveId = tmp[0];
            if (!this.getEntities().exist(retrieveId)) {
                throw new ModelException("Model.setBin - retrieve  EntityId does not exist. EntityId: " + retrieveId);
            }
            try {
                prio = Integer.parseInt(tmp[1]);
            } catch (NumberFormatException e) {
                throw new ModelException("Model.setBin - retrieve priority isn't a Integer  priority: " + tmp[1]);
            }
            try {
                produkteAnz = Long.parseLong(tmp[2]);
            } catch (NumberFormatException e) {
                throw new ModelException("Model.setBin - retrieve produkteAnz isn't a Long  produkteAnz: " + tmp[2]);
            }
            bin.retrieveBegin(retrieveId, prio, produkteAnz, tmp[3], time);
        }

        if (retrieveEnd.length > 0) {
            if (!this.getEntities().exist(retrieveEnd[0])) {
                throw new ModelException(
                    "Model.setBin - retrieveEnd  EntityId does not exist. EntityId: " + retrieveEnd[0]);
            }
            bin.retrieveEnd(retrieveEnd[0], time);
        }

        if (store.length > 0) {
            tmp = Parameter.split(store[0]);
            if (tmp.length != 2) {
                throw new ModelException(
                    "Model.setBin - store  Store must have the form: entityId|resourceAnz. Store: " + store[0]);
            }
            storeId = tmp[0];
            if (!this.getEntities().exist(storeId)) {
                throw new ModelException("Model.setBin - store  EntityId does not exist. EntityId: " + storeId);
            }
            try {
                produkteAnz = Integer.parseInt(tmp[1]);
            } catch (NumberFormatException e) {
                throw new ModelException("Model.setBin - store produkteAnz isn't a Integer  produkteAnz: " + tmp[1]);
            }
            bin.store(storeId, produkteAnz, time);
        }
    }

    public void createWaitQueue(String id, String[] name,
                                String point, String defaultEntityType, String anzVisible,
                                String form, String[] deltaSize, boolean isInit, long time) throws ModelException {

        int x, y, dx, dy, anzVisibleValue;
        String viewId = "";
        String[] tmp;
        boolean horizontal;

        if (this.getWaitingQueues().exist(id)) {
            throw new ModelException("Model.createWaitQueue WaitQueueId already exist. WaitQueueId: " + id);
        }

        tmp = Parameter.split(point);
        try {
            viewId = tmp[0];
            x = Integer.parseInt(tmp[1]);
            y = Integer.parseInt(tmp[2]);
        } catch (NumberFormatException e) {
            throw new ModelException("Model.createWaitQueue  WaitQueueId: " + id + "  parse Error point: " + point);
        }

        if (!this.getEntityTyps().exist(defaultEntityType)) {
            throw new ModelException(
                "Model.createWaitQueue  WaitQueueId: " + id + "  DefaultEntityType does not exist: " +
                    defaultEntityType);
        }

        try {
            anzVisibleValue = Integer.parseInt(anzVisible);
        } catch (NumberFormatException e) {
            throw new ModelException("Model.createWaitQueue  anzVisible isn't a Integer  anzVisible: " + anzVisible);
        }

        if (form.trim().equals("horizontal")) {
            horizontal = true;
        } else horizontal = !form.trim().equals("vertikal");

        tmp[0] = "0";
        tmp[1] = "0";
        if (deltaSize.length > 0) {
            tmp = Parameter.split(deltaSize[0]);
        }
        try {
            dx = Integer.parseInt(tmp[0]);
            dy = Integer.parseInt(tmp[1]);
        } catch (NumberFormatException e) {
            throw new ModelException(
                "Model.createWaitQueue  WaitQueueId: " + id + "  parse Error deltaSize: " + deltaSize);
        }

        WaitingQueue waitingQueue = new WaitingQueue(this, id);
        if (name.length > 0) {
            waitingQueue.setName(name[0]);
        }
        waitingQueue.createGrafic(viewId, x, y, defaultEntityType, anzVisibleValue, horizontal, new Dimension(dx, dy));
    }

    public void setWaitQueue(String id, String[] insertMaster, String[] insertSlave,
                             String[] cooperationBegin, String[] cooperationEnd, boolean isInit, long time)
        throws ModelException {

        String masterId, slaveId, listRule;
        String[] tmp;
        int prio;
        if (isInit) {
            time = this.getSimulationBegin();
        }

        if (!this.getWaitingQueues().exist(id)) {
            throw new ModelException("Model.setWaitQueue WaitQueueId does not exist. WaitQueueId: " + id);
        }
        WaitingQueue waitingQueue = this.getWaitingQueues().get(id);

        if (insertMaster.length > 0) {
            tmp = Parameter.split(insertMaster[0]);
            if (tmp.length != 3) {
                throw new ModelException(
                    "Model.setWaitQueue - insertMaster must have the form: masterId|priority|listRule. insertMaster: " +
                        insertMaster[0]);
            }
            masterId = tmp[0];
            try {
                prio = Integer.parseInt(tmp[1]);
            } catch (NumberFormatException e) {
                throw new ModelException(
                    "Model.setWaitQueue - insertMaster  priority is no Integer. Priority: " + tmp[1]);
            }
            listRule = tmp[2];
            if (!this.getEntities().exist(masterId)) {
                throw new ModelException(
                    "Model.setWaitQueue - insertMaster  EntityId does not exist. EntityId: " + insertMaster[0]);
            }
            waitingQueue.insert(masterId, prio, true, listRule, time);
        }

        if (insertSlave.length > 0) {
            tmp = Parameter.split(insertSlave[0]);
            if (tmp.length != 3) {
                throw new ModelException(
                    "Model.setWaitQueue - insertSlave must have the form: slaveId|listRule. insertSlave: " +
                        insertSlave[0]);
            }
            slaveId = tmp[0];
            try {
                prio = Integer.parseInt(tmp[1]);
            } catch (NumberFormatException e) {
                throw new ModelException(
                    "Model.setWaitQueue - insertSlave  priority is no Integer. Priority: " + tmp[1]);
            }
            listRule = tmp[2];
            if (!this.getEntities().exist(slaveId)) {
                throw new ModelException(
                    "Model.setWaitQueue - insertSlave  EntityId does not exist. EntityId: " + insertSlave[0]);
            }
            waitingQueue.insert(slaveId, prio, false, listRule, time);
        }

        if (cooperationBegin.length > 0) {
            tmp = Parameter.split(cooperationBegin[0]);
            if (tmp.length != 2) {
                throw new ModelException(
                    "Model.setWaitQueue - cooperationBegin must have the form: masterId|slaveId. cooperationBegin: " +
                        cooperationBegin[0]);
            }
            masterId = tmp[0];
            slaveId = tmp[1];
            if (!this.getEntities().exist(masterId)) {
                throw new ModelException(
                    "Model.setWaitQueue - cooperationBegin  MasterId does not exist. MasterId: " + masterId);
            }
            if (!this.getEntities().exist(slaveId)) {
                throw new ModelException(
                    "Model.setWaitQueue - cooperationBegin  SlaveId does not exist. SlaveId: " + masterId);
            }
            waitingQueue.cooperationBegin(masterId, slaveId, time);
        }

        if (cooperationEnd.length > 0) {
            tmp = Parameter.split(cooperationEnd[0]);
            if (tmp.length != 2) {
                throw new ModelException(
                    "Model.setWaitQueue - cooperationEnd must have the form: masterId|slaveId. CooperationEnd: " +
                        cooperationBegin[0]);
            }
            masterId = tmp[0];
            slaveId = tmp[1];
            if (!this.getEntities().exist(masterId)) {
                throw new ModelException(
                    "Model.setWaitQueue - cooperationEnd  MasterId does not exist. MasterId: " + masterId);
            }
            if (!this.getEntities().exist(slaveId)) {
                throw new ModelException(
                    "Model.setWaitQueue - cooperationEnd  SlaveId does not exist. SlaveId: " + masterId);
            }
            waitingQueue.cooperationEnd(masterId, slaveId, time);
        }
    }

    public void createStatistic(String id, String[] name, String typeData,
                                String typeIndex, String[] aggregate, String timeBounds,
                                String valueBounds, String histogramCells, String point, String typeAnimation,
                                String[] isIntValue, String[] deltaSize, boolean isInit, long time)
        throws ModelException {

        String[] tmp;
        int typeDataValue, typeIndexValue, typeAnimationValue;
        int x, y, dx, dy, histogramCellsValue;
        String viewId = "";
        long timeLow, timeHigh;
        double valueLow, valueHigh;
        boolean isAggregate, hasIntValue;

        if (this.getStatistics().exist(id)) {
            throw new ModelException("Model.createStatistic StatisticId already exist. StatisticId: " + id);
        }

        try {
            typeDataValue = Integer.parseInt(typeData);
        } catch (NumberFormatException e) {
            throw new ModelException(
                "Model.createStatistic StatisticId: " + id + " TypeData is no Integer. TypeData: " + typeData);
        }

        try {
            typeIndexValue = Integer.parseInt(typeIndex);
        } catch (NumberFormatException e) {
            throw new ModelException(
                "Model.createStatistic StatisticId: " + id + " TypeIndex is no Integer. TypeData: " + typeIndex);
        }

        isAggregate = aggregate.length > 0;

        tmp = Parameter.split(timeBounds);
        try {
            timeLow = Long.parseLong(tmp[0]);
            timeHigh = Long.parseLong(tmp[1]);
        } catch (NumberFormatException e) {
            throw new ModelException(
                "Model.createStatistic  StatisticId: " + id + "  parse Error timeBounds: " + timeBounds);
        }

        tmp = Parameter.split(valueBounds);
        try {
            valueLow = Double.parseDouble(tmp[0]);
            valueHigh = Double.parseDouble(tmp[1]);
        } catch (NumberFormatException e) {
            throw new ModelException(
                "Model.createStatistic  StatisticId: " + id + "  parse Error valueBounds: " + valueBounds);
        }

        try {
            histogramCellsValue = Integer.parseInt(histogramCells);
        } catch (NumberFormatException e) {
            throw new ModelException(
                "Model.createStatistic StatisticId: " + id + " HistogramCells is no Integer. HistogramCells: " +
                    histogramCells);
        }

        // Grafic Parameter
        tmp = Parameter.split(point);
        try {
            viewId = tmp[0];
            x = Integer.parseInt(tmp[1]);
            y = Integer.parseInt(tmp[2]);
        } catch (NumberFormatException e) {
            throw new ModelException("Model.createStatistic  StatisticId: " + id + "  parse Error point: " + point);
        }

        try {
            typeAnimationValue = Integer.parseInt(typeAnimation);
        } catch (NumberFormatException e) {
            throw new ModelException(
                "Model.createStatistic StatisticId: " + id + " TypeAnimation is no Integer. TypeAnimation: " +
                    typeAnimation);
        }

        hasIntValue = isIntValue.length > 0;

        tmp[0] = "0";
        tmp[1] = "0";
        if (deltaSize.length > 0) {
            tmp = Parameter.split(deltaSize[0]);
        }
        try {
            dx = Integer.parseInt(tmp[0]);
            dy = Integer.parseInt(tmp[1]);
        } catch (NumberFormatException e) {
            throw new ModelException(
                "Model.createStatistic  StatisticId: " + id + "  parse Error deltaSize: " + deltaSize);
        }

        Statistic statistic = new Statistic(this, id, typeDataValue, typeIndexValue, isAggregate,
            timeLow, timeHigh, valueLow, valueHigh, histogramCellsValue);
        if (name.length > 0) {
            statistic.setName(name[0]);
        }
        statistic.createGrafic(viewId, x, y, typeAnimationValue, hasIntValue, new Dimension(dx, dy), false);
    }

    public void setStatistic(String id, String value, boolean isInit, long time) throws ModelException {

        //System.out.println("setStatistic  id: "+id+"  isInit: "+isInit+"  time: "+time);
        double val;


        if (!this.getStatistics().exist(id)) {
            throw new ModelException("Model.setStatistic StatisticId does not exist. StatisticId: " + id);
        }
        Statistic statistic = this.getStatistics().get(id);

        try {
            val = Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new ModelException("Model.setStatistic StatisticId: " + id + " Value is no Double. Value: " + value);
        }
        statistic.update(val, time);
    }

    public void resetStatistic(String id, boolean isInit, long time) throws ModelException {

        //System.out.println("resetStatistic  id: "+id+"  isInit: "+isInit+"  time: "+time);


        if (!this.getStatistics().exist(id)) {
            throw new ModelException("Model.setStatistic StatisticId does not exist. StatisticId: " + id);
        }
        Statistic statistic = this.getStatistics().get(id);
        statistic.reset();
    }

    // -- get-methods ----------------------------------------------

    public String getModelName() {
        return this.modelName;
    }

    public String getModelAuthor() {
        return this.modelAuthor;
    }

    public String getModelDescription() {
        return this.modelDescription;
    }

    public String getModelCreatedAt() {
        return this.modelCreatedAt;
    }

    public String getModelLicence() {
        return this.modelLicence;
    }

    public String getModelRemark() {
        return this.modelRemark;
    }

    public String getModelProjectName() {
        return this.modelProjectName;
    }

    public String getModelProjectURL() {
        return this.modelProjectURL;
    }

    public String getModelProjectIconId() {
        return this.modelProjectIconId;
    }

    public String getDesmojLicense() {
        return this.desmojLicense;
    }

    public String getDesmojLicenseURL() {
        return this.desmojLicenseURL;
    }

    public String getDesmojVersion() {
        return this.desmojVersion;
    }

    public long getSimulationBegin() {
        return this.simulationBegin;
    }

    /**
     * End of SimulationTime. Long.MIN_VALUE means, end of simulation is given by stop role and not by time.
     *
     * @return
     */
    public long getSimulationEnd() {

        return this.simulationEnd;
    }

    public TimeZone getTimeZone() {
        return this.timeZone;
    }

    public double getSimulationSpeed() {
        return this.simulationSpeed;
    }

    public Image getImage(String id) {
        Image out = null;
        ModelImage mi = this.images.get(id);
        if (mi != null) {
            out = mi.image;
        }
        return out;
    }

    public String[] getImageIDs() {
        Iterator<String> it = this.images.keySet().iterator();
        String[] out = new String[this.images.size()];
        int i = 0;
        while (it.hasNext()) {
            out[i++] = it.next();
        }
        return out;
    }

    public boolean containsImageId(String id) {
        return this.images.containsKey(id);
    }

    // -- init ----------------------------------------------

    /**
     * only used for testing
     */
    public void init_EntityTypes() throws ModelException {
        String[] attr = {"created at"};
        String[] posStates = {"active|active", "passive|passive", "bussy|bussy", "outOfOrder|outOfOrder"};
        this.createEntityTyp("Patient", "30", "30", posStates, attr, "3", true, 0);
    }

    /**
     * only used for testing
     *
     * @throws ModelException
     */
    public void init_Images() throws ModelException {
        this.createImage("Duke", "Duke-0.gif", true, 0);
        this.createImage("active", "active1.gif", true, 0);
        this.createImage("passive", "passive.gif", true, 0);
        this.createImage("bussy", "bussy.gif", true, 0);
        this.createImage("outOfOrder", "outOfOrder.gif", true, 0);
        this.createImage("animationError", "error.gif", true, 0);
    }

    // -- sub-classes ----------------------------------------------

    class ModelImage {
        String id;
        String datei;
        Image image;
    }
}
