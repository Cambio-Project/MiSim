package desmoj.extensions.applicationDomains.harbour.report;

import desmoj.core.report.Reporter;
import desmoj.core.statistic.StatisticObject;

/**
 * Captures all relevant information about the Block.
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
 */
public class BlockReporter extends Reporter {

    /**
     * Constructor for a new BlockReporter. Note that although any Reportable is accepted you should make sure that only
     * subtypes of Block are passed to this constructor. Otherwise the number of column titles and their individual
     * headings will differ from the actual content collected by this reporter.
     *
     * @param informationSource desmoj.Reportable : The Block to report about
     */
    public BlockReporter(desmoj.core.simulator.Reportable informationSource) {

        super(informationSource); // make a Reporter

        groupHeading = "Blocks";
        groupID = 1999; // see Reporter for more information about groupID

        numColumns = 12;
        columns = new String[numColumns];
        columns[0] = "Title";
        columns[1] = "(Re)set";
        columns[2] = "Typ";
        columns[3] = "Prod";
        columns[4] = "Cons";
        columns[5] = "Init";
        columns[6] = "Limit";
        columns[7] = "Max";
        columns[8] = "Min";
        columns[9] = "Now";
        columns[10] = "Average";
        columns[11] = "Occup.rate [%]";

        entries = new String[numColumns];
    }

    /**
     * Returns an array of Strings each containing the data for the corresponding column in array
     * <code>columns[]</code>. Implement this method in a way, that an array of the same length as the columntitles is
     * produced containing the data at the point of time this method is called by someone else to produce up-to-date
     * information.
     *
     * @return java.lang.String[] : Array containing the data for reporting
     */
    public String[] getEntries() {

        if (source instanceof desmoj.extensions.applicationDomains.harbour.Block) {
            // the Block we report about (source = informationsource)
            desmoj.extensions.applicationDomains.harbour.Block b =
                (desmoj.extensions.applicationDomains.harbour.Block) source;

            // *** entries of block
            // Title
            entries[0] = b.getName();
            // (Re)set
            entries[1] = b.resetAt().toString();
            // Typ
            switch (b.getTyp()) {
                case 0:
                    entries[2] = "E";
                    break;
                case 1:
                    entries[2] = "I";
                    break;
                case 2:
                    entries[2] = "M";
                    break;
                default:
                    entries[2] = "Unknown";
            }
            // Prod
            entries[3] = Long.toString(b.getProducers());
            // Cons
            entries[4] = Long.toString(b.getConsumers());
            // Init
            entries[5] = Long.toString(b.getInitial());
            // Limit
            entries[6] = Long.toString(b.getCapacity());
            if (b.getCapacity() == Integer.MAX_VALUE) {
                entries[5] = "unlim.";
            }
            // Max
            entries[7] = Long.toString(b.getMaximum());
            // Min
            entries[8] = Long.toString(b.getMinimum());
            // Now
            entries[9] = Long.toString(b.getCurrentCapacity());
            // Average
            entries[10] = Double.toString(b.avgCapacity());
            // Occup.rate
            entries[11] = Double.toString(StatisticObject.round(b.OccupRate() * 100));


        } else {
            for (int i = 0; i < numColumns; i++) {
                entries[i] = "Invalid source!";
            } // end for
        } // end else

        return entries;
    }
}