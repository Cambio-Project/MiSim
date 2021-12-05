package cambio.simulator.orchestration.parsing;

import java.util.Locale;

public enum K8Kind {
    DEPLOYMENT,
    HORIZONTALPODAUTOSCALER;

    public static K8Kind getK8Kind(String s) {
        return K8Kind.valueOf(s.toUpperCase(Locale.ROOT));
    }
}
