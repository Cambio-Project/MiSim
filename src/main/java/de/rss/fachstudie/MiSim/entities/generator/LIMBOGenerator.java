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
 * Generator that produces UserRequestArrivalEvent at a target Service Endpoint based on a LIMBO load model.
 * <p>
 * This generator provides the following json options to the architecture:
 * <table border="1">
 *   <tr>
 *     <td>Name </td> <td> Default Value </td> <td> Description</td>
 *   </tr>
 *   <tr>
 *       <td>limbo_model</td> <td>NONE(required) </td><td>Path to the LIMBO load model</td>
 *   </tr>
 *   <tr>
 *     <td> repeating </td> <td>false</td> <td>Whether the Profile should be repeated after finishing</td>
 *   </tr>
 *   <tr>
 *     <td> repetition_skip </td> <td>1</td> <td>Time in s to wait between repetitions (usually the default interval of the load model)</td>
 *   </tr>
 * </table>
 *
 * @author Lion Wagner
 * @see <a>https://github.com/joakimkistowski/LIMBO</a>
 */
public class LIMBOGenerator extends Generator {

    private static final double DEFAULT_REPETITION_SKIP = 1;
    private static final boolean DEFAULT_REPEATING = false;


    private final List<Pair<Double, Integer>> targetTimes;
    private Pair<Double, Integer> currentPair; //holds the amount of missing request for the current
    private int currentPairIndex = 0;
    private int repetitions = 0;
    private double maxTimeShift = 0;

    private final double repetition_skip;
    private final boolean repeating;

    public LIMBOGenerator(Model model, String name, boolean showInTrace, Operation operation, File limboModel) {
        this(model, name, showInTrace, operation, limboModel, DEFAULT_REPEATING, DEFAULT_REPETITION_SKIP);
    }

    public LIMBOGenerator(Model model, String name, boolean showInTrace, Operation operation, File limboModel, double repetition_skip) {
        this(model, name, showInTrace, operation, limboModel, DEFAULT_REPEATING, repetition_skip);
    }

    public LIMBOGenerator(Model model, String name, boolean showInTrace, Operation operation, File limboModel, boolean repeating) {
        this(model, name, showInTrace, operation, limboModel, repeating, DEFAULT_REPETITION_SKIP);
    }


    public LIMBOGenerator(Model model, String name, boolean showInTrace, Operation operation, File LimboProfile, boolean repeating, double repetition_skip) {
        super(model, name, showInTrace, operation);

        targetTimes = getPairList(LimboProfile);
        this.repeating = repeating;
        this.repetition_skip = repetition_skip;
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
            return new TimeInstant(
                    currentPair.getValue0() +
                            repetitions * (maxTimeShift + repetition_skip));
        } else {
            if (currentPairIndex >= targetTimes.size()) {
                if (repeating) {
                    repetitions++;
                    maxTimeShift = currentPair.getValue0();
                    currentPairIndex = 0;
                    currentPair = null;
                    return getNextTimeInstant();
                } else {
                    throw new GeneratorStopException("No further rates are defined.");
                }
            }
            currentPair = targetTimes.get(currentPairIndex++);
            return getNextTimeInstant();
        }
    }
}
