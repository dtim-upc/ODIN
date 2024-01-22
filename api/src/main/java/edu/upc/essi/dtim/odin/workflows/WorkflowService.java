package edu.upc.essi.dtim.odin.workflows;

import edu.upc.essi.dtim.NextiaCore.queries.Intent;
import edu.upc.essi.dtim.NextiaCore.queries.Workflow;
import edu.upc.essi.dtim.odin.intents.IntentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Service
public class WorkflowService {

    @Autowired
    private IntentService intentService;

    // TODO: Description
    public void postWorkflow(String intentID, Workflow workflow) {
        Intent intent = intentService.getIntent(intentID);
        intent.addWorkflow(workflow);
        intentService.saveIntent(intent);
    }
}
