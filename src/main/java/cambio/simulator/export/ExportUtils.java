package cambio.simulator.export;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import cambio.simulator.cli.CLI;
import cambio.simulator.misc.JarUtil;
import cambio.simulator.models.ExperimentMetaData;
import cambio.simulator.models.MiSimModel;
import cambio.simulator.parsing.GsonHelper;
import com.google.gson.Gson;

/**
 * Utility functions for creating the experiment report.
 *
 * @author Lion Wagner
 */
public final class ExportUtils {


    public static File prepareReportFolder(MiSimModel model) {
        return prepareReportFolder(model.getExperimentMetaData());
    }

    public static File prepareReportFolder(ExperimentMetaData metaData) {
        File reportLocationBaseDirectory;
        if (CLI.reportLocation.getValue() != null) {
            reportLocationBaseDirectory = new File(CLI.reportLocation.getValue());
        } else {
            reportLocationBaseDirectory = metaData.getReportLocation();
        }
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss.SSSZ");
        String dateString = format.format(new Date());
        File reportLocation = Paths.get(reportLocationBaseDirectory.getAbsolutePath(),
            metaData.getExperimentName() + "_" + dateString).toFile();

        Gson gson = GsonHelper.getGson();

        try {
            boolean ignored = reportLocation.mkdirs();
            //copy metadata, architecture and experiment
            String json = gson.toJson(metaData);
            Files.write(Paths.get(reportLocation.getPath(), "meta.json"),
                json.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE);
            Files.copy(metaData.getArchFileLocation().toPath(),
                Paths.get(reportLocation.getPath(), "architecture.json"));
            Files.copy(metaData.getExpFileLocation().toPath(),
                Paths.get(reportLocation.getPath(), "experiment.json"));
            JarUtil.copyFolderFromJar("Report", reportLocation, StandardCopyOption.REPLACE_EXISTING);

        } catch (SecurityException e) {
            System.out.println(
                String.format("[Error] No access to report location %s possible",
                    reportLocationBaseDirectory.getAbsolutePath()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return reportLocation;
    }
}
