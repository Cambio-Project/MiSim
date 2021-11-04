package desmoj.extensions.visualization2d.animation.core.advancedModellingFeatures;

import java.awt.Dimension;
import java.awt.Point;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.advancedModellingFeatures.Bin;
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
 * Animation of an Bin
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
public class BinAnimation extends Bin {

    private boolean showInAnimation;
    private CmdGeneration cmdGen = null;
    private String consumerPriorityAttribute = null;
    private final int consumerSortOrder;
    private final long initialUnits;
    private final String id;

    /**
     * Constructor with the same parameters as in Bin
     *
     * @param owner             used model
     * @param name              as in Bin
     * @param consumerSortOrder as in Bin
     * @param consumerQCapacity as in Bin
     * @param initialUnits      as in Bin
     * @param capacity          as in Bin
     * @param showInReport      as in Bin
     * @param showInTrace       as in Bin
     */
    public BinAnimation(ModelAnimation owner, String name,
                        int consumerSortOrder, int consumerQCapacity, long initialUnits,
                        boolean showInReport, boolean showInTrace) {

        super(owner, name, consumerSortOrder, consumerQCapacity, initialUnits,
            showInReport, showInTrace);
        this.showInAnimation = false;
        this.cmdGen = owner.getCmdGen();
        this.consumerSortOrder = consumerSortOrder;
        this.initialUnits = initialUnits;
        this.id = this.cmdGen.createInternId(name);
    }

    /**
     * Constructor with the same parameters as in Bin
     *
     * @param owner        used model
     * @param name         as in Bin
     * @param initialUnits as in Bin
     * @param showInReport as in Bin
     * @param showInTrace  as in Bin
     */
    public BinAnimation(ModelAnimation owner, String name, long initialUnits,
                        boolean showInReport, boolean showInTrace) {
        this(owner, name, ProcessQueue.FIFO, Integer.MAX_VALUE, initialUnits,
            showInReport, showInTrace);
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

        this.showInAnimation = showInAnimation;
        switch (consumerSortOrder) {
            case BinAnimation.FIFO:
                //Neue Elemente werden in der Queue hinten angefuegt. (Hohe Rankzahl)
                this.consumerPriorityAttribute = List.PRIO_LAST;
                break;
            case BinAnimation.LIFO:
                //Neue Elemente werden in der Queue vorne angefuegt. (Kleine Rankzahl)
                this.consumerPriorityAttribute = List.PRIO_FIRST;
                break;
            default:
                this.consumerPriorityAttribute = List.PRIO_LAST;
        }
        TimeInstant simTime = this.getModel().presentTime();
        boolean init = this.cmdGen.isInitPhase();
        Command c;
        Point p = pos.getPoint();
        Dimension deltaSize = form.getDeltaSize();
        String[] pointA = {pos.getView(), Integer.toString(p.x), Integer.toString(p.y)};
        String[] deltaSizeA = {Integer.toString(deltaSize.width), Integer.toString(deltaSize.height)};

        if (this.showInAnimation) {
            try {
                if (init) {
                    c = Command.getCommandInit("createBin", this.cmdGen.getAnimationTime(simTime));
                } else {
                    c = Command.getCommandTime("createBin", this.cmdGen.getAnimationTime(simTime));
                }
                c.addParameter("BinId", this.id);
                c.addParameter("Name", this.getName());
                c.addParameter("InitialUnits", Long.toString(initialUnits));
                c.addParameter("Point", Parameter.cat(pointA));
                c.addParameter("DefaultEntityType", form.getDefaultType());
                c.addParameter("AnzVisible", Integer.toString(form.getNrVisible()));
                c.addParameter("Form", form.isHorizontal() ? "horizontal" : "vertikal");
                c.addParameter("DeltaSize", Parameter.cat(deltaSizeA));
                c.setRemark(this.getGeneratedBy(BinAnimation.class.getSimpleName()));
                cmdGen.checkAndLog(c);
                cmdGen.write(c);

            } catch (CommandException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * A Consumer wants to retrieve noOfProducts When the stock is full, he must wait in a queue of consumer
     *
     * @param noOfProducts
     * @return true:  Consumer got the products false: Consumer is rejected, because the waiting queue of
     *     consumer is full
     */
    public boolean retrieve(long noOfProducts) throws SuspendExecution {
        SimProcess consumer = this.currentSimProcess();
        boolean out;
        TimeInstant simTime = this.getModel().presentTime();
        boolean init = this.cmdGen.isInitPhase();
        Command c;
        String[] retrieve =
            {consumer.getName(), Integer.toString(consumer.getQueueingPriority()), Long.toString(noOfProducts),
                this.consumerPriorityAttribute};
        if (this.showInAnimation) {
            try {
                if (init) {
                    c = Command.getCommandInit("setBin", this.cmdGen.getAnimationTime(simTime));
                } else {
                    c = Command.getCommandTime("setBin", this.cmdGen.getAnimationTime(simTime));
                }
                c.addParameter("BinId", this.id);
                c.addParameter("RetrieveBegin", Parameter.cat(retrieve));
                c.setRemark(this.getGeneratedBy(BinAnimation.class.getSimpleName()));
                cmdGen.checkAndLog(c);
                cmdGen.write(c);
            } catch (CommandException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
        out = super.retrieve(noOfProducts);
        // when out = true:  wait in waiting queue until resources are free
        // when out = false: waiting queue is full
        if (this.showInAnimation) {
            try {
                if (init) {
                    c = Command.getCommandInit("setBin", this.cmdGen.getAnimationTime(simTime));
                } else {
                    c = Command.getCommandTime("setBin", this.cmdGen.getAnimationTime(simTime));
                }
                c.addParameter("BinId", this.id);
                c.addParameter("RetrieveEnd", consumer.getName());
                c.setRemark(this.getGeneratedBy(BinAnimation.class.getSimpleName()));
                cmdGen.checkAndLog(c);
                cmdGen.write(c);
            } catch (CommandException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
        return out;
    }

    /**
     * A Producer wants to store noOfProducts. When the stock is full, he must wait in a queue of producer
     *
     * @param noOfProducts
     */
    public void store(long noOfProducts) {
        SimProcess producer = this.currentSimProcess();
        super.store(noOfProducts);

        TimeInstant simTime = this.getModel().presentTime();
        boolean init = this.cmdGen.isInitPhase();
        Command c;
        String[] store = {producer.getName(), Long.toString(noOfProducts)};
        if (this.showInAnimation) {
            try {
                if (init) {
                    c = Command.getCommandInit("setBin", this.cmdGen.getAnimationTime(simTime));
                } else {
                    c = Command.getCommandTime("setBin", this.cmdGen.getAnimationTime(simTime));
                }
                c.addParameter("BinId", this.id);
                c.addParameter("Store", Parameter.cat(store));
                c.setRemark(this.getGeneratedBy(BinAnimation.class.getSimpleName()));
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
