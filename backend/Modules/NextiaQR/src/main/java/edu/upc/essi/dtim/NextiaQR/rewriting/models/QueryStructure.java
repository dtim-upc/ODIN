package edu.upc.essi.dtim.NextiaQR.rewriting.models;

import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.sparql.core.BasicPattern;

import java.util.Set;

/**
 * Represents the structure of a parsed SPARQL query in the ODIN query rewriting algorithm.
 * Contains the projections (PI), basic graph pattern (PHI_p), and ontology model (PHI_o).
 */
public class QueryStructure {
    private Set<String> projections;
    private BasicPattern basicGraphPattern;
    private InfModel ontologyModel;

    public QueryStructure(Set<String> projections, BasicPattern basicGraphPattern, InfModel ontologyModel) {
        this.projections = projections;
        this.basicGraphPattern = basicGraphPattern;
        this.ontologyModel = ontologyModel;
    }

    public Set<String> getProjections() {
        return projections;
    }

    public void setProjections(Set<String> projections) {
        this.projections = projections;
    }

    public BasicPattern getBasicGraphPattern() {
        return basicGraphPattern;
    }

    public void setBasicGraphPattern(BasicPattern basicGraphPattern) {
        this.basicGraphPattern = basicGraphPattern;
    }

    public InfModel getOntologyModel() {
        return ontologyModel;
    }

    public void setOntologyModel(InfModel ontologyModel) {
        this.ontologyModel = ontologyModel;
    }

    @Override
    public String toString() {
        return "QueryStructure{" +
                "projections=" + projections +
                ", basicGraphPattern=" + basicGraphPattern +
                ", ontologyModel=" + ontologyModel +
                '}';
    }
}