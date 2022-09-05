package desmoj.extensions.visualization2d.animation.core.simulator;

import java.awt.Dimension;
import java.awt.Point;

import desmoj.core.simulator.Condition;
import desmoj.core.simulator.Entity;
import desmoj.core.simulator.ProcessQueue;
import desmoj.core.simulator.SimProcess;
import desmoj.core.simulator.TimeInstant;
import desmoj.extensions.visualization2d.animation.CmdGeneration;
import desmoj.extensions.visualization2d.animation.Comment;
import desmoj.extensions.visualization2d.animation.FormExt;
import desmoj.extensions.visualization2d.animation.Position;
import desmoj.extensions.visualization2d.engine.command.Command;
import desmoj.extensions.visualization2d.engine.command.CommandException;
import desmoj.extensions.visualization2d.engine.command.Parameter;
import desmoj.extensions.visualization2d.engine.model.List;


/**
 * Animation of ProcessQueue
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
public class ProcessQueueAnimation<P extends SimProcessAnimation>
    extends ProcessQueue<P> implements ListInterface {

    private boolean showInAnimation;
    private CmdGeneration cmdGen = null;
    private final int sortOrder;
    private final String id;

    /**
     * Constructor with the same parameters as in ProcessQueue
     *
     * @param owner        as in ProcessQueue
     * @param name         as in ProcessQueue
     * @param showInReport as in ProcessQueue
     * @param showInTrace  as in ProcessQueue
     */
    public ProcessQueueAnimation(ModelAnimation owner, String name,
                                 boolean showInReport, boolean showInTrace) {
        this(owner, name, ProcessQueue.FIFO, Integer.MAX_VALUE, showInReport, showInTrace);
    }

    /**
     * Constructor with the same parameters as in ProcessQueue
     *
     * @param owner        as in ProcessQueue
     * @param name         as in ProcessQueue
     * @param sortOrder    possible values: ProcessQueueAnimation.FIFO ProcessQueueAnimation.LIFO
     * @param qCapacity    capacity of queue
     * @param showInReport as in ProcessQueue
     * @param showInTrace  as in ProcessQueue
     */
    public ProcessQueueAnimation(ModelAnimation owner, String name,
                                 int sortOrder, int qCapacity,
                                 boolean showInReport, boolean showInTrace) {

        super(owner, name, showInReport, showInTrace);
        this.showInAnimation = false;
        this.cmdGen = owner.getCmdGen();
        this.sortOrder = sortOrder;
        this.setQueueCapacity(qCapacity);
        this.setQueueStrategy(sortOrder);
        this.id = this.cmdGen.createInternId(this.getName());
    }

    /**
     * create animation with full parameterization
     *
     * @param pos             middle point of animation object
     * @param form            form of animation object
     * @param showInAnimation switch animation on or off
     */
    public void createAnimation(Position pos, FormExt form, boolean showInAnimation) {
        this.createAnimation(null, pos, form, showInAnimation);
    }

    /**
     * create animation with full parameterization
     *
     * @param comment
     * @param pos             middle point of animation object
     * @param form            form of animation object
     * @param commentStyle
     * @param commentSize
     * @param commentColor
     * @param commentSizeExt
     * @param showInAnimation switch animation on or off
     */
    public void createAnimation(Comment comment, Position pos, FormExt form,
                                boolean showInAnimation) {

        this.showInAnimation = showInAnimation;
        TimeInstant simTime = this.getModel().presentTime();
        boolean init = this.cmdGen.isInitPhase();
        Command c;
        Point p = pos.getPoint();
        Dimension deltaSize = form.getDeltaSize();
        String[] pointA = {pos.getView(), Integer.toString(p.x), Integer.toString(p.y)};
        String[] deltaSizeA = {Integer.toString(deltaSize.width), Integer.toString(deltaSize.height)};

        if (this.showInAnimation) {
            if (init) {
                c = Command.getCommandInit("createList", this.cmdGen.getAnimationTime(simTime));
            } else {
                c = Command.getCommandTime("createList", this.cmdGen.getAnimationTime(simTime));
            }
            c.addParameter("ListId", this.id);
            c.addParameter("Name", this.getName());
            c.addParameter("Point", Parameter.cat(pointA));
            c.addParameter("DefaultEntityType", form.getDefaultType());
            c.addParameter("NumberOfVisible", Integer.toString(form.getNrVisible()));
            c.addParameter("Form", form.isHorizontal() ? "horizontal" : "vertikal");
            c.addParameter("DeltaSize", Parameter.cat(deltaSizeA));
            if (comment != null) {
                c.addParameter("Comment", Parameter.cat(comment.getProperties()));
            }
            c.setRemark(this.getGeneratedBy(ProcessQueueAnimation.class.getSimpleName()));
            cmdGen.checkAndLog(c);
            cmdGen.write(c);
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
     * insert SimProcess e in sortorder
     *
     * @param e
     * @return true, when successful
     */
    public boolean insert(P e) {
        boolean out = super.insert(e);
        if (out) {
            TimeInstant simTime = this.getModel().presentTime();
            boolean init = this.cmdGen.isInitPhase();
            Command c;
            String[] addPara = {e.getName(), Integer.toString(e.getQueueingPriority()), List.PRIO_FIRST};
            switch (this.sortOrder) {
                case ProcessQueue.FIFO:
                    addPara[2] = List.PRIO_LAST;
                    break;
                case ProcessQueue.LIFO:
                    addPara[2] = List.PRIO_FIRST;
                    break;
            }
            if (this.showInAnimation) {
                try {
                    if (init) {
                        c = Command.getCommandInit("setList", this.cmdGen.getAnimationTime(simTime));
                    } else {
                        c = Command.getCommandTime("setList", this.cmdGen.getAnimationTime(simTime));
                    }
                    c.addParameter("ListId", this.id);
                    c.addParameter("AddEntity", Parameter.cat(addPara));
                    c.setRemark(this.getGeneratedBy(QueueAnimation.class.getSimpleName()));
                    cmdGen.checkAndLog(c);
                    cmdGen.write(c);
                } catch (CommandException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        }
        return out;
    }

    /**
     * insert SimProcess e after SimProcess after
     *
     * @param e
     * @param after
     * @return true, when successful
     */
    public boolean insertAfter(P e, P after) {
        boolean out = super.insertAfter(e, after);
        System.out.println("insertAfter");
        if (out) {
            TimeInstant simTime = this.getModel().presentTime();
            boolean init = this.cmdGen.isInitPhase();
            Command c;
            String[] addPara = {e.getName(), Integer.toString(e.getQueueingPriority()), after.getName()};
            if (this.showInAnimation) {
                try {
                    if (init) {
                        c = Command.getCommandInit("setList", this.cmdGen.getAnimationTime(simTime));
                    } else {
                        c = Command.getCommandTime("setList", this.cmdGen.getAnimationTime(simTime));
                    }
                    c.addParameter("ListId", this.id);
                    c.addParameter("AddEntityAfter", Parameter.cat(addPara));
                    c.setRemark(this.getGeneratedBy(QueueAnimation.class.getSimpleName()));
                    cmdGen.checkAndLog(c);
                    cmdGen.write(c);
                } catch (CommandException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        }
        return out;
    }

    /**
     * insert SimProcess e before SimProcess before
     *
     * @param e
     * @param before
     * @return true, when successful
     */
    public boolean insertBefore(P e, P before) {
        boolean out = super.insertBefore(e, before);
        if (out) {
            TimeInstant simTime = this.getModel().presentTime();
            boolean init = this.cmdGen.isInitPhase();
            Command c;
            String[] addPara = {e.getName(), Integer.toString(e.getQueueingPriority()), before.getName()};
            if (this.showInAnimation) {
                try {
                    if (init) {
                        c = Command.getCommandInit("setList", this.cmdGen.getAnimationTime(simTime));
                    } else {
                        c = Command.getCommandTime("setList", this.cmdGen.getAnimationTime(simTime));
                    }
                    c.addParameter("ListId", this.id);
                    c.addParameter("AddEntityBefore", Parameter.cat(addPara));
                    c.setRemark(this.getGeneratedBy(QueueAnimation.class.getSimpleName()));
                    cmdGen.checkAndLog(c);
                    cmdGen.write(c);
                } catch (CommandException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        }
        return out;
    }

    /** {@inheritDoc} */
    @Override
    public void remove(SimProcess e) {
        super.remove(e);
        removeAnimation(e);
    }

    /** {@inheritDoc} */
    @Override
    public boolean remove(int index) {
        P e = get(index);
        boolean confirm = super.remove(index);
        if (confirm) {
            removeAnimation(e);
        }
        return confirm;
    }

    /** {@inheritDoc} */
    @Override
    public P removeFirst() {
        P e = super.removeFirst();
        if (e != null) {
            removeAnimation(e);
        }
        return e;
    }

    /** {@inheritDoc} */
    @Override
    public P removeFirst(Condition<P> c) {
        P e = super.removeFirst(c);
        if (e != null) {
            removeAnimation(e);
        }
        return e;
    }

    /** {@inheritDoc} */
    @Override
    public P removeLast() {
        P e = super.removeLast();
        if (e != null) {
            removeAnimation(e);
        }
        return e;
    }

    /** {@inheritDoc} */
    @Override
    public P removeLast(Condition<P> c) {
        P e = super.removeLast(c);
        if (e != null) {
            removeAnimation(e);
        }
        return e;
    }

    private void removeAnimation(Entity e) {
        TimeInstant simTime = this.getModel().presentTime();
        boolean init = this.cmdGen.isInitPhase();
        Command c;
        if (this.showInAnimation) {
            try {
                if (init) {
                    c = Command.getCommandInit("setList", this.cmdGen.getAnimationTime(simTime));
                } else {
                    c = Command.getCommandTime("setList", this.cmdGen.getAnimationTime(simTime));
                }
                c.addParameter("ListId", this.id);
                c.addParameter("RemoveEntity", e.getName());
                c.setRemark(this.getGeneratedBy(ProcessQueueAnimation.class.getSimpleName()));
                cmdGen.checkAndLog(c);
                cmdGen.write(c);
            } catch (CommandException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
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
