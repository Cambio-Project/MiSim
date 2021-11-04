package desmoj.extensions.applicationDomains.production.report;

/**
 * Captures all relevant information about the <code>TransportJunction</code>. That means it collects all data from the
 * master (transporter) and the slave (goods) queue.
 *
 * @author Soenke Claassen based on ideas from Tim Lechler
 * @author based on DESMO-C from Thomas Schniewind, 1998
 *     <p>
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 *     with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *     <p>
 *     Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *     the specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 */

public class TransportReporter extends desmoj.core.report.Reporter {

    // ****** methods ******

    /**
     * Constructor for a new TransportReporter. Note that although any Reportable is accepted you should make sure that
     * only subtypes of TransportJunction are passed to this constructor. Otherwise the number of column titles and
     * their individual headings will differ from the actual content collected by this reporter.
     *
     * @param informationSource desmoj.Reportable : The TransportJunction to report about
     */
    public TransportReporter(desmoj.core.simulator.Reportable informationSource) {
        super(informationSource); // make a Reporter

        groupHeading = "Transporter";
        groupID = 511; // see Reporter for more information about groupID

        numColumns = 12;
        columns = new String[numColumns];
        columns[0] = "Title";
        columns[1] = "Order";
        columns[2] = "pass";
        columns[3] = "(Re)set";
        columns[4] = "Obs";
        columns[5] = "QLimit";
        columns[6] = "Qmax";
        columns[7] = "Qnow";
        columns[8] = "Qavg.";
        columns[9] = "Zeros";
        columns[10] = "avg.Wait";
        columns[11] = "refus.";

        entries = new String[numColumns * 2]; // entries of master and slave
        // queue
    }

    /**
     * Returns an array of Strings each containing the data for the corresponding column in array
     * <code>columns[]</code>. This method is fetching the data from the master (transporter) and slave (goods) queue.
     * Implement this method in a way, that an array of the same length as the columntitles is produced containing the
     * data at the point of time this method is called by someone else to produce up-to-date information.
     *
     * @return java.lang.String[] : Array containing the data for reporting
     */
    public String[] getEntries() {

        if (source instanceof desmoj.extensions.applicationDomains.production.TransportJunction) {
            // the TransportJunction we report about (source =
            // informationsource)
            desmoj.extensions.applicationDomains.production.TransportJunction tj =
                (desmoj.extensions.applicationDomains.production.TransportJunction) source;

            // *** entries of the master queue
            // Title
            entries[0] = tj.getName();
            // mOrder
            entries[1] = tj.getMQueueStrategy();
            // pass
            String pass = "no";
            if (tj.getPassBy()) {
                pass = "yes";
            }
            entries[2] = pass;
            // (Re)set
            entries[3] = tj.resetAt().toString();
            // Obs
            entries[4] = Long.toString(tj.getObservations());
            // Qlimit
            entries[5] = Long.toString(tj.getQueueLimit());
            if (tj.getQueueLimit() == Integer.MAX_VALUE) {
                entries[5] = "unlimit.";
            }
            // Qmax
            entries[6] = Long.toString(tj.mMaxLength());
            // Qnow
            entries[7] = Long.toString(tj.mLength());
            // Qavg.
            entries[8] = Double.toString(tj.mAverageLength());
            // Zeros
            entries[9] = Long.toString(tj.mZeroWaits());
            // avg.Wait
            entries[10] = tj.mAverageWaitTime().toString();
            // refused
            entries[11] = Long.toString(tj.getMRefused());

            // *** entries of the slave queue
            // Title
            entries[12] = tj.getSlaveQueue().getName();
            // sOrder
            entries[13] = tj.getSQueueStrategy();
            // pass
            entries[14] = "--";
            // (Re)set
            entries[15] = tj.getSlaveQueue().resetAt().toString();
            // Obs
            entries[16] = Long.toString(tj.getSlaveQueue().getObservations());
            // Qlimit
            entries[17] = Long.toString(tj.getSlaveQueue().getQueueLimit());
            if (tj.getSlaveQueue().getQueueLimit() == Integer.MAX_VALUE) {
                entries[17] = "unlimit.";
            }
            // Qmax
            entries[18] = Long.toString(tj.sMaxLength());
            // Qnow
            entries[19] = Long.toString(tj.sLength());
            // Qavg.
            entries[20] = Double.toString(tj.sAverageLength());
            // Zeros
            entries[21] = Long.toString(tj.sZeroWaits());
            // avg.Wait
            entries[22] = tj.sAverageWaitTime().toString();
            // refused
            entries[23] = Long.toString(tj.getSRefused());
        } else {
            for (int i = 0; i < numColumns * 2; i++) {
                entries[i] = "Invalid source!";
            } // end for
        } // end else

        return entries;
    }

    /*@TODO: Comment */
    public boolean isContinuingReporter() {
        return true;
    }

    /* @TODO: Comment */
    public boolean isTwoRowReporter() {
        return true;
    }
} // end class TransportReporter
