package restAPI.simulation_running;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SimulationRunningController {

    private final SimulationRunningService simulationRunningService;

    Logger logger = LoggerFactory.getLogger(SimulationRunningController.class);

    @Autowired
    public SimulationRunningController(SimulationRunningService simulationRunningService) {
        this.simulationRunningService = simulationRunningService;
    }

    @PostMapping("/simulate")
    public ResponseEntity<String> handleSimulationRequest(@RequestBody String request){

        return new ResponseEntity<>("Im a response", HttpStatus.OK);
    }

}
