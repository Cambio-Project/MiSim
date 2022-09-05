package desmoj.extensions.experimentation.reflect;

import java.lang.reflect.Field;

import desmoj.core.util.MutableAccessPoint;

/**
 * A mutable access point using reflection to get and set a field's value.
 * <p>
 * This pattern can be found in several frameworks for agent- or component-based simulation (e.g. 'Probes' in Swarm -
 * see www.swarm.org). The term 'access point' was adopted from the dissertation "Ein flexibler, CORBA-basierter Ansatz
 * fuer die verteilte, komponentenorientierte Simulation" by Ralf Bachmann (2003).
 *
 * @author Nicolas Knaak
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 *     <p>
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 *     with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *     <p>
 *     Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *     the specific language governing permissions and limitations under the License.
 */
public class MutableFieldAccessPoint extends FieldAccessPoint implements
    MutableAccessPoint {

    // ///////////// KONSTRUKTOREN ///////////////////////////////////

    /**
     * Creates a new MutableFieldAccessPoint for the given field. The name is equal to the field name.
     *
     * @param field    the field to access
     * @param accessed the accessed object
     */
    public MutableFieldAccessPoint(Field field, Object accessed) {
        super(field, accessed);
    }

    /**
     * Creates a new MutableFieldAccessPoint for the field specified by the given field name and declaring class. The
     * access point might have a name different from the field name.
     *
     * @param fieldName the name of the field to access
     * @param name      the name of the access point
     * @param accessed: The inspected object
     */
    public MutableFieldAccessPoint(String name, String fieldName,
                                   Object accessed) {
        super(name, fieldName, accessed);
    }

    /**
     * Creates a new MutableFieldAccessPoint for the field specified by the given field name and object. The access
     * point's name is equal to the field name.
     *
     * @param fieldName the name of the field to access
     * @param accessed: The object possesing the field
     */
    public MutableFieldAccessPoint(String fieldName, Object accessed) {
        super(fieldName, accessed);
    }

    // ///////////// METHODEN ////////////////////////////////////////

    /**
     * sets the current value of the accessed field.
     *
     * @param value the new field value.
     */
    public void setValue(Object value) {
        ReflectionManager.setValue(accessed, getField(), value);
    }
}