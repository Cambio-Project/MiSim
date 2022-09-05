package desmoj.extensions.experimentation.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * A utility class for using the Java Reflection API.
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
public class ReflectionManager {

    /**
     * Retrieves a method of a given class. If anything goes wrong a ReflectException is thrown. In this implementation
     * only methods declared directly by the given class are found.
     *
     * @param c          the class
     * @param methodName name of the method
     * @param params     parameter list of the method
     * @return the method
     * @throws ReflectException
     */
    public static Method getMethod(Class c, String methodName, Class[] params)
        throws ReflectException {
        Method method = null;
        try {
            method = c.getDeclaredMethod(methodName, params);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            throw new ReflectException("** Cannot retrieve method "
                + methodName + ": " + e.getMessage());
        }
    }

    /**
     * Retrieves a method of a given class with an empty parameter list. If anything goes wrong a ReflectException is
     * thrown. In this implementation only methods declared directly by the given class are found.
     *
     * @param c          the class
     * @param methodName name of the method
     * @return the method
     * @throws ReflectException
     */
    public static Method getMethod(Class c, String methodName)
        throws ReflectException {
        return getMethod(c, methodName, new Class[0]);
    }

    /**
     * Invokes a method on an object If anything goes wrong a reflect exception is thrown
     *
     * @param owner  the object to invoke the method on
     * @param method the method to be invoked
     * @param args   arguments of the method call
     * @return return value of the method call
     * @throws ReflectException
     */
    public static Object invokeMethod(Object owner, Method method, Object[] args)
        throws ReflectException {
        try {
            return method.invoke(owner, args);
        } catch (IllegalAccessException e) {
            throw new ReflectException("** Error invoking method " + method
                + ": " + e.getMessage());
        } catch (InvocationTargetException e) {
            throw new ReflectException("** Error invoking method " + method
                + ": " + e.getMessage());
        }
    }

    /**
     * Retrieves a constructor from a given class. If anything goes wrong a reflect exception is thrown
     *
     * @param c      the class
     * @param params the constructor's parameter list.
     * @return a constructor object
     */
    public static Constructor getConstructor(Class c, Class[] params) {
        Constructor constructor = null;
        try {
            constructor = c.getConstructor(params);
            constructor.setAccessible(true);
            return constructor;
        } catch (NoSuchMethodException e) {
            throw new ReflectException(
                "** Cannot retrieve constructor of class" + c.getName()
                    + ": " + e.getMessage());
        }
    }

    /**
     * Invokes a constructor. If anything goes wrong a reflect exception is thrown
     *
     * @param c    the constructor object
     * @param args the arguments for the constructor invocation
     * @return the object created by the constructor
     * @throws ReflectException
     */
    public static Object invokeConstructor(Constructor c, Object[] args)
        throws ReflectException {
        try {
            return c.newInstance(args);
        } catch (IllegalAccessException e) {
            throw new ReflectException("** Error invoking constructor " + c
                + ": " + e.getMessage());
        } catch (InvocationTargetException e) {
            throw new ReflectException("** Error invoking constructor " + c
                + ": " + e.getMessage());
        } catch (InstantiationException e) {
            throw new ReflectException("** Error invoking constructor " + c
                + ": " + e.getMessage());
        }
    }

    /**
     * Retrieves a field from a given class. In this implementation only fields declared directly by the given class are
     * found. On errors a reflect exception is thrown.
     *
     * @param c         the class
     * @param fieldName the field name
     * @return a field object
     * @throws ReflectException
     */
    public static Field getField(Class c, String fieldName)
        throws ReflectException {
        Field field = null;
        Class declaringClass = c;
        while (field == null && declaringClass != null) {
            try {
                field = declaringClass.getDeclaredField(fieldName);
            } catch (Exception e) {
            }
            declaringClass = declaringClass.getSuperclass();
        }
        if (field == null) {
            throw (new ReflectException("** Cannot retrieve field " + fieldName));
        } else {
            field.setAccessible(true);
            return field;
        }
    }

    /**
     * Returns the value of the given field has in the specified object. On any error a reflect exception is thrown
     *
     * @param owner the object who owns the field
     * @param field the field
     * @return the current field value
     * @throws ReflectException
     */
    public static Object getValue(Object owner, Field field)
        throws ReflectException {
        try {
            return field.get(owner);
        } catch (IllegalAccessException e) {
            throw new ReflectException("** Error getting value of field "
                + field + ": " + e.getMessage());
        }
    }

    /**
     * Sets the value of the given field of the specified object. On errors a reflect exception is thrown.
     *
     * @param owner the object owning the field
     * @param field the field
     * @param value the value to set the field to
     * @throws ReflectException
     */
    public static void setValue(Object owner, Field field, Object value)
        throws ReflectException {
        try {
            field.set(owner, value);
        } catch (IllegalAccessException e) {
            throw new ReflectException("** Error setting value of " + owner
                + "'s field " + field + " to " + value + ": "
                + e.getMessage());
        }
    }
}