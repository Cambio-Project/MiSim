package desmoj.extensions.visualization2d.animation.core.advancedModellingFeatures;

import java.awt.Dimension;
import java.awt.Point;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.advancedModellingFeatures.Stock;
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
 * Animation of a Stock
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
public class StockAnimation extends Stock {

    private boolean showInAnimation;
    private CmdGeneration cmdGen = null;
    private String consumerPriorityAttribute = null;
    private String producerPriorityAttribute = null;
    private final int producerSortOrder;
    private final int consumerSortOrder;
    private final long initialUnits;
    private final long capacity;
    private final String id;

    /**
     * Constructor with the same parameters as in Stock
     *
     * @param owner             used model
     * @param name              as in Stock
     * @param producerSortOrder as in Stock
     * @param producerQCapacity as in Stock
     * @param consumerSortOrder as in Stock
     * @param consumerQCapacity as in Stock
     * @param initialUnits      as in Stock
     * @param capacity          as in Stock
     * @param showInReport      as in Stock
     * @param showInTrace       as in Stock
     */
    public StockAnimation(ModelAnimation owner, String name,
                          int producerSortOrder, int producerQCapacity, int consumerSortOrder,
                          int consumerQCapacity, long initialUnits, long capacity,
                          boolean showInReport, boolean showInTrace) {

        super(owner, name, producerSortOrder, producerQCapacity,
            consumerSortOrder, consumerQCapacity, initialUnits, capacity,
            showInReport, showInTrace);
        this.showInAnimation = false;
        this.cmdGen = owner.getCmdGen();
        this.producerSortOrder = producerSortOrder;
        this.consumerSortOrder = consumerSortOrder;
        this.initialUnits = initialUnits;
        this.capacity = capacity;
        this.id = this.cmdGen.createInternId(name);
    }

    /**
     * Constructor with the same parameters as in Stock
     *
     * @param owner        used model
     * @param name         as in Stock
     * @param initialUnits as in Stock
     * @param capacity     as in Stock
     * @param showInReport as in Stock
     * @param showInTrace  as in Stock
     */
    public StockAnimation(ModelAnimation owner, String name,
                          long initialUnits, long capacity,
                          boolean showInReport, boolean showInTrace) {
        this(owner, name, ProcessQueue.FIFO, Integer.MAX_VALUE,
            ProcessQueue.FIFO, Integer.MAX_VALUE, initialUnits, capacity,
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
        switch (producerSortOrder) {
            case StockAnimation.FIFO:
                //Neue Elemente werden in der Queue hinten angefuegt. (Hohe Rankzahl)
                this.producerPriorityAttribute = List.PRIO_LAST;
                break;
            case StockAnimation.LIFO:
                //Neue Elemente werden in der Queue vorne angefuegt. (Kleine Rankzahl)
                this.producerPriorityAttribute = List.PRIO_FIRST;
                break;
            default:
                this.producerPriorityAttribute = List.PRIO_LAST;
        }
        switch (consumerSortOrder) {
            case StockAnimation.FIFO:
                //Neue Elemente werden in der Queue hinten angefuegt. (Hohe Rankzahl)
                this.consumerPriorityAttribute = List.PRIO_LAST;
                break;
            case StockAnimation.LIFO:
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
        if (deltaSize == null) {
            deltaSize = new Dimension(0, 0);
        }
        String[] deltaSizeA = {Integer.toString(deltaSize.width), Integer.toString(deltaSize.height)};

        if (this.showInAnimation) {
            try {
                if (init) {
                    c = Command.getCommandInit("createStock", this.cmdGen.getAnimationTime(simTime));
                } else {
                    c = Command.getCommandTime("createStock", this.cmdGen.getAnimationTime(simTime));
                }
                c.addParameter("StockId", this.id);
                c.addParameter("Name", this.getName());
                c.addParameter("Capacity", Long.toString(capacity));
                c.addParameter("InitialUnits", Long.toString(initialUnits));
                c.addParameter("Point", Parameter.cat(pointA));
                c.addParameter("DefaultEntityType", form.getDefaultType());
                c.addParameter("AnzVisible", Integer.toString(form.getNrVisible()));
                c.addParameter("Form", form.isHorizontal() ? "horizontal" : "vertikal");
                c.addParameter("DeltaSize", Parameter.cat(deltaSizeA));
                c.setRemark(this.getGeneratedBy(StockAnimation.class.getSimpleName()));
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
                    c = Command.getCommandInit("setStock", this.cmdGen.getAnimationTime(simTime));
                } else {
                    c = Command.getCommandTime("setStock", this.cmdGen.getAnimationTime(simTime));
                }
                c.addParameter("StockId", this.id);
                c.addParameter("RetrieveBegin", Parameter.cat(retrieve));
                c.setRemark(this.getGeneratedBy(StockAnimation.class.getSimpleName()));
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
                    c = Command.getCommandInit("setStock", this.cmdGen.getAnimationTime(simTime));
                } else {
                    c = Command.getCommandTime("setStock", this.cmdGen.getAnimationTime(simTime));
                }
                c.addParameter("StockId", this.id);
                c.addParameter("RetrieveEnd", consumer.getName());
                c.setRemark(this.getGeneratedBy(StockAnimation.class.getSimpleName()));
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
     * @return true:  Producer can store the products false: Producer is rejected, because the waiting queue of
     *     producer is full
     */
    public boolean store(long noOfProducts) throws SuspendExecution {
        SimProcess producer = this.currentSimProcess();
        boolean out;
        TimeInstant simTime = this.getModel().presentTime();
        boolean init = this.cmdGen.isInitPhase();
        Command c;
        String[] store =
            {producer.getName(), Integer.toString(producer.getQueueingPriority()), Long.toString(noOfProducts),
                this.producerPriorityAttribute};
        if (this.showInAnimation) {
            try {
                if (init) {
                    c = Command.getCommandInit("setStock", this.cmdGen.getAnimationTime(simTime));
                } else {
                    c = Command.getCommandTime("setStock", this.cmdGen.getAnimationTime(simTime));
                }
                c.addParameter("StockId", this.id);
                c.addParameter("StoreBegin", Parameter.cat(store));
                c.setRemark(this.getGeneratedBy(StockAnimation.class.getSimpleName()));
                cmdGen.checkAndLog(c);
                cmdGen.write(c);
            } catch (CommandException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
        out = super.store(noOfProducts);
        // when out = true:  wait in waiting queue until resources are free
        // when out = false: waiting queue is full
        if (this.showInAnimation) {
            try {
                if (init) {
                    c = Command.getCommandInit("setStock", this.cmdGen.getAnimationTime(simTime));
                } else {
                    c = Command.getCommandTime("setStock", this.cmdGen.getAnimationTime(simTime));
                }
                c.addParameter("StockId", this.id);
                c.addParameter("StoreEnd", producer.getName());
                c.setRemark(this.getGeneratedBy(StockAnimation.class.getSimpleName()));
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

}
