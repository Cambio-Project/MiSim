package desmoj.extensions.visualization2d.engine.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import desmoj.extensions.visualization2d.engine.model.Model;
import desmoj.extensions.visualization2d.engine.model.ModelException;


/**
 * Class to read and write cmdfiles
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
public class CommandSequence {

    private PrintWriter logFile = null;
    private Model model = null;

    // werden zum mehrmaligen Aufruf von read benoetigt
    private List<Command> readCmdBuffer;
    private String readCmd;
    private String readCmdTmp;
    private List<Command> writeCmdBuffer;


    /**
     * Class to read and write cmdfiles
     *
     * @param model   Model used to execute commands
     * @param logFile LogFile, may be null
     */
    public CommandSequence(Model model, PrintWriter logFile) {
        this.model = model;
        this.logFile = logFile;
        this.readCmdBuffer = new LinkedList<Command>();
        this.readCmd = "";
        this.readCmdTmp = "";
        this.writeCmdBuffer = new LinkedList<Command>();

    }

    /**
     * Reads all commands in init-phase from reader f and execute them (Command.execute)
     *
     * @param f Reader with cmds-file
     * @return Time-value of first cmd in run-phase
     * @throws CommandException
     * @throws ModelException
     * @throws IOException
     */
    public long readInit(BufferedReader f) throws CommandException, ModelException, IOException {
        // initalize of Data-Structure
        this.readCmdBuffer = new LinkedList<Command>();
        this.readCmd = "";
        this.readCmdTmp = "";
        this.writeCmdBuffer = new LinkedList<Command>();
        return this.read(f, true, Long.MIN_VALUE);
    }

    /**
     * Reads the next commands in reader f until time-value > time and execute them (Command.execute)
     *
     * @param f
     * @param time
     * @return Time-value of next not readed command
     * @throws CommandException
     * @throws ModelException
     * @throws IOException
     */
    public long readUntilTime(BufferedReader f, long time) throws CommandException, ModelException, IOException {
        return this.read(f, false, time);
    }

    /**
     * liest Commandfile, und fuehrt Commands aus. Commands werden u.U. in Command.cmdBuffer zwischengespeichert.
     *
     * @param f    liest aus BufferedReader f
     * @param init Es werden nur init Commands ausgefuehrt.
     * @param time Es werden alle Commands zwischen dem letzten Aufruf und bis einschliesslich time ausgefuehrt.
     * @return time des naechsten Commands
     */
    private long read(BufferedReader f, boolean init, long time) throws CommandException, ModelException, IOException {
        //System.out.println("Command.read start");
        boolean condition = false;
        String line = "";
        int pos = 0;
        boolean eof = false;
        Command last = null;
        int cmdCount = 0;
        if (!this.readCmdBuffer.isEmpty()) {
            last = this.readCmdBuffer.get(this.readCmdBuffer.size() - 1);
        }

        // Pruefe ob schon alle Commands bis einschl. time eingelesen sind
        if (init || last == null || (last.getTime() <= time)) {
            //System.out.println("einlesen");
            // Commands in Command.readCmdBuffer einfuegen, bis ein nicht init Command erreicht wurde
            do {
                // einlesen bis Command.readCmdTmp mindestens ein Command enthaelt
                do {
                    line = f.readLine();
                    if (line == null) {
                        // at end of file, we insert an end command.
                        line = Cmd.TIME_KEY + Cmd.KEY_VALUE_SEPARATOR + Long.MAX_VALUE + Cmd.PARAMETER_SEPARATOR +
                            Cmd.INIT_KEY + Cmd.KEY_VALUE_SEPARATOR + false + Cmd.PARAMETER_SEPARATOR +
                            Cmd.COMMAND_KEY + Cmd.KEY_VALUE_SEPARATOR + Cmd.END_CMD + Cmd.COMMAND_SEPARATOR + "  ";
                    }
                    this.readCmdTmp += line;
                    pos = this.readCmdTmp.indexOf(Cmd.COMMAND_SEPARATOR);
                } while ((pos == -1));
                // alle Commands aus Command.readCmdTmp auslesen und
                // in Command.readCmdBuffer einfuegen
                do {
                    this.readCmd = this.readCmdTmp.substring(0, pos);
                    //System.out.println("pos: "+pos+" cmd: "+CommandFrame.readCmd);
                    Command c = (Command) Command.parseCommand(this.readCmd);
                    if (c == null) {
                        System.out.println("c ist null");
                    }
                    //System.out.println("time: "+c.getTime()+"  init: "+c.isInit()+" cmd: "+ c.getCmd());
                    this.readCmdBuffer.add(c);
                    this.readCmdTmp = this.readCmdTmp.substring(pos + 1);
                    pos = this.readCmdTmp.indexOf(Cmd.COMMAND_SEPARATOR);
                } while (pos != -1);
                last = this.readCmdBuffer.get(this.readCmdBuffer.size() - 1);
                if (last == null) {
                    System.out.println("last ist null");
                }
                if (init) {
                    condition = last.isInit() && !last.getCmd().equals(Cmd.END_CMD);
                } else {
                    condition = (last.getTime() <= time) && !last.getCmd().equals(Cmd.END_CMD);
                }
                //System.out.println(condition);
            } while (condition);
        } else {
            //System.out.println("nicht einlesen");
        }

        //Command.readCmdBuffer abarbeiten
        cmdCount = 0;
        while (true) {
            //System.out.println("size: "+CommandFrame.readCmdBuffer.size()+"  time: "+time);
            //Command.cmdBuffer.firstElement().printCommand();
            if (init) {
                condition = this.readCmdBuffer.get(0).isInit();
            } else {
                condition = this.readCmdBuffer.get(0).getTime() <= time;
            }
            if (!condition) {
                break;
            }

            cmdCount++;
            CommandFrame command = this.readCmdBuffer.remove(0);
            if (this.logFile != null) {
                logFile.println(command.toString());
                logFile.flush();
            }
            if (!command.syntaxCheck()) {
                throw new CommandException("CommandFrame.read: SyntaxCheck failt", command.toString());
            }
            command.execute(this.model);
            if (command.getCmd().equals(Cmd.END_CMD)) {
                break;
            }
            //System.out.println("time: "+command.getTime()+"   nextTime: "+CommandFrame.readCmdBuffer.firstElement().getTime());
        }
        //System.out.println("size: "+this.readCmdBuffer.size()+"  time: "+time+"   cmdCount: "+cmdCount);
        //System.out.println("Command.read end   nextTime: "+CommandFrame.readCmdBuffer.firstElement().getTime());
        return this.readCmdBuffer.get(0).getTime();
    }

    /**
     * Write a command on internal writeCmdBuffer
     *
     * @param command
     * @return actual size of this buffer
     */
    public int write(Command command) {
        this.writeCmdBuffer.add(command);
        return this.writeCmdBuffer.size();
    }

    /**
     * Flush writeCmdBuffer to PrintWriter f
     *
     * @param f
     * @return nr written commands
     */
    public int flush(PrintWriter f) {
        int out = this.writeCmdBuffer.size();
        for (int i = 0; i < out; i++) {
            this.writeCmdBuffer.get(i).write(f);
        }
        this.writeCmdBuffer.clear();
        return out;
    }


}