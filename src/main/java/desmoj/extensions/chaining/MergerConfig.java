package desmoj.extensions.chaining;

import java.util.HashMap;
import java.util.Map;

import desmoj.core.simulator.Entity;

/**
 * This class is used by the Merger construct for composing the ratio of entities needed for the merging process. The
 * Mergerconfig is a Hashmap with key=The class of the Entity and Value= an integer of the number of required entities.
 *
 * @param <E> The Entitiy which can be handled by the station
 * @author Christian Mentz
 *     <p>
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 *     with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *     <p>
 *     Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *     the specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 */
@SuppressWarnings("serial")
public class MergerConfig<E extends Entity> extends HashMap<Class<? extends E>, Integer> {

    public MergerConfig() {
        super();
    }

    /**
     * The Mergerconfig can be initialized with a Map instead of putting the Key/Value pairs in the map.
     *
     * @param mergerParameters Hashmap with key=The class of the Entity and Value= an integer of the number of required
     *                         entities.
     */
    public MergerConfig(Map<? extends Class<? extends E>, ? extends Integer> mergerParameters) {
        super(mergerParameters);
    }

}
