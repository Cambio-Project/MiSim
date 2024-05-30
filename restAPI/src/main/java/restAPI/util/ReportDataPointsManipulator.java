package restAPI.util;


import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import tech.tablesaw.api.*;
import tech.tablesaw.io.csv.CsvReadOptions;

import java.nio.file.Path;
import java.util.*;

import static tech.tablesaw.aggregate.AggregateFunctions.*;

import static tech.tablesaw.api.ColumnType.DOUBLE;
import static tech.tablesaw.api.ColumnType.INTEGER;

// Aggregation and bucketing - Adjusting the values
public class ReportDataPointsManipulator {

    private static final String[] DISCRETE_VALUED_METRICS = {"Requests_InSystem", "Requests_NotComputed",
        "Requests_WaitingForDependencies", "SendOff_Internal_Requests", "FailedRequests", "SuccessfulRequests",
        "Load."};
    private static final String[] CONTINUOUS_VALUED_METRICS = {"RelativeUtilization", "Utilization",
        "UtilizationBinned", "NL_latency", "ResponseTimes"};

    public static void adjustSimulationResults(String rawResultsFilePath, String simulationId, String executionId)
        throws Exception {
        String simulationResultsDirPath = "." + TempFileUtils.SEPARATOR + rawResultsFilePath;
        Set<String> files = TempFileUtils.getFilesFromResultsDir(Path.of(simulationResultsDirPath));
        String outputDir = TempFileUtils.createOutputDir("." + TempFileUtils.SEPARATOR
            + TempFileUtils.OUTPUT_DIR, simulationId, executionId).toString();
        int i = 1;
        String fileName;
        for (String filePath : files) {
            String completePath = simulationResultsDirPath + TempFileUtils.SEPARATOR + filePath;
            Table t = transformSimulationResults(completePath);
            if (!t.isEmpty()) {
                fileName = t.column(1).name();
                if (fileName.isEmpty()) {
                    fileName = String.format("%s%sstable_%d.csv", outputDir, TempFileUtils.SEPARATOR, i);
                    i += 1;
                }
                t.write().csv(outputDir + TempFileUtils.SEPARATOR + fileName + ".csv");
            }
        }
        // TODO: find the bug with this join method. (The idea is to join all the generated tables into one table
        //      on the SimulationTime column).
        //      Hint: check the dependency tree maybe.
        // Table joined = first.joinOn("SimulationTime").rightOuter(second);
    }

    private static Table transformSimulationResults(String filePath) {
        if (stringContainsItemFromList(filePath, CONTINUOUS_VALUED_METRICS)) {
            return transformContinuousValuedMetric(filePath);
        } else if (stringContainsItemFromList(filePath, DISCRETE_VALUED_METRICS)) {
            return transformDiscreteValuedMetric(filePath);
        } else if (filePath.contains("InstanceCount")) {
            return replicateServiceInstanceCountValue(filePath);
        } else {
            // create an empty table
            return Table.create();
        }
    }

    // Create the column name from file name, and format it as follows: <service_name>_<metric_name>
    // Also remove <#> from service names, because in a later step, the names will be cast as Python variable names.
    private static String createColumnNameFromFileName(String fileName) {
        String regex = ".+\\[.+\\].+\\.csv";
        if (fileName.matches(regex)) {
            String serviceName = StringUtils.substringBetween(fileName, "[", "]")
                .replaceAll("[#-]", "_");
            String metricName = StringUtils.substringBetween(fileName, "]", ".csv");
            return String.format("%s%s", serviceName, metricName);
        } else {
            return FilenameUtils.getName(fileName);
        }
    }

    private static boolean stringContainsItemFromList(String inputStr, String[] items) {
        return Arrays.stream(items).anyMatch(inputStr::contains);
    }

    private static Table createTableFromFile(String filePath, ColumnType[] types) {
        CsvReadOptions.Builder csvReadOptions =
            CsvReadOptions.builder(filePath)
                .separator(';')
                .columnTypes(types);

        return Table.read().usingOptions(csvReadOptions);
    }

    private static Table adjustTime(String filePath) {
        ColumnType[] types = {DOUBLE, DOUBLE};
        Table table = createTableFromFile(filePath, types);

        DoubleColumn oldColumn = (DoubleColumn) table.column("SimulationTime");
        // Cast double column to int column to be categorical for the reduce/summarization step later
        IntColumn newColumn = oldColumn.map(Math::ceil).setName("SimulationTimeAdjusted").asIntColumn();
        table.addColumns(newColumn);
        return table;
    }

    private static Table transformContinuousValuedMetric(String filePath) {
        Table adjustedadTable = adjustTime(filePath);
        // Do the aggregation (calculate the average)
        Table averagedTable = adjustedadTable.summarize("Value", mean)
            .by("SimulationTimeAdjusted");
        averagedTable.column(0).setName("SimulationTime");
        averagedTable.column(1).setName(createColumnNameFromFileName(filePath));
        return averagedTable;
    }

    // The averaging aggregation (for double, and next biggest integer)
    private static Table transformDiscreteValuedMetric(String filePath) {
        Table adjustedadTable = adjustTime(filePath);
        // Do the aggregation (calculate the average)
        Table averagedTable = adjustedadTable.summarize("Value", mean)
            .by("SimulationTimeAdjusted");
        averagedTable.column(0).setName("SimulationTime");
        DoubleColumn oldColumn = (DoubleColumn) averagedTable.column(1);
        IntColumn newColumn = oldColumn.map(Math::ceil).setName(createColumnNameFromFileName(filePath)).asIntColumn();
        averagedTable.removeColumns(1);
        averagedTable.addColumns(newColumn);
        return averagedTable;
    }

    // Value replication for the instant counts "InstanceCount"
    private static Table replicateServiceInstanceCountValue(String filePath) {
        ColumnType[] types = {INTEGER, INTEGER};
        Table table = createTableFromFile(filePath, types);

        Map<Integer, Integer> instancesCount = new LinkedHashMap<>();
        table.forEach(row -> {
            int time = row.getInt(0);
            int value = row.getInt(1);
            instancesCount.put(time, value);
        });

        IntColumn simulationTimeColumn = IntColumn.create("SimulationTime");
        IntColumn valueColumn = IntColumn.create(createColumnNameFromFileName(filePath));
        int fromTime;
        int toTime;

        for (int i = 0; i <= instancesCount.size() - 2; i++) {
            fromTime = (int) instancesCount.keySet().toArray()[i];
            toTime = (int) instancesCount.keySet().toArray()[i + 1];

            int currentValue = instancesCount.get(fromTime);

            while (fromTime <= toTime) {
                simulationTimeColumn.append(fromTime);
                valueColumn.append(currentValue);
                fromTime++;
            }
        }
        return Table.create(simulationTimeColumn, valueColumn);
    }
}
