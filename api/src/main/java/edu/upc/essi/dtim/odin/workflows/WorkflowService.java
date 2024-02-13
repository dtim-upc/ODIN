package edu.upc.essi.dtim.odin.workflows;

import edu.upc.essi.dtim.NextiaCore.graph.CoreGraphFactory;
import edu.upc.essi.dtim.NextiaCore.graph.Graph;
import edu.upc.essi.dtim.NextiaCore.graph.WorkflowGraph;
import edu.upc.essi.dtim.NextiaCore.graph.jena.WorkflowGraphJenaImpl;
import edu.upc.essi.dtim.NextiaCore.queries.Intent;
import edu.upc.essi.dtim.NextiaCore.queries.Workflow;
import edu.upc.essi.dtim.odin.nextiaStore.graphStore.GraphStoreFactory;
import edu.upc.essi.dtim.odin.nextiaStore.graphStore.GraphStoreInterface;
import edu.upc.essi.dtim.odin.nextiaStore.relationalStore.ORMStoreFactory;
import edu.upc.essi.dtim.odin.nextiaStore.relationalStore.ORMStoreInterface;
import edu.upc.essi.dtim.odin.config.AppConfig;
import edu.upc.essi.dtim.odin.exception.ElementNotFoundException;
import edu.upc.essi.dtim.odin.intents.IntentService;
import edu.upc.essi.dtim.odin.workflows.pojos.WorkflowResponse;
import org.apache.jena.rdf.model.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class WorkflowService {
    @Autowired
    private IntentService intentService;
    @Autowired
    private AppConfig appConfig;
    private final ORMStoreInterface ormDataResource = ORMStoreFactory.getInstance();

    // ---------------- CRUD/ORM operations

    /**
     * Saves a Workflow object into the ORM store
     *
     * @param workflow The Workflow object to save.
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
        // Retrieve the content of the graph associated with the workflow
        GraphStoreInterface graphStore = GraphStoreFactory.getInstance(appConfig);

        Graph workflowGraph = graphStore.getGraph(workflow.getWorkflowGraph().getGraphName());
        System.out.println(workflowGraph);

        // Set the local graph of the workflow to the retrieved graph
        workflow.setWorkflowGraph((WorkflowGraphJenaImpl) workflowGraph);

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

    // ---------------- Schema operations

    /**
     * Downloads the schema of a workflow, in .ttl format.
     *
     * @param workflowID Identification of the workflow whose schema will be downloaded
     * @return A ResponseEntity with the headers and the schema
     */
    public ResponseEntity<InputStreamResource> downloadWorkflowSchema(String workflowID) {
        Workflow workflow = getWorkflow(workflowID);

        Model model = workflow.getWorkflowGraph().getGraph(); // Get the RDF model (graph) from the workflow
        StringWriter writer = new StringWriter();

        model.write(writer, "TTL"); // Write the model (graph) to a StringWriter in Turtle format

        HttpHeaders headers = new HttpHeaders();
        // Set the HTTP headers to specify the content disposition as an attachment with the dataset name and .ttl extension
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + workflow.getWorkflowName() + ".ttl");

        // Create an InputStreamResource from the StringWriter
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(writer.toString().getBytes()));
        // Return a ResponseEntity with the Turtle schema file, content type, and headers
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("text/turtle"))
                .body(resource);
    }
}
