package de.rss.fachstudie.MiSim.entities.generator;

import de.rss.fachstudie.MiSim.entities.Operation;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import org.javatuples.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generator that produces NetworkReceiveEvents at the target Service based on a LIMBO profile.
 *
 * @author Lion Wagner
 */
public class LIMBOGenerator extends Generator {

    private final List<Pair<Double, Integer>> targetTimes;
    private Pair<Double, Integer> currentPair; //holds the amount of missing request for the current
    private int currentPairIndex = 0;

    public LIMBOGenerator(Model model, String name, boolean showInTrace, Operation operation, File LimboProfile) {
        super(model, name, showInTrace, operation);

        targetTimes = getPairList(LimboProfile);
    }

    private List<Pair<Double, Integer>> getPairList(File limboProfile) {
        List<Pair<Double, Integer>> tmpList = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(limboProfile.toPath());
            tmpList = lines.stream().map(line -> {
                String[] split = line.split(";|,");
                if (split.length != 2) throw new ArrayIndexOutOfBoundsException("Malformed Limbo File");
                return new Pair<>(Double.valueOf(split[0]), (int) Math.round(Double.parseDouble(split[1]))); //Orientated at Limbo load generator, which just downcasts double values to ints
            }).collect(Collectors.toList());

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException e) {
            //TODO: throw Exception for malformed Limbo file
        }
        tmpList.sort(Comparator.comparing(Pair::getValue0)); //ensure sorting
        return tmpList;
    }

    @Override
    protected TimeInstant getNextTargetTime(TimeInstant lastTargetTime) {
        return getNextTimeInstant();
    }

    @Override
    protected TimeInstant getFirstTargetTime() {
        return getNextTimeInstant();
    }

    private TimeInstant getNextTimeInstant() {
        if (currentPair != null && currentPair.getValue1() > 0) {
            currentPair = currentPair.setAt1(currentPair.getValue1() - 1);
            return new TimeInstant(currentPair.getValue0());
        } else {
            if (currentPairIndex >= targetTimes.size())
                throw new GeneratorStopException("No further rates are defined.");
            currentPair = targetTimes.get(currentPairIndex++);
            return getNextTimeInstant();
        }
    }
}
