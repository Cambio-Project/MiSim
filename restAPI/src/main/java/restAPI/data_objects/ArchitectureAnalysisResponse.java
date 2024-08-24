package restAPI.data_objects;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface ArchitectureAnalysisResponse {
    String toJSON() throws JsonProcessingException;
}
