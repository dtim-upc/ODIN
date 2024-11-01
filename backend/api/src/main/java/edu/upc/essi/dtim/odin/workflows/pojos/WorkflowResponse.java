package edu.upc.essi.dtim.odin.workflows.pojos;

import java.util.List;
import java.util.Map;

public class WorkflowResponse {
    String workflowName;
    Map<String, List<String>> visualRepresentation;
    String stringGraph;

    public String getWorkflowName() { return workflowName; }
    public void setWorkflowName(String workflowName) { this.workflowName = workflowName; }

    public Map<String, List<String>> getVisualRepresentation() { return visualRepresentation; }
    public void setVisualRepresentation(Map<String, List<String>> visualRepresentation) { this.visualRepresentation = visualRepresentation; }

    public String getStringGraph() { return stringGraph; }
    public void setStringGraph(String stringGraph) { this.stringGraph = stringGraph; }
}
