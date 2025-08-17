package edu.upc.essi.dtim.NextiaQR.rewriting.models;

import java.util.Set;

/**
 * Represents a vertex in the integration graph that contains conjunctive queries.
 * Used in the ODIN query rewriting algorithm.
 */
public class CQVertex {
    private String label;
    private Set<ConjunctiveQuery> cqs;

    public CQVertex(String label, Set<ConjunctiveQuery> cqs) {
        this.label = label;
        this.cqs = cqs;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Set<ConjunctiveQuery> getCQs() {
        return cqs;
    }

    public void setCQs(Set<ConjunctiveQuery> cqs) {
        this.cqs = cqs;
    }

    @Override
    public String toString() {
        return "CQVertex{" +
                "label='" + label + '\'' +
                ", cqs=" + cqs +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CQVertex cqVertex = (CQVertex) o;
        return label.equals(cqVertex.label);
    }

    @Override
    public int hashCode() {
        return label.hashCode();
    }
}