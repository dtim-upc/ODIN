package edu.upc.essi.dtim.odin.intents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class IntentController {
    private static final Logger logger = LoggerFactory.getLogger(IntentController.class);
    @Autowired
    private IntentService intentService;


    // TODO: Description
    @PostMapping("/project/{projectID}/intent")
    public ResponseEntity<String> postIntent(@PathVariable("projectID") String projectID,
                                             @RequestParam("intentName") String intentName,
                                             @RequestParam("problem") String problem,
                                             @RequestParam("dataProductID") String dataProductID) {
        logger.info("Creating intent");
        String intentID = intentService.postIntent(intentName, problem, projectID, dataProductID);
        return new ResponseEntity<>(intentID, HttpStatus.OK);
    }

}
