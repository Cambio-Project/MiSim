package desmoj.core.dist;


import org.apache.commons.math.analysis.UnivariateRealFunction;

/**
 * Implementations of this Interface represent double-valued univariate functions.
 *
 * @author Peter Wueppen
 *     <p>
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 *     with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *     <p>
 *     Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *     the specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 * @see Distribution
 */
public interface Function extends UnivariateRealFunction {

    /**
     * Use this to declare what is going to be shown as "Type" of this function by the Reporter.
     *
     * @return String : The description String of this function
     */
	String getDescription();

    /**
     * Method to compute the function value out of the given input value.
     *
     * @return double : The specific function value
     */
	double value(double x);

}
