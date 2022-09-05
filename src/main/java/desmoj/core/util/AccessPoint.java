package desmoj.core.util;

/**
 * An access point provides a generic interface to get attribute values from objects. It can e.g. be used to provide
 * access to a model's or experiment's parameters. This pattern can be found in several frameworks for agent- or
 * component-based simulation (e.g. 'Probes' in Swarm - see www.swarm.org). The term 'access point' was adopted from the
 * dissertation "Ein flexibler, CORBA-basierter Ansatz fuer die verteilte, komponentenorientierte Simulation" by Ralf
 * Bachmann (2003).
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
public interface AccessPoint {

    /**
     * returns the current value of the attribute referenced by this access point.
     *
     * @return the attribute's value
     */
    Object getValue();

    /**
     * returns the name of this AccessPoint.
     *
     * @return the access-point's name as a string
     */
    String getName();

}