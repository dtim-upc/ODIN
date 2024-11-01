package edu.upc.essi.dtim.NextiaCore.queries;

import edu.upc.essi.dtim.NextiaCore.graph.Triple;
import edu.upc.essi.dtim.NextiaCore.graph.jena.GraphJenaImpl;
import edu.upc.essi.dtim.NextiaCore.graph.URI;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class QueryResult implements Iterable<QuerySolution>{
	private Query query; // The query for which this result is generated
	private GraphJenaImpl graphJenaImpl; // The graph containing the data for the query
	private Map<QuerySolution, Map<URI, Triple>> result; // Mapping of QuerySolution to URI-Triple mapping
	private Iterator<QuerySolution> querySolutionIterator; // Iterator for QuerySolution objects

	// Constructor to initialize the QueryResult object
	public QueryResult(Query query, GraphJenaImpl graphJenaImpl) {
		this.query = query;
		this.graphJenaImpl = graphJenaImpl;
		this.result = new HashMap<>();
		this.querySolutionIterator = result.keySet().iterator(); // Initialize the iterator
	}

	// Method to add a QuerySolution, URI, and Triple to the result mapping
	public void addQuerySolution(QuerySolution querySolution, URI uri, Triple triple) {
		// ComputeIfAbsent is used to create a new mapping for querySolution if not already present
		Map<URI, Triple> uriTripleMap = result.computeIfAbsent(querySolution, k -> new HashMap<>());
		uriTripleMap.put(uri, triple);
	}

	// Method to retrieve the URI-Triple mapping for a given QuerySolution
	public Map<URI, Triple> getURITripleMapping(QuerySolution querySolution) {
		return result.get(querySolution);
	}

	// Implementation of iterator() method from Iterable interface to iterate over QuerySolution objects
	@Override
	public Iterator<QuerySolution> iterator() {
		return new Iterator<QuerySolution>() {
			@Override
			public boolean hasNext() {
				return querySolutionIterator.hasNext(); // Check if there are more QuerySolution objects
			}

			@Override
			public QuerySolution next() {
				return querySolutionIterator.next(); // Get the next QuerySolution object
			}
		};
	}
}