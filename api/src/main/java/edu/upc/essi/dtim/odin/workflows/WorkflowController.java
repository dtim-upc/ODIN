package edu.upc.essi.dtim.odin.workflows;

import edu.upc.essi.dtim.NextiaCore.queries.Workflow;
import edu.upc.essi.dtim.odin.query.QueryController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class WorkflowController {
    private static final Logger logger = LoggerFactory.getLogger(QueryController.class);
    @Autowired
    private WorkflowService workflowService;

    /**
     * Adds a new workflow into the system.
     *
     * @param intentID     ID of the intent to which the new workflow will be added to.
     * @param workflow     The workflow to store
     */
    @PostMapping("/project/{projectID}/intent/{intentID}/workflow")
    public ResponseEntity<Boolean> postWorkflow(@PathVariable("intentID") String intentID,
                                                @RequestBody Workflow workflow) {
        logger.info("Storing workflow");
        workflowService.postWorkflow(intentID, workflow);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Edits a workflow (name) in a specific project.
     *
     * @param workflowID          The ID of the workflow to edit.
     * @param workflowName        The new name for the workflow.
     * @return If the task was successful return a ResponseEntity with an OK HTTP code.
     */
    @PutMapping("/project/{projectID}/intent/{intentID}/workflow/{workflowID}")
    public ResponseEntity<Boolean> putWorkflow(@PathVariable("workflowID") String workflowID,
                                               @RequestParam("workflowName") String workflowName) {
        logger.info("Putting workflow " + workflowID);
        workflowService.putWorkflow(workflowID, workflowName);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Deletes a workflow from a specific data intent.
     *
     * @param intentID      The ID of the intent for which to remove the workflow from.
     * @param workflowID    The ID of the workflow to be deleted.
     * @return If the task was successful return a ResponseEntity with an OK HTTP code.
     */
    @DeleteMapping("/project/{projectID}/intent/{intentID}/workflow/{workflowID}")
    public ResponseEntity<Boolean> deleteIntent(@PathVariable("intentID") String intentID,
                                                @PathVariable("workflowID") String workflowID) {
        logger.info("Deleting workflow " + workflowID + " from intent: " +  intentID);
        workflowService.deleteWorkflow(intentID, workflowID);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
