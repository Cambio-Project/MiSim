package desmoj.core.report;

/**
 * Reports all information about a DiscreteDistUniform distribution.
 *
 * @author Tim Lechler
 *     <p>
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 *     with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *     <p>
 *     Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *     the specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 */
public class DiscreteDistUniformReporter extends DistributionReporter {
    /**
     * Creates a new DiscreteDistUniformReporter.
     *
     * @param informationSource desmoj.core.simulator.Reportable : The DiscreteDistUniform distribution to report about
     */
    public DiscreteDistUniformReporter(
        desmoj.core.simulator.Reportable informationSource) {

        super(informationSource);

        groupID = 123;

    }

    /**
     * Returns the array of strings containing all information about the DiscreteDistUniform distribution.
     *
     * @return java.lang.String[] : The array of Strings containing all information about the DiscreteDistUniform
     *     distribution
     */
    public String[] getEntries() {

        if (source instanceof desmoj.core.dist.DiscreteDistUniform) {
            // use casted ide as a shortcut for source
            desmoj.core.dist.DiscreteDistUniform idu = (desmoj.core.dist.DiscreteDistUniform) source;
            // Title
            entries[0] = idu.getName();
            // (Re)set
            entries[1] = idu.resetAt().toString();
            // Obs
            entries[2] = Long.toString(idu.getObservations());
            // Type
            entries[3] = "Discrete Uniform";
            // param1
            entries[4] = Double.toString(idu.getLower());
            // param2
            entries[5] = Double.toString(idu.getUpper());
            // param3
            entries[6] = " ";
            // seed
            entries[7] = Long.toString(idu.getInitialSeed());
        } else {
            for (int i = 0; i < numColumns; i++) {
                entries[i] = "Invalid source!";
            }
        }

        return entries;

    }
}