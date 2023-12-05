package edu.upc.essi.dtim.NextiaCore.graph.jena;

import edu.upc.essi.dtim.NextiaCore.graph.Graph;
import edu.upc.essi.dtim.NextiaCore.graph.LocalGraph;
import org.apache.jena.rdf.model.Model;

import java.util.List;
import java.util.Map;

public class LocalGraphJenaImpl extends GraphJenaImpl implements LocalGraph, Graph {

    public LocalGraphJenaImpl(String id, String name, Model triples) {
        super();
    }

    public LocalGraphJenaImpl() {
        super();
    }

    /**
     * @return String
     */
    public String getGraphName() {
        return super.getGraphName();
    }

    /**
     * @param subject subject
     * @param predicate predicate
     * @param object object
     */
    @Override
    public void addTriple(String subject, String predicate, String object) {
        super.addTriple(subject, predicate,object);
    }

    /**
     * @param subject subject
     * @param predicate predicate
     * @param object object
     */
    @Override
    public void addTripleLiteral(String subject, String predicate, String object) {
        super.addTripleLiteral(subject, predicate,object);
    }

    /**
     * @param subject subject
     * @param predicate predicate
     * @param object object
     */
    @Override
    public void deleteTriple(String subject, String predicate, String object) {
        super.deleteTriple(subject, predicate,object);
    }

    /**
     * @param sparql sparql
     * @return List
     */
    @Override
    public List<Map<String, Object>> query(String sparql) {
        return super.query(sparql);
    }

    /**
     * @return String
     */
    @Override
    public String getGraphicalSchema() {
        return super.getGraphicalSchema();
    }

    /**
     * @param graphicalSchema graphicalSchema
     */
    @Override
    public void setGraphicalSchema(String graphicalSchema) {
        super.setGraphicalSchema(graphicalSchema);
    }

    @Override
    public String getLocalGraphAttribute() {
        return localGraphAttribute;
    }

    @Override
    public void setLocalGraphAttribute(String localGraphAttribute) {
        this.localGraphAttribute = localGraphAttribute;
    }

    String localGraphAttribute;


}