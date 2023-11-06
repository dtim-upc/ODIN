package edu.upc.essi.dtim.odin.integration;

import edu.upc.essi.dtim.NextiaCore.discovery.Alignment;
import edu.upc.essi.dtim.NextiaCore.graph.Graph;
import edu.upc.essi.dtim.odin.integration.pojos.JoinAlignment;

import java.util.List;

/**
 * Interface for integrating and manipulating RDF graphs and alignments.
 */
public interface integrationModuleInterface {

    /**
     * Integrates two RDF graphs based on provided alignments.
     *
     * @param graphA     The first RDF graph to be integrated.
     * @param graphB     The second RDF graph to be integrated.
     * @param alignments A list of alignments specifying how the graphs should be integrated.
     * @return The integrated RDF graph.
     */
    Graph integrate(Graph graphA, Graph graphB, List<Alignment> alignments);

    /**
     * Retrieves the list of unused alignments after integration.
     *
     * @param graphA     The first RDF graph used for integration.
     * @param graphB     The second RDF graph used for integration.
     * @param alignments A list of alignments specifying how the graphs were integrated.
     * @return A list of alignments that were not used during integration.
     */
    List<Alignment> getUnused(Graph graphA, Graph graphB, List<Alignment> alignments);

    /**
     * Integrates two RDF graphs to create a global graph.
     *
     * @param graphA     The first RDF graph to be integrated.
     * @param graphB     The second RDF graph to be integrated.
     * @param alignments A list of alignments specifying how the global graph should be created.
     * @return The integrated global RDF graph.
     */
    Graph globalGraph(Graph graphA, Graph graphB, List<Alignment> alignments);

    /**
     * Performs join integration on an integrated RDF graph.
     *
     * @param integratedGraph The integrated RDF graph to which the join operation will be applied.
     * @param joinAlignments  A list of join alignments specifying how the join integration should be performed.
     * @return The integrated RDF graph after the join integration.
     */
    Graph joinIntegration(Graph integratedGraph, List<JoinAlignment> joinAlignments);

    /**
     * Generates a global RDF graph based on the input RDF graph.
     *
     * @param graph The input RDF graph from which the global graph will be generated.
     * @return The generated global RDF graph.
     */
    Graph generateGlobalGraph(Graph graph);
}
