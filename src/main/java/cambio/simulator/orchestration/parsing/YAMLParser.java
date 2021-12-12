package cambio.simulator.orchestration.parsing;

import cambio.simulator.models.ArchitectureModel;
import cambio.simulator.orchestration.Util;
import cambio.simulator.orchestration.k8objects.K8Kind;
import cambio.simulator.orchestration.k8objects.K8Object;
import cambio.simulator.orchestration.parsing.converter.DtoToDeploymentMapper;
import cambio.simulator.orchestration.parsing.converter.DtoToObjectMapper;
import cambio.simulator.orchestration.parsing.converter.HPAManipulator;
import cambio.simulator.orchestration.parsing.converter.K8ObjectManipulator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class YAMLParser {

    ArchitectureModel architectureModel = null;

    Set<String> remainingFilePaths = new HashSet<>();

    private YAMLParser() {
    }

    private static final YAMLParser instance = new YAMLParser();

    public static YAMLParser getInstance() {
        return instance;
    }


    public String getKindAsString(String src) {
        Yaml yaml = new Yaml();
        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(src);
            Map<String, Object> obj = yaml.load(inputStream);
            final Object kind = obj.get("kind");
            if (kind != null) {
                return kind.toString();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public K8Object parseFile(String src) throws ParsingException, IOException {
        final String s = this.getKindAsString(src);
        final K8Kind k8Kind = Util.searchEnum(K8Kind.class, s);
        if(k8Kind==null){
            throw new ParsingException("Could not identify kind '"+s+"' of Kubernetes Object in YAML file at "+src);
        }
        Class targetClass = null;
        DtoToObjectMapper dtoToObjectMapper = null;
        switch (k8Kind) {
            case DEPLOYMENT:
                targetClass = K8DeploymentDto.class;
                dtoToObjectMapper = DtoToDeploymentMapper.getInstance();
                break;
            case HORIZONTALPODAUTOSCALER:
                //Will be dealt with after k8objects have been initialized.
                remainingFilePaths.add(src);
                return null;
        }

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
        K8ObjectDto k8object = (K8ObjectDto) mapper.readValue(new File(src),targetClass);

        if (k8object != null && dtoToObjectMapper != null) {
            dtoToObjectMapper.setK8ObjectDto(k8object);
            dtoToObjectMapper.setArchitectureModel(architectureModel);
            return (K8Object) dtoToObjectMapper.buildScheme();
        }
        return null;
    }

    public void applyManipulation(String src) throws ParsingException, IOException {
        final String s = this.getKindAsString(src);
        final K8Kind k8Kind = Util.searchEnum(K8Kind.class, s);
        Class targetClass = null;
        K8ObjectManipulator k8ObjectManipulator = null;
        switch (k8Kind) {
            case HORIZONTALPODAUTOSCALER:
                targetClass = K8HPADto.class;
                k8ObjectManipulator = HPAManipulator.getInstance();
                break;
            default:
                throw new ParsingException("Could not identify kind '"+s+"' of Kubernetes Object in YAML file at "+src);
        }

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
        K8ObjectDto k8object = (K8ObjectDto) mapper.readValue(new File(src),targetClass);

        if (k8object != null && k8ObjectManipulator != null) {
            k8ObjectManipulator.setK8ObjectDto(k8object);
            k8ObjectManipulator.manipulate();
        }
    }

    public ArchitectureModel getArchitectureModel() {
        return architectureModel;
    }

    public void setArchitectureModel(ArchitectureModel architectureModel) {
        this.architectureModel = architectureModel;
    }

    public Set<String> getRemainingFilePaths() {
        return remainingFilePaths;
    }

    public void setRemainingFilePaths(Set<String> remainingFilePaths) {
        this.remainingFilePaths = remainingFilePaths;
    }
}

