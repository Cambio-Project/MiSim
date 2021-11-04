package desmoj.extensions.visualization2d.animation.processStation;

import java.awt.Dimension;
import java.awt.Point;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.ModelComponent;
import desmoj.core.simulator.TimeInstant;
import desmoj.extensions.visualization2d.animation.CmdGeneration;
import desmoj.extensions.visualization2d.animation.Comment;
import desmoj.extensions.visualization2d.animation.FormExt;
import desmoj.extensions.visualization2d.animation.Position;
import desmoj.extensions.visualization2d.animation.core.simulator.EntityBasicAnimation;
import desmoj.extensions.visualization2d.animation.core.simulator.ListInterface;
import desmoj.extensions.visualization2d.animation.core.simulator.ModelAnimation;
import desmoj.extensions.visualization2d.animation.internalTools.EntryAnimation;
import desmoj.extensions.visualization2d.animation.internalTools.EntryAnimationVector;
import desmoj.extensions.visualization2d.engine.command.Command;
import desmoj.extensions.visualization2d.engine.command.CommandException;
import desmoj.extensions.visualization2d.engine.command.Parameter;
import desmoj.extensions.visualization2d.engine.model.List;

/**
 * Animation of ProcessStation, there are some SimProcesses as Resource and some as processed Entities
 *
 * @param <Proc> SimProcesses as processed Entity
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
public class ProcessStationResAbstrAnimation
    <Proc extends EntityBasicAnimation> extends ModelComponent {

    private final boolean showInAnimation;
    private CmdGeneration cmdGen = null;
    private Model model = null;
    private String name = null;
    private EntryAnimationVector<Proc, EntityBasicAnimation> entries = null;
    private final int resTotal;
    private final String id;

    /**
     * Build a ProcessStation. This Station contains some process entries. Each Entry contains some animated process
     * entities. For each entry, the no of used resources are shown. With insert a new process entry is created and with
     * remove a process entry will be deleted. A process entry looks like a working station.
     *
     * @param owner
     * @param name
     * @param resName
     * @param resTotal
     * @param pos
     * @param form
     * @param showInAnimation
     */
    public ProcessStationResAbstrAnimation(ModelAnimation owner, String name,
                                           String resName, int resTotal, Position pos, FormExt form,
                                           boolean showInAnimation) {
        this(owner, name, null, resName, resTotal, pos, form, null, showInAnimation);
    }

    /**
     * Build a ProcessStation. This Station contains some process entries. Each Entry contains some animated process
     * entities. For each entry, the no of used resources are shown. With insert a new process entry is created and with
     * remove a process entry will be deleted. A process entry looks like a working station.
     *
     * @param owner
     * @param name
     * @param comment
     * @param resName
     * @param resTotal
     * @param pos
     * @param form
     * @param showInAnimation
     */
    public ProcessStationResAbstrAnimation(ModelAnimation owner, String name, Comment comment,
                                           String resName, int resTotal, Position pos, FormExt form,
                                           boolean showInAnimation) {
        this(owner, name, comment, resName, resTotal, pos, form, null, showInAnimation);
    }

    /**
     * Build a ProcessStation. This Station contains some process entries. Each Entry contains some animated process
     * entities. For each entry, the no of used resources are shown. With insert a new process entry is created and with
     * remove a process entry will be deleted. A process entry looks like a working station.
     *
     * @param owner
     * @param name
     * @param resName
     * @param resTotal
     * @param pos
     * @param form
     * @param listId          not used
     * @param showInAnimation
     */
    @Deprecated
    public ProcessStationResAbstrAnimation(ModelAnimation owner, String name,
                                           String resName, int resTotal, Position pos, FormExt form,
                                           String listId, boolean showInAnimation) {
        this(owner, name, null, resName, resTotal, pos, form, null, showInAnimation);
    }

    /**
     * Build a ProcessStation. This Station contains some process entries. Each Entry contains some animated process
     * entities. For each entry, the no of used resources are shown. With insert a new process entry is created and with
     * remove a process entry will be deleted. A process entry looks like a working station.
     *
     * @param owner           used model
     * @param name            name of process-station
     * @param resName         name of resource (for information only)
     * @param resTotal        total nr of available resources
     * @param pos             middle point of animation object
     * @param form            form of animation object
     * @param list            queue from where process-entities are coming, maybe null
     * @param showInAnimation switch animation on or off
     */

    public ProcessStationResAbstrAnimation(ModelAnimation owner, String name,
                                           String resName, int resTotal, Position pos, FormExt form, ListInterface list,
                                           boolean showInAnimation) {
        this(owner, name, null, resName, resTotal, pos, form, list,
            showInAnimation);
    }

    /**
     * Build a ProcessStation. This Station contains some process entries. Each Entry contains some animated process
     * entities. For each entry, the no of used resources are shown. With insert a new process entry is created and with
     * remove a process entry will be deleted. A process entry looks like a working station.
     *
     * @param owner           used model
     * @param name            name of process-station
     * @param comment
     * @param resName         name of resource (for information only)
     * @param resTotal        total nr of available resources
     * @param pos             middle point of animation object
     * @param form            form of animation object
     * @param list            queue from where process-entities are coming, maybe null
     * @param commentStyle
     * @param commentSize
     * @param commentColor
     * @param commentSizeExt
     * @param showInAnimation switch animation on or off
     */
    public ProcessStationResAbstrAnimation(ModelAnimation owner, String name, Comment comment,
                                           String resName, int resTotal, Position pos, FormExt form, ListInterface list,
                                           boolean showInAnimation) {
        super(owner, name);
        this.model = owner;
        this.name = name;
        this.resTotal = resTotal;
        this.cmdGen = owner.getCmdGen();
        this.entries = new EntryAnimationVector<Proc, EntityBasicAnimation>();
        TimeInstant simTime = this.model.presentTime();
        boolean init = this.cmdGen.isInitPhase();
        Command c;
        Point p = pos.getPoint();
        Dimension deltaSize = form.getDeltaSize();
        this.id = this.cmdGen.createInternId(name);

        this.showInAnimation = showInAnimation;
        if (this.showInAnimation) {
            try {
                if (init) {
                    c = Command.getCommandInit("createProcessNew", this.cmdGen.getAnimationTime(simTime));
                } else {
                    c = Command.getCommandTime("createProcessNew", this.cmdGen.getAnimationTime(simTime));
                }
                c.addParameter("ProcessId", this.id);
                c.addParameter("Name", name);
                c.addParameter("Abstract", "");
                c.addParameter("ResourceType", resName);
                c.addParameter("ResourceTotal", Integer.toString(this.resTotal));
                if (list != null) {
                    c.addParameter("ListId", List.PREFIX_QUEUE + list.getInternId());
                }
                String[] point = {pos.getView(), Integer.toString(p.x), Integer.toString(p.y)};
                c.addParameter("Point", Parameter.cat(point));
                c.addParameter("DefaultEntityType", form.getDefaultType());
                c.addParameter("AnzVisible", Integer.toString(form.getNrVisible()));
                c.addParameter("Form", form.isHorizontal() ? "horizontal" : "vertikal");
                c.addParameter("ShowResources", "");
                if (deltaSize != null) {
                    String[] deltaSize1 = {Integer.toString(deltaSize.width), Integer.toString(deltaSize.height)};
                    c.addParameter("DeltaSize", Parameter.cat(deltaSize1));
                }
                if (comment != null) {
                    c.addParameter("Comment", Parameter.cat(comment.getProperties()));
                }
                c.setRemark(this.getGeneratedBy(ProcessStationResAbstrAnimation.class.getSimpleName()));
                this.cmdGen.checkAndLog(c);
                cmdGen.write(c);

            } catch (CommandException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Create a new process entry with process entities.
     *
     * @param procEntities Array of process entities
     * @param needAnzRes   No of resources, that this entry needs.
     * @return true, when successful
     */
    public boolean insert(java.util.List<Proc> procEntities, int needAnzRes) {
        TimeInstant simTime = this.model.presentTime();
        boolean init = this.cmdGen.isInitPhase();
        Command c;
        this.entries.add(new EntryAnimation<Proc, EntityBasicAnimation>(procEntities, null, new Integer(needAnzRes)));
        boolean out = true;
        String[] entityIds = new String[procEntities.size()];
        for (int i = 0; i < procEntities.size(); i++) {
            entityIds[i] = procEntities.get(i).getName();
        }
        if (this.showInAnimation) {
            try {
                if (init) {
                    c = Command.getCommandInit("setProcessNew", this.cmdGen.getAnimationTime(simTime));
                } else {
                    c = Command.getCommandTime("setProcessNew", this.cmdGen.getAnimationTime(simTime));
                }
                c.addParameter("ProcessId", this.id);
                c.addParameter("AddProcEntity", Parameter.cat(entityIds));
                c.addParameter("AddResAnz", Integer.toString(needAnzRes));
                c.setRemark(ProcessStationResAbstrAnimation.class.getSimpleName());
                cmdGen.checkAndLog(c);
                cmdGen.write(c);
            } catch (CommandException e1) {
                e1.printStackTrace();
                out = false;
            }
        }
        return out;
    }


    /**
     * Remove the process entry which contains the process entity e. All entities of the removed entry are removed
     * also.
     *
     * @param e
     * @return the removed Entry, null when not successful
     */
    public EntryAnimation<Proc, EntityBasicAnimation> remove(Proc e) {
        TimeInstant simTime = this.model.presentTime();
        boolean init = this.cmdGen.isInitPhase();
        Command c;
        EntryAnimation<Proc, EntityBasicAnimation> out = this.entries.remove(e);
        if (this.showInAnimation) {
            try {
                if (init) {
                    c = Command.getCommandInit("setProcessNew", this.cmdGen.getAnimationTime(simTime));
                } else {
                    c = Command.getCommandTime("setProcessNew", this.cmdGen.getAnimationTime(simTime));
                }
                c.addParameter("ProcessId", this.id);
                c.addParameter("RemoveEntity", e.getName());
                c.setRemark(ProcessStationResAbstrAnimation.class.getSimpleName());
                cmdGen.checkAndLog(c);
                cmdGen.write(c);
            } catch (CommandException e1) {
                e1.printStackTrace();
                out = null;
            }
        }
        return out;
    }

    /**
     * Gives the number of available Resources
     *
     * @return
     */
    public int getResAvailable() {
        return this.resTotal - this.entries.getTotalNoRes();
    }

    /**
     * Give the actual Resource Utilization
     *
     * @return
     */
    public double getResUtilization() {
        //System.out.println("total: "+this.resTotal+"  used: "+this.entries.getTotalNoRes());
        return (double) this.entries.getTotalNoRes() / (double) this.resTotal;
    }

    /**
     * Gives nr of entries in station.
     *
     * @return
     */
    public int length() {
        return this.entries.length();
    }

    /**
     * Gives names of Proc entities in entry i.
     *
     * @param i
     * @return
     */
    public java.util.List<String> getProcNames(int i) {
        return this.entries.getProcNames(i);
    }

    /**
     * Gives names of Res entities in entry i. For each abstract entity an empty string is given.
     *
     * @param i
     * @return
     */
    public java.util.List<String> getResourceNames(int i) {
        return this.entries.getResourceNames(i);
    }


    private String getGeneratedBy(String name) {
        String out = "generated by " + name + " and called by ";
        if (this.currentSimProcess() != null) {
            out += this.currentSimProcess().getName();
        } else {
            out += this.currentModel().getName();
        }
        return out;
    }

}
