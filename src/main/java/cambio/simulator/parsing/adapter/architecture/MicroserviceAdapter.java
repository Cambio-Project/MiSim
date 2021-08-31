package cambio.simulator.parsing.adapter.architecture;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.LinkedList;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.entities.microservice.Operation;
import cambio.simulator.entities.networking.DependencyDescription;
import cambio.simulator.entities.patterns.InstanceOwnedPatternConfiguration;
import cambio.simulator.entities.patterns.LoadBalancer;
import cambio.simulator.entities.patterns.ServiceOwnedPattern;
import cambio.simulator.models.MiSimModel;
import cambio.simulator.parsing.GsonHelper;
import cambio.simulator.parsing.adapter.NormalDistributionAdapter;
import com.google.gson.Gson;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import desmoj.core.dist.ContDistNormal;

/**
 * @author Lion Wagner
 */
class MicroserviceAdapter extends TypeAdapter<Microservice> {
    private final MiSimModel baseModel;
    private final LinkedList<DependencyDescription> dependencies;

    public MicroserviceAdapter(MiSimModel baseModel,
                               LinkedList<DependencyDescription> dependencies) {
        this.baseModel = baseModel;
        this.dependencies = dependencies;
    }

    @Override
    public void write(JsonWriter out, Microservice value) throws IOException {

    }

    @Override
    public Microservice read(JsonReader in) throws IOException {
        JsonObject root = JsonParser.parseReader(in).getAsJsonObject();
        String microserviceName = root.get("name").getAsString();

        Gson gson = new GsonHelper()
            .getGsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .registerTypeAdapter(Microservice.class, new MicroserviceInstanceCreator(baseModel, microserviceName))
            .registerTypeAdapter(ContDistNormal.class, new NormalDistributionAdapter(baseModel))
            .registerTypeAdapter(LoadBalancer.class, new LoadBalancerAdapter(baseModel))
            .registerTypeAdapter(Operation.class, new OperationAdapter(baseModel, microserviceName, dependencies))
            .registerTypeAdapter(InstanceOwnedPatternConfiguration.class, new InstanceOwnedPatternConfigAdapter())
            .registerTypeAdapter(ServiceOwnedPattern.class, new ServiceOwnedPatternAdapter(baseModel, microserviceName))
            .create();

        Microservice microservice = gson.fromJson(root, Microservice.class);

        //inject owning microservice into ownerMs field of operations
        try {
            Field ownerInjectionFieldOperation = Operation.class.getDeclaredField("ownerMS");
            ownerInjectionFieldOperation.setAccessible(true);
            for (Operation operation : microservice.getOperations()) {
                ownerInjectionFieldOperation.set(operation, microservice);
            }

            Field ownerInjectionFieldServicePattern = ServiceOwnedPattern.class.getDeclaredField("owner");
            Field serviceOwnedPatternsField = Microservice.class.getDeclaredField("serviceOwnedPatterns");
            ownerInjectionFieldServicePattern.setAccessible(true);
            serviceOwnedPatternsField.setAccessible(true);
            ServiceOwnedPattern[] serviceOwnedPatterns =
                (ServiceOwnedPattern[]) serviceOwnedPatternsField.get(microservice);
            for (ServiceOwnedPattern pattern : serviceOwnedPatterns) {
                ownerInjectionFieldServicePattern.set(pattern, microservice);
            }

        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return microservice;
    }

    private static final class MicroserviceInstanceCreator
        implements InstanceCreator<Microservice> {
        private final MiSimModel baseModel;
        private final String microserviceName;

        public MicroserviceInstanceCreator(MiSimModel baseModel, String name) {

            this.baseModel = baseModel;
            microserviceName = name;
        }

        @Override
        public Microservice createInstance(Type type) {
            return new Microservice(baseModel, microserviceName, true);
        }
    }
}
