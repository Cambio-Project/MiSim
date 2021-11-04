package desmoj.extensions.applicationDomains.harbour;

/**
 * A ChooseBlockYardStrategy is an interface and presents the strategy that is used by the <code>Yard</code> to find a
 * certain block to store or retrieve a container there. The method getBlock(Block[] blocks) that gives the needed block
 * must be implemented by the user in a class that implements TransportStrategy to define the strategy which will be
 * used by the yard. ChooseBlockYardStrategy is part of the strategy design pattern as described in [Gamm97] page 333 in
 * which it represents the strategy interface.
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
 * @see Yard
 */
public interface ChooseBlockYardStrategy {

    /**
     * Implement this method in a class that implements this interface to define the algorithm of the strategy that
     * finds a certain block and is used by the yard.
     *
     * @param blocks <code>Block</code>[] : The array of the blocks.
     * @return <code>Block</code>[] : the found block.
     */
    Block getBlock(Block[] blocks);
}