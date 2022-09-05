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
public class WriteBspArztPraxis0 extends WriteCmds {

    private static final String PATH = "path/";
    private static final String CMD_DATEI = PATH + "BspArztPraxis0" + Constants.FILE_EXTENSION_CMD;
    private static final String LOG_DATEI = PATH + "BspArztPraxis0" + Constants.FILE_EXTENSION_LOG_0;

    public WriteBspArztPraxis0(URL iconUrl) {
        super(CMD_DATEI, LOG_DATEI, iconUrl);
        this.bsp_init(5);
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

        WriteBspArztPraxis0 a = new WriteBspArztPraxis0(iconUrl);
    }

    /**
     * Erzeugen der Cmd's fuer die Init-Phase
     */
    protected void bsp_init(long initTime) {
        Command c = null;

        try {
            c = Command.getCommandInit("createModelBasisData", initTime);
            c.addParameter("ModelName", "Arztpraxis");
            c.addParameter("ModelAuthor", "Chr.Mueller");
            c.addParameter("ModelRemark", "Vers 0");
            c.addParameter("ModelRemark", "15.7.2009");
            c.setRemark("in init");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);

            c = Command.getCommandInit("createSimTimeBounds", initTime);
            c.addParameter("Begin", "0");
            c.addParameter("End", "25000");
            c.addParameter("Speed", "1.0");
            c.setRemark("in init");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);

            c = Command.getCommandInit("createImage", initTime);
            c.addParameter("ImageId", "Duke");
            c.addParameter("File", "Duke-0.gif");
            c.setRemark("in init");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);

            c = Command.getCommandInit("createEntityTyp", initTime);
            c.addParameter("EntityTypId", "Patient");
            c.addParameter("IconWidth", "30");
            c.addParameter("IconHeigth", "40");
            c.addParameter("PossibleState", "active|Duke");
            c.setRemark("in init");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);

            c = Command.getCommandInit("createEntityTyp", initTime);
            c.addParameter("EntityTypId", "Rezeptionist");
            c.addParameter("IconWidth", "30");
            c.addParameter("IconHeigth", "40");
            c.addParameter("PossibleState", "active|Duke");
            c.setRemark("in init");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);

            c = Command.getCommandInit("createEntityTyp", initTime);
            c.addParameter("EntityTypId", "Arzthelfer");
            c.addParameter("IconWidth", "30");
            c.addParameter("IconHeigth", "40");
            c.addParameter("PossibleState", "active|Duke");
            c.setRemark("in init");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);

            c = Command.getCommandInit("createEntityTyp", initTime);
            c.addParameter("EntityTypId", "Arzt");
            c.addParameter("IconWidth", "30");
            c.addParameter("IconHeigth", "40");
            c.addParameter("PossibleState", "active|Duke");
            c.setRemark("in init");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);

            c = Command.getCommandInit("createList", initTime);
            c.addParameter("ListId", "Rezeption");
            c.addParameter("DefaultEntityType", "Patient");
            c.addParameter("NumberOfVisible", "3");
            c.addParameter("Form", "vertikal");
            c.addParameter("Point", "0|100");
            c.setRemark("in init");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);

            c = Command.getCommandInit("createProcess", initTime);
            c.addParameter("ProcessId", "Counter");
            c.addParameter("NumberOfResEntity", "3");
            c.addParameter("NumberOfProcEntity", "3");
            c.addParameter("DefaultResEntityType", "Rezeptionist");
            c.addParameter("DefaultProcEntityType", "Patient");
            c.addParameter("ListId", "Rezeption");
            c.addParameter("Form", "horizontal");
            c.addParameter("Point", "150|100");
            c.setRemark("in init");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);

            c = Command.getCommandInit("createList", initTime);
            c.addParameter("ListId", "Wartezimmer");
            c.addParameter("DefaultEntityType", "Patient");
            c.addParameter("NumberOfVisible", "3");
            c.addParameter("Form", "vertikal");
            c.addParameter("Point", "250|100");
            c.setRemark("in init");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);

            c = Command.getCommandInit("createProcess", initTime);
            c.addParameter("ProcessId", "Vorbehandlung");
            c.addParameter("NumberOfResEntity", "3");
            c.addParameter("NumberOfProcEntity", "3");
            c.addParameter("DefaultResEntityType", "Arzthelfer");
            c.addParameter("DefaultProcEntityType", "Patient");
            c.addParameter("ListId", "Wartezimmer");
            c.addParameter("Form", "horizontal");
            c.addParameter("Point", "400|100");
            c.setRemark("in init");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);

            c = Command.getCommandInit("createList", initTime);
            c.addParameter("ListId", "HelferSchlange");
            c.addParameter("DefaultEntityType", "Arzthelfer");
            c.addParameter("NumberOfVisible", "3");
            c.addParameter("Form", "vertikal");
            c.addParameter("Point", "550|100");
            c.setRemark("in init");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);

            c = Command.getCommandInit("createProcess", initTime);
            c.addParameter("ProcessId", "Behandlung");
            c.addParameter("NumberOfResEntity", "1");
            c.addParameter("NumberOfProcEntity", "1");
            c.addParameter("DefaultResEntityType", "Arzt");
            c.addParameter("DefaultProcEntityType", "Arzthelfer");
            c.addParameter("ListId", "HelferSchlange");
            c.addParameter("Form", "horizontal");
            c.addParameter("Point", "700|100");
            c.setRemark("in init");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);

            c = Command.getCommandInit("createList", initTime);
            c.addParameter("ListId", "RezeptionistUntaetig");
            c.addParameter("DefaultEntityType", "Rezeptionist");
            c.addParameter("NumberOfVisible", "4");
            c.addParameter("Form", "horizontal");
            c.addParameter("Point", "0|300");
            c.setRemark("in init");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);

            c = Command.getCommandInit("createList", initTime);
            c.addParameter("ListId", "ArzthelferUntaetig");
            c.addParameter("DefaultEntityType", "Arzthelfer");
            c.addParameter("NumberOfVisible", "4");
            c.addParameter("Form", "horizontal");
            c.addParameter("Point", "200|300");
            c.setRemark("in init");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);

            c = Command.getCommandInit("createList", initTime);
            c.addParameter("ListId", "ArztUntaetig");
            c.addParameter("DefaultEntityType", "Arzt");
            c.addParameter("NumberOfVisible", "4");
            c.addParameter("Form", "horizontal");
            c.addParameter("Point", "400|300");
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
            c.addParameter("EntityTypeId", "Patient");
            c.addParameter("State", "active");
            c.setRemark("in run");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);

            c = Command.getCommandTime("setList", 0);
            c.addParameter("ListId", "Rezeption");
            c.addParameter("AddEntity", "Meyer|last");
            c.setRemark("in run");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);

            c = Command.getCommandTime("setList", 5000);
            c.addParameter("ListId", "Rezeption");
            c.addParameter("RemoveEntity", "Meyer");
            c.setRemark("in run");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);

            c = Command.getCommandTime("setList", 5000);
            c.addParameter("ListId", "Wartezimmer");
            c.addParameter("AddEntity", "Meyer|last");
            c.setRemark("in run");
            this.checkAndLog(c);
            this.getCommandSequence().write(c);


        } catch (CommandException e) {
            e.printStackTrace();
        }
    }

}
