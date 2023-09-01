package edu.upc.essi.dtim.odin.NextiaGraphy.graphy;

import lombok.Data;

import java.util.List;

/**
 * Represents a graph with nodes and links.
 */
@Data
public class Graphy {

    /**
     * List of nodes in the graph.
     */
    private List<Nodes> nodes;

    /**
     * List of links connecting nodes in the graph.
     */
    private List<Links> links;
}
