package cambio.simulator.export;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import cambio.simulator.ExperimentStartupConfig;
import cambio.simulator.misc.FileUtilities;
import cambio.simulator.models.ExperimentMetaData;
import cambio.simulator.models.MiSimModel;
import cambio.simulator.parsing.GsonHelper;
import cambio.simulator.parsing.adapter.DoubleWriteAdapter;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
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
        final Path reportLocation = generateReportPath(config, metadata);

        if (config != null && config.isOverwriteReportPath()) {
            try {
                FileUtils.deleteDirectory(Paths.get(reportLocation.toString(), "raw").toFile());
            } catch (IOException e) {
                if (config.debugOutputOn()) {
                    e.printStackTrace();
                }
            }
            try {
                FileUtils.deleteDirectory(Paths.get(reportLocation.toString(), "graph").toFile());
            } catch (IOException e) {
                if (config.debugOutputOn()) {
                    e.printStackTrace();
                }
            }
        }

        metadata.setReportLocation(reportLocation);

        try {
            Files.createDirectories(reportLocation);

            //copy metadata, architecture and experiment
            updateMetaData(metadata);

            Files.copy(metadata.getArchitectureDescriptionLocation().toPath(),
                Paths.get(reportLocation.toString(), "architecture.json"), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(metadata.getExperimentDescriptionLocation().toPath(),
                Paths.get(reportLocation.toString(), "experiment.json"), StandardCopyOption.REPLACE_EXISTING);
            FileUtilities.copyFolderFromResources("Report", reportLocation.toFile(),
                StandardCopyOption.REPLACE_EXISTING);

        } catch (SecurityException e) {
            System.out.printf("[Error] No access to report location %s possible%n", reportLocation);
        } catch (IOException e) {
            System.out.printf("[Error] Failed to create report directory at %s possible%n", reportLocation);
            if (config != null && config.debugOutputOn()) {
                e.printStackTrace();
            }
        }
        return reportLocation;
    }

    /**
     * Generates the path to the report directory of the current experiment. Takes into consideration whether the report
     * path should be overwritten or not.
     */
    public static Path generateReportPath(@Nullable ExperimentStartupConfig config,
                                          @NotNull ExperimentMetaData metadata) {
        Objects.requireNonNull(metadata);

        final Path reportLocationBaseDirectory;
        if (config != null && config.getReportLocation() != null) {
            reportLocationBaseDirectory = Paths.get(config.getReportLocation());
        } else {
            reportLocationBaseDirectory = metadata.getReportBaseDirectory();
        }

        final DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss.SSSZ");
        final String dateString = format.format(new Date());
        String subDirectoryPath =
            config != null && config.isOverwriteReportPath() ? "" : metadata.getExperimentName() + "_" + dateString;
        return Paths.get(reportLocationBaseDirectory.toString(), subDirectoryPath);
    }

    /**
     * Serializes the given {@link ExperimentMetaData} as "metadata.json" into the report folder given in
     * {@link ExperimentMetaData#getReportLocation}.
     *
     * @throws IOException if an I/O error occurs writing to or creating or writing the file
     */
    public static void updateMetaData(ExperimentMetaData metaData) throws IOException {
        final Gson gson = GsonHelper.getGsonBuilder()
            .registerTypeAdapter(Double.class, new DoubleWriteAdapter())
            .serializeNulls()
            .setPrettyPrinting().create();
        final String json = gson.toJson(metaData);

        Files.write(Paths.get(metaData.getReportLocation().toString(), "metadata.json"),
            json.getBytes(StandardCharsets.UTF_8),
            StandardOpenOption.CREATE);
    }
}
