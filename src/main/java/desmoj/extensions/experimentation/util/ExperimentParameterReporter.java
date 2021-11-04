package desmoj.extensions.experimentation.util;

import desmoj.core.report.TableReporter;

/**
 * A reporter for the model and experiment parameters of a certain experiment run represented by an experiment runner.
 * Returned by experimentRunner.getReporter(). The groupID of experiment reporters is 10 which makes the appear in the
 * report as one of the last elements.
 *
 * @author Nicolas Knaak
 *     <p>
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 *     with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *     <p>
 *     Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *     the specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 */
public class ExperimentParameterReporter extends TableReporter {

    /** Number of experiment parameters */
    private final int expRows;

    /** Number of model parameters */
    private final int modelRows;

    /**
     * Creates a new reporter for experiment runner e
     *
     * @param e experiment runner to report
     */
    public ExperimentParameterReporter(ExperimentRunner.ReportProvider ep) {
        super(ep, "Parameter Settings");
        numColumns = 2;
        expRows = ep.getExperimentRunner().getExperimentSettingsArray()[0].length;
        modelRows = ep.getExperimentRunner().getModelParameterArray()[0].length;
        numRows = expRows + modelRows + 1;
        this.columns = new String[] {"Parameter", "Value"};
        this.groupHeading = "ExeperimentRunner Parameter Settings";

        groupID = 10000; // see desmoj.Reporter for details
        // groupHeading = "Experiment Parameters";
    }

    /**
     * Returns the table of entries : entryTable 0 1 0 expParamName[0] expParamVal[0] 1 expParamName[1] expParamVal[1]
     * ... numExpParams +1 modelParamName[0] modelParamVal[0] +2 modelParamName[1] modelParamVal[1] +numModParams
     *
     * @return table of entries (model parameters and experiment settings).
     */
    public String[][] getEntryTable() {
        entryTable = new String[numRows][numColumns];
        ExperimentRunner er = ((ExperimentRunner.ReportProvider) getReportable())
            .getExperimentRunner();
        for (int i = 0; i < numColumns; i++) {
            for (int j = 0; j < expRows; j++) {
                Object nextP = er.getExperimentSettingsArray()[i][j];
                String nextS;
                if (nextP == null) {
                    nextS = "UNDEFINED";
                } else {
                    nextS = nextP.toString();
                }
                entryTable[j][i] = nextS;
            }
        }
        entryTable[expRows][0] = " ";
        entryTable[expRows][1] = " ";

        for (int i = 0; i < numColumns; i++) {
            for (int j = 0; j < modelRows; j++) {
                Object nextP = er.getModelParameterArray()[i][j];
                String nextS;
                if (nextP == null) {
                    nextS = "UNEDFINED";
                } else {
                    nextS = nextP.toString();
                }
                entryTable[j + expRows + 1][i] = nextS;
            }
        }
        return entryTable;
    }
}