package desmoj.extensions.applicationDomains.harbour;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.ModelComponent;

/**
 * The MostFreePlaceBlockYardStrategy presents the strategy that is used by the
 * <code>Yard</code> to find a certain block to store a container by the
 * following way: the block with the most free place will be selected. MostFreePlaceBlockYardStrategy impements
 * <code>ChooseBlockYardStrategy</code>.
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
 * @see ChooseBlockYardStrategy
 * @see ModelComponent
 */
public class MostFreePlaceBlockYardStrategy extends ModelComponent implements
    ChooseBlockYardStrategy {
    /**
     * Constructs the MostFreePlaceBlockYardStrategy for a yard.
     *
     * @param owner desmoj.Model : The model this MostFreePlaceBlockYardStrategy is associated to.
     */
    public MostFreePlaceBlockYardStrategy(Model owner) {

        super(owner, "MostFreePlaceBlockYardStrategy"); // make a ModelComponent

    } // end of constructor

    /**
     * This method describes the MostFreePlaceBlockYardStrategy that is used by the yard and returns the block with the
     * most free place (most place to reserve) to store there.
     *
     * @param blocks <code>Block</code>[] : The array of the blocks.
     * @return <code>Block</code>[] : the found block.
     */
    public Block getBlock(Block[] blocks) {

        long max = 0;

        // the block to find
        Block result = null;

        // find the max. and the block with the max. free place
        // if there's more than a one block with the max. the first found block
        // will be returned.
        for (int i = 0; i < blocks.length; i++) {

            if (blocks[i].avail_to_reserve > max) {

                max = blocks[i].avail_to_reserve;
                result = blocks[i];
            }

        } // end of for

        return result;

    }
}