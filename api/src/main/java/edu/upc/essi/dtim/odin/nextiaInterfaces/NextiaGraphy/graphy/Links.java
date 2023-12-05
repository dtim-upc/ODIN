package edu.upc.essi.dtim.odin.nextiaInterfaces.NextiaGraphy.graphy;

/**
 * Represents a link in a graph connecting two nodes.
 */
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
