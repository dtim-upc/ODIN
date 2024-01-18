package edu.upc.essi.dtim.NextiaCore.graph;

import java.util.List;
import java.util.Map;

public interface LocalGraph extends Graph {
    String getGraphicalSchema();
    void setGraphicalSchema(String graphicalSchema);
    public String getLocalGraphAttribute();
    void setLocalGraphAttribute(String localGraphAttribute);

}
