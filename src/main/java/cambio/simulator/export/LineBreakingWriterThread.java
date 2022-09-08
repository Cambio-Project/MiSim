package cambio.simulator.export;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.javatuples.Pair;

/**
 * @author Lion Wagner
 */
public class LineBreakingWriterThread extends WriterThread {


    private double currentTime = -1;

    public LineBreakingWriterThread(Path datasetPath) throws IOException {
        super(datasetPath);
    }

    @Override
    public void addDataPoint(double time, Object data) {
        if (time != currentTime) {
            if (hasStarted) {
                closeLine();
            }
            startNewLine(time);
            buffer.add(new Pair<>(time, data));
            hasStarted = true;
        }
        buffer.add(new Pair<>(time, MiSimReporters.csvSeperator + " " + data));
    }

    private void startNewLine(double time) {
        currentTime = time;
        buffer.add(new Pair<>(time, time + MiSimReporters.csvSeperator + " ["));
    }

    private void closeLine() {
        buffer.add(new Pair<>(currentTime, "]\n"));
    }

    @Override
    public void finalizeWriteout() {
        closeLine();
        super.finalizeWriteout();
    }

    @Override
    protected ScheduledFuture<?> getScheduledFuture() {
        return threadPool.scheduleAtFixedRate(this::pollAndWrite, 0, 100, TimeUnit.MILLISECONDS);
    }

    private void pollAndWrite() {
        while (!buffer.isEmpty()) {
            try {
                Pair<Double, Object> data = buffer.remove(0);
                fileOutputStream.write((data.getValue1().toString()).getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
