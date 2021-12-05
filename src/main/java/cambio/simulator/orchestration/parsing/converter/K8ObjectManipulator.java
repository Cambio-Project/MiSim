package cambio.simulator.orchestration.parsing.converter;

import cambio.simulator.orchestration.k8objects.K8Object;
import cambio.simulator.orchestration.parsing.K8ObjectDto;
import cambio.simulator.orchestration.parsing.ParsingException;

public interface K8ObjectManipulator {

    void manipulate() throws ParsingException;

    void setK8ObjectDto(K8ObjectDto k8ObjectDto);
}
