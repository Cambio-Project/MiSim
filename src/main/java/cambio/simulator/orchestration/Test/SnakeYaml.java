package cambio.simulator.orchestration.Test;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

public class SnakeYaml {

    public void readYAML(){
        Yaml yaml = new Yaml();
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File("src/main/java/cambio/simulator/orchestration/Test/k8_file.yaml"));
            Map<String, Object> obj = yaml.load(inputStream);
            System.out.println(obj);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }


    public static void main(String[] args) {
        final SnakeYaml snakeYaml = new SnakeYaml();
        snakeYaml.readYAML();

    }
}
