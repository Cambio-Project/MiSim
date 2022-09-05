package desmoj.extensions.visualization2d.engine.model;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.util.ListIterator;

import desmoj.extensions.visualization2d.engine.command.Cmd;
import desmoj.extensions.visualization2d.engine.modelGrafic.Grafic;
import desmoj.extensions.visualization2d.engine.modelGrafic.ListGrafic;


/**
 * A list of entities, entities are sorted by priority. Grafical properties are stired in ListGrafic
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
public class List implements Basic {

    public static final String PREFIX_QUEUE = "queue:";
    public static final String PREFIX_RESOURCE = "resource:";
    public static final String PREFIX_BIN_CONSUMER = "binCons:";
    public static final String PREFIX_STOCK_CONSUMER = "stockCons:";
    public static final String PREFIX_STOCK_PRODUCER = "stockProd:";
    public static final String PREFIX_WAIT_MASTER = "waitMaster:";
    public static final String PREFIX_WAIT_SLAVE = "waitSlave:";

    public static final String PRIO_FIRST = "first";
    public static final String PRIO_LAST = "last";
    protected Model model;
    /**
     * hashtable with all lists
     */
    private final String id;
    private String name;
    private ListGrafic grafic;
    private final java.util.LinkedList<Entity> content;

    private String commentText;
    private Font commentFont;
    private Color commentColor;
    private boolean commentSizeExt;

    /**
     * @param model   used animation.model.Model
     * @param praefix
     * @param id
     */
    public List(Model model, String praefix, String id) {
        this.model = model;
        this.id = praefix + id;
        this.name = null;
        if (this.id != null) {
            model.getLists().add(this);
        }
        this.content = new java.util.LinkedList<Entity>();
        this.commentText = null;
        this.commentColor = Grafic.COLOR_FOREGROUND;
        this.commentFont = new Font("SansSerif", 0, 8);
        this.commentSizeExt = true;

    }

    public Model getModel() {
        return this.model;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    /**
     * set list-name
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    public String getCommentText() {
        return this.commentText;
    }

    public void setCommentTest(String text) {
        this.commentText = text;
    }

    public Color getCommentColor() {
        return this.commentColor;
    }

    public void setCommentColor(Color color) {
        this.commentColor = color;
    }

    public Font getCommentFont() {
        return this.commentFont;
    }

    public void setCommentFont(int style, int size) {
        this.commentFont = new Font("SansSerif", style, size);
    }

    public boolean isCommentSizeExt() {
        return this.commentSizeExt;
    }

    public void setCommentSizeExt(boolean commentSizeExt) {
        this.commentSizeExt = commentSizeExt;
    }


    /**
     * check if it contains a entity with entityId
     *
     * @param entityId
     * @return
     */
    public boolean containsInContainer(String entityId) {
        Entity entity = model.getEntities().get(entityId);
        return this.content.contains(entity);
    }

    /**
     * get an array with id's of all entities in container
     *
     * @return array with id's
     */
    public String[] getAllContentFromContainer() {
        String[] out = new String[this.content.size()];
        ListIterator<Entity> it = this.content.listIterator();
        int i = 0;
        while (it.hasNext()) {
            out[i] = it.next().getId();
            i++;
        }
        return out;
    }

    /**
     * create a ListGrafic instance
     *
     * @param viewId              Id of view
     * @param x                   x-coordinate of middlepoint
     * @param y                   y-coordinate of middlepoint
     * @param defaultEntityTypeId for sizing
     * @param anzVisible          nr of visible entities
     * @param horizontal          "horizontal" or "vertical"
     * @return ListGrafic
     */
    public Grafic createGrafic(String viewId, int x, int y,
                               String defaultEntityTypeId, int anzVisible,
                               boolean horizontal) {
        this.grafic = new ListGrafic(this, viewId,
            new Point(x, y), defaultEntityTypeId, anzVisible,
            horizontal, null);
        return this.grafic;
    }

    /**
     * create a ListGrafic instance
     *
     * @param viewId              Id of view
     * @param x                   x-coordinate of middlepoint
     * @param y                   y-coordinate of middlepoint
     * @param defaultEntityTypeId for sizing
     * @param anzVisible          nr of visible entities
     * @param horizontal          "horizontal" or "vertical"
     * @param deltaSize           increment/decrement size of grafic [in pixel]
     * @return ListGrafic
     * @return
     */
    public Grafic createGrafic(String viewId, int x, int y,
                               String defaultEntityTypeId, int anzVisible,
                               boolean horizontal, Dimension deltaSize) {
        this.grafic = new ListGrafic(this, viewId,
            new Point(x, y), defaultEntityTypeId, anzVisible,
            horizontal, deltaSize);
        return this.grafic;
    }

    /**
     * get a ListGrafic instance, created before
     */
    public Grafic getGrafic() {
        return this.grafic;
    }

    /**
     * add entity to container
     *
     * @param entityId     Id of entity to insert
     * @param priority     priority of entity to insert
     * @param priorityRule posible Values: List.FIRST, List.LAST
     * @param time         simulation-time of operating
     * @return
     * @throws ModelException
     */
    public boolean addToContainer(String entityId, int priority, String priorityRule, long time) throws ModelException {
        boolean out = false;
        Entity entity = model.getEntities().get(entityId);
        if (entity != null) {
            // update priority attribute
            if (priority != entity.getPriority()) {
                entity.setAttribute(Cmd.PRIORITY_KEY, Integer.toString(priority), time);
            }
            if (priorityRule.equals(List.PRIO_FIRST)) {
                if (this.content.isEmpty()) {
                    this.content.add(entity);
                    out = true;
                } else if (this.content.getFirst().getPriority() <= priority) {
                    this.content.addFirst(entity);
                    out = true;
                } else if (this.content.getLast().getPriority() > priority) {
                    this.content.addLast(entity);
                    out = true;
                } else {
                    ListIterator<Entity> it = this.content.listIterator();
                    while (it.hasNext()) {
                        Entity e = it.next();
                        if (e.getPriority() <= priority) {
                            it.add(entity);
                            out = true;
                            break;
                        }
                    }
                }
            } else if (priorityRule.equals(List.PRIO_LAST)) {
                if (this.content.isEmpty()) {
                    this.content.add(entity);
                    out = true;
                } else if (this.content.getFirst().getPriority() < priority) {
                    this.content.addFirst(entity);
                    out = true;
                } else if (this.content.getLast().getPriority() >= priority) {
                    this.content.addLast(entity);
                    out = true;
                } else {
                    ListIterator<Entity> it = this.content.listIterator();
                    while (it.hasNext()) {
                        Entity e = it.next();
                        if (e.getPriority() < priority) {
                            it.add(entity);
                            out = true;
                            break;
                        }
                    }
                }
            }
        }
        if (this.grafic != null) {
            this.grafic.update();
        }
        return out;
    }

    /**
     * Add entity after a given entity
     *
     * @param entityId      Id of entity to insert
     * @param priority      priority of new entity
     * @param entityAfterId Id of after entity
     * @param time          simulation time of operation
     * @throws ModelException
     */
    public boolean addToContainerAfter(String entityId, int priority, String entityAfterId, long time)
        throws ModelException {
        boolean out = false;
        Entity entity = model.getEntities().get(entityId);
        // update priority attribute
        if (priority != entity.getPriority()) {
            entity.setAttribute(Cmd.PRIORITY_KEY, Integer.toString(priority), time);
        }
        // add entity
        Entity entityAfter = model.getEntities().get(entityAfterId);
        ListIterator<Entity> it = this.content.listIterator();
        while (it.hasNext()) {
            Entity e = it.next();
            if (e.equals(entityAfter)) {
                it.add(entity);
                out = true;
                break;
            }
        }
        if (this.grafic != null) {
            this.grafic.update();
        }
        return out;
    }

    /**
     * Add entity before a given entity
     *
     * @param entityId       Id of entity to insert
     * @param priority       priority of new entity
     * @param entityBeforeId Id of before entity
     * @param time           simulation time of operation
     * @throws ModelException
     */
    public boolean addToContainerBefore(String entityId, int priority, String entityBeforeId, long time)
        throws ModelException {
        boolean out = false;
        Entity entity = model.getEntities().get(entityId);
        // update priority attribute
        if (priority != entity.getPriority()) {
            entity.setAttribute(Cmd.PRIORITY_KEY, Integer.toString(priority), time);
        }
        // add entity
        Entity entityBefore = model.getEntities().get(entityBeforeId);
        ListIterator<Entity> it = this.content.listIterator();
        while (it.hasNext()) {
            Entity e = it.next();
            if (e.equals(entityBefore)) {
                it.previous();
                it.add(entity);
                out = true;
                break;
            }
        }
        if (this.grafic != null) {
            this.grafic.update();
        }
        return out;
    }

    /**
     * remove entity from list entityId time			operation-time
     */
    public boolean removeFromContainer(String entityId, long time) throws ModelException {
        Entity entity = model.getEntities().get(entityId);
        boolean out = this.content.remove(entity);
        if (this.grafic != null) {
            this.grafic.update();
        }
        return out;
    }


    /**
     * get content of list
     *
     * @return (entity - id, rank)
     */
    public String[][] getContent() {
        ListIterator<Entity> it = this.content.listIterator();
        String[][] out = new String[this.content.size()][2];
        int i = 0;
        while (it.hasNext()) {
            Entity entity = it.next();
            out[i][0] = entity.getId();
            out[i][1] = Integer.toString(entity.getPriority());
            i++;
        }
        return out;
    }
}
