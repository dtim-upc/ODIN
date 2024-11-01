package edu.upc.essi.dtim.odin.nextiaInterfaces.NextiaGraphy;

import edu.upc.essi.dtim.NextiaCore.graph.Graph;

public interface nextiaGraphyModuleInterface {
    /**
     * Generates a visual graph representation from an RDF graph.
     *
     * @param graph The RDF graph to generate the visual graph from.
     * @return A JSON representation of the visual graph.
     */
    String generateVisualGraph(Graph graph);
}
