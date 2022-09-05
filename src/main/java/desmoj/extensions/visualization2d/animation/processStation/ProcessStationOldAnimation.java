package desmoj.extensions.visualization2d.animation.processStation;

import java.awt.Dimension;
import java.awt.Point;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.ModelComponent;
import desmoj.core.simulator.TimeInstant;
import desmoj.extensions.visualization2d.animation.CmdGeneration;
import desmoj.extensions.visualization2d.animation.FormExt;
import desmoj.extensions.visualization2d.animation.Position;
import desmoj.extensions.visualization2d.animation.core.simulator.EntityBasicAnimation;
import desmoj.extensions.visualization2d.animation.core.simulator.ListInterface;
import desmoj.extensions.visualization2d.animation.core.simulator.ModelAnimation;
import desmoj.extensions.visualization2d.engine.command.Command;
import desmoj.extensions.visualization2d.engine.command.CommandException;
import desmoj.extensions.visualization2d.engine.command.Parameter;

/**
 * Animation of ProcessStation, there are some SimProcesses as Resource and some as processed Entities This class is
 * deprecated. Please use: ProcessStationNonAbstrResAnimation or ProcessStationAbstrResAnimation or
 * ProcessStationNoResAnimation
 *
 * @param <Res>  SimProcesses as Resource Entity
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
@Deprecated
public class ProcessStationOldAnimation<Res extends EntityBasicAnimation, Proc extends EntityBasicAnimation>
    extends ModelComponent {

    private final boolean showInAnimation;
    private CmdGeneration cmdGen = null;
    private Model model = null;
    private String name = null;
    private final String id;

    /**
     * Build a ProcessStation. This Station contains some process entries. Each Entry contains some animated process and
     * resource entities. With insert a new process entry is created and with remove a process entry will be deleted. A
     * process entry looks like a working station.
     *
     * @param owner
     * @param name
     * @param nrOfResEntity
     * @param defResEntityType
     * @param pos
     * @param form
     * @param showInAnimation
     */
    public ProcessStationOldAnimation(ModelAnimation owner, String name,
                                      int nrOfResEntity, String defResEntityType,
                                      Position pos, FormExt form, boolean showInAnimation) {
        this(owner, name, nrOfResEntity, defResEntityType, pos, form, (ListInterface) null, showInAnimation);
    }

    /**
     * Build a ProcessStation. This Station contains some process entries. Each Entry contains some animated process and
     * resource entities. With insert a new process entry is created and with remove a process entry will be deleted. A
     * process entry looks like a working station.
     *
     * @param owner
     * @param name
     * @param nrOfResEntity
     * @param defResEntityType
     * @param pos
     * @param form
     * @param listId           not used
     * @param showInAnimation
     */
    @Deprecated
    public ProcessStationOldAnimation(ModelAnimation owner, String name,
                                      int nrOfResEntity, String defResEntityType,
                                      Position pos, FormExt form, String listId, boolean showInAnimation) {
        this(owner, name, nrOfResEntity, defResEntityType, pos, form, (ListInterface) null, showInAnimation);
    }

    /**
     * Build a ProcessStation. This Station contains some process entries. Each Entry contains some animated process and
     * resource entities. With insert a new process entry is created and with remove a process entry will be deleted. A
     * process entry looks like a working station.
     *
     * @param owner            used model
     * @param name             name of process-station
     * @param pos              middle point of animation object
     * @param form             form of animation object
     * @param nrOfResEntity    max nr of resource-entities
     * @param defResEntityType default resource-entity-type
     * @param list             queue from where process-entities are coming, maybe null
     * @param showInAnimation  switch animation on or off
     */
    public ProcessStationOldAnimation(ModelAnimation owner, String name,
                                      int nrOfResEntity, String defResEntityType,
                                      Position pos, FormExt form,
                                      ListInterface list,
                                      boolean showInAnimation) {
        super(owner, name);
        this.model = owner;
        this.name = name;
        this.cmdGen = owner.getCmdGen();
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
                    c = Command.getCommandInit("createProcess", this.cmdGen.getAnimationTime(simTime));
                } else {
                    c = Command.getCommandTime("createProcess", this.cmdGen.getAnimationTime(simTime));
                }
                c.addParameter("ProcessId", this.id);
                c.addParameter("Name", name);
                c.addParameter("NumberOfResEntity", Integer.toString(nrOfResEntity));
                c.addParameter("NumberOfProcEntity", Integer.toString(form.getNrVisible()));
                c.addParameter("DefaultResEntityType", defResEntityType);
                c.addParameter("DefaultProcEntityType", form.getDefaultType());
                if (list != null) {
                    c.addParameter("ListId", list.getInternId());
                }
                c.addParameter("Form", form.isHorizontal() ? "horizontal" : "vertikal");
                c.addParameter("Point", pos.getView() + "|" + p.x + "|" + p.y);
                if (deltaSize != null) {
                    String[] deltaSize1 = {Integer.toString(deltaSize.width), Integer.toString(deltaSize.height)};
                    c.addParameter("DeltaSize", Parameter.cat(deltaSize1));
                }
                c.setRemark(this.getGeneratedBy(ProcessStationOldAnimation.class.getSimpleName()));
                this.cmdGen.checkAndLog(c);
                cmdGen.write(c);

            } catch (CommandException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * insert an resource e
     *
     * @param e
     * @return true, when successful
     */
    public boolean insertRes(Res e) {
        TimeInstant simTime = this.model.presentTime();
        boolean init = this.cmdGen.isInitPhase();
        Command c;
        boolean out = true;
        if (this.showInAnimation) {
            try {
                if (init) {
                    c = Command.getCommandInit("setProcess", this.cmdGen.getAnimationTime(simTime));
                } else {
                    c = Command.getCommandTime("setProcess", this.cmdGen.getAnimationTime(simTime));
                }
                c.addParameter("ProcessId", this.id);
                c.addParameter("AddResEntity", e.getName());
                c.setRemark(this.getGeneratedBy(ProcessStationOldAnimation.class.getSimpleName()));
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
     * remove an resource e
     *
     * @param e
     * @return true, when successful
     */
    public boolean removeRes(Res e) {
        TimeInstant simTime = this.model.presentTime();
        boolean init = this.cmdGen.isInitPhase();
        Command c;
        boolean out = true;
        if (this.showInAnimation) {
            try {
                if (init) {
                    c = Command.getCommandInit("setProcess", this.cmdGen.getAnimationTime(simTime));
                } else {
                    c = Command.getCommandTime("setProcess", this.cmdGen.getAnimationTime(simTime));
                }
                c.addParameter("ProcessId", this.id);
                c.addParameter("RemoveResEntity", e.getName());
                c.setRemark(this.getGeneratedBy(ProcessStationOldAnimation.class.getSimpleName()));
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
     * insert an process e
     *
     * @param e
     * @return true, when successful
     */
    public boolean insertProc(Proc e) {
        TimeInstant simTime = this.model.presentTime();
        boolean init = this.cmdGen.isInitPhase();
        Command c;
        boolean out = true;
        if (this.showInAnimation) {
            try {
                if (init) {
                    c = Command.getCommandInit("setProcess", this.cmdGen.getAnimationTime(simTime));
                } else {
                    c = Command.getCommandTime("setProcess", this.cmdGen.getAnimationTime(simTime));
                }
                c.addParameter("ProcessId", this.id);
                c.addParameter("AddProcEntity", e.getName());
                c.setRemark(this.getGeneratedBy(ProcessStationOldAnimation.class.getSimpleName()));
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
     * remove an process e
     *
     * @param e
     * @return true, when successful
     */
    public boolean removeProc(Proc e) {
        TimeInstant simTime = this.model.presentTime();
        boolean init = this.cmdGen.isInitPhase();
        Command c;
        boolean out = true;
        if (this.showInAnimation) {
            try {
                if (init) {
                    c = Command.getCommandInit("setProcess", this.cmdGen.getAnimationTime(simTime));
                } else {
                    c = Command.getCommandTime("setProcess", this.cmdGen.getAnimationTime(simTime));
                }
                c.addParameter("ProcessId", this.id);
                c.addParameter("RemoveProcEntity", e.getName());
                c.setRemark(this.getGeneratedBy(ProcessStationOldAnimation.class.getSimpleName()));
                cmdGen.checkAndLog(c);
                cmdGen.write(c);
            } catch (CommandException e1) {
                e1.printStackTrace();
                out = false;
            }
        }
        return out;
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
