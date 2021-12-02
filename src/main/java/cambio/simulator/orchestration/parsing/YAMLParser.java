package cambio.simulator.orchestration.parsing;

import cambio.simulator.models.ArchitectureModel;
import cambio.simulator.orchestration.k8objects.K8Object;
import cambio.simulator.orchestration.parsing.converter.DtoToDeploymentMapper;
import cambio.simulator.orchestration.parsing.converter.DtoToObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;

public class YAMLParser {

    ArchitectureModel architectureModel = null;

    private YAMLParser() {}

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

    public K8Object parseFile(String src) throws ParsingException {
        final String s = this.getKindAsString(src);
        final K8Kind k8Kind = getK8Kind(s.toUpperCase(Locale.ROOT));
        K8ObjectDto k8ObjectDtoClass = null;
        DtoToObjectMapper dtoToObjectMapper = null;

        switch (k8Kind){
            case DEPLOYMENT:
                k8ObjectDtoClass = new K8DeploymentDto();
                dtoToObjectMapper = DtoToDeploymentMapper.getInstance();
                break;
            default:
                System.out.println("Could not identify kind of Kubernetes Object in YAML file");
        }

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
        K8ObjectDto k8object = null;
        try {
            k8object = mapper.readValue(new File(src), k8ObjectDtoClass.getClass());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(k8object!= null && dtoToObjectMapper!=null){
            dtoToObjectMapper.setK8ObjectDto(k8object);
            dtoToObjectMapper.setArchitectureModel(architectureModel);
            return dtoToObjectMapper.buildScheme();
        }
        return null;
    }

    public static K8Kind getK8Kind(String s){
        return K8Kind.valueOf(s);
    }

    public static void main(String[] args) throws ParsingException {

        final YAMLParser yamlParser = YAMLParser.getInstance();
//        yamlParser.setArchitectureModel("");

        String src = "src/main/java/cambio/simulator/orchestration/Test/k8_file.yaml";
        final K8Object k8Object = yamlParser.parseFile(src);
        System.out.println(k8Object);
    }

    public ArchitectureModel getArchitectureModel() {
        return architectureModel;
    }

    public void setArchitectureModel(ArchitectureModel architectureModel) {
        this.architectureModel = architectureModel;
    }
}

