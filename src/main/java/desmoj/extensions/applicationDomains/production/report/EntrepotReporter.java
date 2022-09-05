package desmoj.extensions.applicationDomains.production.report;

/**
 * Captures all relevant information about the Entrepot.
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

public class EntrepotReporter extends desmoj.core.report.Reporter {

    // ****** methods ******

    /**
     * Constructor for a new EntrepotReporter. Note that although any Reportable is accepted you should make sure that
     * only subtypes of Entrepot are passed to this constructor. Otherwise the number of column titles and their
     * individual headings will differ from the actual content collected by this reporter.
     *
     * @param informationSource desmoj.Reportable : The Entrepot to report about
     */
    public EntrepotReporter(desmoj.core.simulator.Reportable informationSource) {
        super(informationSource); // make a Reporter

        numColumns = 13;
        columns = new String[numColumns];
        columns[0] = "Title";
        columns[1] = "Order";
        columns[2] = "pass";
        columns[3] = "(Re)set";
        columns[4] = "Custom.";
        columns[5] = "soldUnits";
        columns[6] = "Max";
        columns[7] = "Now";
        columns[8] = "Averg.";
        columns[9] = "avg.Wait";
        columns[10] = "QLimit";
        columns[11] = "QMaxL";
        columns[12] = "refus.";
        groupHeading = "Entrepots";
        groupID = 1011; // see Reporter for more information about groupID
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
        if (source instanceof desmoj.extensions.applicationDomains.production.Entrepot) {
            // the Entrepot we report about
            desmoj.extensions.applicationDomains.production.Entrepot e =
                (desmoj.extensions.applicationDomains.production.Entrepot) source; // source
            // =
            // informationSource

            // Title
            entries[0] = e.getName();
            // Order
            entries[1] = e.getQueueStrategy();
            // pass
            String pass = "no";
            if (e.getPassBy()) {
                pass = "yes";
            }
            entries[2] = pass;
            // (Re)set
            entries[3] = e.resetAt().toString();
            // Customers
            entries[4] = Long.toString(e.getCustomers());
            // soldUnits
            entries[5] = Long.toString(e.getSoldProducts());
            // Max
            entries[6] = Long.toString(e.getMaximum());
            // Now
            entries[7] = Long.toString(e.getAvail());
            // Average.
            entries[8] = Double.toString(e.avgAvail());
            // avg.Wait
            entries[9] = e.averageWaitTime().toString();
            // Qlimit
            entries[10] = Long.toString(e.getQueueLimit());
            if (e.getQueueLimit() == Integer.MAX_VALUE) {
                entries[10] = "unlimit.";
            }
            // QMaxL
            entries[11] = Long.toString(e.maxLength());
            // refused
            entries[12] = Long.toString(e.getRefused());
        } else {
            for (int i = 0; i < numColumns; i++) {
                entries[i] = "Invalid source!";
            } // end for
        } // end else

        return entries;
    }
} // end class EntrepotReporter
