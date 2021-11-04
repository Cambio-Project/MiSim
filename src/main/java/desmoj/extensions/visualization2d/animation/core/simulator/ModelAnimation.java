package desmoj.extensions.visualization2d.animation.core.simulator;

import java.util.Enumeration;
import java.util.Vector;

import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import desmoj.extensions.visualization2d.animation.CmdGeneration;
import desmoj.extensions.visualization2d.animation.internalTools.EntityTypeAnimation;
import desmoj.extensions.visualization2d.animation.internalTools.SimulationException;
import desmoj.extensions.visualization2d.engine.command.Command;
import desmoj.extensions.visualization2d.engine.command.Parameter;

/**
 * Animated version of model. ModelAnimation extends Model about a CmdGeneration instance and a showInAnimation switch.
 * A animated model must extend form ModelAnimation.
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
public abstract class ModelAnimation extends Model {

    private final CmdGeneration cmdGen;
    private final boolean showInAnimation;
    private String modelProjectName;
    private String modelProjectURL;
    private String modelProjectIconId;
    private String modelAuthor;
    private String modelDate;
    private String modelDescription;
    private String modelLicense;
    private String generatedBy;
    private final Vector<String> remarks;
    private final Vector<String> icons;
    private final Vector<EntityTypeAnimation> entityTypes;


    /**
     * ModelAnimation extends Model about a CmdGeneration instance and a showInAnimation switch. In constructor you must
     * define: setModelAuthor(..), setModelDate(), addRemark(), addIcon() and setGeneratedBy()
     *
     * @param owner           supermodel, may be null
     * @param name            name of model
     * @param cmdGen          CmdGeneration object
     * @param showInReport    switch for report generation
     * @param showInTrace     switch for trace generation
     * @param showInAnimation switch for cmds-file generation
     */
    public ModelAnimation(ModelAnimation owner, String name, CmdGeneration cmdGen,
                          boolean showInReport, boolean showInTrace, boolean showInAnimation) {
        super(owner, name, showInReport, showInTrace);
        this.cmdGen = cmdGen;
        this.showInAnimation = showInAnimation;

        this.modelAuthor = null;
        this.modelDate = null;
        this.modelDescription = null;
        this.modelLicense = null;
        this.modelProjectIconId = null;
        this.modelProjectName = null;
        this.modelProjectURL = null;
        this.generatedBy = null;
        this.remarks = new Vector<String>();
        this.icons = new Vector<String>();
        this.entityTypes = new Vector<EntityTypeAnimation>();
    }

    /**
     * place to init black-box components and there createAnimation Statements
     */
    public abstract void initAnimation();

    /**
     * overwrite presentTime from Model. Originally, before experiment start presentTime is 0. Now, in initPhase
     * presentTime is initTime (from cmdGeneration object)
     */
    public TimeInstant presentTime() {
        TimeInstant out = super.presentTime();
        if (this.cmdGen.isInitPhase()) {
            out = this.cmdGen.getInitTime();
        }
        return out;
    }

    /**
     * overwrites init from Model with: initCmds(), initEntityTypes(), and initAnimation()
     */
    public void init() {
        if (this.showInAnimation) {
            this.initCmds();
        }
        this.initAnimation();
    }

    /**
     * gives CmdGeneration Object
     *
     * @return
     */
    public CmdGeneration getCmdGen() {
        return this.cmdGen;
    }

    /**
     * gives showInAnimation switch
     *
     * @return
     */
    public boolean animationIsOn() {
        return this.showInAnimation;
    }

    protected void setModelProjectName(String projectName) {
        this.modelProjectName = Parameter.replaceSyntaxSign(projectName);
    }

    protected void setModelProjectURL(String projectUrl) {
        this.modelProjectURL = projectUrl;
    }

    protected void setModelProjectIconId(String iconId) {
        this.modelProjectIconId = iconId;
    }

    protected void setModelAuthor(String modelAuthor) {
        this.modelAuthor = Parameter.replaceSyntaxSign(modelAuthor);
    }

    protected void setModelDate(String modelDate) {
        this.modelDate = Parameter.replaceSyntaxSign(modelDate);
    }

    protected void setModelDescription(String description) {
        this.modelDescription = Parameter.replaceSyntaxSign(description);
    }

    protected void setModelLicense(String license) {
        this.modelLicense = Parameter.replaceSyntaxSign(license);
    }

    protected void setGeneratedBy(String generatedBy) {
        this.generatedBy = Parameter.replaceSyntaxSign(generatedBy);
    }

    protected void addRemark(String remark) {
        this.remarks.add(Parameter.replaceSyntaxSign(remark));
    }

    protected void addIcon(String id, String fileName) {
        String[] icon = {id, fileName};
        this.icons.add(Parameter.cat(icon));
    }

    protected String getRemark() {
        String out = "";
        for (int i = 0; i < this.remarks.size(); i++) {
            out += this.remarks.get(i) + " ";
        }
        return out;
    }

    protected void addEntityTypeAnimation(EntityTypeAnimation type) {
        this.entityTypes.add(type);
    }

    /**
     * generate "createModelBasisData" Command and EntityTypeAnimation Commands used by init()
     */
    private void initCmds() {
        this.checkData();
        Command c;
        long initTime = this.cmdGen.getAnimationTimeInit();

        c = Command.getCommandInit("createModelBasisData", initTime);
        c.addParameter("ModelName", this.getName());
        c.addParameter("ModelAuthor", this.modelAuthor);
        c.addParameter("ModelDate", this.modelDate);
        if (this.modelProjectName != null) {
            c.addParameter("ProjectName", this.modelProjectName);
        }
        if (this.modelProjectURL != null) {
            c.addParameter("ProjectURL", this.modelProjectURL);
        }
        if (this.modelProjectIconId != null) {
            c.addParameter("ProjectIconId", this.modelProjectIconId);
        }
        if (this.modelDescription != null) {
            c.addParameter("ModelDescription", this.modelDescription);
        }
        if (this.modelLicense != null) {
            c.addParameter("ModelLicense", this.modelLicense);
        }
        for (int i = 0; i < this.remarks.size(); i++) {
            c.addParameter("ModelRemark", this.remarks.get(i));
        }
        c.addParameter("DesmojVersion", Parameter.replaceSyntaxSign(Experiment.getDesmoJVersion()));
        c.addParameter("DesmojLicense", Parameter.replaceSyntaxSign(Experiment.getDesmoJLicense(false)));
        c.addParameter("DesmojLicenseURL", this.getLicenseURL());

        c.setRemark("generated by " + this.generatedBy);
        this.cmdGen.checkAndLog(c);
        cmdGen.write(c);

        for (int i = 0; i < this.icons.size(); i++) {
            c = Command.getCommandInit("createImage", initTime);
            String[] icon = Parameter.split(this.icons.get(i));
            c.addParameter("ImageId", icon[0]);
            c.addParameter("File", icon[1]);
            c.setRemark("generated by " + this.generatedBy);
            this.cmdGen.checkAndLog(c);
            cmdGen.write(c);
        }

        // create EntityTypes
        Enumeration<EntityTypeAnimation> en = this.entityTypes.elements();
        while (en.hasMoreElements()) {
            c = en.nextElement().getEntityTypeCmd(initTime);
            this.cmdGen.checkAndLog(c);
            cmdGen.write(c);
        }

    }

    /**
     * check setting of modelAuthor, modelDate and remarks used by initCmds()
     */
    private void checkData() {
        if (this.modelAuthor == null) {
            throw new SimulationException("ModelAnimation: please, use setModelAuthor.");
        }
        if (this.modelDate == null) {
            throw new SimulationException("ModelAnimation: please, use setModelDate.");
        }
        if (this.modelDescription == null) {
            throw new SimulationException("ModelAnimation: please, use setModelDescription.");
        }
    }

    private String getLicenseURL() {
        String licenseURL;
        licenseURL = Experiment.getDesmoJLicense(true);
        int first = licenseURL.indexOf('=') + 1;
        int last = licenseURL.indexOf('>');
        licenseURL = licenseURL.substring(first, last);
        return licenseURL;
    }

}
