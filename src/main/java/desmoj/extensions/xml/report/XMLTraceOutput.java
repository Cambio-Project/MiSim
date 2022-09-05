package desmoj.extensions.xml.report;

import desmoj.core.report.Message;
import desmoj.core.report.Reporter;
import desmoj.core.report.TraceNote;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * Use this class to produce simulation traces in xml-format.
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

public class XMLTraceOutput extends XMLOutput {

    /** the root node of the document * */
    private Element trace;

    private int noteNo = 1;

    /** create a new XMLTraceOutput class * */
    public XMLTraceOutput() {
    }

    /***************************************************************************
     * method to be called when a Message is received.
     *
     * @param m
     *            Message: The Message that has been send.
     **************************************************************************/
    public void receive(Message m) {
        if (m == null || !(m instanceof TraceNote)) {
            return;
        }
        TraceNote traceNote = (TraceNote) m;
        /**
         * When the first TraceNote is inserted the model and experiment
         * parameters of trace are set aswell*
         */
        if (noteNo == 1) {
            trace = document.createElement("trace");
            trace.setAttribute("experiment", m.getExperimentName());
            document.appendChild(trace);
            noteNo++;
        }
        Element note = document.createElement("note");
        note.setAttribute("modeltime", traceNote.getTime());
        Element model = document.createElement("model");
        Text modelText = document.createTextNode(traceNote.getModelName());
        model.appendChild(modelText);
        note.appendChild(model);
        if (!traceNote.getEvent().equals("----")) {
            Element event = document.createElement("event");
            Text eventText = document.createTextNode(traceNote.getEvent());
            event.appendChild(eventText);
            note.appendChild(event);
        }
        if (!traceNote.getEntity().equals("----")) {
            Element entity = document.createElement("entity");
            Text entityText = document.createTextNode(traceNote.getEntity());
            entity.appendChild(entityText);
            note.appendChild(entity);
        }
        Element actions = document.createElement("actions");
        Text actionsText = document.createTextNode(traceNote.getDescription());
        actions.appendChild(actionsText);
        note.appendChild(actions);
        trace.appendChild(note);
    }

    /***************************************************************************
     * method to be called when a reporter is received reporters will not be
     * handled by this class so it will simply return
     *
     * @param r
     *            Reporter: the Reporter that has been send
     **************************************************************************/
    public void receive(Reporter r) {
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
        super.open(createFileName(pathname, name, "trace"));
    }
}