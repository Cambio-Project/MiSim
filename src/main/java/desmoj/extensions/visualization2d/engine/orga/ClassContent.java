package desmoj.extensions.visualization2d.engine.orga;

import java.util.Hashtable;


/**
 * The most classes in animation.model have a hashtable which store all instances of this class. This hashtable with it
 * management-methods (put, get, exist) are implemented in ClassContent.
 *
 * @param <E>
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
public class ClassContent<E extends ClassBasic> {

    private final Hashtable<String, E> contentTable;

    public ClassContent() {
        this.contentTable = new Hashtable<String, E>();
    }

    public void add(E element) {
        this.contentTable.put(element.getId(), element);
    }

    public E remove(String id) {
        return contentTable.remove(id);
    }

    public boolean exist(String id) {
        return contentTable.containsKey(id);
    }

    public E get(String id) {
        return contentTable.get(id);
    }

    public String[] getAllIds() {
        String[] out = new String[contentTable.size()];
        java.util.Enumeration<String> e = contentTable.keys();
        int i = 0;
        while (e.hasMoreElements()) {
            out[i] = e.nextElement();
            i++;
        }
        return out;
    }


}
