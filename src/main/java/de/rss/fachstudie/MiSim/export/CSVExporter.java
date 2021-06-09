package de.rss.fachstudie.MiSim.export;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import desmoj.core.report.AbstractTableFormatter;
import desmoj.core.report.FileOutput;

/**
 * Exporter that can write the data of a dataset to a csv file.
 *
 * @author Lion Wagner
 */
class CSVExporter extends AbstractTableFormatter {
    private static final String FILE_EXTENSION = ".csv";

    private final FileOutput out;

    private CSVExporter(String datasetName, Path reportFolder) {
        this(datasetName, ";", reportFolder);
    }

    private CSVExporter(String datasetName, String seperator, Path reportFolder) {
        Path targetFolder = Paths.get(String.valueOf(reportFolder), "raw");
        targetFolder.toFile().mkdirs();
        Path targetFilePath = Paths.get(String.valueOf(targetFolder), datasetName + FILE_EXTENSION);
        FileOutput.setSeparator(seperator);
        out = new FileOutput();
        out.open(targetFilePath.toAbsolutePath().toString());
    }

    public static void writeDataset(String datasetName, Map<Double, Object> dataset, Path reportFolder) {
        writeDataset(datasetName, "Value", dataset, reportFolder);
    }

    public static void writeDataset(String datasetName, String nameValueColumn, Map<Double, Object> dataset,
                                    Path reportFolder) {

        final String separator = ";";
        AbstractTableFormatter exporter = new CSVExporter(datasetName, separator, reportFolder);

        exporter.openTable("Simulation Time" + separator + nameValueColumn + separator);

        for (Map.Entry<Double, Object> entry : dataset.entrySet()) {
            exporter.openRow();
            exporter.writeCell(entry.getKey().toString(), 1);
            exporter.writeCell(String.valueOf(entry.getValue()), 1);
            exporter.closeRow();
        }
        exporter.closeTableNoTopTag();
        exporter.close();
    }

    @Override
    public void open(String file) {
        out.open(file);
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
    public void openTable(String value) {
        writeHeading(0, value);
    }

    @Override
    public void writeHeading(int loc, String value) {
        out.writeln(value);
    }

    @Override
    public void writeHeadingCell(String value) {
        writeCell(value, 0);
    }

    @Override
    public void writeCell(String value, int loc) {
        out.writeSep(value);
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

