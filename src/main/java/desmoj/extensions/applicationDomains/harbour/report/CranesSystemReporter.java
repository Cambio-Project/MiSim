package desmoj.extensions.applicationDomains.harbour.report;

import desmoj.core.advancedModellingFeatures.report.StockReporter;
import desmoj.core.statistic.StatisticObject;

/**
 * Captures all relevant information about the <code>CranesSystem</code>. That means from the transporter and the cranes
 * queue.
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
public class CranesSystemReporter extends StockReporter {

    // ****** attributes ******

    /**
     * The column headings of the queues (transporter and cranes) part of this CranesSystemReporter. Entries should
     * contain the elements in the same order as the <code>csEntries[]</code>.
     */
    private final String[] csColumns;

    /**
     * The data entries of the queues (transporter and cranes) part of this CranesSystemReporter. The entries should
     * contain the data elements in the same order as defined in the <code>csColumns[]</code> array.
     */
    private final String[] csEntries;

    /**
     * The number of columns of the queues (transporter and cranes) part of this CranesSystemReporter.
     */
    private final int csNumColumns;

    /**
     * Constructor for a new CranesSystemReporter. Note that although any Reportable is accepted you should make sure
     * that only subtypes of TransporterSystem are passed to this constructor. Otherwise the number of column titles and
     * their individual headings will differ from the actual content collected by this reporter.
     *
     * @param informationSource desmoj.Reportable : The CranesSystem to report about
     */
    public CranesSystemReporter(
        desmoj.core.simulator.Reportable informationSource) {

        super(informationSource); // make a Reporter

        groupHeading = "Cranes-Systems";
        groupID = 1000; // see Reporter for more information about groupID

        // the load-/unloading part of the CranesSystem
        numColumns = 5;
        columns = new String[numColumns];
        columns[0] = "Title";
        columns[1] = "(Re)set";
        columns[2] = "Util.[%]";
        columns[3] = "LoadedUnits";
        columns[4] = "UnloadedUnits";

        entries = new String[numColumns];

        // *** queues for cranes and transporter ***

        csNumColumns = 8;
        csColumns = new String[csNumColumns];
        csColumns[0] = "Queues";
        csColumns[1] = "Order";
        csColumns[2] = "pass";
        csColumns[3] = "(Re)set";
        csColumns[4] = "avg.Wait";
        csColumns[5] = "QLimit";
        csColumns[6] = "QMaxL";
        csColumns[7] = "refused";

        // entries of for the transporter and cranes queue
        csEntries = new String[csNumColumns * 2];

    }

    /**
     * Returns an array of Strings each containing the data for the corresponding column in array
     * <code>columns[]</code>. This method is fetching the data from this CranesSystem. Implement this method in a way,
     * that an array of the same length as the column titles is produced containing the data at the point of time this
     * method is called by someone else to produce up-to-date information.
     *
     * @return java.lang.String[] : Array containing the data for reporting.
     */
    public String[] getEntries() {

        if (source instanceof desmoj.extensions.applicationDomains.harbour.CranesSystem) {
            // the CranesSystem we report about (source = informationsource)
            desmoj.extensions.applicationDomains.harbour.CranesSystem cs =
                (desmoj.extensions.applicationDomains.harbour.CranesSystem) source;

            // *** entries of the CranesSystem
            // Title
            entries[0] = cs.getName();
            // (Re)set
            entries[1] = cs.getTransporterQueue().resetAt().toString();
            // Utilization
            entries[2] = Double.toString(StatisticObject.round(cs.avgUsage() * 100));
            // Loaded Units
            entries[3] = Long.toString(cs.getSumNumLoadedUnits());
            // Unloaded Units
            entries[4] = Long.toString(cs.getSumNumUnloadedUnits());

        } else {
            for (int i = 0; i < numColumns; i++) {
                entries[i] = "Invalid source!";
            } // end for
        } // end else

        return entries;
    }

    /**
     * Returns the titles of the wait queues part of this CranesSystemReporter as an array of Strings.
     *
     * @return java.lang.String[] : Array containing the titles of the wait queues part of this CranesSystemReporter.
     */
    public String[] getStockColumnTitles() {

        return csColumns.clone();
    }

    /**
     * Returns an array of Strings each containing the data for the corresponding column in array
     * <code>stockColumns[]</code>. This method is fetching the data from the master Stock. Implement this method in a
     * way, that an array of the same length as the stockColumns titles is produced containing the data at the point of
     * time this method is called by someone else to produce up-to-date information.
     *
     * @return java.lang.String[] : Array containing the data for reporting.
     */
    public String[] getStockEntries() {
        if (source instanceof desmoj.extensions.applicationDomains.harbour.CranesSystem) {
            // the CranesSystem we report about (source = informationsource)
            desmoj.extensions.applicationDomains.harbour.CranesSystem cs =
                (desmoj.extensions.applicationDomains.harbour.CranesSystem) source;
            // the cranes queue inside the CranesSystem (is a ProcessQueue)
            desmoj.core.simulator.ProcessQueue pq = cs.getCranesQueue();

            // *** Entries of the cranes queue
            // Title
            csEntries[0] = pq.getName();
            // cOrder
            csEntries[1] = pq.getQueueStrategy();
            // pass
            String passProds = "--";
            csEntries[2] = passProds;
            // (Re)set
            csEntries[3] = pq.resetAt().toString();
            // avg.Wait
            csEntries[4] = pq.averageWaitTime().toString();
            // Qlimit
            csEntries[5] = Long.toString(pq.getQueueLimit());
            if (pq.getQueueLimit() == Integer.MAX_VALUE) {
                csEntries[5] = "unlimit.";
            }
            // QMaxL
            csEntries[6] = Long.toString(pq.maxLength());
            // refused
            csEntries[7] = Long.toString(pq.getRefused());

            // *** Entries of the transporter queue

            // the transporter queue inside the TransporterSystem (is a
            // ProcessQueue)
            desmoj.core.simulator.ProcessQueue tq = cs.getTransporterQueue();
            // Title
            csEntries[8] = tq.getName();
            // tOrder
            csEntries[9] = tq.getQueueStrategy();
            // pass
            String passCons = "--";
            csEntries[10] = passCons;
            // (Re)set
            csEntries[11] = tq.resetAt().toString();
            // avg.Wait
            csEntries[12] = tq.averageWaitTime().toString();
            // QLimit
            csEntries[13] = Long.toString(tq.getQueueLimit());
            if (tq.getQueueLimit() == Integer.MAX_VALUE) {
                csEntries[13] = "unlimit.";
            }
            // QMaxL
            csEntries[14] = Long.toString(tq.maxLength());
            // refused
            csEntries[15] = Long.toString(tq.getRefused());

        } else {
            for (int i = 0; i < csNumColumns * 2; i++) {
                csEntries[i] = "Invalid source!";
            } // end for
        } // end else

        return csEntries;

    }

    /**
     * Returns the number of columns of the wait queues part of this CranesSystemReporter.
     *
     * @return int : The number of columns of the wait queues part of this CranesSystemReporter.
     */
    public int getStockNumColumns() {

        return csNumColumns;
    }
}