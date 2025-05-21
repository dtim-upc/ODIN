package edu.upc.essi.dtim.NextiaCore.constraints;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DenialConstraint {

    private List<Predicate> predicates;

    public DenialConstraint(List<Predicate> predicates) {
        this.predicates = predicates;
    }

    public List<Predicate> getPredicates() {
        return predicates;
    }

    public void setPredicates(List<Predicate> predicates) {
        this.predicates = predicates;
    }

    @Override
    public String toString() {
        return "¬(" + predicates.stream()
                .map(Predicate::toString)
                .collect(Collectors.joining(" ^ ")) + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DenialConstraint that = (DenialConstraint) o;
        return Objects.equals(predicates, that.predicates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(predicates);
    }
}