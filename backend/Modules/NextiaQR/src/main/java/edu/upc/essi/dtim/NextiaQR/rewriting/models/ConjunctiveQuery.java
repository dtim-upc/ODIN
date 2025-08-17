package edu.upc.essi.dtim.NextiaQR.rewriting.models;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a conjunctive query in the ODIN query rewriting algorithm.
 * A conjunctive query consists of projections, join conditions, and wrappers.
 */
public class ConjunctiveQuery {
    private Set<String> projections;
    private Set<EquiJoin> joinConditions;
    private Set<Wrapper> wrappers;

    public ConjunctiveQuery() {
        this.projections = new HashSet<>();
        this.joinConditions = new HashSet<>();
        this.wrappers = new HashSet<>();
    }

    public ConjunctiveQuery(Set<String> projections, Set<EquiJoin> joinConditions, Set<Wrapper> wrappers) {
        this.projections = projections;
        this.joinConditions = joinConditions;
        this.wrappers = wrappers;
    }

    public Set<String> getProjections() {
        return projections;
    }

    public void setProjections(Set<String> projections) {
        this.projections = projections;
    }

    public Set<EquiJoin> getJoinConditions() {
        return joinConditions;
    }

    public void setJoinConditions(Set<EquiJoin> joinConditions) {
        this.joinConditions = joinConditions;
    }

    public Set<Wrapper> getWrappers() {
        return wrappers;
    }

    public void setWrappers(Set<Wrapper> wrappers) {
        this.wrappers = wrappers;
    }

    @Override
    public String toString() {
        return "ConjunctiveQuery{" +
                "projections=" + projections +
                ", joinConditions=" + joinConditions +
                ", wrappers=" + wrappers +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConjunctiveQuery that = (ConjunctiveQuery) o;
        return projections.equals(that.projections) &&
                joinConditions.equals(that.joinConditions) &&
                wrappers.equals(that.wrappers);
    }

    @Override
    public int hashCode() {
        int result = projections.hashCode();
        result = 31 * result + joinConditions.hashCode();
        result = 31 * result + wrappers.hashCode();
        return result;
    }
}