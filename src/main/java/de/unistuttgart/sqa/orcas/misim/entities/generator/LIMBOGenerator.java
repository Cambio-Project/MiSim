package de.unistuttgart.sqa.orcas.misim.entities.generator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import de.unistuttgart.sqa.orcas.misim.entities.microservice.Operation;
import de.unistuttgart.sqa.orcas.misim.entities.networking.UserRequest;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import org.javatuples.Pair;

/**
 * Generator that produces UserRequestArrivalEvent at a target Service Endpoint based on a LIMBO load model.
 *
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
 *     <td> repetition_skip </td> <td>1</td> <td>Time in s to wait between repetitions (usually the default interval of
 *     the load model)</td>
 *   </tr>
 *   <caption>Json properties of this generator.</caption>
 * </table>
 *
 * @author Lion Wagner
 * @see <a href="https://github.com/joakimkistowski/LIMBO">https://github.com/joakimkistowski/LIMBO</a>
 */
public class LIMBOGenerator extends Generator {

    private static final double DEFAULT_REPETITION_SKIP = 1;
    private static final boolean DEFAULT_REPEATING = false;


    private final List<Double> alternative;
    private Iterator<Double> iter;
    private final List<Pair<Double, Integer>> targetTimes;
    private final LinkedList<Pair<Double, Integer>> workingCopyTargetTimes;
    private int repetitions = 0;

    private final double repetitionSkip;
    private final boolean repeating;

    public LIMBOGenerator(Model model, String name, boolean showInTrace, Operation operation, File limboModel) {
        this(model, name, showInTrace, operation, limboModel, DEFAULT_REPEATING, DEFAULT_REPETITION_SKIP);
    }

    public LIMBOGenerator(Model model, String name, boolean showInTrace, Operation operation, File limboModel,
                          double repetitionSkip) {
        this(model, name, showInTrace, operation, limboModel, DEFAULT_REPEATING, repetitionSkip);
    }

    public LIMBOGenerator(Model model, String name, boolean showInTrace, Operation operation, File limboModel,
                          boolean repeating) {
        this(model, name, showInTrace, operation, limboModel, repeating, DEFAULT_REPETITION_SKIP);
    }

    /**
     * Constructs a new {@link LIMBOGenerator}.
     *
     * @param model          handling DESMO-J model
     * @param name           name of the generator
     * @param showInTrace    whether the sending of the {@link UserRequest} should be shown in the trace
     * @param operation      which {@link Operation} is the target of all {@link UserRequest}s
     * @param limboModel     path to the corrisponding LIMBO model
     * @param repeating      whether the given profile should be repeated after it was completed
     * @param repetitionSkip wait duration in between repetitions, default is 1
     * @see <a href="https://github.com/joakimkistowski/LIMBO">https://github.com/joakimkistowski/LIMBO</a>
     */
    public LIMBOGenerator(Model model, String name, boolean showInTrace, Operation operation, File limboModel,
                          boolean repeating, double repetitionSkip) {
        super(model, name, showInTrace, operation);

        targetTimes = getPairList(limboModel);
        alternative = new LinkedList<>(applyDistribution(targetTimes));
        iter = alternative.iterator();
        workingCopyTargetTimes = new LinkedList<>(targetTimes);
        this.repeating = repeating;
        this.repetitionSkip = repetitionSkip;
    }

    private List<Double> applyDistribution(List<Pair<Double, Integer>> pairs) {
        return pairs.stream().parallel().flatMap(pair -> expandPair(pair).stream()).collect(Collectors.toList());
    }

    private List<Double> expandPair(final Pair<Double, Integer> pair) {
        return expandPair(pair, 1);
    }

    private List<Double> expandPair(final Pair<Double, Integer> pair, final double distributionDuration) {
        double time = pair.getValue0();
        int currentLoad = pair.getValue1() / 5;
        double timeSteps = distributionDuration / currentLoad;
        List<Double> out = new ArrayList<>(currentLoad);
        for (int i = 0; i < currentLoad; i++, time += timeSteps) {
            out.add(time);
        }
        return out;
    }


    private List<Pair<Double, Integer>> getPairList(File limboProfile) {
        List<Pair<Double, Integer>> tmpList = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(limboProfile.toPath());
            tmpList = lines.stream().map(line -> {
                String[] split = line.split(";|,");
                if (split.length != 2) {
                    throw new ArrayIndexOutOfBoundsException("Malformed Limbo File");
                }
                return new Pair<>(Double.valueOf(split[0]), (int) Math.round(Double.parseDouble(
                    split[1]))); //Orientated at Limbo load generator, which just downcasts double values to ints
            }).collect(Collectors.toList());

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException e) {
            //TODO: throw Exception for malformed Limbo file
        }
        tmpList = tmpList.stream().filter(objects -> objects.getValue1() > 0)
            .collect(Collectors.toList()); // remove entries with 0 load
        tmpList.sort(Comparator.comparing(Pair::getValue0)); //ensure sorting

        return tmpList;
    }

    @Override
    protected TimeInstant getNextTargetTime(TimeInstant lastTargetTime) {
        return getNextTimeInstant();
    }

    @Override
    protected TimeInstant getFirstTargetTime() {
        if (workingCopyTargetTimes.isEmpty()) {
            throw new GeneratorStopException("Load Profile does not define any loads.");
        }
        return getNextTimeInstant();
    }

    private TimeInstant getNextTimeInstant() {
        if (iter.hasNext()) {
            return new TimeInstant(iter.next() + (repetitions * repetitionSkip));
        } else {
            if (repeating) {
                repetitions++;
                iter = alternative.iterator();
                return getNextTargetTime();
            } else {
                throw new GeneratorStopException("No further rates are defined.");
            }
        }

    }
}
