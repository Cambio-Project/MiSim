package desmoj.core.report;

/**
 * Captures all relevant information about the ConfidenceCalculator.
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

public class ConfidenceCalculatorReporter extends
    TallyReporter {

    // ****** methods ******

    /**
     * Constructor for a new ConfidenceCalculatorReporter. Note that although any Reportable is accepted you should make
     * sure that only subtypes of ConfidenceCalculator are passed to this constructor. Otherwise the number of column
     * titles and their individual headings will differ from the actual content collected by this reporter.
     *
     * @param informationSource desmoj.core.simulator.Reportable : The ConfidenceCalculator to report about.
     */
    public ConfidenceCalculatorReporter(
        desmoj.core.simulator.Reportable informationSource) {
        super(informationSource); // make a Reporter

        numColumns = 11;
        columns = new String[numColumns];
        columns[0] = "Title";
        columns[1] = "(Re)set";
        columns[2] = "Obs";
        columns[3] = "Mean";
        columns[4] = "Std.Dv";
        columns[5] = "Min";
        columns[6] = "Max";
        columns[7] = "ConfLevel";
        columns[8] = "ConfMeanLower";
        columns[9] = "ConfMeanUpper";
        columns[10] = "Unit";
        groupHeading = "ConfidenceCalculators";
        groupID = 1661; // see Reporter for more information about groupID
        entries = new String[numColumns];
    }

    /**
     * Returns an array of Strings each containing the data for the corresponding column in array
     * <code>columns[]</code>. Implement this method in a way, that an array of the same length as the column titles is
     * produced containing the data at the point of time this method is called by someone else to produce up-to-date
     * information.
     *
     * @return java.lang.String[] : Array containing the data for reporting
     */
    public String[] getEntries() {
        if (source instanceof desmoj.core.statistic.ConfidenceCalculator) {
            // the Tally we report about (source = informationSource)
            desmoj.core.statistic.ConfidenceCalculator cc = (desmoj.core.statistic.ConfidenceCalculator) source;

            // Title
            entries[0] = cc.getName();
            // (Re)set
            entries[1] = cc.resetAt().toString();
            // Obs
            entries[2] = Long.toString(cc.getObservations());
            // Mean/Total
            // no observations made, so Mean can not be calculated
            if (cc.getObservations() == 0) {
                entries[3] = "Insufficient data";
            } else // return mean value
            {
                entries[3] = Double.toString(cc.getMean());
            }

            // Std.Dev/Total
            // not enough observations are made, so Std.Dev can not be
            // calculated
            if (cc.getObservations() < 2) {
                entries[4] = "Insufficient data";
            } else // return standard deviation
            {
                entries[4] = Double.toString(cc.getStdDev());
            }

            // Min./Total
            if (cc.getObservations() == 0) {
                entries[5] = "Insufficient data";
            } else {
                entries[5] = Double.toString(cc.getMinimum());
            }
            // Max./Total
            if (cc.getObservations() == 0) {
                entries[6] = "Insufficient data";
            } else {
                entries[6] = Double.toString(cc.getMaximum());
            }

            // ConfLevel
            entries[7] = Double.toString(cc.getConfidenceLevel());

            // LowerConfidenceInterval
            if (cc.getObservations() < 2) {
                entries[8] = "Insufficient data";
            } else {
                entries[8] = Double.toString(cc.getConfidenceIntervalOfMeanLowerBound());
            }

            // UpperConfidenceInterval
            if (cc.getObservations() < 2) {
                entries[9] = "Insufficient data";
            } else {
                entries[9] = Double.toString(cc.getConfidenceIntervalOfMeanUpperBound());
            }

            // Unit
            entries[10] = cc.getUnitText();

        } else {
            for (int i = 0; i < numColumns; i++) {
                entries[i] = "Invalid source!";
            } // end for
        } // end else

        return entries;
    }
} // end class TallyReporter

