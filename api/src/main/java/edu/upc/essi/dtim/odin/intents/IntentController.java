package edu.upc.essi.dtim.odin.intents;

import edu.upc.essi.dtim.NextiaCore.queries.Intent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class IntentController {
    private static final Logger logger = LoggerFactory.getLogger(IntentController.class);
    @Autowired
    private IntentService intentService;


    // ---------------- CRUD Operations

    /**
     * Retrieves all intents from a specific project.
     *
     * @param projectID The ID of the project to retrieve intents from.
     * @return A ResponseEntity object containing the list of intents or an error message.
     */
    @GetMapping("/project/{projectID}/intents")
    public ResponseEntity<Object> getIntentsOfProject(@PathVariable("projectID") String projectID) {
        logger.info("Getting all intents from project " + projectID);
        List<Intent> intents = intentService.getIntentsOfProject(projectID);
        return new ResponseEntity<>(intents, HttpStatus.OK);
    }

    /**
     * Adds a new intent into the system
     *
     * @param projectID        ID of the project to which the new dataset will be added.
     * @param intentName       Name of the intent (given by the user)
     * @param problem          Name of the problem to be solved by the intent (given by the user)
     * @param dataProductID    ID of the product associated to the intent
     * @return If the task was successful return a ResponseEntity with an OK HTTP code and a String with the ID of
     *         the intent that was created, as we need it to store the workflows.
     */
    @PostMapping("/project/{projectID}/intent")
    public ResponseEntity<String> postIntent(@PathVariable("projectID") String projectID,
                                             @RequestParam("intentName") String intentName,
                                             @RequestParam("problem") String problem,
                                             @RequestParam("dataProductID") String dataProductID) {
        logger.info("Creating intent");
        String intentID = intentService.postIntent(intentName, problem, projectID, dataProductID);
        return new ResponseEntity<>(intentID, HttpStatus.OK);
    }

    /**
     * Edits an intent (name of the intent) in a specific project.
     *
     * @param intentID          The ID of the intent to edit.
     * @param intentName        The new name for the intent.
     * @return If the task was successful return a ResponseEntity with an OK HTTP code.
     */
    @PutMapping("/project/{projectID}/intent/{intentID}")
    public ResponseEntity<Boolean> putIntent(@PathVariable("intentID") String intentID,
                                             @RequestParam("intentName") String intentName) {
        logger.info("Putting intent " + intentID);
        intentService.putIntent(intentID, intentName);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Deletes an intent from a specific project.
     *
     * @param projectID The ID of the project from which to delete the intent.
     * @param intentID The ID of the intent to delete.
     * @return If the task was successful return a ResponseEntity with an OK HTTP code.
     */
    @DeleteMapping("/project/{projectID}/intent/{intentID}")
    public ResponseEntity<Boolean> deleteIntent(@PathVariable("projectID") String projectID,
                                                 @PathVariable("intentID") String intentID) {
        logger.info("Deleting intent " + intentID + " from project: " +  projectID);
        intentService.deleteIntent(projectID, intentID);
        return new ResponseEntity<>(HttpStatus.OK);
    }


}
