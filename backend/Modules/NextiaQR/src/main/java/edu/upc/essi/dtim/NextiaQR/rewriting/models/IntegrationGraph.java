package edu.upc.essi.dtim.NextiaQR.rewriting.models;

import org.jgrapht.graph.SimpleDirectedGraph;

/**
 * Represents the integration graph used in the ODIN query rewriting algorithm.
 * This graph contains CQVertices connected by IntegrationEdges.
 */
public class IntegrationGraph extends SimpleDirectedGraph<CQVertex, IntegrationEdge> {
    
    public IntegrationGraph() {
        super(IntegrationEdge.class);
    }
}