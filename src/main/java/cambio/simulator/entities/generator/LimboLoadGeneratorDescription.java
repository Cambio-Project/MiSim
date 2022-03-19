package cambio.simulator.entities.generator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collector;

import cambio.simulator.misc.CollectorImpl;
import cambio.simulator.parsing.JsonTypeName;
import com.google.common.base.Strings;
import com.google.gson.annotations.SerializedName;
import org.javatuples.Pair;

/**
 * Adds properties to a {@link LoadGeneratorDescription} for describing a limbo load model-based load generator.
 *
 * <p>
 * A generator of this type sends requests based on a predefined load curve definition.
 *
 * @author Lion Wagner
 * @see <a href="https://se.informatik.uni-wuerzburg.de/software-engineering-group/tools/limbo/">LIMBO Website</a>
 */
@JsonTypeName(value = "limbo", alternativeNames = {"limbo_generator"})
public class LimboLoadGeneratorDescription extends LoadGeneratorDescription {

    @SerializedName(value = "model", alternate = {"limbo_model", "limbo_file", "file", "limbo", "profile"})
    protected File modelFile = null;

    @Override
    protected ArrivalRateModel createArrivalRateModel() {
        return new LimboArrivalRateModel(modelFile);
    }


    private static final class LimboArrivalRateModel extends ArrivalRateModel {

        private final List<Pair<Double, Integer>> arrivalPairs;
        private Iterator<Pair<Double, Integer>> arrivalPairsIterator;

        private int leftOverDemandForCurrentTargetTime = 0;
        private double currentTargetTime = Double.NEGATIVE_INFINITY;

        public LimboArrivalRateModel(File modelFile) {
            Objects.requireNonNull(modelFile, () -> {
                System.out.println("[Error] Model file was not defined in experiment description.");
                return "Model file was not defined in experiment description";
            });
            arrivalPairs = getPairList(modelFile);
            arrivalPairsIterator = arrivalPairs.iterator();
        }


        @Override
        protected double getDuration() {
            return arrivalPairs.get(arrivalPairs.size() - 1).getValue0();
        }

        @Override
        protected void resetModelIteration() {
            leftOverDemandForCurrentTargetTime = 0;
            currentTargetTime = -1;
            arrivalPairsIterator = arrivalPairs.iterator();
        }

        @Override
        public boolean hasNext() {
            return arrivalPairsIterator.hasNext()
                || (leftOverDemandForCurrentTargetTime > 0 && currentTargetTime >= 0);
        }

        @Override
        public Double next() {
            if (!hasNext()) {
                return null;
            }

            if (leftOverDemandForCurrentTargetTime <= 0) {
                Pair<Double, Integer> next = arrivalPairsIterator.next();
                currentTargetTime = next.getValue0();
                leftOverDemandForCurrentTargetTime = next.getValue1();
                return next();
            } else {
                leftOverDemandForCurrentTargetTime--;
                return currentTargetTime;
            }
        }


        private List<Pair<Double, Integer>> getPairList(File limboProfile) {
            List<Pair<Double, Integer>> tmpList;
            try {
                List<String> lines = Files.readAllLines(limboProfile.toPath());
                tmpList = lines.stream()
                    .parallel()
                    .filter(line -> !Strings.isNullOrEmpty(line))
                    .map(line -> {
                        String[] split = line.split("[;,]");
                        if (split.length != 2) {
                            throw new ArrayIndexOutOfBoundsException("Malformed Limbo File");
                        }
                        return new Pair<>(Double.valueOf(split[0]), (int) Math.round(Double.parseDouble(
                            split[1]))); //Orientated at the HTTP Loadgenerator, which just casts double values to ints
                    })
                    // ensure time constraints and remove entries with 0 (or negative) load
                    .filter(pair -> pair.getValue0() >= 0 && pair.getValue1() > 0)
                    // collect into a linked list
                    .collect(
                        new CollectorImpl<Pair<Double, Integer>, LinkedList<Pair<Double, Integer>>,
                            LinkedList<Pair<Double, Integer>>>(LinkedList::new, LinkedList::add, (pairs, pairs2) -> {
                            pairs.addAll(pairs2);
                            return pairs;
                        }, new HashSet<Collector.Characteristics>() {
                            {
                                add(Collector.Characteristics.IDENTITY_FINISH);
                            }
                        })
                    );
                tmpList.sort(Comparator.comparing(Pair::getValue0)); //ensure sorting
                return tmpList;

            } catch (IOException | ArrayIndexOutOfBoundsException e) {
                throw new RuntimeException(String.format("Could not read limbo model '%s' correctly",
                    limboProfile.getAbsolutePath()), e);
            }
        }
    }
}
