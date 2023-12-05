package edu.upc.essi.dtim.NextiaCore.graph;

import java.util.List;
import java.util.Map;

public interface LocalGraph {
    String getGraphName();

    void addTriple(String subject, String predicate, String object);
    void addTripleLiteral(String subject, String predicate, String object);

    void deleteTriple(String subject, String predicate, String object);

    List<Map<String, Object>> query(String sparql);
    String getGraphicalSchema();
    void setGraphicalSchema(String graphicalSchema);
    public String getLocalGraphAttribute();
    void setLocalGraphAttribute(String localGraphAttribute);

}
