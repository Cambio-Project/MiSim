package desmoj.extensions.visualization2d.engine.command;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;

import desmoj.extensions.visualization2d.engine.model.Model;
import desmoj.extensions.visualization2d.engine.model.ModelException;


/**
 * Command extends CommandFrame and implements execute(). Command is the connection to animation.model.
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
public class Command extends CommandFrame {

    /**
     * Constructor for CommandFrame, used for template generation used by CommandFactory.createCommand(cmd, remark,
     * parameter).
     *
     * @param cmd       command-name
     * @param remark    remark about template
     * @param parameter Array of Parameter's where each parameter must have a different type
     */
    public Command(String cmd, String remark, Parameter[] parameter) {
        super(cmd, remark, parameter);
    }

    /**
     * Get a clone of command-template with name cmd for init-phase
     *
     * @param cmd
     * @return clone of command-template with name
     * @throws CommandException
     */
    public static Command getCommandInit(String cmd, long initTime) throws CommandException {
        return (Command) CommandFrame.getCommandInit(cmd, initTime);
    }

    /**
     * Get a clone of command-template with name cmd for run-phase time-value
     *
     * @param cmd
     * @param time
     * @return clone of command-template
     * @throws CommandException
     */
    public static Command getCommandTime(String cmd, long time) throws CommandException {
        return (Command) CommandFrame.getCommandTime(cmd, time);
    }


    // ---to fit for every command, do so in CommandSyntax ---------------------

    /**
     * only for testing
     *
     * @param args no args
     */
    public static void main(String[] args) {
        BufferedReader f;
        PrintWriter p;
        Command c;

		/*
		try{
		System.out.println(Command.writeTemplates());
		}catch(CommandException e){
			e.printStackTrace();
		}
		*/


        try {
            f = new BufferedReader(new FileReader("C:/EclipseSimulationAnimation/Animation1/Test.cmds"));
            //Model.setViewer(new javax.swing.JFrame());
            //Model.setSimulationTime(new animation.viewer.SimulationTime(0, 1000,1.0));
            //Model m = Model.getInstance();
            //Command.setModel(m);
			/*
			Command.readInit(f);
			System.out.println("Ende Init");
			Command.readUntilTime(f, 50);
			System.out.println("Ende 50");
			Command.readUntilTime(f, 51);
			System.out.println("Ende 51");
			Command.readUntilTime(f, 52);
			System.out.println("Ende 52");
			Command.readUntilTime(f, 100);
			System.out.println("Ende 100");
			*/
            f.close();
        } catch (java.io.IOException eio) {
            eio.printStackTrace();
        } catch (CommandException ec) {
            ec.printStackTrace();
        } catch (ModelException em) {
            em.printStackTrace();
        }

		/*
		// Schreiben auf WriteBuffer
		try{
			c = Command.getCommandInit("createEntity");
			c.addParameter("EntityId", "schulze");
			c.setRemark("in init");
			c.syntaxCheck();
			//System.out.println(c.toString());
			Command.write(c);

			c = Command.getCommandTime("goInBuffer", 100);
			String[] a = {"schulze", "m", "bb"};
			c.addParameterCat("EntityId", a);
			System.out.println(c.getParameterSplit("EntityId", 0)[0]);
			c.setRemark("in run");
			c.syntaxCheck();
			Command.write(c);
		}catch(CommandException e){
			e.printStackTrace();
		}

		// Ausgabe des WriteBuffer's
		try{
			p	= new PrintWriter(new BufferedWriter(new FileWriter("C:/EclipseSimulationAnimation/Animation/Write1.cmds")));
			Command.flush(p);
			p.close();
		}catch(java.io.IOException e){
			e.printStackTrace();
		}
		*/

    }
    // ---to fit for each command, fit also CommandSyntax ---------------------

    /**
     * This method describes for every command-type, how it's modeled in animation.model.Model. this method must include
     * a case for every command-type.
     */
    public void execute(Model model) throws CommandException, ModelException {
        //System.out.println("Command.execute   init: "+this.isInit()+"   Cmd: "+this.getCmd());

        if (this.getCmd().equals("createModelBasisData")) {
            String[] projectName = this.getParameterType("ProjectName").getValues();
            String[] projectURL = this.getParameterType("ProjectURL").getValues();
            String[] projectIconId = this.getParameterType("ProjectIconId").getValues();
            String[] modelName = this.getParameterType("ModelName").getValues();
            String[] modelAuthor = this.getParameterType("ModelAuthor").getValues();
            String[] modelDate = this.getParameterType("ModelDate").getValues();
            String[] modelDescription = this.getParameterType("ModelDescription").getValues();
            String[] modelRemark = this.getParameterType("ModelRemark").getValues();
            String[] modelLicense = this.getParameterType("ModelLicense").getValues();
            String[] desmojVersion = this.getParameterType("DesmojVersion").getValues();
            String[] desmojLicense = this.getParameterType("DesmojLicense").getValues();
            String[] desmojLicenseURL = this.getParameterType("DesmojLicenseURL").getValues();
            model.createModelBasisData(projectName, projectURL, projectIconId, modelName[0],
                modelAuthor[0], modelDate, modelDescription, modelRemark, modelLicense,
                desmojVersion, desmojLicense, desmojLicenseURL, this.isInit(), this.getTime());

        } else if (this.getCmd().equals("createSimTimeBounds")) {
            String[] begin = this.getParameterType("Begin").getValues();
            String[] end = this.getParameterType("End").getValues();
            String[] timeZone = this.getParameterType("TimeZone").getValues();
            String[] speed = this.getParameterType("Speed").getValues();
            model.createSimulationTimeBounds(begin[0], end, timeZone, speed[0], this.isInit(), this.getTime());

        } else if (this.getCmd().equals("createImage")) {
            String[] imageId = this.getParameterType("ImageId").getValues();
            String[] file = this.getParameterType("File").getValues();
            model.createImage(imageId[0], file[0], this.isInit(), this.getTime());

        } else if (this.getCmd().equals("createEntityTyp")) {
            String[] entityTypId = this.getParameterType("EntityTypId").getValues();
            String[] iconWidth = this.getParameterType("IconWidth").getValues();
            String[] iconHeigth = this.getParameterType("IconHeigth").getValues();
            String[] posibleStates = this.getParameterType("PossibleState").getValues();
            String[] posibleAttributes = this.getParameterType("PossibleAttribute").getValues();
            String[] show = this.getParameterType("Show").getValues();
            model.createEntityTyp(entityTypId[0], iconWidth[0], iconHeigth[0], posibleStates, posibleAttributes,
                show[0], this.isInit(), this.getTime());

        } else if (this.getCmd().equals("createBackgroundElement")) {
            String[] backgroundElementId = this.getParameterType("BgElemId").getValues();
            String[] name = this.getParameterType("Name").getValues();
            String[] text = this.getParameterType("Text").getValues();
            String[] topLeft = this.getParameterType("TopLeft").getValues();
            String[] bottomRight = this.getParameterType("BottomRight").getValues();
            String[] middle = this.getParameterType("Middle").getValues();
            String[] size = this.getParameterType("Size").getValues();
            String[] foreground = this.getParameterType("Foreground").getValues();
            String[] background = this.getParameterType("Background").getValues();
            String[] level = this.getParameterType("Level").getValues();
            String[] imageId = this.getParameterType("ImageId").getValues();
            model.createBackgroundElement(backgroundElementId[0], name, text, topLeft, bottomRight, middle, size,
                foreground, background, level[0], imageId, this.isInit(), this.getTime());

        } else if (this.getCmd().equals("setBackgroundElement")) {
            String[] backgroundElementId = this.getParameterType("BgElemId").getValues();
            String[] name = this.getParameterType("Name").getValues();
            String[] text = this.getParameterType("Text").getValues();
            String[] foreground = this.getParameterType("Foreground").getValues();
            String[] background = this.getParameterType("Background").getValues();
            String[] imageId = this.getParameterType("ImageId").getValues();
            model.setBackgroundElement(backgroundElementId[0], name, text, foreground, background, imageId,
                this.isInit(), this.getTime());

        } else if (this.getCmd().equals("createBackgroundLine")) {
            String[] backgroundLineId = this.getParameterType("BgLineId").getValues();
            String[] size = this.getParameterType("LineSize").getValues();
            String[] color = this.getParameterType("Color").getValues();
            String[] startPoint = this.getParameterType("StartPoint").getValues();
            String[] addPoint = this.getParameterType("AddPoint").getValues();
            String[] level = this.getParameterType("Level").getValues();
            model.createBackgroundLine(backgroundLineId[0], size, color, startPoint[0], addPoint, level[0],
                this.isInit(), this.getTime());

        } else if (this.getCmd().equals("createEntity")) {
            String[] entityId = this.getParameterType("EntityId").getValues();
            String[] name = this.getParameterType("Name").getValues();
            String[] entityTypeId = this.getParameterType("EntityTypeId").getValues();
            String[] velocity = this.getParameterType("Velocity").getValues();
            String[] state = this.getParameterType("State").getValues();
            String[] attribute = this.getParameterType("Attribute").getValues();
            String[] position = this.getParameterType("Position").getValues();
            model.createEntity(entityId[0], name, entityTypeId[0], velocity, state[0], attribute, position,
                this.isInit(), this.getTime());

        } else if (this.getCmd().equals("setEntity")) {
            String[] entityId = this.getParameterType("EntityId").getValues();
            String[] velocity = this.getParameterType("Velocity").getValues();
            String[] state = this.getParameterType("State").getValues();
            String[] attribute = this.getParameterType("Attribute").getValues();
            model.setEntity(entityId[0], velocity, state, attribute, this.isInit(), this.getTime());

        } else if (this.getCmd().equals("disposeEntity")) {
            String[] entityId = this.getParameterType("EntityId").getValues();
            model.disposeEntity(entityId[0], this.isInit(), this.getTime());

        } else if (this.getCmd().equals("createList")) {
            String[] listId = this.getParameterType("ListId").getValues();
            String[] name = this.getParameterType("Name").getValues();
            String[] defaultEntityType = this.getParameterType("DefaultEntityType").getValues();
            String[] numberOfVisible = this.getParameterType("NumberOfVisible").getValues();
            String[] form = this.getParameterType("Form").getValues();
            String[] point = this.getParameterType("Point").getValues();
            String[] deltaSize = this.getParameterType("DeltaSize").getValues();
            String[] comment = this.getParameterType("Comment").getValues();
            model.createList(listId[0], name, defaultEntityType[0], numberOfVisible[0], form[0], point[0], deltaSize,
                comment, this.isInit(), this.getTime());

        } else if (this.getCmd().equals("setList")) {
            String[] listId = this.getParameterType("ListId").getValues();
            String[] addEntity = this.getParameterType("AddEntity").getValues();
            String[] addEntityAfter = this.getParameterType("AddEntityAfter").getValues();
            String[] addEntityBefore = this.getParameterType("AddEntityBefore").getValues();
            String[] removeEntity = this.getParameterType("RemoveEntity").getValues();
            String[] removeAll = this.getParameterType("RemoveAll").getValues();
            model.setList(listId[0], addEntity, addEntityAfter, addEntityBefore, removeEntity, removeAll, this.isInit(),
                this.getTime());

        } else if (this.getCmd().equals("createStation")) {
            String[] stationId = this.getParameterType("StationId").getValues();
            String[] name = this.getParameterType("Name").getValues();
            String[] point = this.getParameterType("Point").getValues();
            model.createStation(stationId[0], name, point[0], this.isInit(), this.getTime());

        } else if (this.getCmd().equals("createRoute")) {
            String[] routeId = this.getParameterType("RouteId").getValues();
            String[] name = this.getParameterType("Name").getValues();
            String[] length = this.getParameterType("Length").getValues();
            String[] sourceStationId = this.getParameterType("SourceStationId").getValues();
            String[] sinkStationId = this.getParameterType("SinkStationId").getValues();
            String[] point = this.getParameterType("Point").getValues();
            String[] show = this.getParameterType("Show").getValues();
            String[] color = this.getParameterType("Color").getValues();
            String[] lineSize = this.getParameterType("LineSize").getValues();
            model.createRoute(routeId[0], name, length[0], sourceStationId[0], sinkStationId[0], point, show[0], color,
                lineSize[0], this.isInit(), this.getTime());

        } else if (this.getCmd().equals("setRoute")) {
            String[] routeId = this.getParameterType("RouteId").getValues();
            String[] addEntity = this.getParameterType("AddEntity").getValues();
            String[] removeEntity = this.getParameterType("RemoveEntity").getValues();
            String[] removeAll = this.getParameterType("RemoveAll").getValues();
            String[] show = this.getParameterType("Show").getValues();
            String[] color = this.getParameterType("Color").getValues();
            String[] lineSize = this.getParameterType("LineSize").getValues();
            model.setRoute(routeId[0], addEntity, removeEntity, removeAll, show, color, lineSize, this.isInit(),
                this.getTime());

        } else if (this.getCmd().equals("createProcess")) {
            String[] processId = this.getParameterType("ProcessId").getValues();
            String[] name = this.getParameterType("Name").getValues();
            String[] numberOfResEntity = this.getParameterType("NumberOfResEntity").getValues();
            String[] numberOfProcEntity = this.getParameterType("NumberOfProcEntity").getValues();
            String[] defaultResType = this.getParameterType("DefaultResEntityType").getValues();
            String[] defaultProcType = this.getParameterType("DefaultProcEntityType").getValues();
            String[] listId = this.getParameterType("ListId").getValues();
            String[] form = this.getParameterType("Form").getValues();
            String[] point = this.getParameterType("Point").getValues();
            model.createProcess(processId[0], name, numberOfResEntity[0], numberOfProcEntity[0],
                defaultResType[0], defaultProcType[0], listId, form[0], point[0], this.isInit(), this.getTime());

        } else if (this.getCmd().equals("setProcess")) {
            String[] processId = this.getParameterType("ProcessId").getValues();
            String[] addResEntity = this.getParameterType("AddResEntity").getValues();
            String[] removeResEntity = this.getParameterType("RemoveResEntity").getValues();
            String[] addProcEntity = this.getParameterType("AddProcEntity").getValues();
            String[] removeProcEntity = this.getParameterType("RemoveProcEntity").getValues();
            model.setProcess(processId[0], addResEntity, removeResEntity, addProcEntity, removeProcEntity,
                this.isInit(), this.getTime());

        } else if (this.getCmd().equals("createProcessNew")) {
            String[] processId = this.getParameterType("ProcessId").getValues();
            String[] name = this.getParameterType("Name").getValues();
            String[] abstractProc = this.getParameterType("Abstract").getValues();
            String[] resourceType = this.getParameterType("ResourceType").getValues();
            String[] resourceTotal = this.getParameterType("ResourceTotal").getValues();
            String[] listId = this.getParameterType("ListId").getValues();
            String[] point = this.getParameterType("Point").getValues();
            String[] defaultEntityType = this.getParameterType("DefaultEntityType").getValues();
            String[] anzVisible = this.getParameterType("AnzVisible").getValues();
            String[] form = this.getParameterType("Form").getValues();
            String[] showResources = this.getParameterType("ShowResources").getValues();
            String[] deltaSize = this.getParameterType("DeltaSize").getValues();
            String[] comment = this.getParameterType("Comment").getValues();
            model.createProcessNew(processId[0], name, abstractProc, resourceType,
                resourceTotal, listId, point[0], defaultEntityType[0], anzVisible[0],
                form[0], showResources, deltaSize, comment, this.isInit(), this.getTime());

        } else if (this.getCmd().equals("setProcessNew")) {
            String[] processId = this.getParameterType("ProcessId").getValues();
            String[] addProcEntity = this.getParameterType("AddProcEntity").getValues();
            String[] addResEntity = this.getParameterType("AddResEntity").getValues();
            String[] addResAnz = this.getParameterType("AddResAnz").getValues();
            String[] removeEntity = this.getParameterType("RemoveEntity").getValues();
            model.setProcessNew(processId[0], addProcEntity, addResEntity, addResAnz, removeEntity, this.isInit(),
                this.getTime());

        } else if (this.getCmd().equals("createResource")) {
            String[] resourceId = this.getParameterType("ResourceId").getValues();
            String[] name = this.getParameterType("Name").getValues();
            String[] resourceType = this.getParameterType("ResourceType").getValues();
            String[] resourceTotal = this.getParameterType("ResourceTotal").getValues();
            String[] point = this.getParameterType("Point").getValues();
            String[] defaultEntityType = this.getParameterType("DefaultEntityType").getValues();
            String[] anzVisible = this.getParameterType("AnzVisible").getValues();
            String[] form = this.getParameterType("Form").getValues();
            String[] deltaSize = this.getParameterType("DeltaSize").getValues();
            model.createResource(resourceId[0], name, resourceType, resourceTotal[0],
                point[0], defaultEntityType[0], anzVisible[0], form[0], deltaSize, this.isInit(), this.getTime());

        } else if (this.getCmd().equals("setResource")) {
            String[] resourceId = this.getParameterType("ResourceId").getValues();
            String[] provide = this.getParameterType("Provide").getValues();
            String[] takeProcess = this.getParameterType("TakeProcess").getValues();
            String[] takeBackProcess = this.getParameterType("TakeBackProcess").getValues();
            model.setResource(resourceId[0], provide, takeProcess, takeBackProcess, this.isInit(), this.getTime());

        } else if (this.getCmd().equals("createStock")) {
            String[] stockId = this.getParameterType("StockId").getValues();
            String[] name = this.getParameterType("Name").getValues();
            String[] capacity = this.getParameterType("Capacity").getValues();
            String[] initialUnits = this.getParameterType("InitialUnits").getValues();
            String[] point = this.getParameterType("Point").getValues();
            String[] defaultEntityType = this.getParameterType("DefaultEntityType").getValues();
            String[] anzVisible = this.getParameterType("AnzVisible").getValues();
            String[] form = this.getParameterType("Form").getValues();
            String[] deltaSize = this.getParameterType("DeltaSize").getValues();
            model.createStock(stockId[0], name, capacity[0], initialUnits[0],
                point[0], defaultEntityType[0], anzVisible[0], form[0], deltaSize, this.isInit(), this.getTime());

        } else if (this.getCmd().equals("setStock")) {
            String[] stockId = this.getParameterType("StockId").getValues();
            String[] retrieveBegin = this.getParameterType("RetrieveBegin").getValues();
            String[] retrieveEnd = this.getParameterType("RetrieveEnd").getValues();
            String[] storeBegin = this.getParameterType("StoreBegin").getValues();
            String[] storeEnd = this.getParameterType("StoreEnd").getValues();
            model.setStock(stockId[0], retrieveBegin, retrieveEnd, storeBegin, storeEnd, this.isInit(), this.getTime());

        } else if (this.getCmd().equals("createBin")) {
            String[] binId = this.getParameterType("BinId").getValues();
            String[] name = this.getParameterType("Name").getValues();
            String[] initialUnits = this.getParameterType("InitialUnits").getValues();
            String[] point = this.getParameterType("Point").getValues();
            String[] defaultEntityType = this.getParameterType("DefaultEntityType").getValues();
            String[] anzVisible = this.getParameterType("AnzVisible").getValues();
            String[] form = this.getParameterType("Form").getValues();
            String[] deltaSize = this.getParameterType("DeltaSize").getValues();
            model.createBin(binId[0], name, initialUnits[0],
                point[0], defaultEntityType[0], anzVisible[0], form[0], deltaSize, this.isInit(), this.getTime());

        } else if (this.getCmd().equals("setBin")) {
            String[] binId = this.getParameterType("BinId").getValues();
            String[] retrieveBegin = this.getParameterType("RetrieveBegin").getValues();
            String[] retrieveEnd = this.getParameterType("RetrieveEnd").getValues();
            String[] store = this.getParameterType("Store").getValues();
            model.setBin(binId[0], retrieveBegin, retrieveEnd, store, this.isInit(), this.getTime());

        } else if (this.getCmd().equals("createWaitQueue")) {
            String[] waitQueueId = this.getParameterType("WaitQueueId").getValues();
            String[] name = this.getParameterType("Name").getValues();
            String[] point = this.getParameterType("Point").getValues();
            String[] defaultEntityType = this.getParameterType("DefaultEntityType").getValues();
            String[] anzVisible = this.getParameterType("AnzVisible").getValues();
            String[] form = this.getParameterType("Form").getValues();
            String[] deltaSize = this.getParameterType("DeltaSize").getValues();
            model.createWaitQueue(waitQueueId[0], name,
                point[0], defaultEntityType[0], anzVisible[0], form[0], deltaSize, this.isInit(), this.getTime());

        } else if (this.getCmd().equals("setWaitQueue")) {
            String[] waitQueueId = this.getParameterType("WaitQueueId").getValues();
            String[] insertMaster = this.getParameterType("InsertMaster").getValues();
            String[] insertSlave = this.getParameterType("InsertSlave").getValues();
            String[] cooperationBegin = this.getParameterType("CooperationBegin").getValues();
            String[] cooperationEnd = this.getParameterType("CooperationEnd").getValues();
            model.setWaitQueue(waitQueueId[0], insertMaster, insertSlave, cooperationBegin, cooperationEnd,
                this.isInit(), this.getTime());

        } else if (this.getCmd().equals("createStatistic")) {
            String[] statisticId = this.getParameterType("StatisticId").getValues();
            String[] name = this.getParameterType("Name").getValues();
            String[] typeData = this.getParameterType("TypeData").getValues();
            String[] typeIndex = this.getParameterType("TypeIndex").getValues();
            String[] aggregate = this.getParameterType("Aggregate").getValues();
            String[] timeBounds = this.getParameterType("TimeBounds").getValues();
            String[] valueBounds = this.getParameterType("ValueBounds").getValues();
            String[] point = this.getParameterType("Point").getValues();
            String[] histogramCells = this.getParameterType("HistogramCells").getValues();
            String[] typeAnimation = this.getParameterType("TypeAnimation").getValues();
            String[] isIntValue = this.getParameterType("IsIntValue").getValues();
            String[] deltaSize = this.getParameterType("DeltaSize").getValues();
            model.createStatistic(statisticId[0], name, typeData[0], typeIndex[0],
                aggregate, timeBounds[0], valueBounds[0], histogramCells[0], point[0], typeAnimation[0],
                isIntValue, deltaSize, this.isInit(), this.getTime());

        } else if (this.getCmd().equals("setStatistic")) {
            String[] statisticId = this.getParameterType("StatisticId").getValues();
            String[] value = this.getParameterType("Value").getValues();
            model.setStatistic(statisticId[0], value[0], this.isInit(), this.getTime());

        } else if (this.getCmd().equals("resetStatistic")) {
            String[] statisticId = this.getParameterType("StatisticId").getValues();
            model.resetStatistic(statisticId[0], this.isInit(), this.getTime());

        } else if (this.getCmd().equals("end")) {
            System.out.println("end of simulation");
        } else {
            throw new CommandException("Command.execute: Command unknown ", this.toString());
        }
    }
}
