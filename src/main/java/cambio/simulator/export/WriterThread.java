package cambio.simulator.export;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;

import org.javatuples.Pair;

/**
 * @author Lion Wagner
 */
public class WriterThread implements Runnable {

    protected final List<Pair<Double, Object>> buffer = Collections.synchronizedList(new ArrayList<>());
    protected final FileOutputStream fileOutputStream;
    protected final ScheduledExecutorService executor;
    protected final Path datasetPath; //debug variable

    protected boolean hasStarted = false;
    private ScheduledFuture<?> scheduledFuture;

    public WriterThread(Path datasetPath) throws IOException {
        this.datasetPath = datasetPath;
        this.fileOutputStream = new FileOutputStream(datasetPath.toFile());
        this.executor = Executors.newScheduledThreadPool(1);

        fileOutputStream.write("Simulation Time,Value\n".getBytes(StandardCharsets.UTF_8));
        fileOutputStream.flush();
    }


    public void addDataPoint(double time, Object data) {
        buffer.add(new Pair<>(time, data));
    }


    public void finalizeWriteout() {
        try {
            List<Runnable> tasks = executor.shutdownNow();
            ((ScheduledFuture<?>) tasks.get(0)).cancel(false);
            pollAndWrite(); // flush the rest of the buffer into the output stream
            fileOutputStream.flush(); //flush the output stream to the file
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        this.scheduledFuture = executor.scheduleAtFixedRate(this::pollAndWrite, 0, 1000, TimeUnit.MILLISECONDS);
    }

    private void pollAndWrite() {
        while (!buffer.isEmpty()) {
            try {
                Pair<Double, Object> data = buffer.remove(0);
                fileOutputStream.write((data.getValue0() + "," + data.getValue1() + "\n")
                    .getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
