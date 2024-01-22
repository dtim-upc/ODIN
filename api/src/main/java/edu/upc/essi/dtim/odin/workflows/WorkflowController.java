package edu.upc.essi.dtim.odin.workflows;

import edu.upc.essi.dtim.NextiaCore.queries.Workflow;
import edu.upc.essi.dtim.odin.query.QueryController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WorkflowController {

    private static final Logger logger = LoggerFactory.getLogger(QueryController.class);
    @Autowired
    private WorkflowService workflowService;

    // TODO: Description
    @PostMapping("/project/{projectID}/intent/{intentID}/workflow")
    public ResponseEntity<Boolean> postWorkflow(@PathVariable("projectID") String projectID,
                                                 @PathVariable("intentID") String intentID,
                                                 @RequestBody Workflow workflow) {
        logger.info("Storing workflow");
        workflowService.postWorkflow(intentID, workflow);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
