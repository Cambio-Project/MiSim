package de.rss.fachstudie.MiSim.export;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.*;

/**
 * The <code>Table</code> class is used to display data in the report that has been collected during
 * the simulation.
 */
public class Table {

    private TreeMap<String, List<Double>> values;
    private String header;
    private boolean empty = true;

    /**
     * Instantiate <code>Table</code>.
     *
     * @param header String: header of the table
     * @param series TreeMap<String, TreeMap<Double, Double>>
     */
    public Table(String header, TreeMap<String, TreeMap<Double, Double>> series) {
        values = new TreeMap<>();
        this.header = header;

        for(String key : series.keySet()) {
            if (series.get(key).size() > 1)
                empty = false;

            TreeMap<Double, Double> entry = series.get(key);
            double start = Double.NEGATIVE_INFINITY, end = Double.NEGATIVE_INFINITY;
            double min = Double.POSITIVE_INFINITY, mean = 0, max = Double.NEGATIVE_INFINITY;

            if (entry.size() > 0) {
                start = entry.firstEntry().getValue();
                end = entry.lastEntry().getValue();
            }

            for (Double index : entry.keySet()) {
                if(entry.get(index) < min) {
                    min = entry.get(index);
                }
                if(entry.get(index) > max) {
                    max = entry.get(index);
                }
                if(index > 0) {
                    mean += entry.get(index);
                }
            }
            values.put(key, new ArrayList<>(Arrays.asList(start, min, mean / entry.size(), max, end)));
        }
    }

    /**
     * Create the html code of the table
     *
     * @return String: html code of the table
     */
    public String printTable() {
        String html = "";
        StringBuilder buffer = new StringBuilder();
        NumberFormat nf = new DecimalFormat("##.##", new DecimalFormatSymbols(Locale.ENGLISH));

        String id = "table-" + header.replace(" ", "_");
        html += "<table class='stat-table tablesorter' id='" + id + "'>"
                + "<thead><tr><th><span onclick=\\\"toggleTable(this, '" + id + "');\\\">&#x25BA;</span>" + header + "</th>"
                + "<th>Start</th><th>Min</th><th>Mean</th><th>Max</th><th>End</th></thead>"
                + "<tbody class='hidden'>";

        for(String entry : values.keySet()) {
            List<Double> mmm = values.get(entry);
            String start = "-", min = "-", mean = "-", max = "-", end = "-";

            if (mmm.get(0) != Double.NEGATIVE_INFINITY)
                start = nf.format(mmm.get(0));

            if (mmm.get(1) != Double.POSITIVE_INFINITY)
                min = nf.format(mmm.get(1));

            if (mmm.get(3) != Double.NEGATIVE_INFINITY)
                max = nf.format(mmm.get(3));

            if (mmm.get(1) != Double.POSITIVE_INFINITY && mmm.get(3) != Double.NEGATIVE_INFINITY)
                mean = nf.format(mmm.get(2));

            if (mmm.get(4) != Double.NEGATIVE_INFINITY)
                end = nf.format(mmm.get(4));

            buffer.append("<tr><td align='left'>")
                    .append(entry).append("</td><td>")
                    .append(start).append("</td><td>")
                    .append(min).append("</td><td>")
                    .append(mean).append("</td><td>")
                    .append(max).append("</td><td>")
                    .append(end).append("</td></tr>");
        }

        html += buffer.toString()
                + "</tbody>"
                + "</table>";

        if (!empty)
            return "document.getElementById('chart-container').innerHTML += \"" + html + "\"\n";
        return "";
    }
}
