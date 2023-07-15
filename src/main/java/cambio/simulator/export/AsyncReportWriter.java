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

    /**
     * Periode in ms with wich the buffer is flushed to the file. It is only read once when the writer is created. One
     * can try and play around with this parameter when you encounter either CPU or IO performance issues.
     */
    private static int FLUSH_PERIOD_MS = 100;

    /**
     * Number of threads used to write to the file. By default it uses all but one available processor. One can try and
     * play around with this parameter when you encounter either CPU or IO performance issues.
     */
    private static int THREAD_POOL_SIZE = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);


    public static int getFlushPeriodMs() {
        return FLUSH_PERIOD_MS;
    }

    public static void setFlushPeriodMs(int flushPeriodMs) {
        FLUSH_PERIOD_MS = flushPeriodMs;
    }

    public static int getThreadPoolSize() {
        return THREAD_POOL_SIZE;
    }

    public static void setThreadPoolSize(int threadPoolSize) {
        THREAD_POOL_SIZE = threadPoolSize;
    }

    protected final List<T> buffer = Collections.synchronizedList(new ArrayList<>());

    /**
     * Thread pool used by the AsyncReportWriters to write to the output file.
     */
    private static final ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(THREAD_POOL_SIZE);

    private final FileOutputStream fileOutputStream;

    public final Path datasetPath;

    private final ReentrantLock lock = new ReentrantLock();
    private final Function<T, String> formatter;

    /**
     * Future that describes the task of regularly writing the buffer to the file.
     */
    private final ScheduledFuture<?> scheduledFuture;

    /**
     * Creates a new AsyncReportWriter and opens a new output stream to the given file (dataset path).
     *
     * @param datasetPath path to the file to be written to. If the path does not end with .csv, it will be appended.
     * @param headers     headers to be written to the file
     * @throws IOException if the file output stream cannot be opened
     */
    public AsyncReportWriter(final Path datasetPath, final String[] headers) throws IOException {
        this.datasetPath = Objects.requireNonNull(datasetPath).toString().endsWith(".csv")
            ? datasetPath : datasetPath.resolveSibling(datasetPath.getFileName() + ".csv");
        this.fileOutputStream = new FileOutputStream(this.datasetPath.toFile());
        this.formatter = createFormatter(); //grab the formatter implementation for the inheriting class


        fileOutputStream.write(createHeader(headers).getBytes(StandardCharsets.UTF_8));
        fileOutputStream.flush();
        scheduledFuture = threadPool.scheduleAtFixedRate(this::writeout, 0, FLUSH_PERIOD_MS, TimeUnit.MILLISECONDS);
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

    /**
     * Finalizes the writeout.
     * <ol>
     *    <li>Cancels the continuous writeout and flushes the buffer to file</li>
     *    <li>Calls {@link AsyncReportWriter#finalizingTodos()} to collect closing outputs</li>
     *    <li>Writes the collected data to the output stream</li>
     *    <li>Finally, flushing and closing the output stream</li>
     * </ol>
     *
     * @throws RuntimeException if an error occurs while flushing or closing the output stream
     */
    public final void finalizeWriteout() {
        try {
            scheduledFuture.cancel(false); //cancel periodic scheduling
            finalizingTodos();
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
     * <p>
     * The default implementation does nothing.
     */
    protected void finalizingTodos() {
    }

    public abstract Function<T, String> createFormatter();
}
