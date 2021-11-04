package desmoj.extensions.visualization2d.animation.core.advancedModellingFeatures;

import java.awt.Dimension;
import java.awt.Point;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.advancedModellingFeatures.WaitQueue;
import desmoj.core.simulator.Condition;
import desmoj.core.simulator.ProcessQueue;
import desmoj.core.simulator.SimProcess;
import desmoj.core.simulator.TimeInstant;
import desmoj.extensions.visualization2d.animation.CmdGeneration;
import desmoj.extensions.visualization2d.animation.FormExt;
import desmoj.extensions.visualization2d.animation.Position;
import desmoj.extensions.visualization2d.animation.core.simulator.ModelAnimation;
import desmoj.extensions.visualization2d.engine.command.Command;
import desmoj.extensions.visualization2d.engine.command.CommandException;
import desmoj.extensions.visualization2d.engine.command.Parameter;
import desmoj.extensions.visualization2d.engine.model.List;

/**
 * Animation of a WaitQueue
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
public class WaitQueueAnimation extends WaitQueue {

    private boolean showInAnimation;
    private CmdGeneration cmdGen = null;
    private String masterPriorityAttribute = null;
    private String slavePriorityAttribute = null;
    private final int masterSortOrder;
    private final int slaveSortOrder;
    private final String id;

    /**
     * Constructor with same parameters as WaitQueue
     *
     * @param owner           used model
     * @param name            as in WaitQueue
     * @param masterSortOrder as in WaitQueue
     * @param masterQCapacity as in WaitQueue
     * @param slaveSortOrder  as in WaitQueue
     * @param slaveQCapacity  as in WaitQueue
     * @param showInReport    as in WaitQueue
     * @param showInTrace     as in WaitQueue
     */
    public WaitQueueAnimation(ModelAnimation owner, String name,
                              int masterSortOrder, int masterQCapacity, int slaveSortOrder,
                              int slaveQCapacity, boolean showInReport, boolean showInTrace) {

        super(owner, name, masterSortOrder, masterQCapacity,
            slaveSortOrder, slaveQCapacity,
            showInReport, showInTrace);
        this.showInAnimation = false;
        this.cmdGen = owner.getCmdGen();
        this.masterSortOrder = masterSortOrder;
        this.slaveSortOrder = slaveSortOrder;
        this.id = this.cmdGen.createInternId(name);
    }

    /**
     * Constructor with same parameters as WaitQueue
     *
     * @param owner        used model
     * @param name         as in WaitQueue
     * @param showInReport as in WaitQueue
     * @param showInTrace  as in WaitQueue
     */
    public WaitQueueAnimation(ModelAnimation owner, String name,
                              boolean showInReport, boolean showInTrace) {
        this(owner, name, ProcessQueue.FIFO, Integer.MAX_VALUE,
            ProcessQueue.FIFO, Integer.MAX_VALUE,
            showInReport, showInTrace);
    }

    /**
     * create animation with full parameterization
     *
     * @param id              internal objectId for animation. Must be unique.
     * @param pos             middle point of animation object
     * @param form            form of animation object
     * @param showInAnimation switch animation on or off
     */
    public void createAnimation(Position pos, FormExt form,
                                boolean showInAnimation) {

        this.showInAnimation = showInAnimation;
        // Achtung super aendert den Namen
        switch (masterSortOrder) {
            case WaitQueueAnimation.FIFO:
                //Neue Elemente werden in der Queue hinten angefuegt. (Hohe Rankzahl)
                this.masterPriorityAttribute = List.PRIO_LAST;
                break;
            case WaitQueueAnimation.LIFO:
                //Neue Elemente werden in der Queue vorne angefuegt. (Kleine Rankzahl)
                this.masterPriorityAttribute = List.PRIO_FIRST;
                break;
            default:
                this.masterPriorityAttribute = List.PRIO_LAST;
        }
        switch (slaveSortOrder) {
            case WaitQueueAnimation.FIFO:
                //Neue Elemente werden in der Queue hinten angefuegt. (Hohe Rankzahl)
                this.slavePriorityAttribute = List.PRIO_LAST;
                break;
            case WaitQueueAnimation.LIFO:
                //Neue Elemente werden in der Queue vorne angefuegt. (Kleine Rankzahl)
                this.slavePriorityAttribute = List.PRIO_FIRST;
                break;
            default:
                this.slavePriorityAttribute = List.PRIO_LAST;
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
                    c = Command.getCommandInit("createWaitQueue", this.cmdGen.getAnimationTime(simTime));
                } else {
                    c = Command.getCommandTime("createWaitQueue", this.cmdGen.getAnimationTime(simTime));
                }
                c.addParameter("WaitQueueId", this.id);
                c.addParameter("Name", this.getName());
                c.addParameter("Point", Parameter.cat(pointA));
                c.addParameter("DefaultEntityType", form.getDefaultType());
                c.addParameter("AnzVisible", Integer.toString(form.getNrVisible()));
                c.addParameter("Form", form.isHorizontal() ? "horizontal" : "vertikal");
                c.addParameter("DeltaSize", Parameter.cat(deltaSizeA));
                c.setRemark(this.getGeneratedBy(WaitQueueAnimation.class.getSimpleName()));
                cmdGen.checkAndLog(c);
                cmdGen.write(c);

            } catch (CommandException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * gives the internal Id of this animation object.
     *
     * @return
     */
    public String getInternId() {
        return this.id;
    }


    /**
     * This method is called from a SimProcessAnimation which wants to cooperate as master
     *
     * @param coop as in waitQueue
     * @return
     */
    public boolean cooperate(ProcessCoopAnimation coop) throws SuspendExecution {
        SimProcess master = this.currentSimProcess();
        boolean out;
        TimeInstant simTime = this.getModel().presentTime();
        boolean init = this.cmdGen.isInitPhase();
        Command c;
        String[] insertMaster =
            {master.getName(), Integer.toString(master.getQueueingPriority()), this.masterPriorityAttribute};
        if (this.showInAnimation) {
            try {
                if (init) {
                    c = Command.getCommandInit("setWaitQueue", this.cmdGen.getAnimationTime(simTime));
                } else {
                    c = Command.getCommandTime("setWaitQueue", this.cmdGen.getAnimationTime(simTime));
                }
                c.addParameter("WaitQueueId", this.id);
                c.addParameter("InsertMaster", Parameter.cat(insertMaster));
                c.setRemark(this.getGeneratedBy(WaitQueueAnimation.class.getSimpleName()));
                cmdGen.checkAndLog(c);
                cmdGen.write(c);
            } catch (CommandException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
        out = super.cooperate(coop);
        return out;
    }

    /**
     * This method is called from a SimProcessAnimation which wants to cooperate as master and is looking for a slave
     * complying to a certain condition described in cond
     *
     * @param coop as in waitQueue
     * @param cond as in waitQueue
     * @return
     */
    public boolean cooperate(ProcessCoopAnimation coop, Condition cond) throws SuspendExecution {
        SimProcess master = this.currentSimProcess();
        boolean out;
        TimeInstant simTime = this.getModel().presentTime();
        boolean init = this.cmdGen.isInitPhase();
        Command c;
        String[] insertMaster =
            {master.getName(), Integer.toString(master.getQueueingPriority()), this.masterPriorityAttribute};
        if (this.showInAnimation) {
            try {
                if (init) {
                    c = Command.getCommandInit("setWaitQueue", this.cmdGen.getAnimationTime(simTime));
                } else {
                    c = Command.getCommandTime("setWaitQueue", this.cmdGen.getAnimationTime(simTime));
                }
                c.addParameter("WaitQueueId", this.id);
                c.addParameter("InsertMaster", Parameter.cat(insertMaster));
                c.setRemark(this.getGeneratedBy(WaitQueueAnimation.class.getSimpleName()));
                cmdGen.checkAndLog(c);
                cmdGen.write(c);
            } catch (CommandException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
        out = super.cooperate(coop, cond);
        return out;
    }

    /**
     * This method is called from a SimProcessAnimation which wants to cooperate as a slave.
     */
    public boolean waitOnCoop() throws SuspendExecution {
        SimProcess slave = this.currentSimProcess();
        boolean out;
        TimeInstant simTime = this.getModel().presentTime();
        boolean init = this.cmdGen.isInitPhase();
        Command c;
        String[] insertSlave =
            {slave.getName(), Integer.toString(slave.getQueueingPriority()), this.slavePriorityAttribute};
        if (this.showInAnimation) {
            try {
                if (init) {
                    c = Command.getCommandInit("setWaitQueue", this.cmdGen.getAnimationTime(simTime));
                } else {
                    c = Command.getCommandTime("setWaitQueue", this.cmdGen.getAnimationTime(simTime));
                }
                c.addParameter("WaitQueueId", this.id);
                c.addParameter("InsertSlave", Parameter.cat(insertSlave));
                c.setRemark(this.getGeneratedBy(WaitQueueAnimation.class.getSimpleName()));
                cmdGen.checkAndLog(c);
                cmdGen.write(c);
            } catch (CommandException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
        out = super.waitOnCoop();
        return out;
    }

    public boolean animationIsOn() {
        return this.showInAnimation;
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
