package desmoj.extensions.visualization2d.engine.model;

import java.util.Hashtable;
import java.util.Iterator;

import desmoj.extensions.visualization2d.engine.modelGrafic.Grafic;


/**
 * Basis-implementation of an container of entities It's a hashtable.
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
public abstract class Container<E> implements Basic {

    protected Hashtable<String, E> container;
    protected String id;
    protected Grafic grafic;
    protected Model model;

    public Container(Model model, String id) {
        this.model = model;
        this.id = id;
        this.container = new Hashtable<String, E>();
    }

    public Model getModel() {
        return this.model;
    }

    public Grafic getGrafic() {
        return grafic;
    }


    public String getId() {
        return id;
    }

    /**
     * add an entity value with id entityId in to container
     *
     * @param entityId
     * @param value
     * @return true, when successful
     */
    public boolean addToContainer(String entityId, E value) {
        boolean out;
        Entity e = model.getEntities().get(entityId);
        out = (e != null) && e.isFree();
        if (out) {
            this.container.put(entityId, value);
        }
        return out;
    }

    /**
     * remove entityId from container. ???? time is not used ?????
     *
     * @param entityId
     * @param time     not used
     * @return true, when successful
     * @throws ModelException
     */
    public boolean removeFromContainer(String entityId, long time) throws ModelException {
        boolean out;
        Entity e = model.getEntities().get(entityId);
        out = (e != null) && (this.container.containsKey(entityId));
        if (out) {
            this.container.remove(entityId);
        }
        return out;
    }

    /**
     * get entity with id, which is in container
     *
     * @param id
     * @return entity
     */
    public E getFromContainer(String id) {
        return this.container.get(id);
    }

    /**
     * check if it contains a entity with entityId
     *
     * @param entityId
     * @return true, when successful
     */
    public boolean containsInContainer(String entityId) {
        boolean out;
        Entity e = model.getEntities().get(entityId);
        out = (e != null) && (this.container.containsKey(entityId));
        return out;
    }

    /**
     * get an array with id's of all entities in container
     *
     * @return array with id's
     */
    public String[] getAllContentFromContainer() {
        String[] out = null;
        synchronized (this.container) {
            out = new String[this.container.size()];
            Iterator<String> en = this.container.keySet().iterator();
            int i = 0;
            while (en.hasNext()) {
                out[i++] = en.next();
            }
        }
        return out;
    }

    public int sizeOfContainer() {
        return this.container.size();
    }


}
