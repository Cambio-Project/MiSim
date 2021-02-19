package de.rss.fachstudie.MiSim.entities.networking;

import org.javatuples.Quartet;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * simple class to collect responses for now
 *
 * @author Lion Wagner
 */
public class ReportCollector {
    List<Quartet<Request, Double, Double, Double>> finishedRequests = new ArrayList<>();

    public void reportRequestFinished(Request request) {
        Objects.requireNonNull(request);
        finishedRequests.add(new Quartet<>(request, request.getTimestamp_send().getTimeAsDouble(), request.getTimestamp_received().getTimeAsDouble(), request.getResponseTime()));
    }


    public void write_out() {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.GERMAN);
        DecimalFormat df = (DecimalFormat) nf;

        String seperator = ";";
        StringBuilder sb = new StringBuilder("Request" + seperator + "Started" + seperator + "Finished" + seperator + "Response Time\n");
        finishedRequests.forEach(objects -> {
            List<String> elements = objects.toList().stream().map(Object::toString).collect(Collectors.toList());
            sb.append(String.join(seperator, elements)).append("\n");
        });

        try {
            Files.write(Paths.get("./Report/rsp.csv"), sb.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
