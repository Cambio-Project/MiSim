package desmoj.extensions.chaining.report;

import java.util.LinkedList;

import desmoj.core.report.Reporter;

/**
 * The FlexReporterBuilder is a helper class for constructing {@link FlexReporter}s which are special Reporters whose
 * content (i.e. the cells that may displayed in multiple rows) is defined dynamically.
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
public class FlexReporterBuilder {

    /**
     * The rows that are build in the FlexReporterBuilder
     */
    private final LinkedList<Row> rows;
    /**
     * The group heading for the FlexReporter
     */
    private final String groupHeading;
    /**
     * The group id for the FlexReporter
     */
    private final int groupId;
    /**
     * The row that is currently under construction in the FlexReporterBuilder
     */
    private Row currentRow;

    /**
     * Constructs a new FlexReporterBuilder with the given group heading and group id.
     *
     * @param groupHeading The group heading that will be used when the FlexReporter is built with this builder
     * @param groupId      The group id that will be used when the FlexReporter is built with this builder
     */
    public FlexReporterBuilder(String groupHeading, int groupId) {
        this.groupHeading = groupHeading;
        this.groupId = groupId;
        rows = new LinkedList<Row>();
    }

    /**
     * Builds a FlexReporter from all the rows and cells that have been added to this builder
     *
     * @return The newly build FlexReporter
     */
    public FlexReporter build() {
        if (currentRow != null) {
            throw new RuntimeException(
                "Please close the currently open row before building the report.");
        }

        return new FlexReporter(groupHeading, groupId, rows);
    }

    /**
     * Closes the row currently under construction
     *
     * @return The FlexReporterBuilder for chaining
     */
    public FlexReporterBuilder closeRow() {
        rows.add(currentRow);
        currentRow = null;
        return this;
    }

    /**
     * Opens a new row for constructrion
     *
     * @return the new row for chaining
     */
    public Row openRow() {
        if (currentRow != null) {
            throw new RuntimeException(
                "There is already an open row. Please close it before opening a new one.");
        }
        return currentRow = new Row(new RowCloseCallback() {

            public FlexReporterBuilder onRowClosed() {
                if (currentRow == null) {
                    throw new RuntimeException(
                        "There is no open row that can be closed.");
                }

                rows.add(currentRow);
                currentRow = null;
                return FlexReporterBuilder.this;
            }
        });
    }

    /**
     * A callback class that should be notified when a row is closed
     */
    interface RowCloseCallback {
        FlexReporterBuilder onRowClosed();
    }

    /**
     * Represents a cell that will be drawn in a report
     */
    public class Cell {

        /**
         * The value of the cell
         */
        private final String value;

        /**
         * Constructs a cell with the given value
         *
         * @param cellValue The value of the cell
         */
        public Cell(Object cellValue) {
            value = cellValue.toString();
        }

        /**
         * @return the value of the cell
         */
        public String getValue() {
            return value;
        }

    }

    /**
     * A flexible reporter that allows multiple rows with individual formatted cells.
     *
     * @author Malte Unkrig
     */
    public final class FlexReporter extends Reporter {

        /**
         * The rows that will be drawn for this reporter
         */
        private final LinkedList<Row> rows;

        /**
         * Constructs a FlexReporter with the given group heaing, group id and the given list of rows.
         *
         * @param groupHeading The heading for the group this reporter contributes to
         * @param groupId      The id of the group this reporter contributes to
         * @param rows         The list of rows which will be drawn for this reporter in the report
         */
        public FlexReporter(String groupHeading, int groupId,
                            LinkedList<Row> rows) {
            super();
            this.groupHeading = groupHeading;
            this.rows = rows;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public final String[] getColumnTitles() {
            return new String[0];
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public final String[] getEntries() {
            return new String[0];
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public final int getGroupID() {
            return groupId;
        }

        /**
         * @return the rows that will be drawn for this reporter in the report
         */
        public LinkedList<Row> getRows() {
            return rows;
        }

    }

    /**
     * A cell that should be visually displayed as a heading cell in the report. For example by drawing it bold.
     */
    public class HeadingCell extends Cell {

        /**
         * Constructs a heading cell with the given value
         *
         * @param cellValue
         */
        public HeadingCell(Object cellValue) {
            super(cellValue);
        }

    }

    /**
     * Represents a row that will be drawn in the report. A row is made up of individual cells
     */
    public class Row {

        /**
         * Callback for when a row is closed
         */
        private final RowCloseCallback closeCallback;

        /**
         * The cells belonging to this row
         */
        private final LinkedList<Cell> cells;

        /**
         * Constructs the Row with the given close callback
         *
         * @param closeCallback A callback object that is notified when a row is closed
         */
        public Row(RowCloseCallback closeCallback) {
            this.closeCallback = closeCallback;
            cells = new LinkedList<Cell>();
        }

        /**
         * Adds a cell with the given value to this row
         *
         * @param cellValue The value of the new cell
         * @return the row for cahining
         */
        public Row addCell(Object cellValue) {
            cells.add(new Cell(cellValue));
            return this;
        }

        /**
         * Adds a heading cell with the given value to this row
         *
         * @param cellValue Row
         * @return the row for cahining
         */
        public Row addHeadingCell(Object cellValue) {
            cells.add(new HeadingCell(cellValue));
            return this;
        }

        /**
         * Closes the row so a new one can be opened
         *
         * @return The FlexReporterBuilder for chaining
         */
        public FlexReporterBuilder closeRow() {
            return closeCallback.onRowClosed();
        }

        /**
         * @return the list of cells belonging to this row
         */
        public LinkedList<Cell> getCells() {
            return cells;
        }
    }
}
