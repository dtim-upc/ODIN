package edu.upc.essi.dtim.NextiaCore.queries;

import java.util.List;
import java.util.Map;

public class QueryResultSet {
    private List<Map<String, Object>> results;

    public QueryResultSet(List<Map<String, Object>> results) {
        this.results = results;
    }

    public List<Map<String, Object>> getResults() {
        return results;
    }
}

