package edu.upc.essi.dtim.odin.nextiaInterfaces.NextiaGraphy.graphy;

import lombok.Data;

/**
 * Represents a link in a graph connecting two nodes.
 */
@Data
public class Links {

    /**
     * The unique identifier of the link.
     */
    private String id;

    /**
     * The identifier of the node associated with this link.
     */
    private String nodeId;

    /**
     * The source node of the link.
     */
    private String source;

    /**
     * The target node of the link.
     */
    private String target;

    /**
     * The label or description of the link.
     */
    private String label;
}
