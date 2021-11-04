package desmoj.extensions.xml.report;

import desmoj.core.advancedModellingFeatures.report.StockReporter;
import desmoj.core.report.HistogramReporter;
import desmoj.core.report.Message;
import desmoj.core.report.OutputType;
import desmoj.core.report.Reporter;
import desmoj.core.report.TableReporter;
import desmoj.extensions.xml.util.XMLHelper;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * Class to receive information from reporters and create an xml file out of it.
 *
 * @author Gunnar Kiesel
 * @author modified by Nicolas Knaak (1.2.06)
 *     <p>
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 *     with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *     <p>
 *     Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *     the specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 */
public class XMLReportOutput extends XMLOutput implements OutputType {
    // private Reporter r;
    protected Reporter lastR = null;

    private final Element report;

    private Element reporter;

    private int reportNumber;

    /** Create a new XMLReportOutput class * */
    public XMLReportOutput() {
        document = XMLHelper.createDocument();
        report = document.createElement("report");
        document.appendChild(report);
    }

    /***************************************************************************
     * method to be called when a reporter is received
     *
     * @param r
     *            Reporter: the Reporter that has been send
     **************************************************************************/
    public void receive(Reporter r) {

        //System.out.println("Reporter: " + r);

        String[] titleBuf = r.getColumnTitles();
        String[] entryBuf = r.getEntries();

        //System.out.println("--- Entry Buffer: ");
        for (int i = 0; i < entryBuf.length; i++) {
            System.out.println(entryBuf[i]);
        }

        /** Only work with valid reporters * */
        if (r == null) {
            return;
        }
        /***********************************************************************
         * The first Reporter that is received it's not handled as a real
         * reporter here since it just provides the model description
         **********************************************************************/
        if (lastR == null) {
            report.setAttribute("experiment", r.getModel().getExperiment()
                .getName());
            report.setAttribute("model", r.getModel().getName());
            Element param = document.createElement("param");
            param.setAttribute("name", titleBuf[0]);
            Text description = document.createTextNode(entryBuf[0]);
            param.appendChild(description);
            report.appendChild(param);
            lastR = r;
            return;
            /** If a different kind of reporter is received * */
        } else if (Reporter.isOtherGroup(r, lastR)) {
            reporter = document.createElement("reporter");
            reporter.setAttribute("type", r.getHeading());
            report.appendChild(reporter);
        }
        /** For all reporter rows a new item will be created * */
        Element item = document.createElement("item");
        item.setAttribute("name", entryBuf[0]);

        // ###--- geaendert von Nick
        if (!(r instanceof TableReporter)) {
            reporter.appendChild(item);
        }

        /** All Reporters that produce two rows * */
        if (r.isTwoRowReporter()) {
            for (int i = 1; i < (entryBuf.length / 2); i++) {
                Element param = document.createElement("param");
                param.setAttribute("name", titleBuf[i]);
                Text value = document.createTextNode(entryBuf[i]);
                System.out.println("Text 2 row: " + value);
                param.appendChild(value);
                item.appendChild(param);
            }
            Element item2 = document.createElement("item");
            item2.setAttribute("name",
                entryBuf[(entryBuf.length / 2)]);
            reporter.appendChild(item2);
            for (int i = (entryBuf.length / 2) + 1; i < entryBuf.length; i++) {
                Element param = document.createElement("param");
                param.setAttribute("name", titleBuf[i
                    - (entryBuf.length / 2)]);
                Text value = document.createTextNode(entryBuf[i]);
                param.appendChild(value);
                item2.appendChild(param);
            }
        }
        /** Histogramm reporters * */
        else if (r instanceof HistogramReporter) {
            HistogramReporter hr = (HistogramReporter) r;
            for (int i = 1; i < titleBuf.length; i++) {
                Element param = document.createElement("param");
                param.setAttribute("name", titleBuf[i]);
                Text value = document.createTextNode(entryBuf[i]);
                param.appendChild(value);
                item.appendChild(param);
            }
            Element param = document.createElement("param");
            param.setAttribute("name", "Histogram Data");
            item.appendChild(param);
            for (int i = 0; i <= hr.getNoOfCells(); i++) {
                Element subItem = document.createElement("item");
                subItem.setAttribute("name", "Cell "
                    + hr.getHistEntries()[i][0]);
                param.appendChild(subItem);
                for (int j = 1; j < hr.getHistNumColumns(); j++) {
                    if (!(hr.getHistEntries()[i][j].equals("|") || hr
                        .getHistColumnTitles()[j].equals("Graph"))) {
                        Element subParam = document.createElement("param");
                        subParam.setAttribute("name",
                            hr.getHistColumnTitles()[j]);
                        Text value = document.createTextNode(hr
                            .getHistEntries()[i][j]);
                        subParam.appendChild(value);
                        subItem.appendChild(subParam);
                    }
                }
                param.appendChild(subItem);
            }
        }
        /** All reporters of StockReporter type * */
        else if (r instanceof StockReporter) {
            StockReporter sr = (StockReporter) r;
            for (int i = 1; i < titleBuf.length; i++) {
                Element param = document.createElement("param");
                param.setAttribute("name", titleBuf[i]);
                Text value = document.createTextNode(entryBuf[i]);
                param.appendChild(value);
                item.appendChild(param);
            }
            Element param = document.createElement("param");
            param.setAttribute("name", sr.getStockColumnTitles()[0]);
            item.appendChild(param);
            for (int i = 0; i < 2; i++) {
                Element subItem = document.createElement("item");
                subItem.setAttribute("name", sr.getStockEntries()[i
                    * sr.getStockNumColumns()]);
                param.appendChild(subItem);
                for (int j = 1; j < sr.getStockNumColumns(); j++) {
                    Element subParam = document.createElement("param");
                    subParam.setAttribute("name", sr.getStockColumnTitles()[j]);
                    Text subValue = document.createTextNode(sr
                        .getStockEntries()[j
                        + (sr.getStockNumColumns() * i)]);
                    subParam.appendChild(subValue);
                    subItem.appendChild(subParam);
                }
                param.appendChild(subItem);
            }
        }
        // ###--- geaendert von Nick, damit IndividualReporter funktioniert
        /** from a Table reporter a 2 dimensional array will be received * */
        else if (r instanceof TableReporter) {

            TableReporter tr = (TableReporter) r;
            String[][] entries = tr.getEntryTable();

            for (int i = 0; i < tr.numRows(); i++) {
                /** only create tags for parameters that have a value * */
                if (!entries[i][0].equals(" ")) {
                    Element nextItem = document.createElement("item");
                    nextItem.setAttribute("name", entries[i][0]);
                    reporter.appendChild(nextItem);

                    for (int j = 1; j < tr.numColumns(); j++) {
                        /*******************************************************
                         * (only create tags for parameters that have a value)
                         * not used due to problems in HTML converting
                         ******************************************************/
                        // if (!tr.getEntryTable()[i][j].equals(" ")) {
                        Element param = document.createElement("param");
                        param.setAttribute("name", titleBuf[j]);
                        Text value = document.createTextNode(entries[i][j]);
                        param.appendChild(value);
                        nextItem.appendChild(param);
                        // }
                    }
                }
            }
        }

        /** All reporters that produce a single item row * */
        else {
            for (int i = 1; i < titleBuf.length; i++) {
                /** only create tags for parameters that have a value * */
                // if (!r.entries[i].equals(" ")) {
                Element param = document.createElement("param");
                param.setAttribute("name", titleBuf[i]);
                Text value = document.createTextNode(entryBuf[i]);
                System.out.println("Text 1 row: " + value);
                param.appendChild(value);
                item.appendChild(param);
                // }
            }
        }

        lastR = r;
    }

    /***************************************************************************
     * method to be called when a Message is received. this class does not
     * handle Messages so it will simply return
     *
     * @param m
     *            Message: The Message that has been send.
     **************************************************************************/
    public void receive(Message m) {
        return;
    }

    /***************************************************************************
     * open a new file to write the output in
     *
     * @param pathname
     *            String: name of the path to write in
     * @param name
     *            String: name of the file to write in
     **************************************************************************/
    public void open(String pathname, String name) {
        reportNumber++;
        super.open(createFileName(pathname, name, "report"));
    }
}