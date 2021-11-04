package desmoj.extensions.experimentation.util;

import java.util.HashMap;
import java.util.Map;

import desmoj.core.simulator.Model;
import desmoj.extensions.xml.util.DocumentReader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A class representing a parametrized simulation run. The parameters can be read from an XML file. The XML file must
 * start with a <run>tag. This tag contains model and experiment parameter specifications (tags <model>and
 * <exp>). Each parameter specification is stated in the form <param name="..."
 * value="..."/>.
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
public class Run {

    public static final String UNDEFINED = "UNDEF";

    private Model model;

    private ExperimentRunner expRunner;

    private final Map modelParams;

    private final Map expSettings;

    private final int number;

    /**
     * Creates a new run. This constructor is mainly called from the class BatchRunner.
     *
     * @param defaultModel       the model to run an experiment with
     * @param defaultExpRunner   the experiment runner used for running the experiment
     * @param defaultExpSettings the experiment parameter settings
     * @param defaultModelParams the model parameter settings
     * @param number             internal number of the run (only important if used with batches)
     */
    public Run(Model defaultModel, ExperimentRunner defaultExpRunner,
               Map defaultExpSettings, Map defaultModelParams, int number) {

        this.model = defaultModel;
        this.expRunner = defaultExpRunner;
        this.expSettings = defaultExpSettings;
        this.modelParams = defaultModelParams;
        this.number = number;
    }

    /**
     * Creates a new run without changing model and experiment parameter settings.
     *
     * @param m   the model to run an experiment with
     * @param exp the experiment runner used for running the experiment.
     */
    public Run(Model m, ExperimentRunner exp) {
        this(m, exp, new HashMap(), new HashMap(), 0);
    }

    /** Creates a new empty run. */
    public Run() {
        this(null, null);
    }

    /**
     * Reads a parameter list from a DOM node to a java.util.Map. In the map each setting is stored as a key (name of
     * the parameter) and value pair.
     *
     * @param src a DOM node containing XML based parameter settings
     * @param dst a java.uztil.Map to copy the parameter settings to.
     */
    public static void readParamList(Node src, Map dst) {
        NodeList l = src.getChildNodes();
        for (int i = 0; i < l.getLength(); i++) {
            Node n = l.item(i);
            String name = null, value = null;
            System.out.println(n);
            if (n instanceof Element) {
                Element e = (Element) n;
                name = e.getAttribute("name");
                value = e.getAttribute("value");
                // System.out.println("Read " + name + " = " + value);
            }
            if (value == null || value.equals("")) {
                value = UNDEFINED;
            }
            if (name == null || name.equals("")) {
                name = UNDEFINED;
            }
            if (name != UNDEFINED) {
                dst.put(name, value);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out
                .println("Usage: java desmoj.util.Run <parameterfile>.xml");
        } else {
            System.out.println("*** DESMO-J Simulation Runner ***");
            System.out.println("* Reading experiment specification from "
                + args[0]);
            Document d = DocumentReader.getInstance().readDoc(args[0]);
            Run run = new Run();
            run.readFromNode(d.getDocumentElement());
            ExperimentRunner runner = run.getExperimentRunner();
            long startMillis = System.currentTimeMillis();
            runner.start();
            long stopMillis = System.currentTimeMillis();
            System.out.println("* Simulation took "
                + (stopMillis - startMillis) / 1000.0 + " seconds.\n");
        }
    }

    /** @return the model an experiment is run with */
    public Model getModel() {
        return model;
    }

    /**
     * Reads a run from a DOM node representing an XML parameter file.
     *
     * @param n the DOM node to read the run from
     */
    public void readFromNode(Element e) {
        String mclass = null, expRunnerClass = null;
        mclass = e.getAttribute("model");
        expRunnerClass = e.getAttribute("expRunner");
        if (mclass == "") {
            mclass = null;
        }
        if (expRunnerClass == "") {
            expRunnerClass = null;
        }

        try {
            if (mclass != null) {
                model = (Model) Class.forName(mclass).newInstance();
            }
            if (expRunnerClass != null) {
                expRunner = (ExperimentRunner) Class.forName(expRunnerClass)
                    .newInstance();
            } else if (expRunner == null) {
                expRunner = createDefaultExperimentRunner();
                System.out
                    .println("** WARNING: No ExperimentRunner specified. Using desmoj.util.ExperimentRunner");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            model = null;
            expRunner = null;
        }

        Node settings = null, params = null;
        NodeList nl = e.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeName().equals("exp")) {
                settings = n;
            }
            if (n.getNodeName().equals("model")) {
                params = n;
            }
        }

        if (settings != null) {
            readParamList(settings, expSettings);
        }

        if (params != null) {
            readParamList(params, modelParams);
        }
    }

    /**
     * Creates a default desmoj.util.ExperimentRunner. This method might be overridden by subclasses.
     *
     * @return the experiment runner
     */
    protected ExperimentRunner createDefaultExperimentRunner() {
        return new ExperimentRunner();
    }

    /** @return the experiment runner used to run this experiment */
    public ExperimentRunner getExperimentRunner() {
        if (model == null) {
            throw new RuntimeException(
                "** ERROR: Cannot start simulation run. No model specified.");
        } else {
            expRunner.setModel(model);

            // Make exp name
            String expname = (String) expSettings.get("name");
            if (expname == null) {
                expname = model.getName() + "Experiment" + "_"
                    + number;
                expSettings.put("name", expname);
            }

            expRunner.initParameters(expSettings, modelParams);
            return expRunner;
        }
    }
}