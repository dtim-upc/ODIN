package edu.upc.essi.dtim.NextiaCore.graph;

import java.util.List;
import java.util.Map;

public interface WorkflowGraph extends Graph{
    Map<String, List<String>> getWorkflowRepresentation();
    void setWorkflowRepresentation(Map<String, List<String>> intentRepresentation);
}
