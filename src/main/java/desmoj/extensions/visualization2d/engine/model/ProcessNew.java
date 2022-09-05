package desmoj.extensions.visualization2d.engine.model;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.util.LinkedList;
import java.util.List;

import desmoj.extensions.visualization2d.engine.modelGrafic.Grafic;
import desmoj.extensions.visualization2d.engine.modelGrafic.ProcessNewGrafic;


/**
 * Working-station with a line of resource-entities and a line of processed entities. In every step you can add a set of
 * process-entities and a set of resource-entities. This process-entities are processed together and use for this the
 * resources of the associated resource set.
 * <p>
 * There are two different Types of processes: In a non abstract process every involved process- and resource-entity is
 * stored explicitly. In an abstract process only the involved process-entities are stored explicitly. Here we assume,
 * all resource-entities are identical. Only the total no of available and no of used resources are stored.
 * <p>
 * NEW Version
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
public class ProcessNew implements Basic {

    public static final String PREFIX_PROCESS = "process:";
    public static final String PREFIX_RESOURCE = "resource:";
    /*
    public  static final int	TEXT_Style_Plain		= 0;
    public  static final int	TEXT_Style_Bold			= 1;
    public  static final int	TEXT_Style_Italic		= 2;

    public  static final int	TEXT_Size_Normal		= 10;
    public  static final int	TEXT_Size_Small			= 8;
    public  static final int	TEXT_Size_Big			= 20;
    */
    private final String id;
    private String name;
    private final boolean abstractResources;
    private String resourceType; //only for abstractResources
    private int resourceTotal;    //only for abstractResources
    private final List<ProcessEntry> entries;
    private final String listId;
    private Grafic grafic;
    private final Model model;
    private String commentText;
    private Font commentFont;
    private Color commentColor;
    private boolean commentSizeExt;

    /**
     * Constructor of abstract Processes.
     *
     * @param model         used animation.model.Model
     * @param id            processId
     * @param resourceType  resourceTypeName (only for information, its no Id)
     * @param resourceTotal total number of available resources (only for information)
     * @param listId        optional id of a list, which include all processed entities before
     */
    public ProcessNew(Model model, String praefix, String id, String resourceType, int resourceTotal, String listId) {
        this.model = model;
        this.id = praefix + id;
        this.name = null;
        this.abstractResources = true;
        this.resourceType = resourceType;
        this.resourceTotal = resourceTotal;
        this.entries = new LinkedList<ProcessEntry>();
        this.listId = listId;
        this.commentText = null;
        this.commentColor = Grafic.COLOR_FOREGROUND;
        this.commentFont = new Font("SansSerif", 0, 8);
        this.commentSizeExt = false;
        if (this.id != null) {
            model.getProcessNewes().add(this);
        }
    }

    /**
     * Constructor of an non-abstract Processes.
     *
     * @param id     processId
     * @param listId optional id of a list, which include all processed entities before
     */
    public ProcessNew(Model model, String praefix, String id, String listId) {
        this.model = model;
        this.id = praefix + id;
        this.name = null;
        this.abstractResources = false;
        this.entries = new LinkedList<ProcessEntry>();
        this.listId = listId;
        this.commentText = null;
        this.commentColor = Grafic.COLOR_FOREGROUND;
        this.commentFont = new Font("SansSerif", 0, 8);
        this.commentSizeExt = false;
        if (this.id != null) {
            model.getProcessNewes().add(this);
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

    public String getCommentText() {
        return this.commentText;
    }

    public void setCommentText(String text) {
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
     * informs about abstractness of process
     *
     * @return
     */
    public boolean isAbstractResource() {
        return this.abstractResources;
    }

    /**
     * get resource type. Null when process isn't abstract.
     *
     * @return
     */
    public String getResourceType() {
        return this.resourceType;
    }

    /**
     * sets resource type (only for information)
     *
     * @param type
     * @throws ModelException, when process is non abstract.
     */
    public void setResourceType(String type) throws ModelException {
        if (this.abstractResources) {
            this.resourceType = type;
        } else {
            throw new ModelException("a resourceType of an non-abstract resource is not allowed.");
        }
    }

    /**
     * gets total of resources. Has no information when process isn't abstract.
     *
     * @return
     */
    public int getResourceTotal() {
        return this.resourceTotal;
    }

    /**
     * sets total of resources.
     *
     * @param total
     * @throws ModelException, when process is non abstract.
     */
    public void setResourceTotal(int total) throws ModelException {
        if (this.abstractResources) {
            this.resourceTotal = total;
        } else {
            throw new ModelException("resourceTotal of an non-abstract resource is not allowed.");
        }
    }

    /**
     * compute no of used resources. Has no information when process isn't abstract.
     *
     * @return
     */
    public int getResourceUsed() {
        int out = 0;
        for (int i = 0; i < this.entries.size(); i++) {
            ProcessEntry pe = this.entries.get(i);
            out += pe.getNrResourceEntities();
        }
        return out;
    }

    /**
     * compute no. of free resources, as difference between total and used resources. Has no information when process
     * isn't abstract.
     *
     * @return
     */
    public int getResourceFree() {
        return this.resourceTotal - this.getResourceUsed();
    }

    /**
     * gets no. of entries (process-sets) actual processed.
     *
     * @return
     */
    public int getEntriesAnz() {
        return this.entries.size();
    }

    /**
     * gets entity-id's of process-set in entry with index
     *
     * @param index
     * @return
     */
    public String[] getProcessEntries(int index) {
        String[] out = null;
        if ((index >= 0) && (index < this.getEntriesAnz())) {
            out = this.entries.get(index).getProcessEntityIds();
        }
        return out;
    }

    /**
     * gets entity-id's of resource-set in entry with index
     *
     * @param index
     * @return
     */
    public String[] getResourceEntries(int index) {
        String[] out = null;
        if ((!this.abstractResources) && index >= 0 && index < this.getEntriesAnz()) {
            out = this.entries.get(index).getResourceEntityIds();
        }
        return out;
    }

    /**
     * gets no. of resources in resource-set in entry with index
     *
     * @param index
     * @return no. of resources in resource-set with index -1 when no resource-set with index
     */
    public int getResourceEntriesAnz(int index) {
        int out = -1;
        if (index >= 0 && index < this.getEntriesAnz()) {
            out = this.entries.get(index).getNrResourceEntities();
        }
        return out;
    }

    /**
     * adds set of process- and resource-entities to process. An additional entry is created. Only for non abstract
     * processes
     *
     * @param processEntityIds  array of process-entity-id's
     * @param resourceEntityIds array of resource-entity-id's
     * @param time              simulation-time
     * @throws ModelException, when an added entity isn't free
     */
    public void addEntry(String[] processEntityIds, String[] resourceEntityIds, long time) throws ModelException {
        Entity entity;
        if (!this.abstractResources) {
            ProcessEntry pe = new ProcessEntry();
            for (int i = 0; i < processEntityIds.length; i++) {
                // pruefen ob processEntityIds[i] frei ist
                entity = model.getEntities().get(processEntityIds[i]);
                if (entity != null && entity.isFree()) {
                    pe.addProcessEntity(processEntityIds[i]);
                    entity.changeContainer("Process", getId(), "add", time);
                } else {
                    throw new ModelException("processEntity: " + processEntityIds[i] + " does not exist or is in use");
                }
            }
            for (int i = 0; i < resourceEntityIds.length; i++) {
                // pruefen ob resourceEntityIds[i] frei ist
                entity = model.getEntities().get(resourceEntityIds[i]);
                if (entity != null && entity.isFree()) {
                    pe.addResourceEntity(resourceEntityIds[i]);
                    entity.changeContainer("Process", getId(), "add", time);
                } else {
                    throw new ModelException(
                        "resourceEntity: " + resourceEntityIds[i] + " does not exist or is in use");
                }
            }
            this.entries.add(pe);
            if (this.grafic != null) {
                ((ProcessNewGrafic) this.grafic).update();
            }
            //this.printEntries();
        }
    }

    /**
     * adds set of process- and no. of resources to process. An additional entry is created. Only for abstract
     * processes
     *
     * @param processEntityIds  array of process-entity-id's
     * @param resourceEntityAnz no. of resources used to process this process-entities
     * @param time              actual simulation-time
     * @throws ModelException, when an added entity isn't free
     */
    public void addEntry(String[] processEntityIds, int resourceEntityAnz, long time) throws ModelException {
        Entity entity;
        if (this.abstractResources) {
            ProcessEntry pe = new ProcessEntry();
            for (int i = 0; i < processEntityIds.length; i++) {
                // pruefen ob processEntityIds[i] frei ist
                entity = model.getEntities().get(processEntityIds[i]);
                if (entity != null && entity.isFree()) {
                    pe.addProcessEntity(processEntityIds[i]);
                    entity.changeContainer("Process", getId(), "add", time);
                } else {
                    throw new ModelException("processEntity: " + processEntityIds[i] + " does not exist or is in use");
                }
            }
            if (resourceEntityAnz >= 0 && resourceEntityAnz <= this.getResourceFree()) {
                pe.setNrResourceEntry(resourceEntityAnz);
            } else {
                throw new ModelException("resourceEntityAnz: " + resourceEntityAnz + " not allowed");
            }
            this.entries.add(pe);
            if (this.grafic != null) {
                ((ProcessNewGrafic) this.grafic).update();
            }
            //this.printEntries();
        }
    }

    /**
     * remove an entry(process- and resource-set) that include the entity with id entityId
     *
     * @param entityId entityId to find an entry
     * @param time     simulation time
     * @throws ModelException, when this.getId contains a Cmd.VALUE_SEPARATOR
     * @return out[0] array of removed process-id's out[1] array of removed resource-id's (only for non-abstract
     *     processes) this arrays are empty, when no entry to remove found.
     */
    public String[][] removeEntry(String entityId, long time) throws ModelException {
        String[][] out = new String[2][0];
        int index = this.findEntry(entityId);
        ProcessEntry pe = this.entries.get(index);
        String[] pei = pe.getProcessEntityIds();
        for (int i = 0; i < pei.length; i++) {
            model.getEntities().get(pei[i]).changeContainer("Process", this.getId(), "remove", time);
            model.getEntities().get(pei[i]).changeContainer("", "free", "", time);
        }
        if (!this.abstractResources) {
            String[] rei = pe.getResourceEntityIds();
            for (int i = 0; i < rei.length; i++) {
                model.getEntities().get(rei[i]).changeContainer("Process", this.getId(), "remove", time);
                model.getEntities().get(rei[i]).changeContainer("", "free", "", time);
            }
        }
        if (index != -1) {
            this.removeEntry(index);
            out[0] = pe.getProcessEntityIds();
            out[1] = pe.getResourceEntityIds();
        }
        if (this.grafic != null) {
            ((ProcessNewGrafic) this.grafic).update();
        }
        //this.printEntries();
        return out;
    }

    /**
     * check if an entity with id stored in process
     *
     * @param id
     * @return
     */
    public boolean contains(String id) {
        return (this.findEntry(id) != -1);
    }

    /**
     * remove entry with index
     *
     * @param index
     * @return
     */
    private boolean removeEntry(int index) {
        boolean out = false;
        if (index >= 0 && index < this.getEntriesAnz()) {
            this.entries.remove(index);
            out = true;
        }
        return out;
    }

    /**
     * search entry that contains entity with entityId
     *
     * @param entityId
     * @return index of entry or -1 when not found
     */
    private int findEntry(String entityId) {
        boolean found = false;
        for (int i = 0; i < this.getEntriesAnz(); i++) {
            ProcessEntry pe = this.entries.get(i);
            String[] ids = pe.getProcessEntityIds();
            for (int j = 0; j < ids.length; j++) {
                if (ids[j].equals(entityId)) {
                    found = true;
                    break;
                }
            }
            if (!found && !this.abstractResources) {
                ids = pe.getResourceEntityIds();
                for (int j = 0; j < ids.length; j++) {
                    if (ids[j].equals(entityId)) {
                        found = true;
                        break;
                    }
                }
            }
            if (found) {
                return i;
            }
        }
        return -1;  //not found
    }

    /**
     * makes a control-print on System.out
     */
    public void printEntries() {
        String prefix = "ProcessNew: ";
        System.out.println(prefix + "Id					:" + this.getId());
        System.out.println(prefix + "Name					:" + this.getName());
        System.out.println(prefix + "ListId				:" + this.getListId());
        System.out.println(prefix + "EntriesAnz			:" + this.getEntriesAnz());
        System.out.println(prefix + "isAbstractResource	:" + this.isAbstractResource());
        if (this.isAbstractResource()) {
            System.out.println(prefix + "RessourceType		:" + this.getResourceType());
            System.out.println(
                prefix + "Resources free-used-total   :" + this.getResourceFree() + "-" + this.getResourceUsed() + "-" +
                    this.getResourceTotal());
        }
        for (int i = 0; i < this.getEntriesAnz(); i++) {
            System.out.print("Index: " + i + "  ProcessEntries: ");
            String[] ids = this.getProcessEntries(i);
            for (int j = 0; j < ids.length; j++) {
                System.out.print(ids[j] + ", ");
            }
            System.out.print("Resources  Anz: " + this.getResourceEntriesAnz(i) + " : ");
            if (!this.abstractResources) {
                System.out.print("  ResourceEntries: ");
                ids = this.getResourceEntries(i);
                for (int j = 0; j < ids.length; j++) {
                    System.out.print(ids[j] + ", ");
                }
            }
            System.out.println();
        }
        System.out.println();
        System.out.println("---------------------------------");
    }

    /**
     * create a ProcessGrafic
     *
     * @param viewId              Id of view
     * @param x                   middle point x-coordinate
     * @param y                   middle point y-coordinate
     * @param defaultEntityTypeId for sizing
     * @return ProcessGrafic
     */
    public Grafic createGrafic(String viewId, int x, int y, String defaultEntityTypeId,
                               int anzVisible, boolean horizontal, boolean showResources, Dimension deltaSize) {
        this.grafic = new ProcessNewGrafic(this, viewId, new Point(x, y), defaultEntityTypeId, anzVisible, horizontal,
            showResources, deltaSize);
        return this.grafic;
    }

    /**
     * get ProcessGrafic, created before
     */
    public Grafic getGrafic() {
        return grafic;
    }

    /**
     * Inner class, that stored in entry
     *
     * @author tian
     */
    class ProcessEntry {
        List<String> resourceEntityIds;
        List<String> processEntityIds;

        public ProcessEntry() {
            this.processEntityIds = new LinkedList<String>();
            this.resourceEntityIds = new LinkedList<String>();
        }

        public void addProcessEntity(String id) {
            this.processEntityIds.add(id);
        }

        public void addResourceEntity(String id) {
            this.resourceEntityIds.add(id);
        }

        public int setNrResourceEntry(int nr) {
            this.resourceEntityIds.clear();
            for (int i = 0; i < nr; i++) {
                this.resourceEntityIds.add("abstract resource");
            }
            return this.resourceEntityIds.size();
        }

        public String[] getProcessEntityIds() {
            String[] out = new String[this.processEntityIds.size()];
            out = this.processEntityIds.toArray(out);
            return out;
        }

        public String[] getResourceEntityIds() {
            String[] out = new String[this.resourceEntityIds.size()];
            out = this.resourceEntityIds.toArray(out);
            return out;
        }

        public int getNrResourceEntities() {
            return this.resourceEntityIds.size();
        }
    }


}
