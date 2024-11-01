package edu.upc.essi.dtim.NextiaCore.graph.jena;

import edu.upc.essi.dtim.NextiaCore.graph.WorkflowGraph;

import java.util.List;
import java.util.Map;

public class WorkflowGraphJenaImpl extends GraphJenaImpl implements WorkflowGraph {
    Map<String, List<String>> workflowRepresentation;
    public WorkflowGraphJenaImpl(){ super();}

    @Override
    public Map<String, List<String>> getWorkflowRepresentation() { return workflowRepresentation; }
    @Override
    public void setWorkflowRepresentation(Map<String, List<String>> workflowRepresentation) { this.workflowRepresentation = workflowRepresentation; }
}
