package testutils;

import de.rss.fachstudie.MiSim.entities.generator.Generator;
import de.rss.fachstudie.MiSim.entities.generator.IntervalGenerator;
import de.rss.fachstudie.MiSim.entities.microservice.Microservice;
import de.rss.fachstudie.MiSim.entities.microservice.Operation;
import de.rss.fachstudie.MiSim.entities.networking.Dependency;
import de.rss.fachstudie.MiSim.parsing.PatternData;
import desmoj.core.simulator.Model;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static testutils.TestUtils.nextNonNegative;

public class RandomTieredModel extends Model {

    private final ArrayList<Microservice> all_microservices = new ArrayList<>();
    private final ArrayList<Operation> all_operations = new ArrayList<>();
    private final ArrayList<Generator> all_generators = new ArrayList<>();

    private final int maxServicesPerTier;
    private final int tierCount;
    private final Map<Integer, List<Microservice>> tiers = new HashMap<>();

    private int generator_count = 5;
    private double generator_interval = 1;

    public RandomTieredModel(String name, int maxServicesPerTier, int tierCount) {
        super(null, name, false, false);
        this.maxServicesPerTier = maxServicesPerTier;
        this.tierCount = tierCount;
    }

    @Override
    public String description() {
        return null;
    }

    @Override
    public void doInitialSchedules() {
        for (Microservice microservice : all_microservices) {
            microservice.start();
        }
        createGenerators(generator_count, generator_interval);
        all_generators.forEach(Generator::doInitialSelfSchedule);
    }

    @Override
    public void init() {
        IntStream.range(1, tierCount + 1).forEach(value -> tiers.put(value, new ArrayList<>()));
        for (List<Microservice> tier : tiers.values()) {
            for (int i = 0; i < Math.max(1, nextNonNegative(maxServicesPerTier)); i++) {
                Microservice current_ms = new Microservice(this, "MS" + all_microservices.size(), false);
                current_ms.setInstancesCount(2);
                current_ms.setCapacity(nextNonNegative());
                current_ms.setPatternData(new PatternData[]{TestUtils.getRetryPatternMock(this), TestUtils.getCircuitBreaker(this), TestUtils.getAutoscaler(this)});
                ArrayList<Operation> current_ops = new ArrayList<>();
                for (int j = 0; j < new Random().nextInt(5) + 1; j++) {
                    Operation currentOP = new Operation(this, String.format("MS%d_OP%d", i, j), false, current_ms, nextNonNegative(current_ms.getCapacity() / 2));
                    current_ops.add(currentOP);
                }
                current_ms.setOperations(current_ops.toArray(new Operation[0]));
                all_operations.addAll(current_ops);
                all_microservices.add(current_ms);
                tier.add(current_ms);
            }
        }


        for (Microservice microservice : all_microservices) {
            int current_tier = getTier(microservice);
            if (current_tier == tierCount) continue;

            List<Microservice> nextTier = tiers.get(current_tier + 1);
            List<Operation> nextTierOps = new ArrayList<>();
            nextTier.forEach(microservice1 -> nextTierOps.addAll(Arrays.asList(microservice1.getOperations())));

            for (Operation operation : microservice.getOperations()) {
                Set<Dependency> dependencies = new HashSet<>();
                int depTargetCount = nextNonNegative(10);
                Predicate<Operation> alreadyTargeted = op -> dependencies.stream().anyMatch(dependency -> dependency.getTargetOperation() == op);
                for (int i = 0; i < Math.min(depTargetCount, nextTierOps.size()); i++) {
                    Operation targetOP = null;
                    while (targetOP == null || alreadyTargeted.test(targetOP)) {
                        targetOP = all_operations.get(nextNonNegative(all_operations.size()));
                    }
                }
                operation.setDependencies(dependencies.toArray(new Dependency[0]));
            }
        }
    }

    private int getTier(Microservice microservice) {
        for (Map.Entry<Integer, List<Microservice>> entry : tiers.entrySet()) {
            if (entry.getValue().contains(microservice))
                return entry.getKey();
        }
        return -1;
    }

    public void setGenerator_count(int generator_count) {
        this.generator_count = generator_count;
    }

    public void setGenerator_interval(double generator_interval) {
        this.generator_interval = generator_interval;
    }

    private void createGenerators(int generator_count, double interval) {
        List<Operation> tier1Operations = new ArrayList<>();
        tiers.get(1).forEach(microservice -> tier1Operations.addAll(Arrays.asList(microservice.getOperations())));
        all_generators.addAll(IntStream.range(0, generator_count)
                .mapToObj(operand -> (Generator)
                        new IntervalGenerator(this,
                                "intervalgen",
                                false,
                                tier1Operations.get(nextNonNegative(tier1Operations.size())), interval, 0))
                .collect(Collectors.toList()));
    }

    public ArrayList<Microservice> getAll_microservices() {
        return all_microservices;
    }
}

