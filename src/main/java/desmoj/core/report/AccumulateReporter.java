package desmoj.core.report;

import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;

/**
 * Captures all relevant information about the Accumulate.
 *
 * @author Soenke Claassen based on ideas from Tim Lechler
 * @author based on DESMO-C from Thomas Schniewind, 1998
 * @author modified by Chr. M&uuml;ller (TH Wildau) 28.11.2012
 *     <p>
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 *     with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *     <p>
 *     Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *     the specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 */

public class AccumulateReporter extends Reporter {

    // ****** methods ******

    /**
     * Constructor for a new AccumulateReporter. Note that although any Reportable is accepted you should make sure that
     * only subtypes of Accumulate are passed to this constructor. Otherwise the number of column titles and their
     * individual headings will differ from the actual content collected by this reporter.
     *
     * @param informationSource desmoj.core.simulator.Reportable : The Accumulate to report about.
     */
    public AccumulateReporter(desmoj.core.simulator.Reportable informationSource) {
        super(informationSource); // make a Reporter (source =
        // informationSource)

        numColumns = 9;
        columns = new String[numColumns];
        columns[0] = "Title";
        columns[1] = "(Re)set";
        columns[2] = "Obs";
        columns[3] = "Mean";
        columns[4] = "Std.Dv";
        columns[5] = "Min";
        columns[6] = "Max";
        columns[7] = "Period";
        columns[8] = "Unit";
        groupHeading = "Accumulates";
        groupID = 1711; // see Reporter for more information about groupID
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
        if (source instanceof desmoj.core.statistic.Accumulate) {

            // the Accumulate we report about (source = informationSource)
            desmoj.core.statistic.Accumulate acc = (desmoj.core.statistic.Accumulate) source;
            boolean _showTimeSpansInReport = acc.getShowTimeSpansInReport();

            // Title
            entries[0] = acc.getName();
            // (Re)set
            entries[1] = acc.resetAt().toString();
            // Obs
            entries[2] = Long.toString(acc.getObservations());

            // Mean
            // has no time passed since the last reset?
            if (TimeInstant.isEqual(acc.presentTime(), acc.resetAt())
                || acc.getObservations() == 0) // OR no observations are
            // made
            {
                entries[3] = "Insufficient data";
            } else // return mean value
            {
                entries[3] = this.format(_showTimeSpansInReport, acc.getMean());

            }

            // Std.Dev

            // has no time passed since the last reset?
            if (TimeInstant.isEqual(acc.presentTime(), acc.resetAt())
                || acc.getObservations() < 2) // OR not enough
            // observations
            // are made
            {
                entries[4] = "Insufficient data";
            } else // return mean value
            {
                entries[4] = this.format(_showTimeSpansInReport, acc.getStdDev());

            }

            // Min.
            if (acc.getObservations() == 0) {
                entries[5] = "Insufficient data";
            } else {
                entries[5] = this.format(_showTimeSpansInReport, acc.getMinimum());

            }
            // Max
            if (acc.getObservations() == 0) {
                entries[6] = "Insufficient data";
            } else {
                entries[6] = this.format(_showTimeSpansInReport, acc.getMaximum());

            }
            // Period
            entries[7] = acc.getPeriodMeasured().toString();

            //cm 21.11.12  Extension for viewing unit
            entries[8] = acc.getUnitText();

        } else {
            for (int i = 0; i < numColumns; i++) {
                entries[i] = "Invalid source!";
            } // end for
        } // end else

        return entries;
    }

    private String format(boolean showTimeSpans, double value) {
        String out = Double.toString(value);
        if (showTimeSpans && value < 0.0) {
            out += " (Invalid)";
        } else if (showTimeSpans && value >= Long.MAX_VALUE) {
            out += " (Invalid)";
        } else if (showTimeSpans) {
            out = new TimeSpan(value).toString();
        }
        return out;
    }

} // end class AccumulateReporter
