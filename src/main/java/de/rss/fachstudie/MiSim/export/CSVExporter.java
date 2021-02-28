package de.rss.fachstudie.MiSim.export;

import desmoj.core.report.AbstractTableFormatter;
import desmoj.core.report.FileOutput;
import desmoj.core.simulator.TimeInstant;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * @author Lion Wagner
 */
class CSVExporter extends AbstractTableFormatter {

    private static final Path REPORT_FOLDER = Paths.get("Report", "raw");
    private static final String FILE_EXTENSION = ".csv";

    private final Path targetFilePath;
    private final FileOutput out;

    private CSVExporter(String datasetName) {
        this(datasetName, ";");
    }

    private CSVExporter(String datasetName, String seperator) {
        REPORT_FOLDER.toFile().mkdirs();
        targetFilePath = Paths.get(String.valueOf(REPORT_FOLDER), datasetName + FILE_EXTENSION);
        FileOutput.setSeparator(seperator);
        out = new FileOutput();
        out.open(targetFilePath.toAbsolutePath().toString());
    }

    public static void writeDataset(String datasetName, Map<TimeInstant, Object> dataset) {
        writeDataset(datasetName, "Value", dataset);
    }

    public static void writeDataset(String datasetName, String name_value_column, Map<TimeInstant, Object> dataset) {
        final String separator = ";";
        AbstractTableFormatter exporter = new CSVExporter(datasetName, separator);

        exporter.openTable("Time" + separator + name_value_column + separator);

        for (Map.Entry<TimeInstant, Object> entry : dataset.entrySet()) {
            exporter.openRow();
            exporter.writeCell(entry.getKey().toString(), 1);
            exporter.writeCell(String.valueOf(entry.getValue()), 1);
            exporter.closeRow();
        }
        exporter.closeTableNoTopTag();
        exporter.close();
    }

    @Override
    public void open(String s) {
        out.open(s);
    }

    @Override
    public void close() {
        out.close();
    }

    @Override
    public void closeRow() {
        out.write("\n");

    }

    @Override
    public void closeTable() {
        out.write("\n");
    }

    @Override
    public void closeTableNoTopTag() {
        closeTable();
    }

    @Override
    public void openRow() {
    }

    @Override
    public void openTable(String s) {
        writeHeading(0, s);
    }

    @Override
    public void writeHeading(int i, String s) {
        out.writeln(s);
    }

    @Override
    public void writeHeadingCell(String s) {
        writeCell(s, 0);
    }

    @Override
    public void writeCell(String s, int i) {
        out.writeSep(s);
    }

    @Override
    public void writeHorizontalRuler() {
        out.write("##############################");
    }

    @Override
    public String getFileFormat() {
        return "csv";
    }
}

