package desmoj.extensions.visualization2d.engine.model;

import java.awt.Point;

import desmoj.extensions.visualization2d.engine.modelGrafic.Grafic;
import desmoj.extensions.visualization2d.engine.modelGrafic.ProcessGrafic;


/**
 * Working-station with a line of resource-entities and a line of processed entities
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
public class Process implements Basic {

    /**
     * hashtable with all process-instances
     */
    private final String id;
    private String name;
    private final int anz_ResourceEntities;
    private final int anz_ProcessEntities;
    private final String[] resourceEntityIds;
    private final String[] prozessEntityIds;
    private final String listId;
    private Grafic grafic;
    private final Model model;

    /**
     * @param model                used animation.model.Model
     * @param id                   processId
     * @param anz_ResourceEntities max possible resource-entities in one time
     * @param anz_ProcessEntities  max possible processed-entities in one time
     * @param listId               optional id of a list, which include all processed entities before
     */
    public Process(Model model, String id, int anz_ResourceEntities, int anz_ProcessEntities, String listId) {
        this.model = model;
        this.id = id;
        this.name = null;
        this.anz_ProcessEntities = anz_ProcessEntities;
        this.anz_ResourceEntities = anz_ResourceEntities;
        this.prozessEntityIds = new String[this.anz_ProcessEntities];
        this.resourceEntityIds = new String[this.anz_ResourceEntities];
        for (int i = 0; i < anz_ResourceEntities; i++) {
            this.resourceEntityIds[i] = " ";
        }
        for (int i = 0; i < anz_ProcessEntities; i++) {
            this.prozessEntityIds[i] = " ";
        }
        this.listId = listId;
        if (this.id != null) {
            model.getProcesses().add(this);
        }
    }

    public Model getModel() {
        return this.model;
    }

    /**
     * get id of process-station
     */
    public String getId() {
        return this.id;
    }

    /**
     * get name of process-station
     */
    public String getName() {
        return this.name;
    }

    /**
     * set name of process-station
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * get id of list, where are the processed entities are before this may be null
     *
     * @return ListId
     */
    public String getListId() {
        return this.listId;
    }

    /**
     * get array with id's of all processed entities
     *
     * @return array of processed entities
     */
    public String[] getProcessEntity() {
        return this.prozessEntityIds;
    }

    /**
     * set entityId on position index as processed entity
     *
     * @param index    0 <= index < anz_ProcessEntities
     * @param entityId
     * @param time     time of operating
     * @throws ModelException
     */
    public void setProzessEntity(int index, String entityId, long time) throws ModelException {
        if ((index >= 0) && (index < this.anz_ProcessEntities)) {
            if (model.getEntities().get(entityId).isFree()) {
                this.prozessEntityIds[index] = entityId;
                model.getEntities().get(entityId).changeContainer("Process", this.getId(), "add", time);

            } else {
                throw new ModelException(
                    "model.Process.setProcessEntity Entity is not free: ProcessId:" + this.getId() + "  EntityId:" +
                        entityId);
            }
        } else {
            throw new ModelException("model.Process.setProcessEntity Index is not valid: " + index);
        }
        if (this.grafic != null) {
            ((ProcessGrafic) this.grafic).update();
        }
        //this.printEntries();
    }

    /**
     * remove entity from position index as processed entity
     *
     * @param index    0 <= index < anz_ProcessEntities
     * @param entityId
     * @param time     time of operating
     * @throws ModelException
     */
    public void unsetProzessEntity(int index, String entityId, long time) throws ModelException {
        if ((index >= 0) && (index < this.anz_ProcessEntities)) {
            if (this.prozessEntityIds[index].equals(entityId)) {
                this.prozessEntityIds[index] = " ";
                model.getEntities().get(entityId).changeContainer("Process", this.getId(), "remove", time);
                model.getEntities().get(entityId).changeContainer(" ", "free", "", time);

            } else {
                throw new ModelException(
                    "model.Process.unsetProcessEntity index and EntityId are not compatibel: ProcessId:" +
                        this.getId() + "  EntityId:" + entityId + "  Index: " + index);
            }
        } else {
            throw new ModelException("model.Process.unsetProcessEntity Index is not valid: " + index);
        }
        if (this.grafic != null) {
            ((ProcessGrafic) this.grafic).update();
        }
        //this.printEntries();
    }

    /**
     * set entityId on next free position as processed entity This method is normally used
     *
     * @param entityId
     * @param time     time of operation
     * @throws ModelException
     */
    public void setProzessEntity(String entityId, long time) throws ModelException {
        for (int i = 0; i < this.anz_ProcessEntities; i++) {
            if (this.prozessEntityIds[i].trim().equals("")) {
                this.setProzessEntity(i, entityId, time);
                break;
            }
        }
    }

    /**
     * remove entityId from processed entity This method is normally used.
     *
     * @param entityId
     * @param time
     * @throws ModelException
     */
    public void unsetProzessEntity(String entityId, long time) throws ModelException {
        for (int i = 0; i < this.anz_ProcessEntities; i++) {
            if (this.prozessEntityIds[i].equals(entityId)) {
                this.unsetProzessEntity(i, entityId, time);
                break;
            }
        }
    }

    /**
     * get array with id's of all resource-entities
     *
     * @return array of resource-entities
     */
    public String[] getResourceEntity() {
        return this.resourceEntityIds;
    }

    /**
     * set entityId on position index as resource entity
     *
     * @param index    0 <= index < anz_ResourceEntities
     * @param entityId
     * @param time     time of operation
     * @throws ModelException
     */
    public void setResourceEntity(int index, String entityId, long time) throws ModelException {
        if ((index >= 0) && (index < this.anz_ResourceEntities)) {
            if (model.getEntities().get(entityId).isFree()) {
                this.resourceEntityIds[index] = entityId;
                model.getEntities().get(entityId).changeContainer("Process", this.getId(), "add", time);

            } else {
                throw new ModelException(
                    "model.Process.setResourceEntity Entity is not free: ProcessId:" + this.getId() + "  EntityId:" +
                        entityId);
            }
        } else {
            throw new ModelException("model.Process.setResourceEntity Index is not valid: " + index);
        }
        if (this.grafic != null) {
            ((ProcessGrafic) this.grafic).update();
        }
        //this.printEntries();
    }

    /**
     * remove entity from position index as resource entity
     *
     * @param index    0 <= index < anz_ResourceEntities
     * @param entityId
     * @param time     time of operation
     * @throws ModelException
     */
    public void unsetResourceEntity(int index, String entityId, long time) throws ModelException {
        if ((index >= 0) && (index < this.anz_ResourceEntities)) {
            if (this.resourceEntityIds[index].equals(entityId)) {
                this.resourceEntityIds[index] = " ";
                model.getEntities().get(entityId).changeContainer("Process", this.getId(), "remove", time);
                model.getEntities().get(entityId).changeContainer(" ", "free", "", time);

            } else {
                throw new ModelException(
                    "model.Process.unsetResourceEntity index and EntityId are not compatibel: ProcessId:" +
                        this.getId() + "  EntityId:" + entityId + "  Index: " + index);
            }
        } else {
            throw new ModelException("model.Process.unsetResourceEntity Index is not valid: " + index);
        }
        if (this.grafic != null) {
            ((ProcessGrafic) this.grafic).update();
        }
        //this.printEntries();
    }

    /**
     * add resource entity on next free position
     *
     * @param entityId
     * @param time     time of operation
     * @throws ModelException
     */
    public void setResourceEntity(String entityId, long time) throws ModelException {
        for (int i = 0; i < this.anz_ResourceEntities; i++) {
            if (this.resourceEntityIds[i].trim().equals("")) {
                this.setResourceEntity(i, entityId, time);
                break;
            }
        }
    }

    /**
     * remove resource entity with entityId
     *
     * @param entityId
     * @param time     time of operation
     * @throws ModelException
     */
    public void unsetResourceEntity(String entityId, long time) throws ModelException {
        for (int i = 0; i < this.anz_ResourceEntities; i++) {
            if (this.resourceEntityIds[i].equals(entityId)) {
                this.unsetResourceEntity(i, entityId, time);
                break;
            }
        }
    }


    /**
     * makes a control-print on System.out
     */
    public void printEntries() {
        System.out.println("ProcEntities len: " + this.prozessEntityIds.length);
        for (int i = 0; i < this.prozessEntityIds.length; i++) {
            System.out.println(this.prozessEntityIds[i] + ", ");
        }
        System.out.println();
        System.out.println("ResEntities len: " + this.resourceEntityIds.length);
        for (int i = 0; i < this.resourceEntityIds.length; i++) {
            System.out.println(this.resourceEntityIds[i] + ", ");
        }
        System.out.println();
    }

    /**
     * create a ProcessGrafic
     *
     * @param viewId              Id of view
     * @param x                   middlepoint x-coordinate
     * @param y                   middlepoint y-coordinate
     * @param defaultEntityTypeId for sizing
     * @return ProcessGrafic
     */
    public Grafic createGrafic(String viewId, int x, int y, String defaultEntityTypeId) {
        this.grafic = new ProcessGrafic(this, viewId, new Point(x, y), defaultEntityTypeId);
        return this.grafic;
    }

    /**
     * get ProcessGrafic, created before
     */
    public Grafic getGrafic() {
        return grafic;
    }


}
