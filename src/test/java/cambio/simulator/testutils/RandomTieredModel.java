package cambio.simulator.testutils;

import static cambio.simulator.testutils.TestUtils.nextNonNegative;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import cambio.simulator.entities.generator.IntervalLoadGeneratorDescription;
import cambio.simulator.entities.generator.LoadGeneratorDescription;
import cambio.simulator.entities.generator.LoadGeneratorDescriptionExecutor;
import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.entities.microservice.Operation;
import cambio.simulator.entities.networking.DependencyDescription;
import desmoj.core.simulator.Model;
import org.junit.jupiter.api.Assertions;

public class RandomTieredModel extends Model {

    private final ArrayList<Microservice> all_microservices = new ArrayList<>();
    private final ArrayList<Operation> all_operations = new ArrayList<>();
    private final ArrayList<LoadGeneratorDescriptionExecutor> all_generators = new ArrayList<>();

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
        createGenerators(generator_count, generator_interval, this);
        all_generators.forEach(LoadGeneratorDescriptionExecutor::doInitialSelfSchedule);
    }

    @Override
    public void init() {
        IntStream.range(1, tierCount + 1).forEach(value -> tiers.put(value, new ArrayList<>()));

        int tiernb = 0;
        for (List<Microservice> tier : tiers.values()) {
            for (int i = 0; i < Math.max(1, nextNonNegative(maxServicesPerTier)); i++) {
                Microservice current_ms = new Microservice(this, "MS_" + tiernb + "_" + i, false);
                current_ms.setInstancesCount(2);
                current_ms.setCapacity(nextNonNegative());
                //todo: patterns

                ArrayList<Operation> current_ops = new ArrayList<>();
                for (int j = 0; j < new Random().nextInt(5) + 1; j++) {
                    Operation currentOP =
                        new Operation(this, String.format("%s_OP%d", current_ms.getName(), j), false, current_ms,
                            nextNonNegative(Math.max(current_ms.getCapacity() / (int) Math.pow(5, tiernb + 1), 1)));//
                    current_ops.add(currentOP);
                }
                current_ms.setOperations(current_ops.toArray(new Operation[0]));
                all_operations.addAll(current_ops);
                all_microservices.add(current_ms);
                tier.add(current_ms);
            }
            tiernb++;
        }


        for (Microservice microservice : all_microservices) {
            int current_tier = getTier(microservice);
            if (current_tier == tierCount) {
                continue;
            }

            List<Microservice> nextTier = tiers.get(current_tier + 1);
            int operationsInNextTier =
                nextTier.stream().mapToInt(microservice1 -> microservice1.getOperations().length).sum();
            List<Operation> nextTierOps = new ArrayList<>();
            nextTier.forEach(microservice1 -> nextTierOps.addAll(Arrays.asList(microservice1.getOperations())));

            for (Operation operation : microservice.getOperations()) {
                Set<DependencyDescription> dependencies = new HashSet<>();
                int depTargetCount = operationsInNextTier / 2 + nextNonNegative(operationsInNextTier) / 2;
                Predicate<Operation> alreadyTargeted =
                    op -> dependencies.stream().anyMatch(dependency -> dependency.getTargetOperation() == op);
                for (int i = 0; i < depTargetCount; i++) {
                    Operation targetOP = null;
                    while (targetOP == null || alreadyTargeted.test(targetOP)) {
                        targetOP = nextTierOps.get(nextNonNegative(nextTierOps.size()));
                    }
                    dependencies.add(new DependencyDescription(operation, targetOP));
                }
                //                operation.setDependencies(dependencies.toArray(new DependencyDescription[0]));
            }
        }

        //asserting model correctness
        for (Operation operation : all_operations) {
            for (DependencyDescription dependencyDescription : operation.getDependencyDescriptions()) {
                Assertions
                    .assertEquals(1,
                        getTier(dependencyDescription.getTargetMicroservice()) - getTier(operation.getOwnerMS()));
            }
        }

    }

    private int getTier(Microservice microservice) {
        for (Map.Entry<Integer, List<Microservice>> entry : tiers.entrySet()) {
            if (entry.getValue().contains(microservice)) {
                return entry.getKey();
            }
        }
        return -1;
    }

    public void setGenerator_count(int generator_count) {
        this.generator_count = generator_count;
    }

    public void setGenerator_interval(double generator_interval) {
        this.generator_interval = generator_interval;
    }

    private void createGenerators(int generator_count, double interval, Model model) {
        List<Operation> tier1Operations = new ArrayList<>();
        tiers.get(1).forEach(microservice -> tier1Operations.addAll(Arrays.asList(microservice.getOperations())));

        all_generators.addAll(IntStream.range(0, generator_count)
            .mapToObj(i -> {
                IntervalLoadGeneratorDescription loadGeneratorDescription = new IntervalLoadGeneratorDescription();
                try {
                    Field targetOperation = LoadGeneratorDescription.class.getDeclaredField("targetOperation");
                    targetOperation.setAccessible(true);
                    targetOperation.set(loadGeneratorDescription, getRandomFromList(tier1Operations));
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                loadGeneratorDescription.initializeArrivalRateModel();
                return new LoadGeneratorDescriptionExecutor(model,
                    loadGeneratorDescription);
            })
            .collect(Collectors.toList()));
    }

    public ArrayList<Microservice> getAllMicroservices() {
        return all_microservices;
    }

    public ArrayList<Operation> getAllOperations() {
        return all_operations;
    }

    private static Operation getRandomFromList(List<Operation> operations) {
        return operations.get(new Random().nextInt(operations.size()));
    }
}

