package desmoj.extensions.visualization2d.engine;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import desmoj.extensions.visualization2d.engine.command.Command;
import desmoj.extensions.visualization2d.engine.command.CommandException;
import desmoj.extensions.visualization2d.engine.command.WriteCmds;


/**
 * Test-Application for writer testing. Only for devoloper. It's may be not correct.
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
public class WriteBsp0 extends WriteCmds {

    private static final String PATH = "path/";
    private static final String CMD_DATEI = PATH + "Bsp0" + Constants.FILE_EXTENSION_CMD;
    private static final String LOG_DATEI = PATH + "Bsp0" + Constants.FILE_EXTENSION_LOG_0;

    public WriteBsp0(URL iconUrl) {
        super(CMD_DATEI, LOG_DATEI, iconUrl);
        this.bsp_init(10);
        this.bsp_run();
        this.close();
    }

    public static void main(String[] args) {

        URL iconUrl = null;
        try {
            iconUrl = (new File("Bilder")).toURI().toURL();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        WriteBsp0 a = new WriteBsp0(iconUrl);
    }

    /**
     * Erzeugen der Cmd's fuer die Init-Phase
     */
    protected void bsp_init(long initTime) {
        Command c = null;

        try {
            c = Command.getCommandInit("createModelBasisData", initTime);
            c.addParameter("ModelName", "Test Generiert");
            c.addParameter("ModelAuthor", "Chr.Mueller");
            c.addParameter("ModelRemark", "Zeile 1");
            c.addParameter("ModelRemark", "Zeile 2");
            c.setRemark("in init");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);

            c = Command.getCommandInit("createSimTimeBounds", initTime);
            c.addParameter("Begin", "0");
            c.addParameter("End", "25000");
            c.addParameter("Speed", "2.0");
            c.setRemark("in init");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);

            c = Command.getCommandInit("createImage", initTime);
            c.addParameter("ImageId", "Duke0");
            c.addParameter("File", "Duke-0.gif");
            c.setRemark("in init");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);

            c = Command.getCommandInit("createImage", initTime);
            c.addParameter("ImageId", "active");
            c.addParameter("File", "active1.gif");
            c.setRemark("in init");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);

            c = Command.getCommandInit("createImage", initTime);
            c.addParameter("ImageId", "passive");
            c.addParameter("File", "passive.gif");
            c.setRemark("in init");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);

            c = Command.getCommandInit("createImage", initTime);
            c.addParameter("ImageId", "bussy");
            c.addParameter("File", "bussy.gif");
            c.setRemark("in init");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);

            c = Command.getCommandInit("createImage", initTime);
            c.addParameter("ImageId", "outOfOrder");
            c.addParameter("File", "outOfOrder.gif");
            c.setRemark("in init");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);

            c = Command.getCommandInit("createImage", initTime);
            c.addParameter("ImageId", "animationError");
            c.addParameter("File", "error.gif");
            this.checkAndLog(c);
            //System.out.println(c.toString());
            this.getCommandSequence().write(c);

            c = Command.getCommandInit("createImage", initTime);
            c.addParameter("ImageId", "Duke0");
            c.addParameter("File", "Duke-0.gif");
            c.setRemark("in init");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);

            c = Command.getCommandInit("createEntityTyp", initTime);
            c.addParameter("EntityTypId", "Patient");
            c.addParameter("IconWidth", "30");
            c.addParameter("IconHeigth", "30");
            c.addParameter("PossibleState", "active|active");
            c.addParameter("PossibleState", "passive|passive");
            c.addParameter("PossibleState", "bussy|bussy");
            c.addParameter("PossibleState", "outOfOrder|outOfOrder");
            c.addParameter("PossibleAttribute", "myName");
            c.addParameter("PossibleAttribute", "geschwindigkeit");
            c.addParameter("PossibleAttribute", "painLevel");
            c.setRemark("in init");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);

            c = Command.getCommandInit("createList", initTime);
            c.addParameter("ListId", "list1");
            c.addParameter("Name", "Test1");
            c.addParameter("DefaultEntityType", "Patient");
            c.addParameter("NumberOfVisible", "3");
            c.addParameter("Form", "horizontal");
            c.addParameter("Point", "-400|500");
            c.setRemark("in init");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);

            c = Command.getCommandInit("createList", initTime);
            c.addParameter("ListId", "list2");
            c.addParameter("DefaultEntityType", "Patient");
            c.addParameter("NumberOfVisible", "2");
            c.addParameter("Form", "vertikal");
            c.addParameter("Point", "400|500");
            c.setRemark("in init");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);

            c = Command.getCommandInit("createStation", initTime);
            c.addParameter("StationId", "Berlin");
            c.addParameter("Name", "Berlin");
            c.addParameter("Point", "0|100");
            c.setRemark("in init");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);

            c = Command.getCommandInit("createStation", initTime);
            c.addParameter("StationId", "Hamburg");
            c.addParameter("Point", "500|700");
            c.setRemark("in init");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);

            c = Command.getCommandInit("createRoute", initTime);
            c.addParameter("RouteId", "Berlin_Hamburg");
            c.addParameter("Length", "10");
            c.addParameter("SourceStationId", "Berlin");
            c.addParameter("SinkStationId", "Hamburg");
            c.addParameter("Point", "0|700");
            c.setRemark("in init");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);

            c = Command.getCommandInit("createProcess", initTime);
            c.addParameter("ProcessId", "process1");
            c.addParameter("NumberOfResEntity", "0");
            c.addParameter("NumberOfProcEntity", "1");
            c.addParameter("DefaultResEntityType", "Patient");
            c.addParameter("DefaultProcEntityType", "Patient");
            c.addParameter("ListId", "list1");
            c.addParameter("Form", "horizontal");
            c.addParameter("Point", "-400|300");
            c.setRemark("in init");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);

        } catch (CommandException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Erzeugen der Cmd's fuer die Run Phase
     */
    protected void bsp_run() {
        Command c = null;
        try {
            c = Command.getCommandTime("createEntity", 0);
            c.addParameter("EntityId", "Meyer");
            c.addParameter("Name", "myName");
            c.addParameter("EntityTypeId", "Patient");
            c.addParameter("State", "active");
            c.addParameter("Attribute", "myName|K.Meyer");
            c.addParameter("Attribute", "velocity|1.0");
            c.setRemark("in init");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);

            c = Command.getCommandTime("createEntity", 0);
            c.addParameter("EntityId", "Krause");
            c.addParameter("EntityTypeId", "Patient");
            c.addParameter("State", "active");
            c.addParameter("Attribute", "name|E.Krause");
            c.addParameter("Attribute", "velocity|2.0");
            c.setRemark("in init");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);

            c = Command.getCommandTime("createEntity", 0);
            c.addParameter("EntityId", "Mueller");
            c.addParameter("EntityTypeId", "Patient");
            c.addParameter("Velocity", "geschwindigkeit");
            c.addParameter("State", "active");
            c.addParameter("Attribute", "name|E.Krause");
            c.addParameter("Attribute", "geschwindigkeit|0.5");
            c.setRemark("in init");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);

            c = Command.getCommandTime("setEntity", 0);
            c.addParameter("EntityId", "Mueller");
            c.addParameter("Attribute", "painLevel|50");
            c.addParameter("Attribute", "velocity|1.0");
            c.setRemark("in init");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);

            c = Command.getCommandTime("createEntity", 0);
            c.addParameter("EntityId", "Schulze");
            c.addParameter("EntityTypeId", "Patient");
            c.addParameter("State", "bussy");
            c.addParameter("Position", "500|600|1.0|true");
            c.setRemark("in init");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);

            c = Command.getCommandTime("setList", 0);
            c.addParameter("ListId", "list1");
            c.addParameter("AddEntity", "Meyer|last");
            c.addParameter("AddEntity", "Krause|first");
            c.setRemark("in init");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);

            c = Command.getCommandTime("setList", 0);
            c.addParameter("ListId", "list2");
            c.addParameter("AddEntity", "Mueller|value|painLevel");
            c.setRemark("in init");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);

            c = Command.getCommandTime("setList", 0);
            c.addParameter("ListId", "list2");
            c.addParameter("RemoveEntity", "Mueller");
            c.setRemark("in init");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);

            c = Command.getCommandTime("setRoute", 0);
            c.addParameter("RouteId", "Berlin_Hamburg");
            c.addParameter("AddEntity", "Mueller");
            c.setRemark("in init");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);

            c = Command.getCommandTime("createEntity", 1000);
            c.addParameter("EntityId", "Mueller1");
            c.addParameter("EntityTypeId", "Patient");
            c.addParameter("State", "active");
            c.addParameter("Attribute", "velocity|2.0");
            c.setRemark("in run");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);

            c = Command.getCommandTime("setRoute", 1000);
            c.addParameter("RouteId", "Berlin_Hamburg");
            c.addParameter("AddEntity", "Mueller1");
            c.setRemark("in run");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);

            c = Command.getCommandTime("setEntity", 4000);
            c.addParameter("EntityId", "Mueller1");
            c.addParameter("Attribute", "velocity|0.5");
            c.setRemark("in run");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);

            c = Command.getCommandTime("setRoute", 6000);
            c.addParameter("RouteId", "Berlin_Hamburg");
            c.addParameter("RemoveEntity", "Mueller1");
            c.setRemark("in run");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);

            c = Command.getCommandTime("setList", 6000);
            c.addParameter("ListId", "list2");
            c.addParameter("AddEntity", "Mueller1|last");
            c.setRemark("in run");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);

            c = Command.getCommandTime("setList", 10000);
            c.addParameter("ListId", "list1");
            c.addParameter("RemoveEntity", "Meyer");
            c.setRemark("in run");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);

            c = Command.getCommandTime("setProcess", 10000);
            c.addParameter("ProcessId", "process1");
            c.addParameter("AddProcEntity", "Meyer");
            c.setRemark("in run");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);

            c = Command.getCommandTime("setProcess", 15000);
            c.addParameter("ProcessId", "process1");
            c.addParameter("RemoveProcEntity", "Meyer");
            c.setRemark("in run");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);

            c = Command.getCommandTime("setRoute", 15000);
            c.addParameter("RouteId", "Berlin_Hamburg");
            c.addParameter("AddEntity", "Meyer");
            c.setRemark("in run");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);

            c = Command.getCommandTime("setRoute", 20000);
            c.addParameter("RouteId", "Berlin_Hamburg");
            c.addParameter("RemoveEntity", "Mueller");
            c.setRemark("in run");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);

            c = Command.getCommandTime("setList", 20000);
            c.addParameter("ListId", "list2");
            c.addParameter("AddEntity", "Mueller|first");
            c.setRemark("in run");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);

            c = Command.getCommandTime("setRoute", 25000);
            c.addParameter("RouteId", "Berlin_Hamburg");
            c.addParameter("RemoveEntity", "Meyer");
            c.setRemark("in run");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);

            c = Command.getCommandTime("setList", 25000);
            c.addParameter("ListId", "list2");
            c.addParameter("AddEntity", "Meyer|last");
            c.setRemark("in run");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);

        } catch (CommandException e) {
            e.printStackTrace();
        }
    }

}
