package desmoj.extensions.visualization2d.engine.command;

/**
 * Interface with some constants used in cmds-files. This constants are described also in
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
public interface Cmd {

    /**
     * Every command must end with this sign.
     */
    char COMMAND_SEPARATOR = ';';

    /**
     * This sign must be between two command-parameters
     */
    char PARAMETER_SEPARATOR = ',';

    /**
     * Every Parameter has a name and a value. This sign separates name and value.
     */
    char KEY_VALUE_SEPARATOR = ':';

    /**
     * In some parameters the value have some parts (e.g. coordinates). This sign separate this parts.
     */
    char VALUE_SEPARATOR = '|';

    /**
     * Name of command-parameter
     */
    String COMMAND_KEY = "cmd";

    /**
     * Name of time parameter
     */
    String TIME_KEY = "time";

    /**
     * Name of command-parameter.
     */
    String INIT_KEY = "init";
    //public static final String	INIT_TIME_VALUE		= "init";

    /**
     * Name value of last command.
     */
    String END_CMD = "end";

    /**
     * Name of remark parameter. This parameter isn't processed.
     */
    String REMARK_KEY = "remark";


    char REPLACE_CHAR = ' ';
    //public static final char	REPLACE_CHAR			= '\uc2a4';

    /**
     * Name of name- parameter and attribute of an entity. The name-attribute is the standard-attribute to store the
     * name of an entity. When this value is stored in an other attribute, then the name of this attribute is stored in
     * the name-parameter. The viewer shows an entity with his name. When an entity hasn't a name, the viewer extend a
     * name from its id.
     * <p>
     * For lists, routes and processes, there is only a name-parameter with a name as value.
     */
    String NAME_KEY = "name";

    /**
     * Name of velocity- parameter and attribute of an entity. The velocity-attribute is the standard-attribute to store
     * the velocity of an entity on a route. When this value is stored in an other attribute, then the name of this
     * attribute is stored in the velocity-parameter.
     */
    String VELOCITY_KEY = "velocity";

    /**
     * Name of priority- parameter and attribute of an entity. The priority-attribute is the standard-attribute to store
     * the priority of an entity on a list. When this value is stored in an other attribute, then the name of this
     * attribute is stored in the priority-parameter.
     */
    String PRIORITY_KEY = "priority";


}
