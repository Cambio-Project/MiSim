package de.rss.fachstudie.MiSim.export;

import de.rss.fachstudie.MiSim.entities.Microservice;
import de.rss.fachstudie.MiSim.models.MainModel;
import de.rss.fachstudie.MiSim.utils.InputParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.TreeMap;

public class ExportReport {
    private MainModel model;
    private String resourcePath = "Report/resources/";

    public ExportReport(MainModel model) {
        this.model = model;
        this.graphReport();
        this.chartReport();
    }

    private TreeMap<String, TreeMap<Double, Double>> fillDatapoints(TreeMap<String, TreeMap<Double, Double>> series, boolean continous) {

        for (String mapkey : series.keySet()) {

            TreeMap<Double, Double> map = series.get(mapkey);
            TreeMap<Double, Double> newmap = new TreeMap<>();
            double step = model.getSimulationTime() / model.getDatapoints();
            double lastValue = 0;
            double lastIndex = 0;
            int mapIndex = 0;

            if (map.keySet().size() == 0)
                newmap.put(0.0, 0.0);

            for (double x : map.keySet()) {
                double key = Math.round(x * model.getPrecision()) / model.getPrecision();

                while (x > lastIndex) {
                    if (continous)
                        newmap.put(lastIndex, lastValue);
                    else
                        newmap.put(lastIndex, 0.0);
                    lastIndex += step;
                }

                lastValue = Math.round(map.get(x) * model.getPrecision()) / model.getPrecision();
                newmap.put(x, map.get(x));

                if (mapIndex == map.size() - 1 && x < model.getSimulationTime()) {
                    lastIndex = step * Math.round((x + 0.5) / step);
                    while (lastIndex < model.getSimulationTime()) {
                        if (continous)
                            newmap.put(lastIndex, lastValue);
                        else
                            newmap.put(lastIndex, 0.0);
                        lastIndex += step;
                    }
                }
                mapIndex++;
            }
            series.put(mapkey, newmap);
        }
        return series;
    }

    private void graphReport() {
        DependecyGraph graph = new DependecyGraph(model, model.allMicroservices, 0);

        try {
            Files.write(Paths.get("./Report/js/graph.js"), graph.printGraph().getBytes());
            System.out.println("\nCreated graph report.");
        } catch (IOException ex) {
            System.out.println("\nCould not create graph report.");
        }
    }

    private void chartReport() {
        TreeMap<String, TreeMap<Double, Double>> activeInstances = new TreeMap<>();
        TreeMap<String, TreeMap<Double, Double>> existingInstances = new TreeMap<>();
        TreeMap<String, TreeMap<Double, Double>> taskQueueWork = new TreeMap<>();
        TreeMap<String, TreeMap<Double, Double>> usedCPU = new TreeMap<>();
        TreeMap<String, TreeMap<Double, Double>> responseTime = new TreeMap<>();
        TreeMap<String, TreeMap<Double, Double>> resourceLimiter = new TreeMap<>();
        TreeMap<String, TreeMap<Double, Double>> circuitBreaker = new TreeMap<>();
        TreeMap<String, TreeMap<Double, Double>> threadPool = new TreeMap<>();
        TreeMap<String, TreeMap<Double, Double>> threadQueue = new TreeMap<>();

        for(int id = 0; id < model.services.size(); id++) {
            String serviceName = model.services.get(id).get(0).getName();
            int instanceLimit = model.services.get(id).get(0).getInstances();

            if(InputParser.simulation.get("report").equals("minimalistic")) {
                if(model.services.get(id).get(0).getInstances() < 10)
                    instanceLimit = model.services.get(id).get(0).getInstances();
                else
                    instanceLimit = 10;
            }

            for(int instance = 0; instance < instanceLimit; instance++) {
                
                Microservice ms = model.services.get(id).get(instance);
                String file = ms.getName() + "_" + instance + ".txt";

                activeInstances.put(ms.getName() + " #" + instance, this.getTimeSeriesWithKeys(resourcePath + "ActiveThreads_" + file));
                existingInstances.put(ms.getName() + " #" + instance, this.getTimeSeriesWithKeys(resourcePath + "ExistingThreads_" + file));
                usedCPU.put(ms.getName() + " #" + instance, this.getTimeSeriesWithKeys(resourcePath + "CPU_" + file));
                responseTime.put(ms.getName() + " #" + instance, this.getTimeSeriesWithKeys(resourcePath + "ResponseTime_" + file));
                resourceLimiter.put(ms.getName() + " #" + instance, this.getTimeSeriesWithKeys(resourcePath + "ResourceLimiter_" + file));
                threadPool.put(ms.getName() + " #" + instance, this.getTimeSeriesWithKeys(resourcePath + "ThreadPool_" + file));
                threadQueue.put(ms.getName() + " #" + instance, this.getTimeSeriesWithKeys(resourcePath + "ThreadQueue_" + file));
                circuitBreaker.put(serviceName + " #" + instance, this.getTimeSeriesWithKeys(resourcePath + "CircuitBreaker_" + file));
            }
            taskQueueWork.put(serviceName, this.getTimeSeriesWithKeys(resourcePath + "TaskQueue_" + serviceName + ".txt"));
        }

        fillDatapoints(activeInstances, true);
        fillDatapoints(existingInstances, true);
        fillDatapoints(taskQueueWork, true);
        fillDatapoints(usedCPU, true);
        fillDatapoints(responseTime, false);
        fillDatapoints(resourceLimiter, true);
        fillDatapoints(circuitBreaker, true);
        fillDatapoints(threadPool, true);
        fillDatapoints(threadQueue, true);

        DataChart chart1 = new DataChart(model, "spline", "Active Microservice Threads", activeInstances, "");
        DataChart chart2 = new DataChart(model, "spline", "Existing Microservice Threads", taskQueueWork, "");
        DataChart chart4 = new DataChart(model, "spline", "Used CPU in percent", usedCPU, "");
        DataChart chart5 = new DataChart(model, "scatter", "Thread Response Time", responseTime, "");
        DataChart chart6 = new DataChart(model, "spline", "Tasks refused by Resource Limiter", resourceLimiter, "");
        DataChart chart7 = new DataChart(model, "spline", "Tasks refused by Circuit Breaker", circuitBreaker, "");
        DataChart chart8 = new DataChart(model, "spline", "Tasks refused by Thread Pool", threadPool, "");
        DataChart chart9 = new DataChart(model, "spline", "Tasks refused by Thread Queue", threadQueue, "");

        Table table1 = new Table("Active Microservice Threads", activeInstances);
        Table table2 = new Table("Existing Microservice Threads", taskQueueWork);
        Table table4 = new Table("Used CPU in percent", usedCPU);
        Table table5 = new Table("Thread Response Time", responseTime);
        Table table6 = new Table("Tasks refused by Resource Limiter", resourceLimiter);
        Table table7 = new Table("Tasks refused by Circuit Breaker", circuitBreaker);
        Table table8 = new Table("Tasks refused by Thread Pool", threadPool);
        Table table9 = new Table("Tasks refused by Thread Queue", threadQueue);

        String divs = chart1.printDiv() + table1.printTable()
                + chart2.printDiv() + table2.printTable()
                + chart4.printDiv() + table4.printTable()
                + chart5.printDiv() + table5.printTable()
                + chart6.printDiv() + table6.printTable()
                + chart7.printDiv() + table7.printTable()
                + chart8.printDiv() + table8.printTable()
                + chart9.printDiv() + table9.printTable();

        String charts = chart1.printStockChart()
                + chart2.printStockChart()
                + chart4.printStockChart()
                + chart5.printStockChart()
                + chart6.printStockChart()
                + chart7.printStockChart()
                + chart9.printStockChart();

        String contents = divs + charts;

        try {
            Files.write(Paths.get("./Report/js/chart.js"), contents.getBytes());
            System.out.println("\nCreated chart report.");
        } catch (IOException ex) {
            System.out.println("\nCould not create chart report.");
        }
    }

    private TreeMap<Double, Double> getTimeSeriesWithKeys(String filename) {
        TreeMap<Double, Double> values = new TreeMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(new File(filename)))) {
            String line;
            int index = 0;
            while ((line = br.readLine()) != null) {
                if(index > 0) {
                    String kvp[] = line.split("\\s+");
                    if(kvp.length > 1) {
                        values.put(Double.parseDouble(kvp[0]), Double.parseDouble(kvp[1]));
                    }
                }
                index++;
            }
        } catch (IOException ex) {
            System.out.println("Error while reading file: " + filename);
        }
        return values;
    }
}
