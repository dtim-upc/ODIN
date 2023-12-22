package edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaDI;

import edu.upc.essi.dtim.NextiaCore.graph.CoreGraphFactory;
import edu.upc.essi.dtim.NextiaCore.graph.Graph;
import edu.upc.essi.dtim.NextiaDI;
import edu.upc.essi.dtim.NextiaCore.discovery.Alignment;
import edu.upc.essi.dtim.odin.nextiaInterfaces.NextiaGraphy.nextiaGraphyModuleImpl;
import edu.upc.essi.dtim.odin.nextiaInterfaces.NextiaGraphy.nextiaGraphyModuleInterface;
import edu.upc.essi.dtim.odin.nextiaInterfaces.NextiaGraphy.vocabulary.Namespaces;
import edu.upc.essi.dtim.odin.integration.pojos.JoinAlignment;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;

import java.util.List;
import java.util.stream.Collectors;

public class integrationModuleImpl implements integrationModuleInterface {
    @Override
    public Graph integrate(Graph graphA, Graph graphB, List<Alignment> alignments) {
        Graph integratedGraph = CoreGraphFactory.createGraphInstance("normal");

        NextiaDI n = new NextiaDI();

        integratedGraph.setGraph(
                n.Integrate(retrieveSourceGraph(alignments, graphA), retrieveSourceGraph(alignments, graphB), alignments)
        );

        return integratedGraph;
    }

    @Override
    public List<Alignment> getUnused(Graph graphA, Graph graphB, List<Alignment> alignments) {
        NextiaDI n = new NextiaDI();
        n.Integrate(retrieveSourceGraph(alignments, graphA), retrieveSourceGraph(alignments, graphB), alignments);
        return n.getUnused();
    }

    @Override
    public Graph joinIntegration(Graph integratedGraph, List<JoinAlignment> joinAlignments) {
        Graph joinGraph = CoreGraphFactory.createGraphInstance("normal");
        Graph schemaIntegration = CoreGraphFactory.createGraphInstance("normal");
        NextiaDI n = new NextiaDI();

        for (JoinAlignment a : joinAlignments) {

            if (a.getRightArrow())
                schemaIntegration.setGraph(n.JoinIntegration(integratedGraph.getGraph(), a.getIriA(), a.getIriB(), a.getL(), a.getRelationship(), a.getDomainA(), a.getDomainB()));
            else
                schemaIntegration.setGraph(n.JoinIntegration(integratedGraph.getGraph(), a.getIriA(), a.getIriB(), a.getL(), a.getRelationship(), a.getDomainB(), a.getDomainA()));
        }

        joinGraph.setGraph(
                schemaIntegration.getGraph()
        );

        return joinGraph;
    }

    /**
     * Generates a global graph based on the input graph.
     *
     * @param graph The input graph.
     * @return A global graph.
     */
    public Graph generateGlobalGraph(Graph graph) {
        Graph globalGraph = CoreGraphFactory.createGlobalGraph();

        NextiaDI n = new NextiaDI();
        globalGraph.setGraph(n.generateMinimalGraph(graph.getGraph()));

        nextiaGraphyModuleInterface visualLibInterface = new nextiaGraphyModuleImpl();
        globalGraph.setGraphicalSchema(visualLibInterface.generateVisualGraph(globalGraph));

        return globalGraph;
    }

    /**
     * Retrieves a source graph with specified alignments.
     *
     * @param alignments The list of alignments.
     * @param graph      The input graph.
     * @return A source graph.
     */
    public Model retrieveSourceGraph(List<Alignment> alignments, Graph graph) {
        // Todo think in a better way to do this. Maybe identifiers should be declared when loading data
        List<Alignment> aligId = alignments.stream().filter(x -> x.getType().contains("datatype")).collect(Collectors.toList());

        Model sourceG = graph.getGraph();

        for (Alignment a : aligId) {
            Resource rA = sourceG.createResource(a.getIriA());
            Resource rB = sourceG.createResource(a.getIriB());

            if (sourceG.containsResource(rA)) {
                graph.addTriple(rA.getURI(), RDFS.subClassOf.getURI(), Namespaces.SCHEMA.val() + "identifier");
            } else {
                graph.addTriple(rB.getURI(), RDFS.subClassOf.getURI(), Namespaces.SCHEMA.val() + "identifier");
            }
        }

        sourceG = graph.getGraph();
        return sourceG;
    }
}
