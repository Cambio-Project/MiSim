package desmoj.extensions.experimentation.util;

import java.util.HashMap;

import desmoj.core.simulator.Model;
import desmoj.extensions.xml.util.DocumentReader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A class to run a batch of simulation experiments from the console. The batches must be specified in an XML file. The
 * XML file must start with a
 * <batch>tag. In the top level tag it is possible to specify model and
 * experiment settings valid for every run using the <model>and <exp>tag known from the Run class. Furthermore an
 * arbitrary number of runs (specified by a
 * <run>tag) containing the variable settings can be embedded.
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
public class BatchRunner implements Runnable {

    protected Document batchfile;

    protected HashMap defaultExpSettings = new HashMap();

    protected HashMap defaultModelParams = new HashMap();

    protected Model model = null;

    protected ExperimentRunner expRunner = null;

    protected String batchfilename = null;

    /**
     * Creates a new BatchRunner that loads the given batch file.
     *
     * @param batchfilename name of an (XML-)batchfile
     */
    public BatchRunner(String batchfilename) {
        try {
            this.batchfilename = batchfilename;
            this.batchfile = DocumentReader.getInstance()
                .readDoc(batchfilename);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Command line interface for the batch runner */
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out
                .println("Usage: java desmoj.util.BatchRunner <batchfile>.xml");
        } else {
            String filename = args[0];
            BatchRunner br = new BatchRunner(filename);
            br.run();
            // br.join();
        }
    }

    /** Runs the simulations specified in the assigned batch file */
    public void run() {
        System.out.println("*** DESMO-J Batch Runner ***");
        if (batchfile == null) {
            System.out
                .println("** ERROR: Batch runner cannot open batchfile. Exiting");
            return;
        } else {
            System.out.println("* Reading batch specification from "
                + batchfilename);

            // Get root node of document. Should have tag "batch"
            Element root = batchfile.getDocumentElement();

            Node settings = null, params = null;
            NodeList nl = root.getChildNodes();
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                if (n.getNodeName().equals("exp")) {
                    settings = n;
                }
                if (n.getNodeName().equals(model.toString())) {
                    params = n;
                }
            }

            if (settings != null) {
                Run.readParamList(settings, defaultExpSettings);
            }
            if (params != null) {
                Run.readParamList(params, defaultModelParams);
            }

            // Getting default model from node
            String defaultModelName = root.getAttribute("model");
            if (defaultModelName.equals("")) {
                defaultModelName = null;
            }

            String defaultExpRunnerName = root.getAttribute("expRunner");
            if (defaultExpRunnerName == null || defaultExpRunnerName.equals("")) {
                defaultExpRunnerName = "desmoj.extensions.experimentation.util.ExperimentRunner";
            }

            // Create the default model

            System.out.println("* Processing batch...\n");

            // Read list of runs
            NodeList runs = root.getChildNodes();
            int count = 0;
            for (int i = 0; i < runs.getLength(); i++) {
                HashMap expSettings = new HashMap(defaultExpSettings);
                HashMap modelParams = new HashMap(defaultModelParams);
                Node nextDesc = runs.item(i);
                if (nextDesc.getNodeName().equals("run")) {
                    count++;
                    System.out.println("* Initializing run no " + count);

                    try {

                        if (defaultModelName != null) {
                            model = (Model) Class.forName(defaultModelName)
                                .newInstance();
                        }
                        if (defaultExpRunnerName != null) {
                            expRunner = (ExperimentRunner) Class.forName(
                                defaultExpRunnerName).newInstance();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Run nextRun = new Run(model, expRunner, expSettings,
                        modelParams, count);
                    nextRun.readFromNode((Element) nextDesc);
                    ExperimentRunner er = nextRun.getExperimentRunner();
                    long startMillis = System.currentTimeMillis();
                    er.start();
                    try {
                        Thread t = er.getThread();
                        t.join();
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException("Cannot access experiment runner thread.");
                    }
                    long stopMillis = System.currentTimeMillis();
                    System.out.println("* Simulation took "
                        + (stopMillis - startMillis) / 1000.0
                        + " seconds.\n");
                }
            }
            System.out.println("* Batch completed.");
        }
    }
}