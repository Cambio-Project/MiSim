package desmoj.extensions.applicationDomains.harbour;

import java.util.Vector;

import desmoj.core.report.Reporter;
import desmoj.core.simulator.Model;

/**
 * Yard is the place where containers (or other kind of goods) can be stored by transpoters or cranes and retrieved by
 * transporters or cranes. The yard consists of blocks. The storing in the Yard is possible using
 * <code>store(Block b)</code> .With the <code>retrieve(Block b)</code> a
 * container can be retrieved from the Yard. Yard is part of the composite design pattern as described in [Gamm97] page
 * 163. Yard is derived from Block, which provides the functionality of the Block.
 *
 * @author Eugenia Neufeld
 *     <p>
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 *     with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *     <p>
 *     Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *     the specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 * @see Block
 */
public class Yard extends Block {

    /**
     * The vector where all the blocks of the yard are stored.
     */
    private final Vector blocks;

    /**
     * The strategy for the choosing of the block to store a container there.
     */
    private ChooseBlockYardStrategy s = null;

    /**
     * The value for the yard overflow: place where the containers can be stored if the yard is full.
     */
    private long availOverflow = 0;

    /**
     * Constructor for a Yard.
     *
     * @param owner        desmoj.Model : The model this Yard is associated to.
     * @param name         java.lang.String : The Yard's name.
     * @param typ          int : The Block's type. It can be only 0 - for export containers, 1- for import containers or
     *                     2 - mixed: for import and export containers.
     * @param capacity     long : The maximum capacity (TEU) of this Block.
     * @param showInReport boolean : Flag, if this Block should produce a report or not.
     * @param showInTrace  boolean : Flag for trace to produce trace messages.
     */
    public Yard(Model owner, String name, ChooseBlockYardStrategy s,
                boolean showInReport, boolean showInTrace) {

        super(owner, name, 2, 0, showInReport, showInTrace);
        reset();
        this.currentCapacity = 0;
        this.capacity = 0;
        this.blocks = new Vector();
        this.s = s;

    }

    /**
     * Returns a Reporter to produce a report about this Yard.
     *
     * @return desmoj.report.Reporter : The Reporter reporting about the statistics of this Yard.
     */
    public Reporter createDefaultReporter() {

        return new desmoj.extensions.applicationDomains.harbour.report.YardReporter(
            this);
    }

    /**
     * Method for producers to make the Yard add a block. This is only allowed as long as the Yard has not been used or
     * the Yard has just been reset.
     *
     * @param b Block : The Block that must be added to this yard.
     */
    public void addBlock(Block b) {

        if ((this.consumers != 0) || (this.producers != 0)) {
            sendWarning(
                "Attempt to add a Block to an Yard already"
                    + " in use. The attempt will be ignored!",
                "Yard: " + this.getName()
                    + " Method: void addBlock (Block b)",
                "A Block can't be added to an Yard which has already be"
                    + " used.",
                "Do not try to add a block to an Yard which might have been"
                    + " used already. Or reset the Yard before adding a block.");

            return; // go to where you came from; igno
        }
        // add the block
        this.blocks.addElement(b);
        // set the capacity and initial
        this.capacity = this.capacity + b.getCapacity();
        this.init = this.init + b.getInitial();
        this.currentCapacity = this.currentCapacity + b.getCurrentCapacity();
        this.max = this.max + b.getMaximum();
        this.min = this.min + b.getMinimum();
        this.avail_to_reserve = this.capacity - this.init;
        this.avail_to_plan = this.avail_to_plan + b.avail_to_plan;
    }

    /**
     * Gets all the blocks of the yard that have the certain typ.
     *
     * @param typ int : The typ of the blocks.
     * @return <code>Block[]</code>: The Blocks of the yard of a certain typ.
     */
    public Block[] getBlocks(int typ) {

        Vector b = new Vector();

        // check all the blocks of this yard
        for (int i = 0; i < blocks.size(); i++) {

            Block block = (Block) blocks.elementAt(i);

            // has a block the needed typ
            if (block.getTyp() == typ) {
                b.addElement(block);
            }
        }

        Block[] result = new Block[b.size()];

        for (int i = 0; i < result.length; i++) {

            result[i] = (Block) b.elementAt(i);
        }

        return result;

    }

    /**
     * Gets all the blocks of a part of the yard that have the certain typ.
     *
     * @param typ int : The typ of the blocks.
     * @param b   Block[] : The array of the certain blocks of the yard.
     * @return <code>Block[]</code>: The Blocks of the yard of a certain typ.
     */
    public Block[] getBlocks(int typ, Block[] b) {

        Vector blocks = new Vector();

        // check all the blocks
        for (int i = 0; i < b.length; i++) {

            // has a block the needed typ
            if (b[i].getTyp() == typ) {
                blocks.addElement(b[i]);
            }
        }

        Block[] result = new Block[blocks.size()];

        for (int i = 0; i < result.length; i++) {

            result[i] = (Block) blocks.elementAt(i);
        }

        return result;

    }

    /**
     * Gets all the blocks of the yard that have some free place to store containers.
     *
     * @param n long : The number of TEUs that are at least must be avalaible (free) in that blocks.
     * @return <code>Block[]</code>: The Blocks of the yard that have some
     *     free place.
     */
    public Block[] getFreeBlocks(long n) {

        Vector b = new Vector();

        // check all the blocks
        for (int i = 0; i < blocks.size(); i++) {

            Block block = (Block) blocks.elementAt(i);

            // is a block free
            if (block.isFree(n)) {
                b.addElement(block);
            }
        }

        Block[] result = new Block[b.size()];

        for (int i = 0; i < result.length; i++) {

            result[i] = (Block) b.elementAt(i);
        }

        return result;
    }

    /**
     * Gets all the blocks from a part of the yard blocks that have some free place to store containers.
     *
     * @param b Block[] : The array of the certain blocks of the yard.
     * @return <code>Block[]</code>: The Blocks of the yard that have some
     *     free place.
     */
    public Block[] getFreeBlocks(Block[] b, long n) {

        Vector blocks = new Vector();

        // check all the blocks
        for (int i = 0; i < b.length; i++) {

            if (b[i].isFree(n)) {
                blocks.addElement(b[i]);
            }
        }

        Block[] result = new Block[blocks.size()];

        for (int i = 0; i < result.length; i++) {

            result[i] = (Block) blocks.elementAt(i);
        }

        return result;
    }

    /**
     * Gets all the blocks of the yard.
     *
     * @return <code>Block[]</code>: The Blocks of the yard.
     */
    public Block[] getAllBlocks() {

        Block[] result = new Block[blocks.size()];

        // get all the blocks
        for (int i = 0; i < result.length; i++) {

            result[i] = (Block) blocks.elementAt(i);

        }

        return result;
    }

    /**
     * Gets the Block of the yard that was found by using the current yard strategy of the yard.
     *
     * @return <code>Block</code>: The Block of the yard found by using yard
     *     strategy.
     */
    public Block getBlock(Block[] blocks) {

        return this.s.getBlock(blocks);
    }

    /**
     * Method for producers to make the Yard store a number of n TEUs (a container) in its overflow part.
     *
     * @param n long : The weight of the container to be stored in the overflow part of this yard. n must be positive.
     */
    public void storeInOverflow(long n) {

        this.availOverflow = this.availOverflow + n;
    }

    /**
     * Method for consumers to retrieve from the overflow part of this Yard a number of n TEUs (a container).
     *
     * @param n long : The weight of the container to be stored in the overflow part of this yard. n must be positive.
     * @return boolean : Is <code>true</code> if the try was successfull,
     *     <code>false</code> otherwise.
     */
    public boolean retrieveFromOverflow(long n) {

        where = "boolean retrieveFromOverflow (long n)";

        if (this.availOverflow == 0) {
            sendWarning(
                "Attempt to retrieve an unit "
                    + " from a Yard which overflow part si empty. The attempted action is ignored!",
                "Yard: " + getName() + " Method: " + where,
                "It does not make sense to retrieve that way from a Yard.",
                "Make sure to strore something in the overflow to retrieve an unit.");
            return false;
        }

        this.availOverflow = this.availOverflow - n;

        return true;
    }

    /**
     * Method for producers to make the Yard store a number of n TEUs (a container). When the capacity of the yard can
     * not hold the additional incoming TEUs the try to store will be refused.
     *
     * @param b Block : The block of the yard to store.
     * @param n long : The weight of the container to be stored in this Yard. n must be positive.
     * @return boolean : Is <code>true</code> if the container can been stored successfully, <code>false</code>
     *     otherwise.
     */
    public boolean store(Block b, long n) {

        // update the statistics of the yard
        super.store(n);
        // store in the block
        return b.store(n);
    }

    /**
     * Method for consumers to make the Yard retrieve a number of n TEUs (a container). When the yard is empty the try
     * to retrieve will be refused.
     *
     * @param b Block : The block of the yard to retrieve.
     * @param n long : The weight of the container to be stored in this Yard. n must be positive.
     * @return boolean : Is <code>true</code> if the container can been stored successfully, <code>false</code>
     *     otherwise.
     */
    public boolean retrieve(Block b, long n) {

        // update the statistics of the yard
        super.retrieve(n);
        // retrieve from the block
        return b.retrieve(n);

    }

    /**
     * Gets the current number of TEUs/containers in the overflow part of this yard.
     *
     * @return long : The current capacity of te overflow part of the yard.
     */
    public long getOverflow() {

        return this.availOverflow;
    }

    /**
     * Method to check if the Yard is free (there's at least one block that is free).
     *
     * @param n long : The number of TEUs that are at least must be avalaible (free) in the Yard.
     * @return boolean : Is <code>true</code> if the Yard is free,
     *     <code>false</code> otherwise.
     */
    public boolean isFree(long n) {

        boolean result = false;

        // check all the blocks
        for (int i = 0; i < blocks.size(); i++) {

            Block block = (Block) blocks.elementAt(i);
            if (block.isFree(n)) // there's one free block
            {
                result = true;
                break;
            }
        }

        return result;
    }

    /**
     * Method for producers to make the Yard reserve a number of n TEUs (a place for a container) to be stored there
     * later. When the whole available capacity is already reserved the try to reserve will be refused.
     *
     * @param b Block : The block of the yard to reserve.
     * @param n long : The number of TEUs that are needed for a container to be stored in the Yard. n must be positive.
     * @return boolean : Is <code>true</code> if a place for a container can been reserved successfully,
     *     <code>false</code> otherwise.
     */
    public boolean reserve(Block b, long n) {

        // update the statistics of the yard
        super.reserve(n);
        // reserve in the block
        return b.reserve(n);
    }

    /**
     * Method for consumers to make the Yard plan a number of n TEUs (a place for a container) to be retrieved there
     * later. When all the containers are already planed for the later retrieving the try to plan will be refused.
     *
     * @param b Block : The block of the yard to plan.
     * @param n long : The number of TEUs that a container that is in the Yard weights. n must be positive.
     * @return boolean : Is <code>true</code> if a container can been planed successfully, <code>false</code> otherwise.
     */
    public boolean plan(Block b, long n) {

        // update the statistics of the yard
        super.plan(n);
        // plan in the block
        return b.plan(n);
    }

    /**
     * Returns the number of all the blocks of this yard.
     *
     * @return int : The number of the yard blocks.
     */
    public int getNumBlocks() {

        return this.blocks.size();
    }

    /**
     * Returns the strategy of this yard for the choosing of a block.
     *
     * @return <code>ChooseBlockYardStrategy</code>: The yard strategy.
     */
    public ChooseBlockYardStrategy getYardStrategy() {

        return this.s;
    }

    /**
     * Sets strategy of this yard for the choosing of a block to a new value.
     *
     * @param s ChooseBlockYardStrategy : The new yard strategy.
     */
    public void setYardStrategy(ChooseBlockYardStrategy s) {

        this.s = s;
    }

    /**
     * To reset the statistics of this Yard. The statistics of all yard blocks will be reset too.
     */
    public void reset() {
        super.reset(); // reset the Yard as Block also

        // reset all the blocks

        if (blocks != null) {
            for (int i = 0; i < blocks.size(); i++) {

                Block block = (Block) blocks.elementAt(i);
                block.reset();
            }
        }
    }

    /**
     * Gets all the blocks of the yard that are assigned a certain holding area to.
     *
     * @param ho <code>HoldingArea</code>: The holding area that the blocks
     *           are assigned to.
     * @return <code>Block[]</code>: The Blocks of the yard of a certain
     *     holding area.
     */
    public Block[] getHOBlocks(HoldingArea ho) {

        Vector b = new Vector();

        // check all the blocks
        for (int i = 0; i < blocks.size(); i++) {

            Block block = (Block) blocks.elementAt(i);

            if (block.getHO().equals(ho)) {
                b.addElement(block); // is a block with this HO
            }
        }

        Block[] result = new Block[b.size()];

        for (int i = 0; i < result.length; i++) {

            result[i] = (Block) b.elementAt(i);
        }

        return result;

    }

    /**
     * Gets all the blocks of the yard with a certain container typ.
     *
     * @param ctyp int : The typ of the contaners of this block.
     * @return <code>Block[]</code>: The Blocks of the yard of a certain
     *     container typ.
     */
    public Block[] getCBlocks(int ctyp) {

        Vector b = new Vector();

        // check all the blocks
        for (int i = 0; i < blocks.size(); i++) {

            Block block = (Block) blocks.elementAt(i);

            // has a block this container typ
            if (block.getCTyp() == ctyp) {
                b.addElement(block);
            }
        }

        Block[] result = new Block[b.size()];

        for (int i = 0; i < result.length; i++) {

            result[i] = (Block) b.elementAt(i);
        }

        return result;

    }

    /**
     * Gets all the blocks of the yard that have at least some TEUs (not empty).
     *
     * @param n long : The number of TEUs that are at least must be with containers in the blocks of the yard.
     * @return <code>Block[]</code>: The Blocks of the yard that are not
     *     empty.
     */
    public Block[] getFullBlocks(Block[] b, long n) {

        Vector blocks = new Vector();

        // check all the blocks
        for (int i = 0; i < b.length; i++) {

            // can be n TEUs planed in a block
            if (b[i].avail_to_plan >= n) {
                blocks.addElement(b[i]);
            }
        }

        Block[] result = new Block[blocks.size()];

        for (int i = 0; i < result.length; i++) {

            result[i] = (Block) blocks.elementAt(i);
        }

        return result;

    }
}