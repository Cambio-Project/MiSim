package desmoj.extensions.visualization2d.animation.extensions.applicationDomains.production;

import java.util.Hashtable;

import desmoj.core.simulator.SimProcess;
import desmoj.core.simulator.TimeInstant;
import desmoj.extensions.visualization2d.animation.CmdGeneration;
import desmoj.extensions.visualization2d.animation.core.simulator.ModelAnimation;
import desmoj.extensions.visualization2d.animation.internalTools.EntityTypeAnimation;
import desmoj.extensions.visualization2d.engine.command.Command;
import desmoj.extensions.visualization2d.engine.command.CommandException;

/**
 * superclass to animate a restock process
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
public abstract class RestockProcessAnimation {

    protected SimProcess restock;

    /**
     * Create the producer that restocks the stock
     *
     * @param iconId          producers iconId, must declared in ModelAnimation Constructor
     * @param showInAnimation switch animation on or off
     */
    public void createAnimation(String iconId, boolean showInAnimation) {
        CmdGeneration cmdGen = ((ModelAnimation) this.restock.getModel()).getCmdGen();
        String state = "active";
        Hashtable<String, String> attribute = new Hashtable<String, String>();
        TimeInstant simTime = this.restock.getModel().presentTime();

        EntityTypeAnimation entityType = new EntityTypeAnimation();
        entityType.setId(this.restock.getName());
        entityType.setGenereratedBy(RestockProcessAnimation.class.getSimpleName());
        entityType.addPossibleState(state, iconId);
        Command entityTypeCmd = entityType.getEntityTypeCmd(cmdGen.getAnimationTime(simTime));
        cmdGen.checkAndLog(entityTypeCmd);
        cmdGen.write(entityTypeCmd);

        boolean init = cmdGen.isInitPhase();
        Command c;

        if (showInAnimation) {
            try {
                if (init) {
                    c = Command.getCommandInit("createEntity", cmdGen.getAnimationTime(simTime));
                } else {
                    c = Command.getCommandTime("createEntity", cmdGen.getAnimationTime(simTime));
                }
                c.addParameter("EntityId", this.restock.getName());
                c.addParameter("EntityTypeId", entityType.getId());
                if (state != null) {
                    c.addParameter("State", state);
                }
                c.setRemark("created by " + RestockProcessAnimation.class.getSimpleName());
                cmdGen.checkAndLog(c);
                cmdGen.write(c);
            } catch (CommandException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            attribute.put("name", this.restock.getName());
        }
    }

    public SimProcess getRestockProcess() {
        return this.restock;
    }

}