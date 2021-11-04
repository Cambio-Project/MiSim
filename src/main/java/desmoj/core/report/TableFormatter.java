package desmoj.core.report;

/**
 * An interface representing basic facilites for writing data into tables. The specified operations are adapted from the
 * deprecated class demoj.report.HTMLFileOutput.
 *
 * @author Tim Lechler (HTMLFileOutput), Nicolas Knaak
 *     <p>
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 *     with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *     <p>
 *     Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *     the specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 */
public interface TableFormatter {

    /**
     * Should open a new document containing multiple tables with the given name
     *
     * @param name table name
     */
	void open(String name);

    /** Should close the document */
	void close();

    /** Should close a table row */
	void closeRow();

    /** Should close a table */
	void closeTable();

    /** Should close a table without writing a top tag (HTML specific) */
	void closeTableNoTopTag();

    /** Should open a row */
	void openRow();

    /**
     * Should open a table with the given heading
     *
     * @param heading table heading
     */
	void openTable(String heading);

    /**
     * @return <code>true</code> if a row is currently open,
     *     <code>false</code> otherwise
     */
	boolean rowIsOpen();

    /**
     * @return <code>true</code> if a table is currently open,
     *     <code>false</code> otherwise
     */
	boolean tableIsOpen();

    /**
     * Should write the given string into a new table cell
     *
     * @param s        string to write
     * @param spanning number of cells to span
     */
	void writeCell(String s, int spanning);

    /**
     * Should write the given heading of size i into a new table cell
     *
     * @param s string to write
     * @param i size (must be interpreted in a sensible way).
     */
	void writeHeading(int i, String s);

    /**
     * Should write the given heading of default size into a new table cell
     *
     * @param s string to write
     */
	void writeHeadingCell(String s);

    /** Writes a horizontal ruler */
	void writeHorizontalRuler();

    /**
     * Should format the given time String and write it into a cell
     *
     * @param s a string containing simulation time in float format.
     */
	String writeTime(String s);

    /**
     * Should return the precision used for time values.
     *
     * @return precision
     */
	int timePrecision();

    /**
     * Should set an output file to write the table to
     *
     * @param out a desmoj.report.FileOutput to write the table to
     */
	void setOutput(FileOutput out);

    /**
     * Should set the required precision of time values.
     *
     * @param tp precision
     */
	void setTimePrecision(int tp);

    /**
     * @return appendix of the file format the table is stored in (e.g. "html" or "txt")
     */
	String getFileFormat();
}