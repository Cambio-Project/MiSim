package desmoj.extensions.visualization2d.engine.model;

import java.awt.Dimension;
import java.awt.Point;
import java.util.Hashtable;

import desmoj.extensions.visualization2d.engine.modelGrafic.Grafic;
import desmoj.extensions.visualization2d.engine.modelGrafic.ResourceGrafic;


/**
 * Resource-station with waiting queue and process station. Every resource station has a total no of available
 * resources. A entity needs a specified no of resources. The entities are waiting in a waiting-queue until the required
 * resources are free.
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
public class Resource implements Basic {

    private final String id;
    private String name;
    private final List list;
    private final ProcessNew process;
    private final Hashtable<String, Integer> neededRes;
    private Grafic grafic;
    private final Model model;


    /**
     * Constructor, a Resource contains a List and a ProcessNew instance. The id's of this instances have a prefix
     * "res".
     *
     * @param model         used animation.model.Model
     * @param id            resourceId
     * @param resourceType  resourceTypeName (only for information, its no Id)
     * @param resourceTotal total number of resources (only for information)
     */
    public Resource(Model model, String id, String resourceType, int resourceTotal) {
        this.model = model;
        this.id = id;
        this.name = null;
        this.list = new List(model, List.PREFIX_RESOURCE, this.id);
        this.process = new ProcessNew(model, ProcessNew.PREFIX_RESOURCE, this.id, resourceType, resourceTotal, null);
        this.neededRes = new Hashtable<String, Integer>();
        if (this.id != null) {
            model.getResources().add(this);
        }
    }

    public Model getModel() {
        return this.model;
    }


    /**
     * get id of resource
     */
    public String getId() {
        return this.id;
    }

    /**
     * get name of resource
     */
    public String getName() {
        return this.name;
    }

    /**
     * set name of resource
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
        this.list.setName(name);
        this.process.setName(name);
    }

    /**
     * get resource Type (only for information)
     *
     * @return
     */
    public String getResourceType() {
        return this.process.getResourceType();
    }

    /**
     * total no. of available resources
     *
     * @return
     */
    public int getResourceTotal() {
        return this.process.getResourceTotal();
    }

    /**
     * no of actual used resources
     *
     * @return
     */
    public int getResourceUsed() {
        return this.process.getResourceUsed();
    }

    /**
     * actual no of free resources
     *
     * @return
     */
    public int getResourceFree() {
        return this.process.getResourceTotal() - this.process.getResourceUsed();
    }

    /**
     * actual no. of entities (sets with process-entities) in process-part
     *
     * @return
     */
    public int getProcessEntriesAnz() {
        return this.process.getEntriesAnz();
    }

    /**
     * get id of entity in position index in process
     *
     * @param index
     * @return
     * @throws ModelException, when a internal error occurred
     */
    public String getProcessEntry(int index) throws ModelException {
        String out = "";
        String[] pe = this.process.getProcessEntries(index);
        if (pe != null && pe.length == 1) {
            out = pe[0];
        } else {
            throw new ModelException("ProcessEntry must have exact one Process");
        }
        return out;
    }

    /**
     * get actual no. of resources, used by entity in position index
     *
     * @param index
     * @return
     */
    public int getResourceEntriesAnz(int index) {
        return this.process.getResourceEntriesAnz(index);
    }

    /**
     * get content of waiting queue
     *
     * @return (index)(id, rank, resources needed)
     */
    public String[][] getWaitingQueueContent() {
        String[][] content = this.list.getContent();
        int queueLength = content.length;
        String[][] out = new String[queueLength][3];
        for (int i = 0; i < queueLength; i++) {
            out[i][0] = content[i][0];
            out[i][1] = content[i][1];
            out[i][2] = this.neededRes.get(content[i][0]).toString();
        }
        return out;
    }

    /**
     * put a processEntity, that require resourceEntityAnz resources, into waiting queue
     *
     * @param processEntityId   id of processEntity
     * @param resourceEntityAnz required no. of resources
     * @param time              simulation time
     * @throws ModelException, when processEntity isn't free
     */
    public void provide(String processEntityId, int priority, int resourceEntityAnz,
                        String priorityAttribute, long time) throws ModelException {
        Entity entity = model.getEntities().get(processEntityId);
        if (entity != null && entity.isFree()) {
            this.list.addToContainer(processEntityId, priority, priorityAttribute, time);
            this.neededRes.put(processEntityId, new Integer(resourceEntityAnz));
        } else {
            throw new ModelException("Entity does not exist or isn't free Id: " + processEntityId);
        }
        if (this.grafic != null) {
            ((ResourceGrafic) (this.grafic)).update();
        }
    }

    /**
     * move processEntity from waiting queue to process
     *
     * @param processEntityId id of processEntity
     * @param time            simulation time
     * @throws ModelException, when entity isn't in waiting queue
     */
    public void takeProcess(String processEntityId, long time) throws ModelException {
        if (this.list.containsInContainer(processEntityId)) {
            this.list.removeFromContainer(processEntityId, time);
            String[] processEntityIds = new String[1];
            processEntityIds[0] = processEntityId;
            Integer resourceEntityAnz = this.neededRes.get(processEntityId);
            if (resourceEntityAnz != null) {
                this.process.addEntry(processEntityIds, resourceEntityAnz, time);
            } else {
                throw new ModelException("Missing processEntry " + processEntityId + " in Resource.neededRes");
            }
        } else {
            throw new ModelException("Entity isn't in waiting queue of resource EntityId: " + processEntityId);
        }
        if (this.grafic != null) {
            ((ResourceGrafic) (this.grafic)).update();
        }
    }

    /**
     * removes processEntity from process
     *
     * @param processEntityId id of processEntity
     * @param time            simulation time
     * @throws ModelException, when entity isn't in process
     */
    public void takeBack(String processEntityId, int resourceGiveBackAnz, long time) throws ModelException {
        Integer anzRes = this.neededRes.get(processEntityId);
        if (anzRes != null) {
            if (resourceGiveBackAnz < anzRes.intValue()) {
                // reduce no of needed resources
                this.neededRes.put(processEntityId, new Integer(anzRes.intValue() - resourceGiveBackAnz));
            } else {
                // remove entity
                if (this.process.contains(processEntityId)) {
                    this.process.removeEntry(processEntityId, time);
                    this.neededRes.remove(processEntityId);
                } else {
                    throw new ModelException("Entity isn't in process of resource EntityId: " + processEntityId);
                }
            }
        } else {
            throw new ModelException("Entity is in resource " + this.getId() + " unknown EntityId: " + processEntityId);
        }
        if (this.grafic != null) {
            ((ResourceGrafic) (this.grafic)).update();
        }
    }

    /**
     * create a ProcessGrafic
     *
     * @param viewId              Id of view
     * @param x                   middlepoint x-coordinate
     * @param y                   middlepoint y-coordinate
     * @param defaultEntityTypeId for sizing
     * @return ProcessGrafic
     * @throws ModelException
     */
    public Grafic createGrafic(String viewId, int x, int y, String defaultEntityTypeId,
                               int anzVisible, boolean horizontal, Dimension deltaSize) throws ModelException {
        this.grafic =
            new ResourceGrafic(this, viewId, new Point(x, y), defaultEntityTypeId, anzVisible, horizontal, deltaSize);
        return this.grafic;
    }

    /**
     * get ProcessGrafic, created before
     */
    public Grafic getGrafic() {
        return grafic;
    }


}
