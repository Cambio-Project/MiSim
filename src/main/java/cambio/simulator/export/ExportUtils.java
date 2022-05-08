package cambio.simulator.export;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import cambio.simulator.ExperimentStartupConfig;
import cambio.simulator.misc.FileUtilities;
import cambio.simulator.models.ExperimentMetaData;
import cambio.simulator.models.MiSimModel;
import cambio.simulator.parsing.GsonHelper;
import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Utility functions for creating the experiment report.
 *
 * @author Lion Wagner
 */
public final class ExportUtils {

    /**
     * Creates the report directory of the current experiment.
     *
     * @see ExportUtils#prepareReportDirectory(ExperimentStartupConfig, ExperimentMetaData)
     */
    public static Path prepareReportDirectory(@Nullable ExperimentStartupConfig config, @NotNull MiSimModel model) {
        return prepareReportDirectory(config, model.getExperimentMetaData());
    }

    /**
     * Creates the report directory of the current experiment. The directory name will consist of the experiment name
     * and a timestamp of when the directory was created.
     *
     * @param config   startup configuration, can overwrite the report folder location of the metadata. Can be null.
     * @param metadata metadata that should be serialized and contains the report directory base location
     * @return the {@link Path} to the created report directory.
     */
    public static Path prepareReportDirectory(@Nullable ExperimentStartupConfig config,
                                              @NotNull ExperimentMetaData metadata) {
        final Path reportLocationBaseDirectory;
        if (config != null && config.getReportLocation() != null) {
            reportLocationBaseDirectory = Paths.get(config.getReportLocation());
        } else {
            reportLocationBaseDirectory = metadata.getReportBaseDirectory();
        }

        final DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss.SSSZ");
        final String dateString = format.format(new Date());
        final Path reportLocation = Paths.get(reportLocationBaseDirectory.toString(),
            metadata.getExperimentName() + "_" + dateString);

        metadata.setReportLocation(reportLocation);

        try {
            Files.createDirectories(reportLocation);

            //copy metadata, architecture and experiment
            updateMetaData(metadata);

            Files.copy(metadata.getArchitectureDescriptionLocation().toPath(),
                Paths.get(reportLocation.toString(), "architecture.json"));
            Files.copy(metadata.getExperimentDescriptionLocation().toPath(),
                Paths.get(reportLocation.toString(), "experiment.json"));
            FileUtilities.copyFolderFromResources("Report", reportLocation.toFile(),
                StandardCopyOption.REPLACE_EXISTING);

        } catch (SecurityException e) {
            System.out.printf("[Error] No access to report location %s possible%n", reportLocationBaseDirectory);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return reportLocation;
    }

    /**
     * Serializes the given {@link ExperimentMetaData} as "metadata.json" into the report folder given in {@link
     * ExperimentMetaData#getReportLocation}.
     *
     * @throws IOException if an I/O error occurs writing to or creating or writing the file
     */
    public static void updateMetaData(ExperimentMetaData metaData) throws IOException {
        final Gson gson = GsonHelper.getGsonBuilder().serializeNulls().setPrettyPrinting().create();
        final String json = gson.toJson(metaData);

        Files.write(Paths.get(metaData.getReportLocation().toString(), "metadata.json"),
            json.getBytes(StandardCharsets.UTF_8),
            StandardOpenOption.CREATE);
    }
}
