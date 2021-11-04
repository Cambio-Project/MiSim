package desmoj.extensions.visualization2d.engine.model;

import java.awt.Dimension;
import java.awt.Point;
import java.util.LinkedList;

import desmoj.extensions.visualization2d.engine.modelGrafic.Grafic;
import desmoj.extensions.visualization2d.engine.modelGrafic.WaitingQueueGrafic;

/**
 * Waiting Queue for master slave cooperation
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
public class WaitingQueue implements Basic {

    private final String id;
    private String name;
    private final List masters;
    private final List slaves;
    private final java.util.List<Cooperation> cooperations;
    private Grafic grafic;
    private final Model model;


    /**
     * @param model used engine.model.Model
     * @param id
     */
    public WaitingQueue(Model model, String id) {
        this.model = model;
        this.id = id;
        this.name = null;
        this.masters = new List(model, List.PREFIX_WAIT_MASTER, this.id);
        this.slaves = new List(model, List.PREFIX_WAIT_SLAVE, this.id);
        this.cooperations = new LinkedList<Cooperation>();
        if (this.id != null) {
            model.getWaitingQueues().add(this);
        }
    }

    public Model getModel() {
        return this.model;
    }


    /**
     * get id of waiting queue
     */
    public String getId() {
        return this.id;
    }

    /**
     * get name of waiting queue
     */
    public String getName() {
        return this.name;
    }

    /**
     * set name of waiting queue
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    public String[][] getContentMasters() {
        return this.masters.getContent();
    }

    public String[][] getContentSlaves() {
        return this.slaves.getContent();
    }

    public int getCooperationsNo() {
        return this.cooperations.size();
    }

    public String getCooperationMaster(int i) {
        String out = null;
        if ((i >= 0) && (i < this.cooperations.size())) {
            out = this.cooperations.get(i).masterId;
        }
        return out;
    }

    public String getCooperationSlave(int i) {
        String out = null;
        if ((i >= 0) && (i < this.cooperations.size())) {
            out = this.cooperations.get(i).slaveId;
        }
        return out;
    }

    public void reset() {
        // fehlt noch
    }

    public void insert(String entityId, int priority, boolean master, String priorityRule, long time) {
        if (master) {
            this.masters.addToContainer(entityId, priority, priorityRule, time);
        } else {
            this.slaves.addToContainer(entityId, priority, priorityRule, time);
        }
        ((WaitingQueueGrafic) this.getGrafic()).update();
    }

    public void remove(String entityId, boolean master, long time) {
        if (master) {
            this.masters.removeFromContainer(entityId, time);
        } else {
            this.slaves.removeFromContainer(entityId, time);
        }
        ((WaitingQueueGrafic) this.getGrafic()).update();
    }

    public void cooperationBegin(String masterId, String slaveId, long time) {
        //System.out.println("cooperationBegin master: "+masterId+"  slave:"+slaveId);
        if (this.masters.containsInContainer(masterId)) {
            this.remove(masterId, true, time);
        } else {
            throw new ModelException(
                "WaitingQueue.cooperationBegin: Master isn't in masters waiting queue. MasterId: " + masterId);
        }
        if (this.slaves.containsInContainer(slaveId)) {
            this.remove(slaveId, false, time);
        } else {
            throw new ModelException(
                "WaitingQueue.cooperationBegin: Slave isn't in slaves waiting queue. SlaveId: " + slaveId);
        }
        Cooperation coop = new Cooperation(masterId, slaveId);
        this.cooperations.add(coop);
        ((WaitingQueueGrafic) this.getGrafic()).update();
    }

    public void cooperationEnd(String masterId, String slaveId, long time) {
        boolean found = false;
        for (int i = 0; i < this.cooperations.size(); i++) {
            String m = this.cooperations.get(i).getMasterId();
            String s = this.cooperations.get(i).getSlaveId();
            if (masterId.equals(m) && slaveId.equals(s)) {
                this.cooperations.remove(i);
                found = true;
                break;
            }
        }
        if (!found) {
            throw new ModelException(
                "WaitingQueue.cooperationEnd: The master-slave couple isn't in a actual cooperation. MasterId: " +
                    masterId + "  SlaveId: " + slaveId);
        }
        ((WaitingQueueGrafic) this.getGrafic()).update();
    }

    /**
     * create a WaitingQueueGrafic
     *
     * @param viewId              Id of view
     * @param x                   middlepoint x-coordinate
     * @param y                   middlepoint y-coordinate
     * @param defaultEntityTypeId for sizing
     * @return StockGrafic
     * @throws ModelException
     */
    public Grafic createGrafic(String viewId, int x, int y, String defaultEntityTypeId,
                               int anzVisible, boolean horizontal, Dimension deltaSize) throws ModelException {
        this.grafic = new WaitingQueueGrafic(this, viewId, new Point(x, y), defaultEntityTypeId, anzVisible, horizontal,
            deltaSize);
        return this.grafic;
    }

    /**
     * get ProcessGrafic, created before
     */
    public Grafic getGrafic() {
        return grafic;
    }

    class Cooperation {
        private final String masterId;
        private final String slaveId;

        public Cooperation(String masterId, String slaveId) {
            this.masterId = masterId;
            this.slaveId = slaveId;
            if (!model.getEntities().exist(masterId)) {
                throw new ModelException(
                    "animation.model.WaitingQueue.Cooperation: MasterId don't exist! (master, slave): (" + masterId +
                        "," + slaveId + ")");
            }
            if (!model.getEntities().exist(slaveId)) {
                throw new ModelException(
                    "animation.model.WaitingQueue.Cooperation: SlaveId don't exist! (master, slave): (" + masterId +
                        "," + slaveId + ")");
            }
        }

        public String getMasterId() {
            return this.masterId;
        }

        public String getSlaveId() {
            return this.slaveId;
        }
    }


}
