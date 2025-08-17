package edu.upc.essi.dtim.NextiaQR.rewriting;

import edu.upc.essi.dtim.NextiaQR.rewriting.models.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

import java.util.Set;

/**
 * Example class demonstrating how to use the ODIN query rewriting algorithm.
 * This class shows how to set up the necessary data structures and run the algorithm.
 */
public class QueryRewritingExample {

    /**
     * Example method showing how to use the query rewriting algorithm
     */
    public static void main(String[] args) {
        // Create a sample model with some triples
        Model model = createSampleModel();
        
        // Example SPARQL query
        String sparqlQuery = "SELECT ?x ?y WHERE { " +
                "?x <http://www.dtim.upc.edu/odin/HAS_FEATURE> ?f1 . " +
                "?y <http://www.dtim.upc.edu/odin/HAS_FEATURE> ?f2 . " +
                "?x <http://example.org/relatedTo> ?y " +
                "}";
        
        try {
            // Parse the SPARQL query
            QueryStructure queryStructure = ODINQueryRewriting.parseSPARQL(sparqlQuery, model);
            System.out.println("Query structure parsed successfully");
            System.out.println("Projections: " + queryStructure.getProjections());
            System.out.println("Basic graph pattern: " + queryStructure.getBasicGraphPattern());
            
            // Rewrite the query
            QueryRewritingResult result = ODINQueryRewriting.rewriteToUnionOfConjunctiveQueries(queryStructure, model);
            
            System.out.println("Query rewriting completed");
            System.out.println("Number of rewritings: " + result.getNumberOfRewritings());
            System.out.println("Conjunctive queries: " + result.getConjunctiveQueries());
            
        } catch (Exception e) {
            System.err.println("Error during query rewriting: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Creates a sample model with some triples for testing
     */
    private static Model createSampleModel() {
        Model model = ModelFactory.createDefaultModel();
        
        // Add some sample triples to the model
        Resource concept1 = ResourceFactory.createResource("http://example.org/Concept1");
        Resource concept2 = ResourceFactory.createResource("http://example.org/Concept2");
        Resource feature1 = ResourceFactory.createResource("http://example.org/Feature1");
        Resource feature2 = ResourceFactory.createResource("http://example.org/Feature2");
        Resource attribute1 = ResourceFactory.createResource("http://example.org/Attribute1");
        Resource attribute2 = ResourceFactory.createResource("http://example.org/Attribute2");
        Resource wrapper1 = ResourceFactory.createResource("http://example.org/wrapper1");
        
        // Add triples to the model
        model.add(concept1, ResourceFactory.createProperty("http://www.dtim.upc.edu/odin/HAS_FEATURE"), feature1);
        model.add(concept2, ResourceFactory.createProperty("http://www.dtim.upc.edu/odin/HAS_FEATURE"), feature2);
        model.add(attribute1, ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#sameAs"), feature1);
        model.add(attribute2, ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#sameAs"), feature2);
        model.add(wrapper1, ResourceFactory.createProperty("http://www.dtim.upc.edu/odin/HAS_ATTRIBUTE"), attribute1);
        model.add(wrapper1, ResourceFactory.createProperty("http://www.dtim.upc.edu/odin/HAS_ATTRIBUTE"), attribute2);
        
        return model;
    }
    
    /**
     * Example method showing how to use the query rewriting with the interface
     */
    public static void exampleWithInterface() {
        // Create an instance of the query rewriting algorithm
        ODINQueryRewriting queryRewriting = new ODINQueryRewriting();
        
        // This would typically be called with actual integrated graph and datasets
        // queryRewriting.generateQueryingStructures(integratedGraph, datasets);
        
        System.out.println("Query rewriting interface example completed");
    }
}