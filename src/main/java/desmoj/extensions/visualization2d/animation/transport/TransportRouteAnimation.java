package desmoj.extensions.visualization2d.animation.transport;

import java.awt.Color;
import java.awt.Point;
import java.util.concurrent.TimeUnit;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.ModelComponent;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;
import desmoj.extensions.visualization2d.animation.CmdGeneration;
import desmoj.extensions.visualization2d.animation.Position;
import desmoj.extensions.visualization2d.animation.core.simulator.EntityBasicAnimation;
import desmoj.extensions.visualization2d.animation.core.simulator.ModelAnimation;
import desmoj.extensions.visualization2d.animation.internalTools.SimulationException;
import desmoj.extensions.visualization2d.engine.command.Command;
import desmoj.extensions.visualization2d.engine.command.CommandException;
import desmoj.extensions.visualization2d.engine.command.Parameter;
import desmoj.extensions.visualization2d.engine.model.Route;

/**
 * Creates a RouteAnimation for EntityAnimations or SimProcessAnimations
 *
 * @param <E> EntityAnimations or SimProcessAnimation
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
public class TransportRouteAnimation
    <E extends EntityBasicAnimation> extends ModelComponent {

    private Model model = null;
    private String name = null;
    private double length = 1.0;
    private CmdGeneration cmdGen = null;
    private boolean showInAnimation = true;
    private final String id;
    private int show;
    private int lineSize;
    private Color color = Color.BLACK;

    /**
     * creates a route from sourceStation tp sinkStation. The route includes some intermediate points p[]
     *
     * @param owner
     * @param name            Name of route
     * @param length          Length of route
     * @param sourceStation
     * @param sinkStation
     * @param pos             Intermediate positions
     * @param showInAnimation
     */
    public TransportRouteAnimation(ModelAnimation owner, String name, double length,
                                   TransportStationAnimation sourceStation, TransportStationAnimation sinkStation,
                                   Position[] pos, boolean showInAnimation) {
        super(owner, name);
        this.model = owner;
        this.name = name;
        this.length = length;
        this.show = Route.SHOW_NAME | Route.SHOW_LENGTH | Route.SHOW_DIRECTION | Route.SHOW_LINE;
        this.lineSize = Route.LINE_Size_Small;
        this.color = Color.black;
        this.cmdGen = owner.getCmdGen();
        this.id = this.cmdGen.createInternId(name);
        TimeInstant simTime = this.model.presentTime();
        boolean init = this.cmdGen.isInitPhase();
        Command c;
        this.showInAnimation = showInAnimation;
        String sourceView = sourceStation.getPosition().getView();
        String sinkView = sinkStation.getPosition().getView();
        if (!sourceView.equals(sinkView)) {
            throw new SimulationException("SourceStation and SinkStation don't have the same view. " +
                "RouteId: " + this.name + "  SourceView: " + sourceView + "  SinkView: " + sinkView);
        }

        if (this.showInAnimation) {
            try {
                if (init) {
                    c = Command.getCommandInit("createRoute", this.cmdGen.getAnimationTime(simTime));
                } else {
                    c = Command.getCommandTime("createRoute", this.cmdGen.getAnimationTime(simTime));
                }
                c.addParameter("RouteId", this.id);
                c.addParameter("Name", this.name);
                c.addParameter("Length", Double.toString(this.length));
                c.addParameter("SourceStationId", sourceStation.getInternId());
                c.addParameter("SinkStationId", sinkStation.getInternId());
                if (pos != null) {
                    for (int i = 0; i < pos.length; i++) {
                        Point p = pos[i].getPoint();
                        c.addParameter("Point", p.x + "|" + p.y);
                    }
                }
                c.addParameter("Show", Integer.toString(this.show));
                c.addParameter("LineSize", Integer.toString(this.lineSize));
                if (this.color != null) {
                    String[] color1 = {Integer.toString(this.color.getRed()),
                        Integer.toString(this.color.getGreen()), Integer.toString(this.color.getBlue())};
                    c.addParameter("Color", Parameter.cat(color1));
                }
                c.setRemark(this.getGeneratedBy(TransportRouteAnimation.class.getSimpleName()));
                this.cmdGen.checkAndLog(c);
                cmdGen.write(c);
            } catch (CommandException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    /**
     * change attribute values
     *
     * @param show     combination of Route.SHOW_ flags
     * @param lineSize look at Route.LINE_Size_
     * @param color    color of route animation
     * @return
     */
    public boolean setAttributes(int show, int lineSize, Color color) {
        boolean out = true;
        this.show = show;
        this.lineSize = lineSize;
        this.color = color;
        TimeInstant simTime = this.model.presentTime();
        boolean init = this.cmdGen.isInitPhase();
        Command c;
        if (this.showInAnimation) {
            try {
                if (init) {
                    c = Command.getCommandInit("setRoute", this.cmdGen.getAnimationTime(simTime));
                } else {
                    c = Command.getCommandTime("setRoute", this.cmdGen.getAnimationTime(simTime));
                }
                c.addParameter("RouteId", this.id);
                c.addParameter("Show", Integer.toString(this.show));
                c.addParameter("LineSize", Integer.toString(this.lineSize));
                if (this.color != null) {
                    String[] color1 = {Integer.toString(this.color.getRed()),
                        Integer.toString(this.color.getGreen()), Integer.toString(this.color.getBlue())};
                    c.addParameter("Color", Parameter.cat(color1));
                }
                c.setRemark(this.getGeneratedBy(TransportRouteAnimation.class.getSimpleName()));
                this.cmdGen.checkAndLog(c);
                cmdGen.write(c);
            } catch (CommandException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                out = false;
            }
        }
        return out;
    }


    /**
     * remove all entities from route
     *
     * @return true when successful
     */
    public boolean clear() {
        boolean out = true;
        TimeInstant simTime = this.model.presentTime();
        boolean init = this.cmdGen.isInitPhase();
        Command c;
        if (this.showInAnimation) {
            try {
                if (init) {
                    c = Command.getCommandInit("setRoute", this.cmdGen.getAnimationTime(simTime));
                } else {
                    c = Command.getCommandTime("setRoute", this.cmdGen.getAnimationTime(simTime));
                }
                c.addParameter("RouteId", this.id);
                c.addParameter("RemoveAll", "");
                c.setRemark(this.getGeneratedBy(TransportRouteAnimation.class.getSimpleName()));
                this.cmdGen.checkAndLog(c);
                cmdGen.write(c);
            } catch (CommandException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                out = false;
            }
        }
        return out;
    }

    /**
     * remove entity from route
     *
     * @param entity entity to remove
     * @return true when successful
     */
    public boolean remove(E entity) {
        boolean out = true;
        TimeInstant simTime = this.model.presentTime();
        boolean init = this.cmdGen.isInitPhase();
        Command c;
        if (this.showInAnimation) {
            try {
                if (init) {
                    c = Command.getCommandInit("setRoute", this.cmdGen.getAnimationTime(simTime));
                } else {
                    c = Command.getCommandTime("setRoute", this.cmdGen.getAnimationTime(simTime));
                }
                c.addParameter("RouteId", this.id);
                c.addParameter("RemoveEntity", entity.getName());
                c.setRemark(this.getGeneratedBy(TransportRouteAnimation.class.getSimpleName()));
                cmdGen.checkAndLog(c);
                cmdGen.write(c);
            } catch (CommandException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                out = false;
            }
        }
        return out;
    }

    /**
     * Insert an entity at begin of route. The Entity moves on route with velocity specified in entry attribute
     * velocity.
     *
     * @param entity
     * @return true when successful
     */
    public boolean insert(E entity) {
        boolean out = true;
        TimeInstant simTime = this.model.presentTime();
        boolean init = this.cmdGen.isInitPhase();
        Command c;
        if (this.showInAnimation) {
            try {
                if (init) {
                    c = Command.getCommandInit("setRoute", this.cmdGen.getAnimationTime(simTime));
                } else {
                    c = Command.getCommandTime("setRoute", this.cmdGen.getAnimationTime(simTime));
                }
                c.addParameter("RouteId", this.id);
                c.addParameter("AddEntity", entity.getName());
                c.setRemark(this.getGeneratedBy(TransportRouteAnimation.class.getSimpleName()));
                cmdGen.checkAndLog(c);
                cmdGen.write(c);
            } catch (CommandException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                out = false;
            }
        }
        return out;
    }

    /**
     * Insert an entity at begin of route. The velocity-attribute is set to given value
     *
     * @param entity
     * @param velocity
     * @return true when successful
     */
    public boolean insert(E entity, double velocity) {
        boolean out = true;
        TimeInstant simTime = this.model.presentTime();
        boolean init = this.cmdGen.isInitPhase();
        Command c;
        if (this.showInAnimation) {
            try {
                if (init) {
                    c = Command.getCommandInit("setEntity", this.cmdGen.getAnimationTime(simTime));
                } else {
                    c = Command.getCommandTime("setEntity", this.cmdGen.getAnimationTime(simTime));
                }
                c.addParameter("EntityId", entity.getName());
                c.addParameter("Attribute", "velocity|" + velocity);
                c.setRemark("erzeugt von TransportStationAnimation");
                cmdGen.checkAndLog(c);
                cmdGen.write(c);

                if (init) {
                    c = Command.getCommandInit("setRoute", this.cmdGen.getAnimationTime(simTime));
                } else {
                    c = Command.getCommandTime("setRoute", this.cmdGen.getAnimationTime(simTime));
                }
                c.addParameter("RouteId", this.id);
                c.addParameter("AddEntity", entity.getName());
                c.setRemark(this.getGeneratedBy(TransportRouteAnimation.class.getSimpleName()));
                cmdGen.checkAndLog(c);
                cmdGen.write(c);

            } catch (CommandException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                out = false;
            }
        }
        return out;
    }

    /**
     * Insert an entity at begin of route. The entity passes route in routeTime
     *
     * @param entity
     * @param routeTime
     * @throws SimulationException when routeTime <= 0
     * @return true when successful
     */
    public boolean insert(E entity, TimeSpan routeTime) {
        boolean out = true;
        if (routeTime.getTimeInEpsilon() <= 0.0) {
            throw new SimulationException("TransportRouteAnimation.insertEntity  Sample of routeTime is <= 0");
        }

        // compute velocity in [length/sec]
        double velocity = this.length / routeTime.getTimeRounded(TimeUnit.SECONDS);
        //insert entity with speed
        out = insert(entity, velocity);
        return out;

    }

    private String getGeneratedBy(String name) {
        String out = "generated by " + name + " and called by ";
        if (this.currentSimProcess() != null) {
            out += this.currentSimProcess().getName();
        } else {
            out += this.currentModel().getName();
        }
        return out;
    }

}
