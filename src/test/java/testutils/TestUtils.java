package testutils;

import de.rss.fachstudie.MiSim.entities.Dependency;
import de.rss.fachstudie.MiSim.entities.Operation;
import de.rss.fachstudie.MiSim.entities.microservice.Microservice;
import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.Model;
import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.Random;

/**
 * @author Lion Wagner
 */
public class TestUtils {
    private static final Random rng = new Random();

    private static TestModel currentModel;
    private static TestExperiment currentExperiment;
    private static int current_service_count;

    private static ArrayList<Microservice> all_microservices = new ArrayList<>();
    private static ArrayList<Operation> all_operations = new ArrayList<>();


    public synchronized static Pair<Model, Experiment> getExampleExperiment(int service_count) {
        currentModel = new TestModel(null, "TestModel", true, true, TestUtils::initSchedule, TestUtils::init);
        currentExperiment = new TestExperiment();
        current_service_count = service_count;

        currentModel.connectToExperiment(currentExperiment);
        return new Pair<>(currentModel, currentExperiment);
    }

    private static void initSchedule() {
        for (Microservice microservice : all_microservices) {
            microservice.start();
        }
    }


    public static void init() {
        for (int i = 0; i < current_service_count; i++) {
            Microservice current_ms = new Microservice(currentModel, "MS" + i, true);
            current_ms.setInstancesCount(1);
            current_ms.setCapacity(nextNonNegative());
            ArrayList<Operation> current_ops = new ArrayList<>();
            for (int j = 0; j < new Random().nextInt(5) + 1; j++) {
                Operation currentOP = new Operation(currentModel, String.format("MS%d_OP%d", i, j), true);
                currentOP.setDemand(nextNonNegative());
                current_ops.add(currentOP);
            }
            current_ms.setOperations(current_ops.toArray(new Operation[0]));
            all_operations.addAll(current_ops);
            all_microservices.add(current_ms);
        }

        for (Microservice microservice : all_microservices) {
            for (Operation operation : microservice.getOperations()) {
                ArrayList<Dependency> dependencies = new ArrayList<>();
                int depTargetCount = nextNonNegative(10);
                for (int i = 0; i < depTargetCount; i++) {
                    Operation nextDep;
                    do {
                        nextDep = all_operations.get(nextNonNegative(all_operations.size()));
                    } while (microservice.getOperation(nextDep.getName()) == null);
                    dependencies.add(new Dependency(nextDep, null, rng.nextDouble()));
                }
                operation.setDependencies(dependencies.toArray(new Dependency[0]));
            }
        }
    }

    private static int nextNonNegative() {
        return nextNonNegative(Integer.MAX_VALUE);
    }

    private static int nextNonNegative(int bound) {
        return rng.nextInt(bound) & Integer.MAX_VALUE;
    }
}
