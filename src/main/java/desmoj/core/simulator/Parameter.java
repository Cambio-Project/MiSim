package desmoj.core.simulator;

/**
 * Class to Represent a Parameter. This parameter can be an experiment- parameter or a model-parameter. Parameters are
 * variables that are accessible from everywhere in the model. Model-parameters are assignable from model,
 * experiment-parameters from experiment.
 *
 * @author Tim Janz
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 *     <p>
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 *     with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *     <p>
 *     Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *     the specific language governing permissions and limitations under the License.
 * @see ParameterManager
 * @see ModelParameterManager
 * @see ExperimentParameterManager
 */
public class Parameter {
    /**
     * Parameter can be a model-parameter or an experiment-parameter
     */
    private final ParameterType _parameterType;
    /**
     * The type of the parameter's value
     */
    private final Class<?> _type;
    /**
     * The parameter's name
     */
    private final String _name;
    /**
     * The parameter's value
     */
    private Object _value;
    /**
     * The parameter's default value (only for experiment-parameters)
     */
    private final Object _defaultValue;

    /**
     * Constructs a new parameter. New Parameters should be created by the factory- methods of this class.
     *
     * @param parameterType is this parameter a model-parameter or an experiment-parameter
     * @param type          the parameter's type
     * @param name          the parameter's name
     * @param value         the parameter's value
     * @param defaultValue  the parameter's default value
     */
    private Parameter(ParameterType parameterType, Class<?> type, String name, Object value, Object defaultValue) {
        this._parameterType = parameterType;
        this._type = type;
        this._name = name;
        this._value = value;
        this._defaultValue = defaultValue;
    }

    /**
     * Factory-method to create an experiment-parameter.
     *
     * @param type         the parameter's type
     * @param name         the parameter's name
     * @param defaultValue the parameter's default value
     * @return the created instance of Parameter
     */
    public static Parameter createExperimentParameter(Class<?> type, String name, Object defaultValue) {
        return new Parameter(ParameterType.EXPERIMENTPARAMETER, type, name, null, defaultValue);
    }

    /**
     * Factory-method to create an experiment-parameter.
     *
     * @param type the parameter's type
     * @param name the parameter's name
     * @return the created instance of Parameter
     */
    public static Parameter createExperimentParameter(Class<?> type, String name) {
        return createExperimentParameter(type, name, null);
    }

    /**
     * Factory-method to create a model-parameter.
     *
     * @param type  the parameter's type
     * @param name  the parameter's name
     * @param value the parameter's value
     * @return the created instance of Parameter
     */
    public static Parameter createModelParameter(Class<?> type, String name, Object value) {
        return new Parameter(ParameterType.MODELPARAMETER, type, name, value, null);
    }

    /**
     * Factory-method to create a model-parameter.
     *
     * @param type the parameter's type
     * @param name the parameter's name
     * @return the created instance of Parameter
     */
    public static Parameter createModelParameter(Class<?> type, String name) {
        return createModelParameter(type, name, null);
    }

    /**
     * Returns the parameter's type.
     *
     * @return the parameter's type
     */
    public Class<?> getType() {
        return _type;
    }

    /**
     * Returns the parameter's name
     *
     * @return the parameter's name
     */
    public String getName() {
        return _name;
    }

    /**
     * returns the parameter's value
     *
     * @return the parameter's value
     */
    public Object getValue() {
        if (!hasValue() && !hasDefaultValue()) {
            //TODO Exception
        }

        return hasValue() ? _value : _defaultValue;
    }

    /**
     * sets the parameter's value.
     *
     * @param value the parameter's new value
     */
    public void setValue(Object value) {
        if (_type.isAssignableFrom(value.getClass())) {
            this._value = value;
        } else {
            //TODO Exception (wrong type)
        }
    }

    /**
     * Checks, if this parameter has a value.
     *
     * @return if parameter has a value
     */
    public boolean hasValue() {
        return (_value != null);
    }

    /**
     * Checks, if this parameter has a default value.
     *
     * @return if parameter has a default value
     */
    public boolean hasDefaultValue() {
        return (_defaultValue != null);
    }

    /**
     * Is this parameter a model-parameter or an experiment-parameter?
     *
     * @return ParameterType.MODELPARAMETER if this parameter is a model-parameter, or ParameterType.EXPERIMENTPARAMETER
     *     if this parameter is an experiment-parameter
     */
    public ParameterType getParameterType() {
        return _parameterType;
    }

    /**
     * Returns the parameter as a String
     *
     * @return the parameter as a String
     */
    @Override
    public String toString() {
        return "(" + _type.toString() + ") " + _name + ": " + _value.toString();
    }

    /**
     * Enum to decide, if a parameter is a model-parameter or an experiment- parameter
     */
    public enum ParameterType {
        EXPERIMENTPARAMETER, MODELPARAMETER
    }
}

