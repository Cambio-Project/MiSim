package desmoj.extensions.visualization2d.engine.command;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import desmoj.extensions.visualization2d.engine.model.Model;
import desmoj.extensions.visualization2d.engine.model.ModelException;
import desmoj.extensions.visualization2d.engine.viewer.SimulationTime;


/**
 * Basic methods for cmds-file generation
 *
 * @author christian.mueller@th-wildau.de For information about subproject: desmoj.extensions.visualization2d please
 *     have a look at: http://www.th-wildau.de/cmueller/Desmo-J/Visualization2d/
 *     <p>
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 *     with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *     <p>
 *     Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *     the specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 */
public abstract class WriteCmds {

    protected URL iconPathURL = null;
    protected String cmdFileName;
    protected String logFileName;
    private Model model = null;
    private PrintWriter cmdWriter = null;
    private PrintWriter logWriter = null;
    private CommandSequence cmdSeq = null;
    private int flushSeq = 1000;

    /**
     * Opens cmds-file and log-file and organize the animation.model.Model instance (singelton)
     *
     * @param cmdFileName
     * @param logFileName
     */
    protected WriteCmds(String cmdFileName, String logFileName, URL simulationIconDir) {

        try {
            this.cmdFileName = java.net.URLDecoder.decode(cmdFileName, "UTF-8");
            this.logFileName = java.net.URLDecoder.decode(logFileName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();  // should always work (UFT-8 should be supported by all environments) 
        }

        try {
            this.cmdWriter = new PrintWriter(new BufferedWriter(new FileWriter(this.cmdFileName)));
            this.logWriter = new PrintWriter(new BufferedWriter(new FileWriter(this.logFileName)));
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        this.iconPathURL = simulationIconDir;
        if (this.iconPathURL == null) {
            throw new CommandException("IconPathURL is null", "");
        }
        this.model = new Model(this.iconPathURL, null, null);
        this.model.createModelGrafic();
        this.model.setSimulationTime(new SimulationTime(0, 1, 1.0, null, null));
        this.cmdSeq = new CommandSequence(this.model, this.logWriter);
        this.flushSeq = 1000;  // Default value
    }

    /**
     * get a CommandSequence Object to read and write cmdFile
     *
     * @return
     */
    protected CommandSequence getCommandSequence() {
        return this.cmdSeq;
    }

    /**
     * sets the number of comands, when CommandFrame.writeCmdBuffer is flushed in cmdFile. Default is 1000
     *
     * @param flushSeq
     */
    protected void setFlushSeq(int flushSeq) {
        this.flushSeq = flushSeq;
    }

    /**
     * write a command in CommandFrame.writeCmdBuffer. When the buffer has more than this.flushSeq comands, the buffer
     * is flushed in cmdFile
     *
     * @param c Command to write
     */
    public void write(Command c) {
        int seqLength = this.getCommandSequence().write(c);
        if (seqLength > this.flushSeq) {
            this.flush();
        }
    }


    /**
     * flush CommandFrame.writeCmdBuffer into cmdWriter, seted by Constructor
     */
    protected void flush() {
        this.cmdSeq.flush(this.cmdWriter);
    }

    /**
     * close cmdWriter and logWriter
     */
    protected void close() {
        this.cmdSeq.flush(this.cmdWriter);
        this.cmdWriter.close();
        this.logWriter.close();
    }

    /**
     * checks a command c by syntax (c.syntaxCheck) and by semantic (c.execute()). Also a log-file is written.
     *
     * @param c Command to check
     */
    protected void checkAndLog(Command c) {
        this.logWriter.println(c.toString());
        this.logWriter.flush();
        try {
            //macht Syntax Check
            c.syntaxCheck();
            //macht Semantik Check (noch nicht Vollstaendig)
            c.execute(this.model);
        } catch (CommandException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            e.printStackTrace(this.logWriter);
            this.logWriter.flush();
            System.exit(1);
        } catch (ModelException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            e.printStackTrace(this.logWriter);
            this.logWriter.flush();
            System.exit(1);
        }
    }

    protected void setIconPathURL(URL url) {
        this.iconPathURL = url;
    }


}
