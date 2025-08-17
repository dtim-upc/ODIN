package edu.upc.essi.dtim.NextiaQR.rewriting.models;

import java.util.Set;

/**
 * Represents an edge in the integration graph that contains wrappers.
 * Used in the ODIN query rewriting algorithm.
 */
public class IntegrationEdge {
    private String label;
    private Set<Wrapper> wrappers;

    public IntegrationEdge(String label, Set<Wrapper> wrappers) {
        this.label = label;
        this.wrappers = wrappers;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Set<Wrapper> getWrappers() {
        return wrappers;
    }

    public void setWrappers(Set<Wrapper> wrappers) {
        this.wrappers = wrappers;
    }

    @Override
    public String toString() {
        return "IntegrationEdge{" +
                "label='" + label + '\'' +
                ", wrappers=" + wrappers +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntegrationEdge that = (IntegrationEdge) o;
        return label.equals(that.label);
    }

    @Override
    public int hashCode() {
        return label.hashCode();
    }
}