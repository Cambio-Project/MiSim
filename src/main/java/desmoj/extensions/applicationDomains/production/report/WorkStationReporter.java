package desmoj.extensions.applicationDomains.production.report;

import desmoj.core.simulator.ProcessQueue;

/**
 * Captures all relevant information about the <code>WorkStation</code>. That means from the master and all the slave
 * queues.
 *
 * @author Soenke Claassen
 *     <p>
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 *     with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *     <p>
 *     Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *     the specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 */

public class WorkStationReporter extends desmoj.core.report.Reporter {

    // ****** attributes ******

    /**
     * The number of different kinds of parts (= the number of slave queues). Default is one!
     */
    private int numOfSlaveQueues = 1;

    // ****** methods ******

    /**
     * Constructor for a new WorkStationReporter. Note that although any Reportable is accepted you should make sure
     * that only subtypes of WorkStation are passed to this constructor. Otherwise the number of column titles and their
     * individual headings will differ from the actual content collected by this reporter.
     *
     * @param informationSource desmoj.Reportable : The WorkStation to report about
     */
    public WorkStationReporter(
        desmoj.core.simulator.Reportable informationSource) {
        super(informationSource); // make a Reporter

        if (informationSource instanceof desmoj.extensions.applicationDomains.production.WorkStation) {
            // the WorkStation we report about
            desmoj.extensions.applicationDomains.production.WorkStation ws =
                (desmoj.extensions.applicationDomains.production.WorkStation) informationSource;

            // get the number of different kinds od parts (slave queues)
            numOfSlaveQueues = ws.getNumOfParts();
        }

        groupHeading = "Work-Stations";
        groupID = 411; // see Reporter for more information about groupID

        numColumns = 12;
        columns = new String[numColumns];
        columns[0] = "Title";
        columns[1] = "#need.";
        columns[2] = "Qorder";
        columns[3] = "(Re)set";
        columns[4] = "Obs";
        columns[5] = "QLimit";
        columns[6] = "Qmax";
        columns[7] = "Qnow";
        columns[8] = "Qavg.";
        columns[9] = "Zeros";
        columns[10] = "avg.Wait";
        columns[11] = "refus.";

        // entries of the master queue and the different slave queues
        entries = new String[numColumns * (numOfSlaveQueues + 1)];
    }

    /**
     * Returns an array of Strings each containing the data for the corresponding column in array
     * <code>columns[]</code>. This method is fetching the data from the master and the slave queues from the
     * WorkStation. Implement this method in a way, that an array of the same length as the columntitles is produced
     * containing the data at the point of time this method is called by someone else to produce up-to-date
     * information.
     *
     * @return java.lang.String[] : Array containing the data for reporting
     */
    public String[] getEntries() {

        if (source instanceof desmoj.extensions.applicationDomains.production.WorkStation) {
            // the WorkStation we report about (source = informationSource)
            desmoj.extensions.applicationDomains.production.WorkStation ws =
                (desmoj.extensions.applicationDomains.production.WorkStation) source;

            // *** entries of the master queue
            // Title
            entries[0] = ws.getName();
            // #needed
            entries[1] = "1"; // there is always only one master needed!
            // mQorder
            entries[2] = ws.getMQueueStrategy();
            // (Re)set
            entries[3] = ws.resetAt().toString();
            // Obs
            entries[4] = Long.toString(ws.getObservations());
            // Qlimit
            entries[5] = Long.toString(ws.getQueueLimit());
            if (ws.getQueueLimit() == Integer.MAX_VALUE) {
                entries[5] = "unlimit.";
            }
            // Qmax
            entries[6] = Long.toString(ws.mMaxLength());
            // Qnow
            entries[7] = Long.toString(ws.mLength());
            // Qavg.
            entries[8] = Double.toString(ws.mAverageLength());
            // Zeros
            entries[9] = Long.toString(ws.mZeroWaits());
            // avg.Wait
            entries[10] = ws.mAverageWaitTime().toString();
            // refused
            entries[11] = Long.toString(ws.getMRefused());

            // get the slave queues array
            ProcessQueue[] slaveQueues = ws.getSlaveQueues();

            // *** entries of the slave queues
            for (int i = 0; i < ws.getNumOfParts(); i++) {
                // Title
                entries[(i * 12) + 12] = slaveQueues[i].getName();
                // #needed
                entries[(i * 12) + 13] = Integer.toString(ws.getPartsList()
                    .getQuantityOfPart(i));
                // sQorder
                entries[(i * 12) + 14] = ws.getSQueueStrategy(i);
                // (Re)set
                entries[(i * 12) + 15] = slaveQueues[i].resetAt().toString();
                // Obs
                entries[(i * 12) + 16] = Long.toString(slaveQueues[i]
                    .getObservations());
                // Qlimit
                entries[(i * 12) + 17] = Long.toString(slaveQueues[i]
                    .getQueueLimit());
                if (slaveQueues[i].getQueueLimit() == Integer.MAX_VALUE) {
                    entries[(i * 12) + 17] = "unlimit.";
                }
                // Qmax
                entries[(i * 12) + 18] = Long.toString(ws.sMaxLength(i));
                // Qnow
                entries[(i * 12) + 19] = Long.toString(ws.sLength(i));
                // Qavg.
                entries[(i * 12) + 20] = Double.toString(ws.sAverageLength(i));
                // Zeros
                entries[(i * 12) + 21] = Long.toString(ws.sZeroWaits(i));
                // avg.Wait
                entries[(i * 12) + 22] = ws.sAverageWaitTime(i).toString();
                // refused
                entries[(i * 12) + 23] = Long.toString(ws.getSRefused(i));
            }
        } else {
            for (int j = 0; j < (numColumns * 2); j++) {
                entries[j] = "Invalid source!";
            } // end for
        } // end else

        return entries;
    }

    /**
     * Returns the number of slave queues of this WorkStation. That is the number of different kinds of parts handled at
     * this WorkStation.
     *
     * @return int : The number of slave queues of this WorkStation.
     */
    public int getNumOfSlaveQueues() {

        return this.numOfSlaveQueues;
    }

    /*@TODO: Comment */
    public boolean isContinuingReporter() {
        return true;
    }

    /* @TODO: Comment */
    public boolean isTwoRowReporter() {
        return true;
    }
} // end class WorkStationReporter
