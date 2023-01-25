package cambio.simulator.export;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * A base class that provides functionalities to asynchronously write data to a file.
 *
 * @param <T> Type of the data stored in the buffer and transferred to the writer thread and formatter
 * @author Lion Wagner
 */
public abstract class AsyncReportWriter<T> {

    protected final List<T> buffer = Collections.synchronizedList(new ArrayList<>());

    private static final ScheduledExecutorService threadPool =
        Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());

    private final FileOutputStream fileOutputStream;

    public final Path datasetPath;

    private final ReentrantLock lock = new ReentrantLock();
    private final Function<T, String> formatter;
    private ScheduledFuture<?> scheduledFuture;

    public AsyncReportWriter(final Path datasetPath, final String[] headers) throws IOException {
        this.datasetPath = Objects.requireNonNull(datasetPath).toString().endsWith(".csv")
            ? datasetPath : datasetPath.resolveSibling(datasetPath.getFileName() + ".csv");
        this.fileOutputStream = new FileOutputStream(this.datasetPath.toFile());
        this.formatter = createFormatter();


        fileOutputStream.write(createHeader(headers).getBytes(StandardCharsets.UTF_8));
        fileOutputStream.flush();
        scheduledFuture = threadPool.scheduleAtFixedRate(this::writeout, 0, 100, TimeUnit.MILLISECONDS);
    }

    private String createHeader(String[] headers) {
        StringBuilder builder = new StringBuilder(MiSimReporters.DEFAULT_TIME_COLUMN_NAME);
        for (String header : headers) {
            builder
                .append(MiSimReporters.csvSeperator)
                .append(header);
        }
        builder.append("\n");
        return builder.toString();
    }

    public abstract void addDataPoint(double time, Object data);

    public final void finalizeWriteout() {
        try {
            scheduledFuture.cancel(false); //cancel periodic scheduling
            finalizingTODOs();
            writeout();
            fileOutputStream.flush(); //flush the output stream to the file
            fileOutputStream.close();
        } catch (IOException e) {
            System.out.println("Error while finalizing writeout.");
            throw new RuntimeException(e);
        }
    }

    private void writeout() {
        try {
            lock.lock();
            while (!buffer.isEmpty() && fileOutputStream.getChannel().isOpen()) {
                try {
                    T data = buffer.remove(0);
                    String formattedData = formatter.apply(data);
                    fileOutputStream.write(formattedData.getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } finally {
            lock.unlock();
        }
    }


    /**
     * This method is called before the final flush to disk and offers the possibility to add some finalizing
     * information to the buffer.
     *
     * The default implementation does nothing.
     */
    protected void finalizingTODOs() {
    }

    public abstract Function<T, String> createFormatter();
}
