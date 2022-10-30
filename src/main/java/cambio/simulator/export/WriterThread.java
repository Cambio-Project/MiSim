package cambio.simulator.export;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

import org.javatuples.Pair;

/**
 * @author Lion Wagner
 */
public class WriterThread implements Runnable {

    public static final ScheduledExecutorService threadPool =
        Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());

    protected final List<Pair<Double, Object>> buffer = Collections.synchronizedList(new ArrayList<>());
    protected final FileOutputStream fileOutputStream;
    protected final Path datasetPath; //debug variable

    protected boolean hasStarted = false;
    private ScheduledFuture<?> scheduledFuture;
    private final ReentrantLock lock = new ReentrantLock();

    public WriterThread(Path datasetPath) throws IOException {
        this.datasetPath = datasetPath;
        this.fileOutputStream = new FileOutputStream(datasetPath.toFile());

        fileOutputStream.write(
            ("Simulation Time" + MiSimReporters.csvSeperator + "Value\n").getBytes(StandardCharsets.UTF_8));
        fileOutputStream.flush();
    }


    public void addDataPoint(double time, Object data) {
        buffer.add(new Pair<>(time, data));
    }


    public void finalizeWriteout() {
        try {
            scheduledFuture.cancel(false); //cancel periodic scheduling
            pollAndWrite(); // flush the rest of the buffer into the output stream
            fileOutputStream.flush(); //flush the output stream to the file
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        this.scheduledFuture = getScheduledFuture();
    }

    protected ScheduledFuture<?> getScheduledFuture() {
        return threadPool.scheduleAtFixedRate(this::pollAndWrite, 0, 1000, TimeUnit.MILLISECONDS);
    }

    private void pollAndWrite() {
        try {
            lock.lock(); //ensure that at maximum 1 thread is writing at a time
            while (!buffer.isEmpty()) {
                try {
                    Pair<Double, Object> data = buffer.remove(0);
                    fileOutputStream.write((data.getValue0() + MiSimReporters.csvSeperator + data.getValue1() + "\n")
                        .getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            lock.unlock();
        }
    }
}
