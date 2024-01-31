package edu.upc.essi.dtim.NextiaCore.queries;

import edu.upc.essi.dtim.NextiaCore.graph.WorkflowGraph;

import java.util.List;
import java.util.Map;

public class Workflow {
    private String workflowID;
    private String workflowName;
    private WorkflowGraph workflowGraph;

    public String getWorkflowID() { return workflowID; }
    public void setWorkflowID(String workflowID) { this.workflowID = workflowID; }

    public String getWorkflowName() { return workflowName; }
    public void setWorkflowName(String workflowName) { this.workflowName = workflowName; }

    public WorkflowGraph getWorkflowGraph() { return workflowGraph; }
    public void setWorkflowGraph(WorkflowGraph workflowGraph) { this.workflowGraph = workflowGraph; }
}
