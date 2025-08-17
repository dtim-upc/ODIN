package edu.upc.essi.dtim.NextiaQR.rewriting.models;

import java.util.Set;

/**
 * Represents the result of the ODIN query rewriting algorithm.
 * Contains the number of rewritings and the set of conjunctive queries.
 */
public class QueryRewritingResult {
    private int numberOfRewritings;
    private Set<ConjunctiveQuery> conjunctiveQueries;

    public QueryRewritingResult(int numberOfRewritings, Set<ConjunctiveQuery> conjunctiveQueries) {
        this.numberOfRewritings = numberOfRewritings;
        this.conjunctiveQueries = conjunctiveQueries;
    }

    public int getNumberOfRewritings() {
        return numberOfRewritings;
    }

    public void setNumberOfRewritings(int numberOfRewritings) {
        this.numberOfRewritings = numberOfRewritings;
    }

    public Set<ConjunctiveQuery> getConjunctiveQueries() {
        return conjunctiveQueries;
    }

    public void setConjunctiveQueries(Set<ConjunctiveQuery> conjunctiveQueries) {
        this.conjunctiveQueries = conjunctiveQueries;
    }

    @Override
    public String toString() {
        return "QueryRewritingResult{" +
                "numberOfRewritings=" + numberOfRewritings +
                ", conjunctiveQueries=" + conjunctiveQueries +
                '}';
    }
}