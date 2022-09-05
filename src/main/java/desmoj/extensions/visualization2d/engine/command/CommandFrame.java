package desmoj.extensions.visualization2d.engine.command;

import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Iterator;

import desmoj.extensions.visualization2d.engine.model.Model;
import desmoj.extensions.visualization2d.engine.model.ModelException;


/**
 * CommandFrame includes basic-methods for command-processing. In CommandSyntax describes the syntax of all
 * commands-types. Command is the only subclass of CommandFrame. The method Command.execute makes a semantic check and
 * execute a command.
 * <p>
 * For every command-type, commands with the same command-parameter-value, described in CommandSyntax, exists a
 * template, stored in a hashtable under it's name. When a command of this type is created, a clone of the assizioated
 * command-type is generated and filed with the parameters.
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
abstract class CommandFrame implements CommandSyntax, Cloneable {

    /**
     * Hashtable with command-templates. Key is the command-name.
     */
    private static Hashtable<String, CommandFrame> cmdTemplate;

    static {
        try {
            CommandFrame.init_cmdTemplate();
        } catch (CommandException e) {
            e.printStackTrace();
        }
    }

    private String cmd;
    private String remark;
    private Parameter[] parameter;
    private long time;
    private boolean template;
    private boolean init;

    /**
     * Constructor for CommandFrame, used for template generation
     *
     * @param cmd       command-name
     * @param remark    remark about template
     * @param parameter Array of Parameter's where each parameter must have a different type
     */
    public CommandFrame(String cmd, String remark, Parameter[] parameter) {
        this.cmd = cmd;
        this.remark = remark;
        this.parameter = parameter;
        this.time = Long.MIN_VALUE;
        this.template = true;
        this.init = true;
    }

    /**
     * Reads CommandSyntax, creates for each a templeate and store them in cmdTemplate
     *
     * @throws CommandException
     */
    public static void init_cmdTemplate() throws CommandException {
        // Initialisierung der cmdTemplate
        //System.out.println("Command.init_cmdTemplate start");
        cmdTemplate = new Hashtable<String, CommandFrame>();
        for (int i = 0; i < CMD_SYNTAX.length; i++) {
            String cmd = CMD_SYNTAX[i][0];
            int l = CMD_SYNTAX[i].length;
            String remark = CMD_SYNTAX[i][l - 1];
            int anzParameter = 0;
            if (l % 2 == 0) {
                anzParameter = (l - 2) / 2;
            } else {
                anzParameter = 0;
                throw new CommandException("CommandFrame.init_cmdTemplate : Parameterfehler", "cmd: " + cmd);
            }
            Parameter[] parameter = new Parameter[anzParameter];
            for (int j = 0; j < anzParameter; j++) {
                String quantifier = CMD_SYNTAX[i][2 * j + 1];
                String type = CMD_SYNTAX[i][2 * j + 2];

                // + heisst 1 .. unendlich
                if (quantifier.equals("+")) {
                    parameter[j] = new Parameter(type, 1, Integer.MAX_VALUE);
                }

                // * heisst 0 .. unendlich
                else if (quantifier.equals("*")) {
                    parameter[j] = new Parameter(type, 0, Integer.MAX_VALUE);
                }

                // ? heisst optional d.h. 0 .. 1
                else if (quantifier.equals("?")) {
                    parameter[j] = new Parameter(type, 0, 1);
                }

                // x..y means all values between x and y
                else if (quantifier.indexOf("..") >= 0) {
                    int k = quantifier.indexOf("..");
                    try {
                        int len = quantifier.length();
                        if (quantifier.length() > k + 2) {
                            //System.out.println(quantifier.substring(0, k)+"xx"+quantifier.substring(k+2)+"yy"+len);
                            int min = Integer.parseInt(quantifier.substring(0, k));
                            int max = Integer.parseInt(quantifier.substring(k + 2));
                            if (0 <= min && min <= max) {
                                parameter[j] = new Parameter(type, min, max);
                            } else {
                                throw new CommandException("CommandFrame.init_cmdTemplate : Quantifier is not valid",
                                    "cmd: " + cmd + " quantifier: " + quantifier + " type: " + type);
                            }
                        } else {
                            throw new CommandException("CommandFrame.init_cmdTemplate : Quantifier is not valid",
                                "cmd: " + cmd + " quantifier: " + quantifier + " type: " + type);
                        }
                    } catch (NumberFormatException e) {
                        throw new CommandException("CommandFrame.init_cmdTemplate : Quantifier is not valid",
                            "cmd: " + cmd + " quantifier: " + quantifier + " type: " + type);
                    }
                }

                // + heisst feste Anzahl
                else {
                    try {
                        int q = Integer.parseInt(quantifier);
                        if (q >= 0) {
                            parameter[j] = new Parameter(type, q, q);
                        } else {
                            parameter[j] = null;
                            throw new CommandException("CommandFrame.init_cmdTemplate : Quantifier ist negativ",
                                "cmd: " + cmd + " quantifier: " + quantifier + " type: " + type);
                        }
                    } catch (NumberFormatException e) {
                        throw new CommandException("CommandFrame.init_cmdTemplate: Quantifier ist kein int",
                            "cmd: " + cmd + " quantifier: " + quantifier + " type: " + type);
                    }
                }
            }
            cmdTemplate.put(cmd, CommandFactory.createCommand(cmd, remark, parameter));
        }
        //System.out.println("Command.init_cmdTemplate end");
    }

    /**
     * checks if a command with name cmd in cmdTemplate exist.
     *
     * @param cmd
     * @return true, if one exist
     */
    public static boolean existCommand(String cmd) {
        return cmdTemplate.containsKey(cmd);
    }

    /**
     * Get a clone of command-template with name cmd for init-phase
     *
     * @param cmd
     * @return clone of command-template with name
     * @throws CommandException
     */
    public static CommandFrame getCommandInit(String cmd, long initTime) throws CommandException {
        CommandFrame out = null;
        CommandFrame command = cmdTemplate.get(cmd);
        if (command == null) {
            throw new CommandException("CommandFrame.getTemplateInit: Command is not in CommandFrame.cmdTemplate",
                "cmd: " + cmd);
        }
        try {
            out = command.clone();
            out.setInit(initTime);
            out.setNoTemplate();
            out.setRemark(" ");
        } catch (CloneNotSupportedException e) {
            throw new CommandException("CommandFrame.getTemplateInit: Error by cloning of command", command.toString());
        }
        return out;
    }

    /**
     * Get a clone of command-template with name cmd for run-phase time-value
     *
     * @param cmd
     * @param time
     * @return clone of command-template
     * @throws CommandException
     */
    public static CommandFrame getCommandTime(String cmd, long time) throws CommandException {
        CommandFrame out = null;
        CommandFrame command = cmdTemplate.get(cmd);
        if (command == null) {
            throw new CommandException("CommandFrame.getTemplateTime: Command is not in CommandFrame.cmdTemplate",
                "cmd: " + cmd);
        }
        try {
            out = command.clone();
            out.setTime(time);
            out.setNoTemplate();
            out.setRemark(" ");
        } catch (CloneNotSupportedException e) {
            throw new CommandException("CommandFrame.getTemplateTime: Error by cloning of command", command.toString());
        }
        return out;
    }

    /**
     * Makes a syntax-check for cmd-string and create a CommandFrame of parsed cmd-string
     *
     * @param cmd
     * @return created CommandFrame
     * @throws CommandException
     */
    public static CommandFrame parseCommand(String cmd) throws CommandException {
        //System.out.println("Command.parseCommand start");
        CommandFrame command = null;
        String time = null;
        String init = null;
        String cmdTmp = cmd;
        int tokenPos = -1;
        String part, key, value;

        //parsen
        do {
            tokenPos = cmdTmp.indexOf(Cmd.PARAMETER_SEPARATOR);
            if (tokenPos == -1) {
                part = cmdTmp;
                cmdTmp = "";

            } else {
                part = cmdTmp.substring(0, tokenPos);
                cmdTmp = cmdTmp.substring(tokenPos + 1);
            }
            // parsen des Parameters
            tokenPos = part.indexOf(Cmd.KEY_VALUE_SEPARATOR);
            if (tokenPos != -1) {
                key = part.substring(0, tokenPos).trim();
                value = part.substring(tokenPos + 1).trim();
                if ((command == null) && (key.equals(Cmd.TIME_KEY))) {
                    time = value;
                } else if ((command == null) && (key.equals(Cmd.INIT_KEY))) {
                    init = value;
                } else if ((command == null) && (key.equals(Cmd.COMMAND_KEY))) {
                    if ((time != null) && (init != null)) {
                        if (Boolean.parseBoolean(init)) {
                            command = CommandFrame.getCommandInit(value, Long.parseLong(time));
                        } else {
                            command = CommandFrame.getCommandTime(value, Long.parseLong(time));
                        }
                    } else {
                        //throw new CommandException("CommandFrame.parseCommand Cmd Syntax is: time - init - cmd", cmd);
                    }
                } else if ((command != null) && (key.equals(Cmd.REMARK_KEY))) {
                    command.setRemark(value);
                } else if ((command != null)) {
                    command.addParameter(key, value);
                }
            } else {
                throw new CommandException(
                    "CommandFrame.parseCommand Parameter \"" + part + "\" of Command include no \"" +
                        Cmd.KEY_VALUE_SEPARATOR + "\" Separator", cmd);
            }
        } while (!cmdTmp.equals(""));
        //System.out.println("Command.parseCommand end");
        if (command == null) {
            System.out.println("cmd it null " + cmd);
        }
        return command;
    }

    /**
     * Write a information-string with all existing command-templates
     *
     * @return information-string
     * @throws CommandException
     */
    public static String writeTemplates() throws CommandException {
        String out = "";
        String nl = System.getProperty("line.separator");
        out += "---- Content of CommandFrame.cmdTemplate ------------------" + nl;
        Iterator<String> it = CommandFrame.cmdTemplate.keySet().iterator();
        int i = 0;
        while (it.hasNext()) {
            String cmd = it.next();
            out += CommandFrame.cmdTemplate.get(cmd).toString() + nl;
        }
        out += "-----------------------------------------------------------" + nl;
        return out;
    }

    /**
     * execute command, definied in Command
     *
     * @param model
     * @throws CommandException
     * @throws ModelException
     */
    abstract void execute(Model model) throws CommandException, ModelException;

    /**
     * Geter of command-name (command-type)
     *
     * @return command-name
     */
    public String getCmd() {
        return this.cmd;
    }

    /**
     * Geter of command-remark
     *
     * @return remark-string
     */
    public String getRemark() {
        return this.remark;
    }

    /**
     * Setter of command-remark
     *
     * @param remark
     */
    public void setRemark(String remark) {
        this.remark = remark;
    }

    /**
     * Get's i.th parameter from command's parameter-array
     *
     * @param i
     * @return i.th parameter, null if no exist
     */
    public Parameter getParameter(int i) {
        Parameter out = null;
        if (i < this.parameter.length) {
            out = this.parameter[i];
        }
        return out;
    }

    /**
     * Get's the parameter of type from command's parameter-array The parameter-type is unique in parameter-array
     *
     * @param type
     * @return parameter if one found, null otherwise
     */
    public Parameter getParameterType(String type) {
        Parameter out = null;
        for (int i = 0; i < this.parameter.length; i++) {
            if (this.parameter[i].getTyp().equals(type)) {
                out = this.parameter[i];
                break;
            }
        }
        return out;
    }

    /**
     * Add a parameter in commands parameter-array
     *
     * @param key   parameter-type
     * @param value parameter-value
     * @throws CommandException, when parameter-type is not valid (why no quantifier check????)
     */
    public void addParameter(String key, String value) throws CommandException {
        Parameter p = this.getParameterType(key);
        if (p == null) {
            throw new CommandException("CommandFrame.addParameter key: " + key + " is not valid.", this.toString());
        }
        p.addValue(value);
    }

    /**
     * Add a parameter with value-compnents (e.g. coordinates) in commands parameter-array
     *
     * @param key   parameter-type
     * @param value parameter-components
     * @throws CommandException, when parameter-type is not valid
     */
    public void addParameterCat(String key, String[] value) throws CommandException {
        Parameter p = this.getParameterType(key);
        if (p == null) {
            throw new CommandException("CommandFrame.addParameter key: " + key + " is not valid.", this.toString());
        }
        p.addValueCat(value);
    }

    /**
     * Get the i.th parameter-value of type key. The component of the value are splited in an array
     *
     * @param key
     * @param i
     * @return Array of value-components
     * @throws CommandException
     */
    public String[] getParameterSplit(String key, int i) throws CommandException {
        Parameter p = this.getParameterType(key);
        if (p == null) {
            throw new CommandException("CommandFrame.addParameter key: " + key + " is not valid.", this.toString());
        }
        return p.getValueSplit(i);
    }

    /**
     * when set this, this command is no template
     */
    public void setNoTemplate() {
        this.template = false;
    }

    /**
     * check if command belongs to init-phase
     *
     * @return true when command in init-phase
     */
    public boolean isInit() {
        return this.init;
    }

    /**
     * when set this, command belongs to init-phase
     */
    public void setInit(long startTime) {
        this.init = true;
        //this.time	= Long.MIN_VALUE;
        this.time = startTime;
    }

    /**
     * check if command is a template
     *
     * @return true when template
     */
    public boolean isTempleate() {
        return this.template;
    }

    /**
     * get's time-value of command, makes sense only in run-phase
     *
     * @return time-value, stored in CommandFrame
     */
    public long getTime() {
        return this.time;
    }

    /**
     * when set this, command belongs to run-phase with time-value time
     *
     * @param time
     */
    public void setTime(long time) {
        this.init = false;
        this.time = time;
    }

    /**
     * Makes a syntax-check of command
     *
     * @return result of check
     * @throws CommandException
     */
    public boolean syntaxCheck() throws CommandException {
        boolean out = true;
        if (!CommandFrame.existCommand(this.cmd)) {
            out = false;
            throw new CommandException("CommandFrame.syntaxCheck: cmd not exist", "cmd: " + this.cmd);
        } else {
            //Parameter Check
            for (int i = 0; i < this.parameter.length; i++) {
                Parameter p = this.parameter[i];
                if (!(p.getMin() <= p.getValues().length)) {
                    out = false;
                    throw new CommandException("CommandFrame.syntaxCheck Not enough parameter of type: " + p.getTyp(),
                        this.toString());
                }
                if (!(p.getValues().length <= p.getMax())) {
                    out = false;
                    throw new CommandException("CommandFrame.syntaxCheck To much parameter of type: " + p.getTyp(),
                        this.toString());
                }
            }
        }
        return out;
    }

    /**
     * Creates an information-string of command, used for log-file
     */
    public String toString() {
        String out = "";
        String nl = System.getProperty("line.separator");
        out += "command: " + this.getCmd() + nl;
        out += "         remark     : " + this.getRemark() + nl;
        for (int j = 0; this.getParameter(j) != null; j++) {
            Parameter p = this.getParameter(j);
            if (p != null) {
                String max = (p.getMax() == Integer.MAX_VALUE ? "inf" : Integer.toString(p.getMax()));
                out +=
                    "         parameter  : " + p.getTyp() + " possibleCounts: " + p.getMin() + ".." + max + " values: ";
                for (int i = 0; i < p.getValues().length; i++) {
                    out += p.getValues()[i] + ", ";
                }
                out += nl;
            } else {
                out += "         Parameter  : ist null (Error)" + nl;
            }
        }
        out += "         isInit     : " + this.isInit() + nl;
        out += "         time       : " + this.getTime() + nl;
        out += "         isTempleate: " + this.isTempleate() + nl;
        return out;
    }

    /**
     * creates a clone of a command
     */
    public CommandFrame clone() throws CloneNotSupportedException {
        //System.out.println("Command.clone start");
        CommandFrame out = CommandFactory.createCommand(this.cmd, this.remark, this.parameter);
        out.cmd = this.cmd;
        out.remark = this.remark;
        out.parameter = this.parameter.clone();
        for (int i = 0; i < out.parameter.length; i++) {
            out.parameter[i] = out.parameter[i].clone();
        }
        out.time = this.time;
        out.template = this.template;
        out.init = this.init;
        //System.out.println("Command.clone end");
        return out;
    }

    /**
     * Write a command on PrintWriter, used in flush
     *
     * @param f
     */
    protected void write(PrintWriter f) {
        //public String write(){
        String timeForm = "%-20s";
        String initForm = "%-15s";
        String cmdForm = "%-30s";
        String form = "%-40s";
        String out = "";
        String p = "";
        // write time
        if (this.isInit()) {
            p = Cmd.TIME_KEY + Cmd.KEY_VALUE_SEPARATOR + this.getTime() + Cmd.PARAMETER_SEPARATOR;
            out += String.format(timeForm, p);
            p = Cmd.INIT_KEY + Cmd.KEY_VALUE_SEPARATOR + true + Cmd.PARAMETER_SEPARATOR;
            out += String.format(initForm, p);
        } else {
            p = Cmd.TIME_KEY + Cmd.KEY_VALUE_SEPARATOR + this.getTime() + Cmd.PARAMETER_SEPARATOR;
            out += String.format(timeForm, p);
            p = Cmd.INIT_KEY + Cmd.KEY_VALUE_SEPARATOR + false + Cmd.PARAMETER_SEPARATOR;
            out += String.format(initForm, p);
        }
        // write cmd
        p = Cmd.COMMAND_KEY + Cmd.KEY_VALUE_SEPARATOR + this.getCmd() + Cmd.PARAMETER_SEPARATOR;
        out += String.format(cmdForm, p);
        // write parameter
        for (int j = 0; this.getParameter(j) != null; j++) {
            Parameter parameter = this.getParameter(j);
            if (parameter != null) {
                for (int i = 0; i < parameter.getValues().length; i++) {
                    p = parameter.getTyp() + Cmd.KEY_VALUE_SEPARATOR + parameter.getValues()[i] +
                        Cmd.PARAMETER_SEPARATOR;
                    out += String.format(form, p);
                }
            }
        }
        // write remark
        out += Cmd.REMARK_KEY + Cmd.KEY_VALUE_SEPARATOR + this.getRemark() + Cmd.COMMAND_SEPARATOR;
        f.println(out);
    }


}
