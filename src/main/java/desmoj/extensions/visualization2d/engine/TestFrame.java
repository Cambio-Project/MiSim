package desmoj.extensions.visualization2d.engine;


import javax.swing.JFrame;
import javax.swing.JScrollPane;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import desmoj.extensions.visualization2d.engine.command.CommandSequence;
import desmoj.extensions.visualization2d.engine.model.BackgroundElement;
import desmoj.extensions.visualization2d.engine.model.Entity;
import desmoj.extensions.visualization2d.engine.model.List;
import desmoj.extensions.visualization2d.engine.model.Model;
import desmoj.extensions.visualization2d.engine.model.ModelException;
import desmoj.extensions.visualization2d.engine.model.ProcessNew;
import desmoj.extensions.visualization2d.engine.model.Route;
import desmoj.extensions.visualization2d.engine.model.Station;
import desmoj.extensions.visualization2d.engine.model.Statistic;
import desmoj.extensions.visualization2d.engine.model.WaitingQueue;
import desmoj.extensions.visualization2d.engine.modelGrafic.Grafic;
import desmoj.extensions.visualization2d.engine.modelGrafic.ModelGraficException;
import desmoj.extensions.visualization2d.engine.modelGrafic.StatisticGrafic;
import desmoj.extensions.visualization2d.engine.viewer.SimulationTime;


/**
 * Test-Application for viewer testing. Only for devoloper. It's may be not correct.
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
public class TestFrame extends JFrame {

    private static final long serialVersionUID = 1L;
    List l1 = null, l2 = null;
    Entity entity = null;
    Route r = null;
    Container contentPane = null;
    BufferedReader f = null;
    Model model = null;
    JScrollPane scroll = null;
    CommandSequence cmdSeq = null;


    public TestFrame() throws ModelException, ModelGraficException {
        super("Fenster Titel");
        this.setLocation(0, 0);
        this.setSize(800, 500);

        this.addWindowListener(new desmoj.extensions.visualization2d.engine.modelGrafic.WindowClosingAdapter(true));
        contentPane = this.getContentPane();


        SimulationTime simTime = new SimulationTime(0, 10000, 0.1, null, null);
        URL url = null;
        try {
            url = (new File("Bilder")).toURI().toURL();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        model = new Model(url, null, null);
        model.setSimulationTime(simTime);
        //Command.setModel(model);
        cmdSeq = new CommandSequence(model, null);
        model.createModelGrafic();


        testInit();
        //model.getModelGrafic().setZoomFactor("main", 1.0, null);
        model.getModelGrafic().updateInit(0);
		/*
		try{
			scroll = new JScrollPane(model.getModelGrafic(), JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		}catch(ModelGraficException emg){	emg.printStackTrace();	}
		*/
        contentPane.setLayout(new GridLayout(1, 1));
        contentPane.add(model.getModelGrafic());
        //scroll.setBackground(Grafic.COLOR_BACKGROUND);
        contentPane.setBackground(Grafic.COLOR_BACKGROUND);
        this.setVisible(true);

		/*
		try{ Thread.sleep(1000);
		}catch(java.lang.InterruptedException ex){
		}
		model.getModelGrafic().setZoomFactor(1.0);
		Dimension d = scroll.getViewport().getExtentSize();
		Point p = ModelGrafic.transformToIntern(new Point(50,250));
		Rectangle r = new Rectangle(p.x-d.width/2, p.y-d.height/2, d.width, d.height);
		scroll.getViewport().scrollRectToVisible(r);
		*/
        simTime.start();
        grafikTestRun();
    }

    public static void main(String[] args) {
        try {
            TestFrame f = new TestFrame();
        } catch (ModelException em) {
            em.printStackTrace();
        } catch (ModelGraficException emg) {
            emg.printStackTrace();
        }
    }

    public void testInit() throws ModelException, ModelGraficException {

        this.model.createModelBasisData(new String[0], new String[0], new String[0], "test", "cm", new String[0],
            new String[0], new String[0], new String[0], new String[0], new String[0], new String[0], true, 0);
        this.model.init_Images();
        this.model.init_EntityTypes();

        entity = new Entity(this.model, "Schulze", "Patient", "bussy", model.getSimulationTime().getSimulationTime());
        entity.createGraficStatic("otto", 600.0, 50.0, -Math.PI / 4.0, true,
            model.getSimulationTime().getSimulationTime());

        entity = new Entity(this.model, "Meier", "Patient", "active", model.getSimulationTime().getSimulationTime());
        entity.setAttribute("name", "E.Meyer", model.getSimulationTime().getSimulationTime());
        entity.createGraficFree(model.getSimulationTime().getSimulationTime());

        entity = new Entity(this.model, "Krause", "Patient", "active", model.getSimulationTime().getSimulationTime());
        entity.createGraficFree(model.getSimulationTime().getSimulationTime());


        l1 = new List(this.model, List.PREFIX_QUEUE, "l1");
        l1.createGrafic("fritz", -450, 150, "Patient", 3, true);
        l2 = new List(this.model, List.PREFIX_QUEUE, "l2");
        l2.setName("l2 Test");
        l2.createGrafic("fritz", 50, -50, "Patient", 3, false);
        l1.addToContainer("Meier", 3, List.PRIO_FIRST, this.model.getSimulationTime().getSimulationTime());

        Station s1 = new Station(this.model, "Berlin");
        s1.setName("Berlin");
        this.model.getStations().get("Berlin").createGrafic("trans", 50, 250);
        Station s2 = new Station(this.model, "Hamburg");
        this.model.getStations().get("Hamburg").createGrafic("trans", 250, 50);

        Route r = new Route(this.model, "test", "Berlin", "Hamburg", 5.0);
        Point[] points = new Point[1];
        points[0] = new Point(250, 250);
        r.createGrafic(points);
        r.addToContainer("Krause", this.model.getSimulationTime().getSimulationTime());

        entity =
            new Entity(this.model, "Res1", "Patient", "active", this.model.getSimulationTime().getSimulationTime());
        entity.createGraficFree(this.model.getSimulationTime().getSimulationTime());
        entity =
            new Entity(this.model, "Res2", "Patient", "active", this.model.getSimulationTime().getSimulationTime());
        entity.createGraficFree(this.model.getSimulationTime().getSimulationTime());
        entity =
            new Entity(this.model, "Pat11", "Patient", "active", this.model.getSimulationTime().getSimulationTime());
        entity.createGraficFree(this.model.getSimulationTime().getSimulationTime());
        entity =
            new Entity(this.model, "Pat1", "Patient", "active", this.model.getSimulationTime().getSimulationTime());
        entity.createGraficFree(this.model.getSimulationTime().getSimulationTime());
        entity =
            new Entity(this.model, "Pat2", "Patient", "active", this.model.getSimulationTime().getSimulationTime());
        entity.createGraficFree(this.model.getSimulationTime().getSimulationTime());
        entity =
            new Entity(this.model, "Res11", "Patient", "active", this.model.getSimulationTime().getSimulationTime());
        entity.createGraficFree(this.model.getSimulationTime().getSimulationTime());

		/*
		Resource res = new Resource("R4711", "Pat's", 8);
		res.setName("ResNew");
		res.provide("Pat1", 2, List.PRIO_LAST, Model.getSimulationTime());
		res.provide("Pat2", 4, List.PRIO_LAST, Model.getSimulationTime());
		res.provide("Pat11", 4,List.PRIO_LAST,  Model.getSimulationTime());
		res.provide("Res11", 4,List.PRIO_LAST, Model.getSimulationTime());
		res.createGrafic(-150, 250, "Patient", 2, true, null);
		*/

        entity =
            new Entity(this.model, "Res1a", "Patient", "active", this.model.getSimulationTime().getSimulationTime());
        entity.createGraficFree(this.model.getSimulationTime().getSimulationTime());
        entity =
            new Entity(this.model, "Res2a", "Patient", "active", this.model.getSimulationTime().getSimulationTime());
        entity.createGraficFree(this.model.getSimulationTime().getSimulationTime());
        entity =
            new Entity(this.model, "Pat11a", "Patient", "active", this.model.getSimulationTime().getSimulationTime());
        entity.createGraficFree(this.model.getSimulationTime().getSimulationTime());
        entity =
            new Entity(this.model, "Pat1a", "Patient", "active", this.model.getSimulationTime().getSimulationTime());
        entity.createGraficFree(this.model.getSimulationTime().getSimulationTime());
        entity =
            new Entity(this.model, "Pat2a", "Patient", "active", this.model.getSimulationTime().getSimulationTime());
        entity.createGraficFree(this.model.getSimulationTime().getSimulationTime());
        entity =
            new Entity(this.model, "Res11a", "Patient", "active", this.model.getSimulationTime().getSimulationTime());
        entity.createGraficFree(this.model.getSimulationTime().getSimulationTime());

        ProcessNew p1 = new ProcessNew(this.model, ProcessNew.PREFIX_PROCESS, "P4711", "l1");
        p1.setName("ProcNew1");
        String[] processEntityIds1 = {"Pat1a", "Pat2a"};
        String[] resourceEntityIds1 = {"Res11a"};
        p1.addEntry(processEntityIds1, resourceEntityIds1, this.model.getSimulationTime().getSimulationTime());
        String[] processEntityIds1a = {"Pat11a"};
        String[] resourceEntityIds1a = {"Res1a", "Res2a"};
        p1.addEntry(processEntityIds1a, resourceEntityIds1a, this.model.getSimulationTime().getSimulationTime());
        p1.printEntries();
        p1.createGrafic("fritz", -350, 250, "Patient", 3, true, true, null);


        entity =
            new Entity(this.model, "Res1b", "Patient", "active", this.model.getSimulationTime().getSimulationTime());
        entity.createGraficFree(this.model.getSimulationTime().getSimulationTime());
        entity =
            new Entity(this.model, "Res2b", "Patient", "active", this.model.getSimulationTime().getSimulationTime());
        entity.createGraficFree(this.model.getSimulationTime().getSimulationTime());
        entity =
            new Entity(this.model, "Pat11b", "Patient", "active", this.model.getSimulationTime().getSimulationTime());
        entity.createGraficFree(this.model.getSimulationTime().getSimulationTime());
        entity =
            new Entity(this.model, "Pat1b", "Patient", "active", this.model.getSimulationTime().getSimulationTime());
        entity.createGraficFree(this.model.getSimulationTime().getSimulationTime());
        entity =
            new Entity(this.model, "Pat2b", "Patient", "active", this.model.getSimulationTime().getSimulationTime());
        entity.createGraficFree(this.model.getSimulationTime().getSimulationTime());
        entity =
            new Entity(this.model, "Res11b", "Patient", "active", this.model.getSimulationTime().getSimulationTime());
        entity.createGraficFree(this.model.getSimulationTime().getSimulationTime());

        ProcessNew p2 = new ProcessNew(this.model, ProcessNew.PREFIX_PROCESS, "P4712", "Pat's", 8, "l2");
        p2.setName("ProcNew2");
        String[] processEntityIds2 = {"Pat11b"};
        p2.addEntry(processEntityIds2, 2, this.model.getSimulationTime().getSimulationTime());
        String[] processEntityIds2a = {"Pat1b", "Pat2b"};
        p2.addEntry(processEntityIds2a, 1, this.model.getSimulationTime().getSimulationTime());
        p2.printEntries();
        p2.createGrafic("fritz", -350, 50, "Patient", 3, true, true, null);

        //Process p = new animation.model.Process("Proc1", 3,3,"l1");
        //p.setProzessEntity(0, "Pat11", Model.getSimulationTime());
        //p.setProzessEntity(2, "Res1",Model.getSimulationTime());
        //p.setResourceEntity(0, "Pat11", Model.getSimulationTime());
        //p.setResourceEntity(2, "Res1", Model.getSimulationTime());
        //p.createGrafic(-450, 250, "Patient");

        Statistic statistic =
            new Statistic(this.model, "4713", Statistic.DATA_Observations, Statistic.INDEX_Mean_StdDev, false, 0, 4000,
                1100.0, 1700.0, 3);
        statistic.setName("test-statistik");
        statistic.createGrafic("fritz", -50, 20, StatisticGrafic.ANIMATION_LastValue, true, null, true);
        //statistic.update(100, 0);

		/*
		Stock stock = new Stock("4715", 500, 67);
		stock.setName("test-stock");
		stock.createGrafic(-350, -100, "Patient", 1, true, null);
		stock.retrieveBegin("Pat11", 44, List.PRIO_LAST, Model.getSimulationTime());
		stock.storeBegin("Res1", 5, List.PRIO_LAST, Model.getSimulationTime());
		stock.storeEnd("Res1", Model.getSimulationTime());
		*/
		/*
		Bin bin = new Bin("4716", 50);
		bin.setName("test-bin");
		bin.createGrafic(-350, -100, "Patient", 1, false, null);
		bin.retrieveBegin("Pat11", 44, List.PRIO_LAST, Model.getSimulationTime());
		bin.store("Res1", 5, Model.getSimulationTime());
		*/
        WaitingQueue wq = new WaitingQueue(this.model, "4800");
        wq.setName("test-wq");
        wq.createGrafic("otto", 0, -200, "Patient", 3, false, null);
        wq.insert("Pat11", 2, true, List.PRIO_LAST, this.model.getSimulationTime().getSimulationTime());
        wq.insert("Res1", 2, false, List.PRIO_LAST, this.model.getSimulationTime().getSimulationTime());
        wq.cooperationBegin("Pat11", "Res1", this.model.getSimulationTime().getSimulationTime());
        //wq.cooperationEnd("Pat11", "Res1", Model.getSimulationTime());


        BackgroundElement painting =
            new BackgroundElement(this.model, "4789", null, BackgroundElement.TEXT_POSITION_Middle,
                BackgroundElement.TEXT_Size_Big, BackgroundElement.TEXT_Style_Italic, 1, null);
        painting.setName("lol");
        //painting.createGrafic(100, 100, Color.blue, null);
        painting.createGrafic("main", new Point(-350, -100), Color.blue, null, new Dimension(200, 200));
        //painting.createGrafic(100, 100, 400, 200, Color.red, null);

        BackgroundElement painting1 =
            new BackgroundElement(this.model, "4788", "hugo<BR>eva", BackgroundElement.TEXT_POSITION_Middle,
                BackgroundElement.TEXT_Size_Small, BackgroundElement.TEXT_Style_Italic, 2, null);
        painting1.setName("lol1");
        painting1.createGrafic("lol1", new Point(-200, -120), Color.blue, Color.red, new Dimension(200, 200));

        BackgroundElement painting2 =
            new BackgroundElement(this.model, "4787", "TEST", BackgroundElement.TEXT_POSITION_Middle,
                BackgroundElement.TEXT_Size_Big, BackgroundElement.TEXT_Style_Bold, 0, null);
        painting2.createGrafic("main", new Point(-250, -110), Color.blue, null);

        System.out.print("Entities: ");
        String[] e = this.model.getEntities().getAllIds();
        for (int i = 0; i < e.length; i++) {
            System.out.print(e[i] + "  ");
        }
        System.out.println();

    }

    public void grafikTestRun() throws ModelException, ModelGraficException {

        for (int i = 0; i < 1; i++) {
            Entity e = new Entity(this.model, "P" + i, "Patient", "active",
                this.model.getSimulationTime().getSimulationTime());
            e.createGraficFree(0);
            //List.classContent.get("l2").addToContainer("P"+i, List.PRIO_LAST, "", Model.getSimulationTime());

            //String[][] free = ProcessNew.classContent.get("P4711").removeEntry("Pat1", Model.getSimulationTime());
            //Process.classContent.get("Proc1").setProzessEntity(1, "Pat11", Model.getSimulationTime());

            //Resource.classContent.get("R4711").takeProcess("Pat1", Model.getSimulationTime());

            // aktualisiert die Positionen der Route-Elemente
            model.getModelGrafic().updateDynamic(this.model.getSimulationTime().getSimulationTime());
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
            }

            //ProcessNew.classContent.get("P4711").addEntry(free[0], free[1], Model.getSimulationTime());

            //Resource.classContent.get("R4711").takeProcess("Pat2", Model.getSimulationTime());
            //Resource.classContent.get("R4711").takeBack("Pat1", 2, Model.getSimulationTime());


            // aktualisiert die Positionen der Route-Elemente
            model.getModelGrafic().updateDynamic(this.model.getSimulationTime().getSimulationTime());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
            }
        }

        model.getModelGrafic().setZoomFactor(1.4, new Point(50, 250), 0);


        for (int i = 10; i < 40; i++) {

            this.model.getStatistics().get("4713")
                .update(i * 100.0, this.model.getSimulationTime().getSimulationTime());
            this.model.getStatistics().get("4713")
                .update(i * 100.0 + 5000.0, this.model.getSimulationTime().getSimulationTime());

            Entity e = new Entity(this.model, "P" + i, "Patient", "active",
                this.model.getSimulationTime().getSimulationTime());
            e.createGraficFree(0);
            //List.classContent.get("l2").addToContainer("P"+i, List.PRIO_LAST, "", Model.getSimulationTime());

            //Process.classContent.get("Proc1").setProzessEntity(1, "Pat11",Model.getSimulationTime());

            // aktualisiert die Positionen der Route-Elemente
            model.getModelGrafic().updateDynamic(this.model.getSimulationTime().getSimulationTime());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
            }

            //Process.classContent.get("Proc1").unsetProzessEntity(1, "Pat11",0);

            // aktualisiert die Positionen der Route-Elemente
            model.getModelGrafic().updateDynamic(this.model.getSimulationTime().getSimulationTime());
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
            }
        }
        System.out.print("Entities: ");
        String[] e = this.model.getEntities().getAllIds();
        for (int i = 0; i < e.length; i++) {
            System.out.print(e[i] + "  ");
        }
        System.out.println();
    }

}
