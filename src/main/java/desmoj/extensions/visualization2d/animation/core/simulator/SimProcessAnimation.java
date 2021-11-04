package desmoj.extensions.visualization2d.animation.core.simulator;


import java.awt.Point;
import java.util.Hashtable;

import desmoj.core.simulator.SimProcess;
import desmoj.core.simulator.TimeInstant;
import desmoj.extensions.visualization2d.animation.CmdGeneration;
import desmoj.extensions.visualization2d.animation.PositionExt;
import desmoj.extensions.visualization2d.engine.command.Command;
import desmoj.extensions.visualization2d.engine.command.CommandException;
import desmoj.extensions.visualization2d.engine.command.Parameter;

/**
 * Animation of a SimProcess. It extends from SimProcess. A SimProcess is animated by an entity. Free and static
 * entities are supported. Free Entities can be included in Containers, as Queues, Processes, ect. and have no fixed
 * location. Static Entities have a fixed location and can not be included in Containers, as Queues, Processes, ect.
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
public abstract class SimProcessAnimation extends SimProcess implements EntityBasicAnimation {


    private CmdGeneration cmdGen = null;
    private boolean showInAnimation;
    private String state = null;
    private Hashtable<String, String> attribute = null;

    /**
     * constructor with same parameters as in SimProcess
     *
     * @param owner
     * @param name
     * @param showInTrace
     */
    public SimProcessAnimation(ModelAnimation owner, String name,
                               boolean showInTrace) {
        this(owner, name, false, showInTrace);
    }

    /**
     * constructor with same parameters as in SimProcess
     *
     * @param owner
     * @param name
     * @param repeating
     * @param showInTrace
     */
    public SimProcessAnimation(ModelAnimation owner, String name,
                               boolean repeating, boolean showInTrace) {
        super(owner, name, repeating, showInTrace);

        this.cmdGen = owner.getCmdGen();
        this.showInAnimation = false;
    }

    /**
     * createAnimation method of free entities, with initial state "active".
     *
     * @param entityTypeId    entitType of Entity
     * @param showInAnimation is shown in animation
     */
    public void createAnimation(String entityTypeId, boolean showInAnimation) {
        this.createAnimation(entityTypeId, "active", showInAnimation);
    }


    /**
     * createAnimation method of free entities. These are non static entities and can be included in Containers, as
     * Queues, Processes, ect.
     *
     * @param entityTypeId    entitType of Entity
     * @param state           initial state
     * @param showInAnimation is shown in animation
     */
    public void createAnimation(String entityTypeId, String state,
                                boolean showInAnimation) {
        this.createAnimation(entityTypeId, state, null, showInAnimation);
    }

    /**
     * createAnimation method of static entities. These entities have a fixed location, and can not be part of a
     * Container.
     *
     * @param entityTypeId    entitType of Entity
     * @param state           initial state
     * @param pos             extended position (middle point, angle, direction)
     * @param showInAnimation is shown in animation
     */
    public void createAnimation(String entityTypeId, String state,
                                PositionExt pos, boolean showInAnimation) {

        this.showInAnimation = showInAnimation;
        TimeInstant simTime = this.getModel().presentTime();
        boolean init = this.cmdGen.isInitPhase();
        this.state = state != null ? state : "";
        this.attribute = new Hashtable<String, String>();
        Command c;

        if (this.showInAnimation) {
            try {
                if (init) {
                    c = Command.getCommandInit("createEntity", this.cmdGen.getAnimationTime(simTime));
                } else {
                    c = Command.getCommandTime("createEntity", this.cmdGen.getAnimationTime(simTime));
                }
                c.addParameter("EntityId", this.getName());
                c.addParameter("EntityTypeId", entityTypeId);
                if (state != null) {
                    c.addParameter("State", state);
                }
                if (pos != null) {
                    Point point = pos.getPoint();
                    String[] position = {pos.getView(), Integer.toString(point.x), Integer.toString(point.y),
                        Double.toString(pos.getAngle()), Boolean.toString(pos.getDirection())};
                    c.addParameter("Position", Parameter.cat(position));
                }
                c.setRemark(this.getGeneratedBy(SimProcessAnimation.class.getSimpleName()));
                cmdGen.checkAndLog(c);
                cmdGen.write(c);
            } catch (CommandException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            this.setAttribute("name", this.getName());
        }
    }

    /**
     * Dispose the animation of this SimProcess
     */
    public void disposeAnimation() {

        TimeInstant simTime = this.getModel().presentTime();
        boolean init = this.cmdGen.isInitPhase();
        Command c;

        if (this.showInAnimation) {
            try {
                if (init) {
                    c = Command.getCommandInit("disposeEntity", this.cmdGen.getAnimationTime(simTime));
                } else {
                    c = Command.getCommandTime("disposeEntity", this.cmdGen.getAnimationTime(simTime));
                }
                c.addParameter("EntityId", this.getName());
                c.setRemark(this.getGeneratedBy(EntityAnimation.class.getSimpleName()));
                cmdGen.checkAndLog(c);
                cmdGen.write(c);
            } catch (CommandException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }


    /**
     * Set Attributes
     *
     * @param key   Attribute-key, possible keys are def in EntityType
     * @param value Attribute-value
     */
    public void setAttribute(String key, String value) {
        TimeInstant simTime = this.getModel().presentTime();
        boolean init = this.cmdGen.isInitPhase();
        this.attribute.put(key, value);
        Command c;
        if (this.showInAnimation) {
            try {
                if (init) {
                    c = Command.getCommandInit("setEntity", this.cmdGen.getAnimationTime(simTime));
                } else {
                    c = Command.getCommandTime("setEntity", this.cmdGen.getAnimationTime(simTime));
                }
                c.addParameter("EntityId", this.getName());
                c.addParameter("Attribute", key + "|" + value);
                c.setRemark(this.getGeneratedBy(SimProcessAnimation.class.getSimpleName()));
                cmdGen.checkAndLog(c);
                cmdGen.write(c);
            } catch (CommandException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * Set an attribute key as velocity attribute. when a entity moves on a route, velocity attribute describes the
     * speed. Default attribute is velocity.
     *
     * @param attributeKey
     */
    public void setVelocity(String attributeKey) {
        TimeInstant simTime = this.getModel().presentTime();
        boolean init = this.cmdGen.isInitPhase();
        Command c;
        if (this.showInAnimation) {
            try {
                if (init) {
                    c = Command.getCommandInit("setEntity", this.cmdGen.getAnimationTime(simTime));
                } else {
                    c = Command.getCommandTime("setEntity", this.cmdGen.getAnimationTime(simTime));
                }
                c.addParameter("EntityId", this.getName());
                c.addParameter("Velocity", attributeKey);
                c.setRemark(this.getGeneratedBy(SimProcessAnimation.class.getSimpleName()));
                cmdGen.checkAndLog(c);
                cmdGen.write(c);
            } catch (CommandException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    public String getState() {
        return this.state;
    }

    /**
     * Set State
     *
     * @param state , possible states are def in EntityType
     */
    public void setState(String state) {
        TimeInstant simTime = this.getModel().presentTime();
        boolean init = this.cmdGen.isInitPhase();
        this.state = state;
        Command c;
        if (this.showInAnimation) {
            try {
                if (init) {
                    c = Command.getCommandInit("setEntity", this.cmdGen.getAnimationTime(simTime));
                } else {
                    c = Command.getCommandTime("setEntity", this.cmdGen.getAnimationTime(simTime));
                }
                c.addParameter("EntityId", this.getName());
                c.addParameter("State", state);
                c.setRemark(this.getGeneratedBy(SimProcessAnimation.class.getSimpleName()));
                cmdGen.checkAndLog(c);
                cmdGen.write(c);
            } catch (CommandException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public String getAttribute(String key) {
        return this.attribute.get(key);
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


