package desmoj.extensions.applicationDomains.harbour.report;

import desmoj.core.advancedModellingFeatures.report.StockReporter;
import desmoj.core.statistic.StatisticObject;

/**
 * Captures all relevant information about the <code>TransporterSystem</code>. That means from the transporter and the
 * jobs queue.
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
public class TSReporter extends StockReporter {

    // ****** attributes ******

    /**
     * The column headings of the queues (transporter and jobs) part of this TransporterSystemReporter. Entries should
     * contain the elements in the same order as the <code>tsEntries[]</code>.
     */
    private final String[] tsColumns;

    /**
     * The data entries of the queues (transporter and jobs) part of this TransporterSystemReporter. The entries should
     * contain the data elements in the same order as defined in the <code>tsColumns[]</code> array.
     */
    private final String[] tsEntries;

    /**
     * The number of columns of the queues (transporter and jobs) part of this TransporterSystemReporter.
     */
    private final int tsNumColumns;

    /**
     * Constructor for a new TSReporter. Note that although any Reportable is accepted you should make sure that only
     * subtypes of TransporterSystem are passed to this constructor. Otherwise the number of column titles and their
     * individual headings will differ from the actual content collected by this reporter.
     *
     * @param informationSource desmoj.Reportable : The TransporterSystem to report about
     */
    public TSReporter(desmoj.core.simulator.Reportable informationSource) {

        super(informationSource); // make a Reporter

        groupHeading = "InternalTransporter-Systems";
        groupID = 500; // see Reporter for more information about groupID

        // the drives part of the TransporterSystem
        numColumns = 7;
        columns = new String[numColumns];
        columns[0] = "Title";
        columns[1] = "(Re)set";
        columns[2] = "Util.[%]";
        columns[3] = "LoadedDrives";
        columns[4] = "EmptyDrives";
        columns[5] = "T.DistanceOfLDrives";
        columns[6] = "T.DistanceOfEDrives";

        entries = new String[numColumns];

        // *** queues for internal transporter and jobs queue ***

        tsNumColumns = 8;
        tsColumns = new String[tsNumColumns];
        tsColumns[0] = "Queues";
        tsColumns[1] = "Order";
        tsColumns[2] = "pass";
        tsColumns[3] = "(Re)set";
        tsColumns[4] = "avg.Wait";
        tsColumns[5] = "QLimit";
        tsColumns[6] = "QMaxL";
        tsColumns[7] = "refused";

        // entries of for internal transporter and jobs queue
        tsEntries = new String[tsNumColumns * 2];
    }

    /**
     * Returns an array of Strings each containing the data for the corresponding column in array
     * <code>columns[]</code>. This method is fetching the data from this TransporterSystem. Implement this method in a
     * way, that an array of the same length as the column titles is produced containing the data at the point of time
     * this method is called by someone else to produce up-to-date information.
     *
     * @return java.lang.String[] : Array containing the data for reporting.
     */
    public String[] getEntries() {

        if (source instanceof desmoj.extensions.applicationDomains.harbour.TransporterSystem) {
            // the TransporterSystem we report about (source =
            // informationsource)
            desmoj.extensions.applicationDomains.harbour.TransporterSystem ts =
                (desmoj.extensions.applicationDomains.harbour.TransporterSystem) source;

            // *** entries of of the TransporterSystem
            // Title
            entries[0] = ts.getName();
            // (Re)set
            entries[1] = ts.resetAt().toString();
            // Utilization
            entries[2] = Double.toString(StatisticObject.round(ts.avgUsage() * 100));
            // Loaded Drives
            entries[3] = Long.toString(ts.getSumNumLoadedDrives());
            // Empty Drives
            entries[4] = Long.toString(ts.getSumNumEmptyDrives());
            // Distance of Loaded Drives
            entries[5] = Double.toString(ts.getSumDistanceLoadedDrives());
            // Distance of Empty Drives
            entries[6] = Double.toString(ts.getSumDistanceEmptyDrives());

        } else {
            for (int i = 0; i < numColumns; i++) {
                entries[i] = "Invalid source!";
            } // end for
        } // end else

        return entries;
    }

    /**
     * Returns the titles of the wait queues part of this TSReporter as an array of Strings.
     *
     * @return java.lang.String[] : Array containing the titles of the wait queues part of this TSReporter.
     */
    public String[] getStockColumnTitles() {

        return tsColumns.clone();
    }

    /**
     * Returns an array of Strings each containing the data for the corresponding column in array
     * <code>stockColumns[]</code>. This method is fetching the data from the transporter and jobs queue of this
     * TransporterSystem. Implement this method in a way, that an array of the same length as the tsColumns titles is
     * produced containing the data at the point of time this method is called by someone else to produce up-to-date
     * information.
     *
     * @return java.lang.String[] : Array containing the data for reporting.
     */
    public String[] getStockEntries() {
        if (source instanceof desmoj.extensions.applicationDomains.harbour.TransporterSystem) {
            // the TransporterSystem we report about (source =
            // informationsource)
            desmoj.extensions.applicationDomains.harbour.TransporterSystem ts =
                (desmoj.extensions.applicationDomains.harbour.TransporterSystem) source;

            // the transporter queue inside the TransporterSystem (is a
            // ProcessQueue)
            desmoj.core.simulator.ProcessQueue pq = ts.getTransporter();

            // *** TSEntries of the transporters queue
            // Title
            tsEntries[0] = pq.getName();
            // tOrder
            tsEntries[1] = ts.getTransporterQueueStrategy();
            // pass
            String passProds = "--";
            tsEntries[2] = passProds;
            // (Re)set
            tsEntries[3] = pq.resetAt().toString();
            // avg.Wait
            tsEntries[4] = pq.averageWaitTime().toString();
            // Qlimit
            tsEntries[5] = Long.toString(pq.getQueueLimit());
            if (pq.getQueueLimit() == Integer.MAX_VALUE) {
                tsEntries[5] = "unlimit.";
            }
            // QMaxL
            tsEntries[6] = Long.toString(pq.maxLength());
            // refused
            tsEntries[7] = Long.toString(pq.getRefused());

            // *** TSEntries of the jobs queue

            // the jobs queue inside the TransporterSystem (is a Queue)
            desmoj.core.simulator.Queue jq = ts.getJobs();
            // Title
            tsEntries[8] = jq.getName();
            // pOrder
            tsEntries[9] = ts.getJobsQueueStrategy();
            // pass
            String passCons = "--";
            tsEntries[10] = passCons;
            // (Re)set
            tsEntries[11] = jq.resetAt().toString();
            // avg.Wait
            tsEntries[12] = jq.averageWaitTime().toString();
            // QLimit
            tsEntries[13] = Long.toString(jq.getQueueLimit());
            if (jq.getQueueLimit() == Integer.MAX_VALUE) {
                tsEntries[13] = "unlimit.";
            }
            // QMaxL
            tsEntries[14] = Long.toString(jq.maxLength());
            // refused
            tsEntries[15] = Long.toString(jq.getRefused());

        } else {
            for (int i = 0; i < tsNumColumns * 2; i++) {
                tsEntries[i] = "Invalid source!";
            } // end for
        } // end else

        return tsEntries;

    }

    /**
     * Returns the number of columns of the wait queues part of this TSReporter.
     *
     * @return int : The number of columns of the wait queues part of this TSReporter
     */
    public int getStockNumColumns() {

        return tsNumColumns;
    }
}