package cambio.simulator.export;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

import org.javatuples.Pair;

/**
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

    public AsyncReportWriter(final Path datasetPath) throws IOException {
        this.datasetPath = Objects.requireNonNull(datasetPath).toString().endsWith(".csv")
            ? datasetPath : datasetPath.resolveSibling(datasetPath.getFileName() + ".csv");
        this.fileOutputStream = new FileOutputStream(this.datasetPath.toFile());
        this.formatter = createFormatter();

        fileOutputStream.write(
            ("Simulation Time" + MiSimReporters.csvSeperator + "Value\n").getBytes(StandardCharsets.UTF_8));
        fileOutputStream.flush();
        scheduledFuture = threadPool.scheduleAtFixedRate(this::writeout, 0, 100, TimeUnit.MILLISECONDS);
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
            while (!buffer.isEmpty()) {
                try {
                    String data = formatter.apply(buffer.remove(0));
                    fileOutputStream.write(data.getBytes(StandardCharsets.UTF_8));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } finally {
            lock.unlock();
        }
    }


    protected void finalizingTODOs() {
    }

    public abstract Function<T, String> createFormatter();
}
