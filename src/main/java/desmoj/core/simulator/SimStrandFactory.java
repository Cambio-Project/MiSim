package desmoj.core.simulator;

import co.paralleluniverse.strands.Strand;
import co.paralleluniverse.strands.SuspendableRunnable;

/**
 * An abstract factory for "Strands". Strands are quasar's abstraction of threads and fibers.
 *
 * @author Tobias Baum
 *     <p>
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 *     with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *     <p>
 *     Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *     the specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 */
abstract class SimStrandFactory {

    /**
     * Create a {@link Strand} for the given {@link SimProcess} that will execute the given {@link SuspendableRunnable}
     * when started.
     */
    public abstract Strand create(SimProcess process, SuspendableRunnable runnable);

    /**
     * Create a {@link Strand} with the given name that will execute the given {@link SuspendableRunnable} when
     * started.
     */
    public abstract Strand create(String name, SuspendableRunnable runnable);

    /**
     * Allow the subclasses to perform some cleanup when an experiment ends.
     */
    public abstract void cleanUp();

}
