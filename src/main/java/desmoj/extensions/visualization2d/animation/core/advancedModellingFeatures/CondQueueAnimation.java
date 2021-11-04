package desmoj.extensions.visualization2d.animation.core.advancedModellingFeatures;

import java.awt.Dimension;
import java.awt.Point;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.advancedModellingFeatures.CondQueue;
import desmoj.core.simulator.Condition;
import desmoj.core.simulator.TimeInstant;
import desmoj.extensions.visualization2d.animation.CmdGeneration;
import desmoj.extensions.visualization2d.animation.Comment;
import desmoj.extensions.visualization2d.animation.FormExt;
import desmoj.extensions.visualization2d.animation.Position;
import desmoj.extensions.visualization2d.animation.core.simulator.ListInterface;
import desmoj.extensions.visualization2d.animation.core.simulator.ModelAnimation;
import desmoj.extensions.visualization2d.animation.core.simulator.ProcessQueueAnimation;
import desmoj.extensions.visualization2d.animation.core.simulator.SimProcessAnimation;
import desmoj.extensions.visualization2d.engine.command.Command;
import desmoj.extensions.visualization2d.engine.command.CommandException;
import desmoj.extensions.visualization2d.engine.command.Parameter;
import desmoj.extensions.visualization2d.engine.model.List;


/**
 * Animation of a CondQueue
 *
 * @param <P>
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
public class CondQueueAnimation extends CondQueue implements ListInterface {

    private boolean showInAnimation;
    private CmdGeneration cmdGen = null;
    private String priorityAttribute = List.PRIO_LAST;
    private final int sortOrder;
    private final String id;

    /**
     * Constructor with the same parameters as in CondQueue
     *
     * @param owner        used model
     * @param name         as in CondQueue
     * @param sortOrder    of inout queue possible values: ProcessQueueAnimation.FIFO ProcessQueueAnimation.LIFO
     * @param qCapacity    capacity of input queue
     * @param capacity     No of resources available in resource pool
     * @param showInReport as in CondQueue
     * @param showInTrace  as in CondQueue
     */
    public CondQueueAnimation(ModelAnimation owner, String name, int sortOrder,
                              int qCapacity, boolean showInReport, boolean showInTrace) {

        super(owner, name, sortOrder, qCapacity, showInReport, showInTrace);
        this.showInAnimation = false;
        this.cmdGen = owner.getCmdGen();
        this.sortOrder = sortOrder;
        this.id = this.cmdGen.createInternId(name);
    }

    /**
     * Constructor with the same parameters as in CondQueue
     *
     * @param owner        used model
     * @param name         as in CondQueue
     * @param showInReport as in CondQueue
     * @param showInTrace  as in CondQueue
     */
    public CondQueueAnimation(ModelAnimation owner, String name,
                              boolean showInReport, boolean showInTrace) {

        super(owner, name, CondQueue.FIFO, Integer.MAX_VALUE, showInReport, showInTrace);
        this.showInAnimation = false;
        this.cmdGen = owner.getCmdGen();
        this.sortOrder = CondQueue.FIFO;
        this.id = this.cmdGen.createInternId(name);
    }

    /**
     * create animation with full parameterization
     *
     * @param pos             middle point of animation object
     * @param form            form of animation object
     * @param showInAnimation switch animation on or off
     */
    public void createAnimation(Position pos, FormExt form,
                                boolean showInAnimation) {
        this.createAnimation(null, pos, form, showInAnimation);
    }

    /**
     * create animation with full parameterization
     *
     * @param comment
     * @param pos             middle point of animation object
     * @param form            form of animation object
     * @param showInAnimation switch animation on or off
     */
    public void createAnimation(Comment comment, Position pos, FormExt form,
                                boolean showInAnimation) {

        this.showInAnimation = showInAnimation;
        switch (sortOrder) {
            case ProcessQueueAnimation.FIFO:
                //Neue Elemente werden in der Queue hinten angefuegt. (Hohe Rankzahl)
                this.priorityAttribute = List.PRIO_LAST;
                break;
            case ProcessQueueAnimation.LIFO:
                //Neue Elemente werden in der Queue vorne angefuegt. (Kleine Rankzahl)
                this.priorityAttribute = List.PRIO_FIRST;
                break;
            default:
                this.priorityAttribute = List.PRIO_LAST;
        }

        TimeInstant simTime = this.getModel().presentTime();
        boolean init = this.cmdGen.isInitPhase();
        Command c;
        Point p = pos.getPoint();
        Dimension deltaSize = form.getDeltaSize();
        String[] point = {pos.getView(), String.valueOf(p.x), String.valueOf(p.y)};
        if (deltaSize == null) {
            deltaSize = new Dimension(0, 0);
        }
        String[] deltaSizeA = {Integer.toString(deltaSize.width), Integer.toString(deltaSize.height)};

        if (this.showInAnimation) {
            try {
                if (init) {
                    c = Command.getCommandInit("createList", this.cmdGen.getAnimationTime(simTime));
                } else {
                    c = Command.getCommandTime("createList", this.cmdGen.getAnimationTime(simTime));
                }
                c.addParameter("ListId", this.id);
                c.addParameter("Name", this.getName());
                c.addParameter("DefaultEntityType", form.getDefaultType());
                c.addParameter("NumberOfVisible", Integer.toString(form.getNrVisible()));
                c.addParameter("Form", form.isHorizontal() ? "horizontal" : "vertikal");
                c.addParameter("Point", Parameter.cat(point));
                c.addParameter("DeltaSize", Parameter.cat(deltaSizeA));
                if (comment != null) {
                    c.addParameter("Comment", Parameter.cat(comment.getProperties()));
                }
                c.setRemark(this.getGeneratedBy(CondQueueAnimation.class.getSimpleName()));
                cmdGen.checkAndLog(c);
                cmdGen.write(c);

            } catch (CommandException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * e wird in die CondQueue eingefuegt und wieder entfernt wenn cond erfuellt ist.
     *
     * @param cond siehe CondQueue
     * @param e    e wird in seinem Ablauf solange unterbrochen bis er wieder aus der Queue entfernt ist.
     * @return
     */
    public boolean waitUntil(Condition cond, SimProcessAnimation e) throws SuspendExecution {
        TimeInstant simTime = this.getModel().presentTime();
        boolean init = this.cmdGen.isInitPhase();
        Command c;
        // Element in Animation Einfuegen
        String[] add = {e.getName(), Integer.toString(e.getQueueingPriority()), this.priorityAttribute};
        if (this.showInAnimation) {
            try {
                if (init) {
                    c = Command.getCommandInit("setList", this.cmdGen.getAnimationTime(simTime));
                } else {
                    c = Command.getCommandTime("setList", this.cmdGen.getAnimationTime(simTime));
                }
                c.addParameter("ListId", this.id);
                c.addParameter("AddEntity", Parameter.cat(add));
                c.setRemark(this.getGeneratedBy(CondQueueAnimation.class.getSimpleName()));
                cmdGen.checkAndLog(c);
                cmdGen.write(c);
            } catch (CommandException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
        // Ablauf wartet bis Bedingung erfuellt
        boolean out = super.waitUntil(cond);
        // Element aus Animation entfernen
        simTime = this.getModel().presentTime();
        init = this.cmdGen.isInitPhase();

        if (this.showInAnimation) {
            try {
                if (init) {
                    c = Command.getCommandInit("setList", this.cmdGen.getAnimationTime(simTime));
                } else {
                    c = Command.getCommandTime("setList", this.cmdGen.getAnimationTime(simTime));
                }
                c.addParameter("ListId", this.id);
                c.addParameter("RemoveEntity", e.getName());
                c.setRemark(this.getGeneratedBy(CondQueueAnimation.class.getSimpleName()));
                cmdGen.checkAndLog(c);
                cmdGen.write(c);
            } catch (CommandException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }

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

    /**
     * gives the internal Id of this animation object.
     *
     * @return
     */
    public String getInternId() {
        return this.id;
    }

}
