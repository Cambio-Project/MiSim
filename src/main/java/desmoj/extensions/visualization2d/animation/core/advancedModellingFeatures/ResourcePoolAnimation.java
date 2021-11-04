package desmoj.extensions.visualization2d.animation.core.advancedModellingFeatures;

import java.awt.Dimension;
import java.awt.Point;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.advancedModellingFeatures.Res;
import desmoj.core.simulator.ProcessQueue;
import desmoj.core.simulator.TimeInstant;
import desmoj.extensions.visualization2d.animation.CmdGeneration;
import desmoj.extensions.visualization2d.animation.FormExt;
import desmoj.extensions.visualization2d.animation.Position;
import desmoj.extensions.visualization2d.animation.core.simulator.EntityBasicAnimation;
import desmoj.extensions.visualization2d.animation.core.simulator.ModelAnimation;
import desmoj.extensions.visualization2d.engine.command.Command;
import desmoj.extensions.visualization2d.engine.command.CommandException;
import desmoj.extensions.visualization2d.engine.command.Parameter;
import desmoj.extensions.visualization2d.engine.model.List;


/**
 * Animation of a ResourcePool (Res)
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
public class ResourcePoolAnimation extends Res {

    private boolean showInAnimation;
    private CmdGeneration cmdGen = null;
    private String priorityAttribute = null;
    private final int sortOrder;
    private final int capacity;
    private final String id;

    /**
     * Constructor with the same parameters as in Res
     *
     * @param owner        used model
     * @param name         as in Res
     * @param sortOrder    of inout queue possible values: ProcessQueueAnimation.FIFO ProcessQueueAnimation.LIFO
     * @param qCapacity    capacity of input queue
     * @param capacity     No of resources available in resource pool
     * @param showInReport as in Res
     * @param showInTrace  as in Res
     */
    public ResourcePoolAnimation(ModelAnimation owner, String name,
                                 int sortOrder, int qCapacity, int capacity,
                                 boolean showInReport, boolean showInTrace) {

        super(owner, name, sortOrder, qCapacity, capacity, showInReport, showInTrace);
        this.showInAnimation = false;
        this.cmdGen = owner.getCmdGen();
        this.sortOrder = sortOrder;
        this.capacity = capacity;
        this.id = this.cmdGen.createInternId(name);
    }

    /**
     * Constructor with the same parameters as in Accumulate
     *
     * @param owner        used model
     * @param name         as in Res
     * @param capacity     No of resources available in resource pool
     * @param showInReport as in Res
     * @param showInTrace  as in Res
     */
    public ResourcePoolAnimation(ModelAnimation owner, String name,
                                 int capacity, boolean showInReport, boolean showInTrace) {
        this(owner, name, ProcessQueue.FIFO, Integer.MAX_VALUE, capacity,
            showInReport, showInTrace);
    }


    /**
     * create animation with full parameterization
     *
     * @param pos             middle point of animation object
     * @param form            form of animation object
     * @param resourceType    resourceTypeName (only for information, its no Id)
     * @param showInAnimation switch animation on or off
     */
    public void createAnimation(Position pos, FormExt form, String resourceType,
                                boolean showInAnimation) {

        this.showInAnimation = showInAnimation;
        switch (sortOrder) {
            case ResourcePoolAnimation.FIFO:
                //Neue Elemente werden in der Queue hinten angefuegt. (Hohe Rankzahl)
                this.priorityAttribute = List.PRIO_LAST;
                break;
            case ResourcePoolAnimation.LIFO:
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
        String[] pointA = {pos.getView(), Integer.toString(p.x), Integer.toString(p.y)};
        if (deltaSize == null) {
            deltaSize = new Dimension(0, 0);
        }
        String[] deltaSizeA = {Integer.toString(deltaSize.width), Integer.toString(deltaSize.height)};

        if (this.showInAnimation) {
            try {
                if (init) {
                    c = Command.getCommandInit("createResource", this.cmdGen.getAnimationTime(simTime));
                } else {
                    c = Command.getCommandTime("createResource", this.cmdGen.getAnimationTime(simTime));
                }
                c.addParameter("ResourceId", this.id);
                c.addParameter("Name", this.getName());
                c.addParameter("ResourceType", resourceType);
                c.addParameter("ResourceTotal", Integer.toString(capacity));
                c.addParameter("Point", Parameter.cat(pointA));
                c.addParameter("DefaultEntityType", form.getDefaultType());
                c.addParameter("AnzVisible", Integer.toString(form.getNrVisible()));
                c.addParameter("Form", form.isHorizontal() ? "horizontal" : "vertikal");
                c.addParameter("DeltaSize", Parameter.cat(deltaSizeA));
                c.setRemark(this.getGeneratedBy(ResourcePoolAnimation.class.getSimpleName()));
                cmdGen.checkAndLog(c);
                cmdGen.write(c);

            } catch (CommandException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * Entity proc need needRes resources to process. It want to move into input queue and wait to processing.
     *
     * @param proc
     * @param needRes
     * @return true: its moved in input queue false: its rejected
     */
    public boolean provide(EntityBasicAnimation proc, int needRes) throws SuspendExecution {
        boolean out;
        TimeInstant simTime = this.getModel().presentTime();
        boolean init = this.cmdGen.isInitPhase();
        Command c;
        String[] provide = {proc.getName(), Integer.toString(proc.getQueueingPriority()), Integer.toString(needRes),
            this.priorityAttribute};
        if (this.showInAnimation) {
            try {
                if (init) {
                    c = Command.getCommandInit("setResource", this.cmdGen.getAnimationTime(simTime));
                } else {
                    c = Command.getCommandTime("setResource", this.cmdGen.getAnimationTime(simTime));
                }
                c.addParameter("ResourceId", this.id);
                c.addParameter("Provide", Parameter.cat(provide));
                c.setRemark(this.getGeneratedBy(ResourcePoolAnimation.class.getSimpleName()));
                cmdGen.checkAndLog(c);
                cmdGen.write(c);
            } catch (CommandException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
        out = super.provide(needRes);
        // when out = true:  wait in waiting queue until resources are free
        // when out = false: waiting queue is full
        if (this.showInAnimation) {
            try {
                if (init) {
                    c = Command.getCommandInit("setResource", this.cmdGen.getAnimationTime(simTime));
                } else {
                    c = Command.getCommandTime("setResource", this.cmdGen.getAnimationTime(simTime));
                }
                c.addParameter("ResourceId", this.id);
                c.addParameter("TakeProcess", proc.getName());
                c.setRemark(this.getGeneratedBy(ResourcePoolAnimation.class.getSimpleName()));
                cmdGen.checkAndLog(c);
                cmdGen.write(c);
            } catch (CommandException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            // when out = false: proc must take back from process part.
            if (!out) {
                this.takeBack(needRes);
            }
        }
        return out;
    }


    /**
     * proc is in processing and gives resources back. when all resources are given back, it leaves the resource pool.
     *
     * @param proc
     * @param takeBackRes
     */
    public void takeBack(EntityBasicAnimation proc, int takeBackRes) {
        super.takeBack(takeBackRes);
        TimeInstant simTime = this.getModel().presentTime();
        boolean init = this.cmdGen.isInitPhase();
        Command c;
        String[] takeBack = {proc.getName(), Integer.toString(takeBackRes)};
        if (this.showInAnimation) {
            try {
                if (init) {
                    c = Command.getCommandInit("setResource", this.cmdGen.getAnimationTime(simTime));
                } else {
                    c = Command.getCommandTime("setResource", this.cmdGen.getAnimationTime(simTime));
                }
                c.addParameter("ResourceId", this.id);
                c.addParameter("TakeBackProcess", Parameter.cat(takeBack));
                c.setRemark(this.getGeneratedBy(ResourcePoolAnimation.class.getSimpleName()));
                cmdGen.checkAndLog(c);
                cmdGen.write(c);
            } catch (CommandException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }

    /**
     * The currentSimProcess need needRes resources to process. It want to move into input queue and wait to
     * processing.
     *
     * @param needRes
     * @return true: its moved in input queue false: its rejected
     */
    public boolean provide(int needRes) throws SuspendExecution {
        EntityBasicAnimation proc = (EntityBasicAnimation) this.currentSimProcess();
        return this.provide(proc, needRes);
    }

    /**
     * The currentSimProcess give resources back. When all resources are given back, it leaves the resource pool.
     *
     * @param proc
     * @param takeBackRes
     */
    public void takeBack(int takeBackRes) {
        EntityBasicAnimation proc = (EntityBasicAnimation) this.currentSimProcess();
        this.takeBack(proc, takeBackRes);
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
