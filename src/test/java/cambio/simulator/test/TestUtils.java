package cambio.simulator.test;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.entities.networking.NetworkRequestSendEvent;
import cambio.simulator.entities.patterns.ServiceOwnedPattern;
import cambio.simulator.export.CSVData;
import cambio.simulator.export.ReportCollector;
import desmoj.core.simulator.*;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.javatuples.Pair;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;

/**
 * @author Lion Wagner
 */
public class TestUtils {
    private static final Random rng = new Random();


    /**
     * Allowed difference between two text files. Will be measured in Levenshtein distance/Max File length.
     */
    private static final double ALLOWED_FILE_DIFFERENCE_FACTOR = 0.01;

    public static void compareFileContentsOfDirectories(Path dir1, Path dir2) throws IOException {
        System.out.println("\nComparing: \n" + dir1.toAbsolutePath() + " and\n" + dir2.toAbsolutePath());

        Map<Path, byte[]> hashes = new ConcurrentHashMap<>();

        List<String> errors = Collections.synchronizedList(new ArrayList<>());
        List<String> warnings = Collections.synchronizedList(new ArrayList<>());

        List<Path> files1 = Files.walk(dir1).filter(Files::isRegularFile).collect(Collectors.toList());
        List<Path> files2 = Files.walk(dir2).filter(Files::isRegularFile).collect(Collectors.toList());

        if (files1.size() != files2.size()) {
            errors.add("Different number of files in directories. " + files1.size() + " vs " + files2.size());
        }

        files1.parallelStream().forEach(path -> {
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                try (InputStream is = Files.newInputStream(path);
                     DigestInputStream dis = new DigestInputStream(is, md)) {
                    while (dis.read() != -1) ; //read all bytes
                }
                byte[] digest = md.digest();
                hashes.put(path.getFileName(), digest);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });


        files2.parallelStream().forEach(path -> {
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                try (InputStream is = Files.newInputStream(path);
                     DigestInputStream dis = new DigestInputStream(is, md)) {
                    while (dis.read() != -1) ; //read all bytes
                }
                byte[] digest = md.digest();

                Assertions.assertArrayEquals(hashes.get(path.getFileName()), digest);
            } catch (AssertionError e) {

                //read all lines of paths and compute levenshtein distance
                File f1 = Paths.get(dir1.toString(), path.getFileName().toString()).toFile();
                File f2 = Paths.get(dir2.toString(), path.getFileName().toString()).toFile();
                try {
                    String content1 = String.join("\n", Files.readAllLines(f1.toPath(), StandardCharsets.UTF_8));
                    String content2 = String.join("\n", Files.readAllLines(f2.toPath(), StandardCharsets.UTF_8));
                    int threshold = Math.min(1, (int) (Math.max(content1.length(), content2.length()) *
                        ALLOWED_FILE_DIFFERENCE_FACTOR));
                    int distance = new LevenshteinDistance(threshold).apply(content1, content2);

                    if (distance == -1) {
                        errors.add(
                            String.format("%s differs by %d or more character(s) (more than 1 percent difference)",
                                path.getFileName().toString(), threshold));
                    } else {
                        warnings.add(
                            String.format("%s differs by %d character(s)", path.getFileName().toString(), distance));
                    }
                } catch (IOException ex) {
                    errors.add(path.getFileName().toString() + " differs");
                }
                hashes.put(path.getFileName(), new byte[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        List<Path> files1Names = files1.stream().map(Path::getFileName).collect(Collectors.toList());
        List<Path> files2Names = files2.stream().map(Path::getFileName).collect(Collectors.toList());

        //print out the files that are in one but not the other
        files1Names.stream()
            .filter(path -> !files2Names.contains(path))
            .map(path -> path + " only in first directory")
            .forEach(errors::add);
        files2Names.stream()
            .filter(path -> !files1Names.contains(path))
            .map(path -> path + " only in second directory")
            .forEach(errors::add);


        int total = hashes.size();
        int failed = errors.size();
        if (errors.size() > 0 || warnings.size() > 0) {
            System.out.printf("The following %d/%d files differ:%n", failed, total);
        }

        if (warnings.size() > 0) {
            System.out.println("\nWARNINGS:");
            warnings.forEach(System.out::println);
        }

        if (errors.size() > 0) {
            System.out.println("\nERRORS:");
            errors.forEach(System.out::println);
            Assertions.fail(String.format("%d/%d files differ. See above for details.", failed, total));
        }
    }

    public static int nextNonNegative() {
        return nextNonNegative(Integer.MAX_VALUE);
    }

    public static int nextNonNegative(int exclusive_bound) {
        return rng.nextInt(exclusive_bound) & Integer.MAX_VALUE;
    }


    public static void writeOutput(List<? extends CSVData> testResults, String fileName) {
        Path filePath = Paths.get(fileName);

        StringBuilder builder = new StringBuilder(testResults.get(0).toCSVHeader()).append('\n');
        for (CSVData csvData : testResults) {
            builder.append(csvData.toCSVData()).append('\n');
        }

        try {
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
            Files.write(filePath, builder.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Pair<String, String>> compareTwoFiles(File f1, File f2) throws IOException {
        List<String> content1 = Files.readAllLines(f1.toPath(), StandardCharsets.UTF_8);
        List<String> content2 = Files.readAllLines(f2.toPath(), StandardCharsets.UTF_8);
        List<Pair<String, String>> output = Collections.synchronizedList(new ArrayList<>());

        List<String> longerList = content1.size() > content2.size() ? content1 : content2;
        List<String> shorterList = content1.size() > content2.size() ? content2 : content1;

        List<Pair<String,String>> combined = new ArrayList<>();
        for (int i = 0; i < longerList.size(); i++) {
            String s1 = longerList.get(i);
            String s2 = i < shorterList.size() ? shorterList.get(i) : "";
            combined.add(new Pair<>(s1, s2));
        }

        combined.stream().forEach(pair -> {
            String s1 = pair.getValue0();
            String s2 = pair.getValue1();
            if (!s1.equals(s2)) {
                output.add(new Pair<>(s1, s2));
            }
        });

        return output;
    }


}
