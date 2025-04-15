package edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaDI;

import edu.upc.essi.dtim.NextiaCore.graph.CoreGraphFactory;
import edu.upc.essi.dtim.NextiaCore.graph.Graph;
import edu.upc.essi.dtim.NextiaCore.vocabulary.R2RML;
import edu.upc.essi.dtim.NextiaDI;
import edu.upc.essi.dtim.NextiaCore.discovery.Alignment;
import edu.upc.essi.dtim.odin.nextiaInterfaces.NextiaGraphy.nextiaGraphyModuleImpl;
import edu.upc.essi.dtim.odin.nextiaInterfaces.NextiaGraphy.nextiaGraphyModuleInterface;
import edu.upc.essi.dtim.odin.nextiaInterfaces.NextiaGraphy.vocabulary.Namespaces;
import edu.upc.essi.dtim.odin.integration.pojos.JoinAlignment;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import java.io.FileOutputStream;
import java.io.FileNotFoundException;

public class    integrationModuleImpl implements integrationModuleInterface {
    @Override
    public Graph integrate(Graph graphA, Graph graphB, List<Alignment> alignments) {
        Graph integratedGraph = CoreGraphFactory.createGraphInstance("normal");
        NextiaDI nextiaDI = new NextiaDI();

        integratedGraph.setGraph(
                nextiaDI.Integrate(retrieveSourceGraph(alignments, graphA), retrieveSourceGraph(alignments, graphB), alignments)
        );

        return integratedGraph;
    }

    @Override
    public List<Alignment> getUnused(Graph graphA, Graph graphB, List<Alignment> alignments) {
        NextiaDI nextiaDI = new NextiaDI();
        nextiaDI.Integrate(retrieveSourceGraph(alignments, graphA), retrieveSourceGraph(alignments, graphB), alignments);
        return nextiaDI.getUnused();
    }

    @Override
    public Graph joinIntegration(Graph integratedGraph, List<JoinAlignment> joinAlignments) {
        Graph joinGraph = CoreGraphFactory.createGraphInstance("normal");
        Graph schemaIntegration = CoreGraphFactory.createGraphInstance("normal");
        NextiaDI nextiaDI = new NextiaDI();

        for (JoinAlignment a : joinAlignments) {
            if (a.getRightArrow()) {
                schemaIntegration.setGraph(nextiaDI.JoinIntegration(integratedGraph.getGraph(), a.getIriA(), a.getIriB(), a.getL(), a.getRelationship(), a.getDomainA(), a.getDomainB()));
            }
            else {
                schemaIntegration.setGraph(nextiaDI.JoinIntegration(integratedGraph.getGraph(), a.getIriA(), a.getIriB(), a.getL(), a.getRelationship(), a.getDomainB(), a.getDomainA()));
            }
        }

        joinGraph.setGraph(schemaIntegration.getGraph());
        return joinGraph;
    }

    public Graph generateGlobalGraph(Graph graph) {
        Graph globalGraph = CoreGraphFactory.createGlobalGraph();

        NextiaDI nextiaDI = new NextiaDI();
        globalGraph.setGraph(nextiaDI.generateMinimalGraph(graph.getGraph()));

        nextiaGraphyModuleInterface visualLibInterface = new nextiaGraphyModuleImpl();
        globalGraph.setGraphicalSchema(visualLibInterface.generateVisualGraph(globalGraph));

        return globalGraph;
    }

    // borrar el seguent
    public String getDomainOfProperty(Graph graph, String propertyIRI) {
        // Define a SPARQL query to retrieve the domain of the property and execute the query on the graph
        String query = "SELECT ?domain WHERE { <" + propertyIRI + "> <" + RDFS.domain.toString() + "> ?domain. }";
        List<Map<String, Object>> res = graph.query(query);

        // If the query result is not empty we extract and return the domain as a string. Otherwise, return null
        if (!res.isEmpty()) {
            return res.get(0).get("domain").toString();
        }
        return null;
    }

    public String getRDFSLabel(Graph graph, String resourceIRI) {
        // Define a SPARQL query to retrieve the RDFS label of the resource and execute the query on the graph
        String query = "SELECT ?label WHERE { <" + resourceIRI + "> <" + RDFS.label.toString() + "> ?label. }";
        List<Map<String, Object>> res = graph.query(query);

        // If the query result is not empty we extract and return the RDFS label as a string. Otherwise, return null
        if (!res.isEmpty()) {
            return res.get(0).get("label").toString();
        }
        return null;
    }

    /**
     * Retrieves a source graph with specified alignments.
     *
     * @param alignments The list of alignments.
     * @param graph      The input graph.
     * @return A source graph.
     */
    private Model retrieveSourceGraph(List<Alignment> alignments, Graph graph) {
        // TODO think in a better way to do this. Maybe identifiers should be declared when loading data
        List<Alignment> aligId = alignments.stream().filter(x -> x.getType().contains("datatype")).collect(Collectors.toList());

        Model sourceG = graph.getGraph();

        for (Alignment a : aligId) {
            Resource rA = sourceG.createResource(a.getIriA());
            Resource rB = sourceG.createResource(a.getIriB());

            if (sourceG.containsResource(rA)) {
                graph.addTriple(rA.getURI(), RDFS.subClassOf.getURI(), Namespaces.SCHEMA.getElement() + "identifier");
            } else {
                graph.addTriple(rB.getURI(), RDFS.subClassOf.getURI(), Namespaces.SCHEMA.getElement() + "identifier");
            }
        }

        sourceG = graph.getGraph();
        return sourceG;
    }




    public static void main(String[] args) {

        Model modelA = RDFDataMgr.loadModel("/Users/anbipa/Desktop/DTIM/Cyclops/Cyclops-Test/customers.ttl") ;
        Model modelB = RDFDataMgr.loadModel("/Users/anbipa/Desktop/DTIM/Cyclops/Cyclops-Test/orders.ttl") ;

        Graph graphA = CoreGraphFactory.createGraphInstance("normal");
        graphA.setGraph(modelA);

        Graph graphB = CoreGraphFactory.createGraphInstance("normal");
        graphB.setGraph(modelB);

        // join aligment better
        Alignment a1 = new Alignment();
        a1.setType("datatype");
        a1.setIriA("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/customers/customer_id");
        a1.setIriB("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/orders/customer_id");
        a1.setL("iCustomerid");

        // list of alignments
        List<Alignment> alignments = new ArrayList<>();
        alignments.add(a1);


        // Get a list of unused alignments between graphA and graphB.
        integrationModuleInterface integrationInterface = new integrationModuleImpl();
        // integrate
        Graph integratedGraph = integrationInterface.integrate(graphA, graphB, alignments);

        // Create join alignments from potential join alignments.
        List<JoinAlignment> joinProperties = new ArrayList<>();

        JoinAlignment joinAlignment = new JoinAlignment();
        joinAlignment.setDomainA("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/orders/orders");
        joinAlignment.setDomainB("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/customers/customers");
        joinAlignment.setIriA("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/orders/customer_id");
        joinAlignment.setIriB("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/customers/customer_id");
        joinAlignment.setL("iCustomerid");
        joinAlignment.setRightArrow(true);
        joinAlignment.setRelationship("placed_by");
        joinProperties.add(joinAlignment);


        Graph joinGraph = integrationInterface.joinIntegration(integratedGraph, joinProperties);
        Graph minimal = integrationInterface.generateGlobalGraph(joinGraph);

        Model minimalModel = minimal.getGraph();
        minimalModel.setNsPrefix("rdf", RDF.getURI());
        minimalModel.setNsPrefix("rdfs", RDFS.getURI());
        minimalModel.setNsPrefix("rr", R2RML.getURI());
        minimalModel.setNsPrefix("ex", "http://example.org/");
        minimalModel.setNsPrefix("nextiaDIschema", "http://www.essi.upc.edu/DTIM/NextiaDI/");
        minimalModel.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");

        Model joinModel = joinGraph.getGraph();
        joinModel.setNsPrefix("rdf", RDF.getURI());
        joinModel.setNsPrefix("rdfs", RDFS.getURI());
        joinModel.setNsPrefix("rr", R2RML.getURI());
        joinModel.setNsPrefix("ex", "http://example.org/");
        joinModel.setNsPrefix("nextiaDIschema", "http://www.essi.upc.edu/DTIM/NextiaDI/");
        joinModel.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");




        try {
            RDFDataMgr.write(new FileOutputStream("/Users/anbipa/Desktop/DTIM/Cyclops/Cyclops-Test/minimal_graph.ttl"), minimalModel, Lang.TURTLE);
            System.out.println("file written temporal");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // store integratedModel
        try {
            RDFDataMgr.write(new FileOutputStream("/Users/anbipa/Desktop/DTIM/Cyclops/Cyclops-Test/integrated_graph.ttl"), joinModel, Lang.TURTLE);
            System.out.println("file written temporal");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
