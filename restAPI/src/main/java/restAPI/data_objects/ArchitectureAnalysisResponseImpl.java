package restAPI.data_objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Set;

public final class ArchitectureAnalysisResponseImpl implements ArchitectureAnalysisResponse {

    private final Set<String> serviceNames;
    private final Set<String> endpointNames;

    public ArchitectureAnalysisResponseImpl(Set<String> serviceNames, Set<String> endpointNames) {
        this.serviceNames = serviceNames;
        this.endpointNames = endpointNames;
    }

    @Override
    public String toJSON() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }

    public Set<String> getServiceNames() {
        return serviceNames;
    }

    public Set<String> getEndpointNames() {
        return endpointNames;
    }
}
