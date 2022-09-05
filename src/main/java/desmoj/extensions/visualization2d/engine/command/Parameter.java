package desmoj.extensions.visualization2d.engine.command;

import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;


/**
 * Parameter of a Command Each parameter has a type (name) and can have between min and max values.
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
public class Parameter implements Cloneable {
    private String typ;
    private int min;
    private int max;
    private List<String> values;

    /**
     * Constructor
     *
     * @param typ Typ of Parameter
     * @param min Min number of possible values
     * @param max Max number of possible values
     */
    public Parameter(String typ, int min, int max) {
        this.typ = typ;
        this.min = min;    // minimale Anzahl von Parametern dieses Typs
        this.max = max;    // maximale Anzahl von Parametern dieses Typs
        this.values = new LinkedList<String>();
    }

    /**
     * Parameter-value-components v are connected with a value-separator to one string
     *
     * @param v
     * @return string with connected parameters
     * @throws CommandException, when v[i] contains a Cmd.VALUE_SEPARATOR
     */
    public static String cat(String[] v) throws CommandException {
        String out = "";
        for (int i = 0; i < v.length; i++) {
            if (v[i].equals("")) {
                v[i] = " ";
            }
            if (v[i].indexOf(Cmd.VALUE_SEPARATOR) != -1) {
                throw new CommandException("Parameter.cat  Value contains VALUE_SEPARATOR " + v[i], "");
            }
            out += v[i] + Cmd.VALUE_SEPARATOR;
        }
        return out.substring(0, Math.max(0, out.length() - 1));
    }

    /**
     * A String v is splited in to its parameter-value-components
     *
     * @param v
     * @return splitted string
     */
    public static String[] split(String v) {
        StringTokenizer st = new StringTokenizer(v, String.valueOf(Cmd.VALUE_SEPARATOR));
        String[] out = new String[st.countTokens()];
        int i = 0;
        while (st.hasMoreTokens()) {
            out[i] = st.nextToken();
            i++;
        }
        //System.out.println("split: "+v+" length: "+out.length);
        return out;
    }

    public static String replaceSyntaxSign(String value) {
        value = value.replace(Cmd.COMMAND_SEPARATOR, Cmd.REPLACE_CHAR);
        value = value.replace(Cmd.KEY_VALUE_SEPARATOR, Cmd.REPLACE_CHAR);
        value = value.replace(Cmd.PARAMETER_SEPARATOR, Cmd.REPLACE_CHAR);
        value = value.replace(Cmd.VALUE_SEPARATOR, Cmd.REPLACE_CHAR);
        return value;
    }

    /**
     * Get type of parameter
     *
     * @return type of parameter
     */
    public String getTyp() {
        return this.typ;
    }

    /**
     * get min number of possible values
     *
     * @return min number of possible values
     */
    public int getMin() {
        return this.min;
    }

    /**
     * get max number of possible values
     *
     * @return max number of possible values
     */
    public int getMax() {
        return this.max;
    }

    /**
     * get values of parameter
     *
     * @return array with parameter-values
     */
    public String[] getValues() {
        String[] out = new String[this.values.size()];
        for (int i = 0; i < this.values.size(); i++) {
            out[i] = this.values.get(i);
        }
        return out;
    }

    /**
     * add a value to a parameter
     *
     * @param v
     */
    public void addValue(String v) {
        this.values.add(v);
    }

    /**
     * add a value with value-component to a parameter
     *
     * @param v
     * @throws CommandException
     */
    public void addValueCat(String[] v) throws CommandException {
        this.values.add(Parameter.cat(v));
    }

    /**
     * get the i.th value from a parameter. It's components will be splited in a array
     *
     * @param i
     * @return i.th value of parameter-array
     */
    public String[] getValueSplit(int i) {
        return Parameter.split(this.values.get(i));
    }

    /**
     * create an clone of an parameter, used by CommandFrame.clone
     */
    public Parameter clone() throws CloneNotSupportedException {
        //System.out.println("Parameter.clone start");
        Parameter out = new Parameter(this.typ, this.min, this.max);
        out.typ = this.typ;
        out.min = this.min;
        out.max = this.max;
        out.values = new LinkedList<String>(this.values);
        //out.values	= new Vector<String>(this.value);
        //System.out.println("Parameter.clone end");
        return out;
    }
}
