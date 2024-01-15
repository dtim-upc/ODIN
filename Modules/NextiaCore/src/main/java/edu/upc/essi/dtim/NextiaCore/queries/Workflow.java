package edu.upc.essi.dtim.NextiaCore.queries;

import java.util.List;
import java.util.Map;

public class Workflow {
    private String workflowID;
    private String workflowName;
    private Map<String, List<String>> visualRepresentation;

    public String getWorkflowID() { return workflowID; }
    public void setWorkflowID(String workflowID) { this.workflowID = workflowID; }

    public String getWorkflowName() { return workflowName; }
    public void setWorkflowName(String workflowName) { this.workflowName = workflowName; }

    public Map<String, List<String>> getVisualRepresentation() { return visualRepresentation;}
    public void setVisualRepresentation(Map<String, List<String>> visualRepresentation) { this.visualRepresentation = visualRepresentation; }
}
