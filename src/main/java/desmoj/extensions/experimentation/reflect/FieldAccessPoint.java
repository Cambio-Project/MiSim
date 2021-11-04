package desmoj.extensions.experimentation.reflect;

import java.lang.reflect.Field;

import desmoj.core.util.AccessPoint;

/**
 * An AccessPoint for an attribute (field) of a class. This pattern can be found in several frameworks for agent- or
 * component-based simulation (e.g. 'Probes' in Swarm - see www.swarm.org). The term 'access point' was adopted from the
 * dissertation "Ein flexibler, CORBA-basierter Ansatz fuer die verteilte, komponentenorientierte Simulation" by Ralf
 * Bachmann (2003).
 *
 * @author Ruth Meyer, Nicolas Knaak
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 *     <p>
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 *     with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *     <p>
 *     Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *     the specific language governing permissions and limitations under the License.
 */
public class FieldAccessPoint implements AccessPoint {

    // ///////////// ATTRIBUTE ///////////////////////////////////////

    /** the object possessing the inspectable field. */
    protected Object accessed;
    /** the field to be inspected via this access point. */
    private final Field inspectableField;
    /** The class of the inspected object */
    private final Class declaringClass;

    /** the access point's name */
    private final String name;

    // ///////////// KONSTRUKTOREN ///////////////////////////////////

    /**
     * constructs an access point object for the specified field name and the given declaring object. The parameter
     * <code>fieldName</code> must denote a field declared in the class of <code>declaringObject</code> or inherited
     * from one of its superclasses.
     *
     * @param fieldName the name of the field to be inspected
     * @param accessed  the object whose field values are to be inspected
     * @SBGen Constructor
     */
    public FieldAccessPoint(String fieldName, Object accessed) {
        this(fieldName, fieldName, accessed);
    }

    /**
     * constructs an access point object from the given field and object. The parameter <code>inspectableField</code>
     * must reference a field declared in the class of <code>declaringObject</code> or inherited from one of its
     * superclasses.
     *
     * @param inspectableField the field to be inspected
     * @param accessed         the accessed object
     */
    public FieldAccessPoint(Field inspectableField, Object accessed) {
        this(inspectableField.getName(), inspectableField, accessed);
    }

    /**
     * constructs an access point object for the specified field name and the given declaring object. The parameter
     * <code>fieldName</code> must denote a field declared in the class of <code>declaringObject</code> or inherited
     * from one of its superclasses. The name of the access point may differ from the field name.
     *
     * @param name      the name for this access point
     * @param fieldName the name of the field to be inspected
     * @param accessed  the object whose field values are to be inspected
     */
    public FieldAccessPoint(String name, String fieldName, Object accessed)
        throws ReflectException {
        this.name = name;
        this.accessed = accessed;
        this.declaringClass = accessed.getClass();
        // rekursiv in der Klassenhierarchie die deklarierende Klasse bestimmen,
        // um das Field-Objekt f�r fieldName zu bekommen
        this.inspectableField = ReflectionManager.getField(declaringClass,
            fieldName);
    }

    /**
     * constructs an access point object for the specified field name and the given declaring object. The parameter
     * <code>fieldName</code> must denote a field declared in the class of <code>declaringObject</code> or inherited
     * from one of its superclasses. The name of the access point may differ from the field name.
     *
     * @param name           the name for this access point
     * @param fieldName      the name of the field to be inspected
     * @param declaringClass the class whose field is to be inspected
     */
    protected FieldAccessPoint(Class declaringClass, String name, String fieldName)
        throws ReflectException {
        this.name = name;
        this.accessed = null;
        this.declaringClass = declaringClass;
        // rekursiv in der Klassenhierarchie die deklarierende Klasse bestimmen,
        // um das Field-Objekt f�r fieldName zu bekommen
        this.inspectableField = ReflectionManager.getField(declaringClass,
            fieldName);
    }

    /**
     * constructs an access point object from the given field and object. The parameter
     * <code>inspectableField</code> must reference a field declared in the
     * class of <code>declaringObject</code> or inherited from one of its superclasses. The name of the access point may
     * differ from the field's name.
     *
     * @param name             the name for this access point
     * @param inspectableField the field to be inspected
     * @param inspected        the inspected object
     */
    public FieldAccessPoint(String name, Field inspectableField, Object inspected) {
        this.name = name;
        this.inspectableField = inspectableField;
        this.declaringClass = inspectableField.getDeclaringClass();
    }

    /**
     * constructs an access point object from the given field and object. The parameter
     * <code>inspectableField</code> must reference a field declared in the
     * class of <code>declaringObject</code> or inherited from one of its superclasses. The name of the access point may
     * differ from the field's name.
     *
     * @param name             the name for this access point
     * @param inspectableField the field to be inspected
     */
    protected FieldAccessPoint(String name, Field inspectableField) {
        this.name = name;
        this.inspectableField = inspectableField;
        this.declaringClass = inspectableField.getDeclaringClass();
        this.accessed = null;
    }

    // ///////////// METHODEN ////////////////////////////////////////

    /**
     * @return the current value of the accessed field for the declaring object.
     */
    public Object getValue() throws ReflectException {
        return ReflectionManager.getValue(accessed, inspectableField);
    }

    /** @return the name of this field access point. */
    public String getName() {
        return name;
    }

    /**
     * @return the name of the inspectable field.
     */
    public String getFieldName() {
        return this.inspectableField.getName();
    }

    /** @return the field assigned to this access point */
    protected Field getField() {
        return this.inspectableField;
    }

    /** @return the declaring class assigned to this access point */
    protected Class getDeclaringClass() {
        return this.declaringClass;
    }

} /* end of class FieldAccessPoint */
