package edu.upc.essi.dtim.NextiaQR.rewriting.models;

/**
 * Represents a relationship edge in the concepts graph.
 * Used to connect concepts in the query rewriting algorithm.
 */
public class RelationshipEdge {
    private String label;

    public RelationshipEdge(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return "RelationshipEdge{" +
                "label='" + label + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RelationshipEdge that = (RelationshipEdge) o;
        return label.equals(that.label);
    }

    @Override
    public int hashCode() {
        return label.hashCode();
    }
}