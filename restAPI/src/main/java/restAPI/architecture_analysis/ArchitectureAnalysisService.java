package restAPI.architecture_analysis;

import cambio.simulator.ExperimentCreator;
import cambio.simulator.ExperimentStartupConfig;
import cambio.simulator.entities.NamedEntity;
import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.entities.microservice.Operation;
import cambio.simulator.models.ArchitectureModel;
import cambio.simulator.models.MiSimModel;
import com.google.common.collect.Multimap;
import desmoj.core.simulator.Experiment;
import org.springframework.stereotype.Service;
import restAPI.data_objects.ArchitectureAnalysisResponse;
import restAPI.data_objects.ArchitectureAnalysisResponseImpl;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Service
public class ArchitectureAnalysisService {
    private final File experiment;

    public ArchitectureAnalysisService() throws IOException {
        this.experiment = (new ArchitectureAnalysisExperimentFileGenerator()).generateExperimentFile();
    }

    public ArchitectureAnalysisResponse runAnalysis(Multimap<String, String> inputFiles) throws Exception {
        String archDescPath = getArchtectureDescPath(inputFiles);
        ArchitectureModelAdapter architectureModel = new ArchitectureModelAdapter(getArchitectureModel(archDescPath));
        return new ArchitectureAnalysisResponseImpl(architectureModel.getServiceNames(), architectureModel.getEndpointNames());
    }

    private String getArchtectureDescPath(Multimap<String, String> inputFiles) throws Exception {
        Collection<String> archDescPathCollection = inputFiles.get(ArchitectureAnalysisController.architectureFieldName);
        if (archDescPathCollection.isEmpty()) {
            throw new Exception("You have to provide an architecture description file.");
        }
        return archDescPathCollection.iterator().next();
    }

    private ArchitectureModel getArchitectureModel(final String archDescPath) {
        ExperimentStartupConfig config = new ExperimentStartupConfig(archDescPath, experiment.getAbsolutePath(),
                null, null, null, false,
                false, false, null);
        Experiment experiment = (new ExperimentCreator()).createSimulationExperiment(config);
        MiSimModel model = (MiSimModel) experiment.getModel();
        return model.getArchitectureModel();
    }

    private static class ArchitectureModelAdapter {
        private final ArchitectureModel architectureModel;

        public ArchitectureModelAdapter(ArchitectureModel architectureModel) {
            this.architectureModel = architectureModel;
        }

        public Set<String> getServiceNames() {
            return architectureModel.getMicroservices().stream().map(NamedEntity::getPlainName).collect(toSet());
        }

        public Set<String> getEndpointNames() {
            Set<String> names = new HashSet<>();
            for (Microservice service : architectureModel.getMicroservices()) {
                for (Operation operation : service.getOperations()) {
                    names.add(operation.getFullyQualifiedPlainName());
                }
            }
            return names;
        }
    }

}
