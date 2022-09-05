package desmoj.extensions.applicationDomains.harbour.report;

import desmoj.core.report.Reporter;
import desmoj.core.statistic.StatisticObject;

/**
 * Captures all relevant information about the Yard.
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
public class YardReporter extends Reporter {

    /**
     * Constructor for a new YardReporter. Note that although any Reportable is accepted you should make sure that only
     * subtypes of Yard are passed to this constructor. Otherwise the number of column titles and their individual
     * headings will differ from the actual content collected by this reporter.
     *
     * @param informationSource desmoj.Reportable : The Yard to report about
     */
    public YardReporter(desmoj.core.simulator.Reportable informationSource) {

        super(informationSource); // make a Reporter

        groupHeading = "Yards";
        groupID = 1299; // see Reporter for more information about groupID

        numColumns = 9;
        columns = new String[numColumns];
        columns[0] = "Title";
        columns[1] = "(Re)set";
        columns[2] = "Blocks";
        columns[3] = "Overflow";
        columns[4] = "Prod";
        columns[5] = "Cons";
        columns[6] = "Limit";
        columns[7] = "Now";
        columns[8] = "Occup.rate [%]";

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

        if (source instanceof desmoj.extensions.applicationDomains.harbour.Yard) {
            // the Yard we report about (source = informationsource)
            desmoj.extensions.applicationDomains.harbour.Yard y =
                (desmoj.extensions.applicationDomains.harbour.Yard) source;

            // *** entries of yard
            // Title
            entries[0] = y.getName();
            // (Re)set
            entries[1] = y.resetAt().toString();
            // Number of the blocks
            entries[2] = Integer.toString(y.getNumBlocks());
            // Overflow
            entries[3] = Long.toString(y.getOverflow());
            // Prod
            entries[4] = Long.toString(y.getProducers());
            // Cons
            entries[5] = Long.toString(y.getConsumers());
            // Limit
            entries[6] = Long.toString(y.getCapacity());
            if (y.getCapacity() == Integer.MAX_VALUE) {
                entries[5] = "unlim.";
            }
            // Now
            entries[7] = Long.toString(y.getCurrentCapacity());
            // Occup.rate
            entries[8] = Double.toString(StatisticObject.round(y.OccupRate() * 100));

        } else {
            for (int i = 0; i < numColumns; i++) {
                entries[i] = "Invalid source!";
            } // end for
        } // end else

        return entries;
    }
}