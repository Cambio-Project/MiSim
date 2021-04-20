package de.rss.fachstudie.MiSim.export;

import de.rss.fachstudie.MiSim.models.ExperimentMetaData;
import de.rss.fachstudie.MiSim.models.MainModel;

import java.util.TreeMap;

/**
 * A <code>DataChart</code> is a chart with x and y values.
 */
public class DataChart {
    private String chartId = "";
    private String options = "";
    private boolean empty = true;

    /**
     * A Chart with x and y values
     *
     * @param model     Model: The model that owns this dataChart
     * @param chartId   String: The ID of this chart
     * @param series    ThreeMap<String, TreeMap<Double, Double>>: The data that will be plotted
     */
    public DataChart(MainModel model, String chartType, String chartId, TreeMap<String, TreeMap<Double, Double>> series, String legendPrefix) {
        StringBuilder buffer = new StringBuilder();
        this.chartId = chartId;
        this.options = "chart:{type:'" + chartType + "'},title:{text:'" + chartId + "'}"
                + ",legend:{enabled:true},xAxis:{min:0,max:" + ExperimentMetaData.get().getDuration()
                + "},colors:colors(" + series.keySet().size() + "),series:[ ";
        int index = 0;

        for (String mapkey : series.keySet()) {
            if (series.get(mapkey).size() > 1)
                empty = false;

            buffer.append("{name:'").append(mapkey).append("',index:").append(index).append(",dataGrouping:{enabled: false},data:[");

            TreeMap<Double, Double> map = series.get(mapkey);

            if (map.keySet().size() == 0)
                map.put(ExperimentMetaData.get().getDuration(), 0.0);

            for(double x : map.keySet()) {
                double key = Math.round(x * 1000000) / 1000000.0;
                String value = "[" + key + "," + Math.round(map.get(x) * 1000000) / 1000000 + "],";
                buffer.append(value);
            }
            buffer = buffer.delete(buffer.length() - 1, buffer.length()).append("]},");
            index++;
        }
        if(series.keySet().size() > 0)
            options += buffer.delete(buffer.length() - 1, buffer.length()) + "]";
        else
            options += "]";
    }

    /**
     * Creates the div embedding the chart.
     *
     * @return String
     */
    public String printDiv() {
        if (!empty)
            return "document.getElementById('chart-container').innerHTML += \"<div id='" + chartId.replace(" ", "_") + "' class='stat-chart'></div>"
                    + "<button onclick=\\\"toggleLines('" + chartId.replace(" ", "_") + "');\\\">Toggle Visibility</button>"
                    + "<button onclick=\\\"unsmoothYAxis('" + chartId.replace(" ", "_") + "');\\\">Unsmooth YAxis</button>"
                    + "<button onclick=\\\"smoothYAxis('" + chartId.replace(" ", "_") + "');\\\">Smooth YAxis</button>\"\n";
        return "";
    }

    /**
     * A standart chart plots lines for each series
     *
     * @return String: js code for the chart
     */
    public String printChart() {
        if (!empty)
            return "Highcharts.stockChart('" + chartId.replace(" ", "_") + "', {" + options + "});\n";
        return "";
    }

    /**
     * A stock chart is able to compare multiple values and has a scrollbar
     * @return String: js code for the chart
     */
    public String printStockChart() {
        if (!empty)
            return "Highcharts.stockChart('" + chartId.replace(" ", "_") + "', {" + options + "});\n";
        return "";
    }
}
