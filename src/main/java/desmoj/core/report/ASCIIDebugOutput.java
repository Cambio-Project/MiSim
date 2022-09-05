package desmoj.core.report;

/**
 * Use this class to create ASCII formatted Simulation Debug Output.
 *
 * @author Gunnar Kiesel
 *     <p>
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 *     with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *     <p>
 *     Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *     the specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 */

public class ASCIIDebugOutput extends DebugFileOut implements OutputType {

    /** Create a new ASCIIDebugOutput class * */
    public ASCIIDebugOutput() {
        super(1, "desmoj.core.report.ASCIITableFormatter");
    }

    /***************************************************************************
     * Create a new ASCIIDebugOutput class
     *
     * @param simTimeFloatingDigits
     *            int: The number of floating point digits of the simulation
     *            time values to be displayed
     **************************************************************************/
    public ASCIIDebugOutput(int simTimeFloatingDigits) {
        super(simTimeFloatingDigits, "desmoj.core.report.ASCIITableFormatter");
    }

    /***************************************************************************
     * set the time of the current output
     *
     * @param timeFloats
     *            int: time of the curent output
     **************************************************************************/
    public void setTimeFloats(int timeFloats) {
        this.formatter.setTimePrecision(timeFloats);
    }

    /** returns the file appendix for this output type * */
    public String getAppendix() {
        return ".txt";
    }
}