package edu.upc.essi.dtim.odin.workflows;

import edu.upc.essi.dtim.NextiaCore.graph.CoreGraphFactory;
import edu.upc.essi.dtim.NextiaCore.graph.WorkflowGraph;
import edu.upc.essi.dtim.NextiaCore.queries.Intent;
import edu.upc.essi.dtim.NextiaCore.queries.Workflow;
import edu.upc.essi.dtim.odin.NextiaStore.GraphStore.GraphStoreFactory;
import edu.upc.essi.dtim.odin.NextiaStore.GraphStore.GraphStoreInterface;
import edu.upc.essi.dtim.odin.NextiaStore.RelationalStore.ORMStoreFactory;
import edu.upc.essi.dtim.odin.NextiaStore.RelationalStore.ORMStoreInterface;
import edu.upc.essi.dtim.odin.config.AppConfig;
import edu.upc.essi.dtim.odin.exception.ElementNotFoundException;
import edu.upc.essi.dtim.odin.intents.IntentService;
import edu.upc.essi.dtim.odin.workflows.pojo.WorkflowResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class WorkflowService {
    private final ORMStoreInterface ormDataResource = ORMStoreFactory.getInstance();
    @Autowired
    private IntentService intentService;
    @Autowired
    private AppConfig appConfig;

    // ---------------- CRUD/ORM operations

    /**
     * Saves a Workflow object into the ORM store
     *
     * @param workflow The DataProduct object to save.
     * @return The saved Workflow object.
     */
    public Workflow saveWorkflow(Workflow workflow) {
        return ormDataResource.save(workflow);
    }

    /**
     * Retrieves a workflow by its unique identifier
     *
     * @param workflowID The unique identifier of the workflow to be retrieved
     * @return The workflow object.
     */
    public Workflow getWorkflow(String workflowID) {
        Workflow workflow = ormDataResource.findById(Workflow.class, workflowID);
        if (workflow == null) {
            throw new ElementNotFoundException("Workflow not found with ID: " + workflowID);
        }
        return workflow;
    }

    /**
     * Adds a new workflow into the system.
     *
     * @param intentID     ID of the intent to which the new workflow will be added to.
     * @param workflowResponse  The workflow to store (the format needs to be adapted)
     */
    public void postWorkflow(String intentID, WorkflowResponse workflowResponse) {
        Intent intent = intentService.getIntent(intentID);

        Workflow workflow = new Workflow();
        workflow.setWorkflowName(workflowResponse.getWorkflowName());

        WorkflowGraph graph = CoreGraphFactory.createWorkflowGraph();
        graph.setWorkflowRepresentation(workflowResponse.getVisualRepresentation());
        workflow.setWorkflowGraph(graph);

        workflow = saveWorkflow(workflow); // Give an id to the workflow graph

        GraphStoreInterface graphStoreInterface;
        try {
            graphStoreInterface = GraphStoreFactory.getInstance(appConfig);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        graphStoreInterface.saveGraphFromStringRepresentation(workflow.getWorkflowGraph(), workflowResponse.getStringGraph());


        intent.addWorkflow(workflow);
        intentService.saveIntent(intent);
    }

    /**
     * Deletes a workflow from a specific data intent.
     *
     * @param intentID      The ID of the intent for which to remove the workflow from.
     * @param workflowID    The ID of the workflow to be deleted.
     */
    public void deleteWorkflow(String intentID, String workflowID) {
        Intent intent = intentService.getIntent(intentID);
        List<Workflow> workflowsOfIntent = intent.getWorkflows();
        boolean workflowFound = false;
        // Iterate through the workflows
        for (Workflow workflowInIntent : workflowsOfIntent) {
            if (workflowInIntent.getWorkflowID().equals(workflowID)) {
                workflowFound = true;
                workflowsOfIntent.remove(workflowInIntent);
                break;
            }
        }
        intent.setWorkflows(workflowsOfIntent); // Save and set the updated list of data repositories
        // Throw an exception if the repository was not found
        if (!workflowFound) {
            throw new NoSuchElementException("Workflow not found with id: " + workflowID);
        }
        intentService.saveIntent(intent); // Save the updated project without the repository
    }

    /**
     * Edits a workflow (name) in a specific project.
     *
     * @param workflowID          The ID of the workflow to edit.
     * @param workflowName        The new name for the workflow.
     */
    public void putWorkflow(String workflowID, String workflowName) {
        Workflow originalWorkflow = getWorkflow(workflowID);

        originalWorkflow.setWorkflowName(workflowName);

        saveWorkflow(originalWorkflow);
    }
}
