package cambio.simulator.testutils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.Gson;

/**
 * @author Lion Wagner
 */
public final class ArchitectureGenerator {

    public static String createArchitecture(String networkDelay, final int layerCount, final int maxLayerWidth) {
        Map<String, String> archMap = new HashMap<>();
        archMap.put("networkDelay", networkDelay);

        Map<Integer, Integer> layerMap = new HashMap<>();

        //create a parabolic function that will generate a diamond shaped architecture with max height of
        // maxLayerWidth and a width of layerCount
        Function<Integer, Integer> distributionFunction = (x) -> (int) Math.max(1,
            -4.0 * maxLayerWidth / (layerCount * layerCount) * x * x + 4.0 * maxLayerWidth / layerCount * x);

        for (int i = 1; i < layerCount; i++) {
            int currentLayerWidth = distributionFunction.apply(i);
            layerMap.put(i, currentLayerWidth);
        }


        return new Gson().toJson(archMap);
    }

}
