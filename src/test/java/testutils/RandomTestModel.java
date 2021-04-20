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

public class RandomTestModel extends Model {

    public final static int duration = 1200;

    private final ArrayList<Microservice> all_microservices = new ArrayList<>();
    private final ArrayList<Operation> all_operations = new ArrayList<>();


    private final int current_service_count;
    private final int current_tier_count;
    private final Map<Integer, List<Microservice>> tiers = new HashMap<>();


    public RandomTestModel(Model model, String s, int current_service_count, int current_tier_count) {
        super(model, s, false, false);
        this.current_service_count = current_service_count;
        this.current_tier_count = current_tier_count;
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
        createGenerators().forEach(Generator::doInitialSelfSchedule);

    }

    @Override
    public void init() {
        IntStream.range(1, current_tier_count + 1).forEach(value -> tiers.put(value, new ArrayList<>()));
        for (List<Microservice> tier : tiers.values()) {
            for (int i = 0; i < Math.max(1, nextNonNegative(current_service_count)); i++) {
                Microservice current_ms = new Microservice(this, "MS" + all_microservices.size(), false);
                current_ms.setInstancesCount(1);
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
            if (current_tier == current_tier_count) continue;

            List<Microservice> nextTier = tiers.get(current_tier + 1);
            List<Operation> nextTierOps = new ArrayList<>();
            nextTier.forEach(microservice1 -> nextTierOps.addAll(Arrays.asList(microservice1.getOperations())));

            for (Operation operation : microservice.getOperations()) {
                Set<Dependency> dependencies = new HashSet<>();
                int depTargetCount = nextNonNegative(10);
//                Predicate<Operation> testForCircular = op -> op == null || op.getOwnerMS() == operation.getOwnerMS() ||
//                        dependencies.stream().anyMatch(dependency -> dependency.getTargetOperation().equals(op));

//                for (int i = 0; i < depTargetCount && all_operations.stream().anyMatch(testForCircular.negate()); i++) {
//                    Operation targetOP = null;
//                    while (testForCircular.test(targetOP)) {
//                        targetOP = all_operations.get(nextNonNegative(all_operations.size()));
//                    }
//                    dependencies.add(new Dependency(operation, targetOP, rng.nextDouble()));
//                }
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

    private List<Generator> createGenerators() {
        List<Operation> tier1Operations = new ArrayList<>();
        tiers.get(1).forEach(microservice -> tier1Operations.addAll(Arrays.asList(microservice.getOperations())));
        return IntStream.range(0, 5)
                .mapToObj(operand -> (Generator) new IntervalGenerator(this, "intervalgen", false, tier1Operations.get(nextNonNegative(tier1Operations.size())), 1, nextNonNegative(duration / 2)))
                .collect(Collectors.toList());
    }
}

