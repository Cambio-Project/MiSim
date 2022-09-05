package desmoj.core.report;

/**
 * Interface for all Outputs (Report, Trace, Error, Debug) in format like HTML, ASCII, XML written line by line.
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

public interface OutputType extends MessageReceiver {

    /**
     * opens a new file for writting the output
     *
     * @param pathname String: path to write in
     * @param name     String: name of the file
     */
	void open(String pathname, String name);

    /**
     * Closes the file
     */
	void close();

    /***************************************************************************
     * deliever the file appendix used for the specific format (e.g. html, txt)
     **************************************************************************/
	String getAppendix();

}