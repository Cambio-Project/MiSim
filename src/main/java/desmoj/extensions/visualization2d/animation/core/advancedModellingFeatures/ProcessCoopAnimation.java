package desmoj.extensions.visualization2d.animation.core.advancedModellingFeatures;


import desmoj.core.advancedModellingFeatures.ProcessCoop;
import desmoj.core.simulator.SimProcess;
import desmoj.core.simulator.TimeInstant;
import desmoj.extensions.visualization2d.animation.CmdGeneration;
import desmoj.extensions.visualization2d.animation.core.simulator.ModelAnimation;
import desmoj.extensions.visualization2d.engine.command.Command;
import desmoj.extensions.visualization2d.engine.command.CommandException;
import desmoj.extensions.visualization2d.engine.command.Parameter;

/**
 * This class is used in method cooperation in class WaitQueueanimation
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
public abstract class ProcessCoopAnimation extends ProcessCoop {

    private boolean showInAnimation;
    private CmdGeneration cmdGen = null;
    private String waitQueueAnimationId = null;

    /**
     * Constructor with same parameters as in ProcessCoop
     *
     * @param owner
     * @param name
     * @param showInTrace
     */
    public ProcessCoopAnimation(ModelAnimation owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
        this.cmdGen = owner.getCmdGen();
        this.showInAnimation = false;
    }

    /**
     * This animation is part of animation of WaitQueue
     *
     * @param waitQueueAnimation used WaitQueue
     */
    public void createAnimation(WaitQueueAnimation waitQueueAnimation) {
        this.showInAnimation = waitQueueAnimation.animationIsOn();
        this.waitQueueAnimationId = waitQueueAnimation.getInternId();
		/*
		if(! WaitingQueue.classContent.exist(waitQueueAnimationId))
			throw new SimulationException("WaitQueueAnimationId is unknown! id: "+waitQueueAnimationId);
		*/
    }

    /**
     * Implemention of cooperation method of ProcessCoop. It calls: beginCooperation, cooperationAnimation,
     * endCooperation
     */
    public void cooperation(SimProcess master, SimProcess slave) {
        //System.out.println("cooperation master: "+master.getName()+"  slave: "+slave.getName());
        this.beginCooperation(master, slave);
        this.cooperationAnimation(master, slave);
        this.endCooperation(master, slave);
    }

    /**
     * This method must be implement and describe the cooperation. The master is active and slave is passive.
     *
     * @param master
     * @param slave
     */
    public abstract void cooperationAnimation(SimProcess master, SimProcess slave);

    /**
     * Animate cooperation begin
     *
     * @param master
     * @param slave
     */
    private void beginCooperation(SimProcess master, SimProcess slave) {
        TimeInstant simTime = this.getModel().presentTime();
        boolean init = this.cmdGen.isInitPhase();
        Command c;
        String[] cooperationBegin = {master.getName(), slave.getName()};
        if (this.showInAnimation) {
            try {
                if (init) {
                    c = Command.getCommandInit("setWaitQueue", this.cmdGen.getAnimationTime(simTime));
                } else {
                    c = Command.getCommandTime("setWaitQueue", this.cmdGen.getAnimationTime(simTime));
                }
                c.addParameter("WaitQueueId", this.waitQueueAnimationId);
                c.addParameter("CooperationBegin", Parameter.cat(cooperationBegin));
                c.setRemark(this.getGeneratedBy(ProcessCoopAnimation.class.getSimpleName()));
                cmdGen.checkAndLog(c);
                cmdGen.write(c);
            } catch (CommandException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }


    /**
     * Animate cooperation end
     *
     * @param master
     * @param slave
     */
    private void endCooperation(SimProcess master, SimProcess slave) {
        TimeInstant simTime = this.getModel().presentTime();
        boolean init = this.cmdGen.isInitPhase();
        Command c;
        String[] cooperationBegin = {master.getName(), slave.getName()};
        if (this.showInAnimation) {
            try {
                if (init) {
                    c = Command.getCommandInit("setWaitQueue", this.cmdGen.getAnimationTime(simTime));
                } else {
                    c = Command.getCommandTime("setWaitQueue", this.cmdGen.getAnimationTime(simTime));
                }
                c.addParameter("WaitQueueId", this.waitQueueAnimationId);
                c.addParameter("CooperationEnd", Parameter.cat(cooperationBegin));
                c.setRemark(this.getGeneratedBy(ProcessCoopAnimation.class.getSimpleName()));
                cmdGen.checkAndLog(c);
                cmdGen.write(c);
            } catch (CommandException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
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
