package desmoj.extensions.chaining.report;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import desmoj.core.report.Reporter;
import desmoj.core.simulator.Reportable;

/**
 * The SmartReporter is a helper class for generating reporters based on user-defined header-value pairs, supporting the
 * modification of entries.
 *
 * @author Malte Unkrig
 *     <p>
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 *     with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *     <p>
 *     Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *     the specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 */
public class SmartReporter extends Reporter {

    public SmartReporter(String groupHeading, int groupId, List<HeaderValuePair> headerValuePairs,
                         Reportable reportable) {
        super(reportable);
        super.groupHeading = groupHeading;
        super.groupID = groupId;

        numColumns = headerValuePairs.size();
        columns = new String[numColumns];
        entries = new String[numColumns];

        for (int i = 0; i < numColumns; i++) {
            HeaderValuePair pair = headerValuePairs.get(i);
            columns[i] = pair.header;
            entries[i] = pair.value;
        }
    }

    public void overrideValueAt(String newValue, int columnPos) {
        entries[columnPos] = newValue;
    }

    @Override
    public String[] getEntries() {
        return entries;
    }

    public static class HeaderValuePair {
        private final String header;
        private final String value;

        public HeaderValuePair(String header, Object value) {
            super();
            this.header = header;

            if (value instanceof Double || value instanceof Float) {
                NumberFormat numberFormat;

                numberFormat = DecimalFormat.getInstance(Locale.ENGLISH);
                numberFormat.setMaximumFractionDigits(4);
                numberFormat.setMinimumFractionDigits(4);
                numberFormat.setGroupingUsed(false);

                this.value = numberFormat.format(value);
            } else {
                this.value = value.toString();
            }
        }

        @Override
        public String toString() {
            return header + ": " + value;
        }

    }
}
