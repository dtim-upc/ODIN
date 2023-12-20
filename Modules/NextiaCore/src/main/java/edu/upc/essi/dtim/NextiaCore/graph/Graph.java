package edu.upc.essi.dtim.NextiaCore.graph;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResIterator;

import java.util.List;
import java.util.Map;

public interface Graph {

    String getGraphName();
    void setGraphName(String graphName);

    String getGraphicalSchema();
    void setGraphicalSchema(String graphicalSchema);

    Model getGraph();
    void setGraph(Model graph);

    void addTriple(String subject, String predicate, String object);
    void addTripleLiteral(String subject, String predicate, String object);
    void deleteTriple(String subject, String predicate, String object);

    List<Map<String, Object>> query(String sparql);

    ResIterator retrieveSubjects();
    List<String> retrievePredicates();

    void write(String file);

    String getDomainOfProperty(String propertyIRI);
    String getRDFSLabel(String resourceIRI);
}
