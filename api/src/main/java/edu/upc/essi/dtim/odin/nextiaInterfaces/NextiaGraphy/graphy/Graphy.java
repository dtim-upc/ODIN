package edu.upc.essi.dtim.odin.nextiaInterfaces.NextiaGraphy.graphy;

import java.util.List;

/**
 * Represents a graph with nodes and links.
 */
public class Graphy {

    /**
     * List of nodes in the graph.
     */
    private List<Nodes> nodes;

    /**
     * List of links connecting nodes in the graph.
     */
    private List<Links> links;

    public List<Nodes> getNodes() {
        return nodes;
    }

    public void setNodes(List<Nodes> nodes) {
        this.nodes = nodes;
    }

    public List<Links> getLinks() {
        return links;
    }

    public void setLinks(List<Links> links) {
        this.links = links;
    }
}
