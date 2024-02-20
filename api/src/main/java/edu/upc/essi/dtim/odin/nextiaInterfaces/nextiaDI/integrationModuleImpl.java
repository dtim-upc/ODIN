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
}
