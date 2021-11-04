package desmoj.extensions.applicationDomains.harbour.report;

import desmoj.core.report.Reporter;
import desmoj.core.statistic.StatisticObject;

/**
 * Captures all relevant information about the Berth.
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
public class BerthReporter extends Reporter {

    /**
     * Constructor for a new BerthReporter. Note that although any Reportable is accepted you should make sure that only
     * subtypes of Yard are passed to this constructor. Otherwise the number of column titles and their individual
     * headings will differ from the actual content collected by this reporter.
     *
     * @param informationSource desmoj.Reportable : The berth to report about
     */
    public BerthReporter(desmoj.core.simulator.Reportable informationSource) {

        super(informationSource); // make a Reporter

        numColumns = 13;
        columns = new String[numColumns];
        columns[0] = "Title";
        columns[1] = "Order";
        columns[2] = "(Re)set";
        columns[3] = "Users";
        columns[4] = "Util.[%]";
        columns[5] = "Avg.BerthTime";
        columns[6] = "QLimit";
        columns[7] = "QMax";
        columns[8] = "QNow";
        columns[9] = "Qavg.";
        columns[10] = "Zeros";
        columns[11] = "avg.Wait";
        columns[12] = "refus.";
        groupHeading = "Berthes";
        groupID = 800; // see Reporter for more information about groupID
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

        if (source instanceof desmoj.extensions.applicationDomains.harbour.Berth) {
            // the Berth we report about
            desmoj.extensions.applicationDomains.harbour.Berth b =
                (desmoj.extensions.applicationDomains.harbour.Berth) source; // source
            // =
            // informationSource

            // Title
            entries[0] = b.getName();
            // order
            entries[1] = b.getQueueStrategy();
            // (Re)set
            entries[2] = b.resetAt().toString();
            // Users
            entries[3] = Long.toString(b.getUsers());
            // Utilization round to percentage
            entries[4] = Double.toString(StatisticObject.round(b.avgUsage() * 100));
            // Avg. berth time
            entries[5] = Double.toString(StatisticObject.round(b.avgServTime()));
            // QLimit
            entries[6] = Long.toString(b.getQueueLimit());
            if (b.getQueueLimit() == Integer.MAX_VALUE) {
                entries[6] = "unlimit.";
            }
            // Qmax.
            entries[7] = Long.toString(b.maxLength());
            // Qnow
            entries[8] = Long.toString(b.length());
            // Qavg.
            entries[9] = Double.toString(b.averageLength());
            // Zeros
            entries[10] = Long.toString(b.zeroWaits());
            // avg.Wait
            entries[11] = b.averageWaitTime().toString();
            // refused
            entries[12] = Long.toString(b.getRefused());

        } else {
            for (int i = 0; i < numColumns; i++) {
                entries[i] = "Invalid source!";
            } // end for
        } // end else

        return entries;
    }

}