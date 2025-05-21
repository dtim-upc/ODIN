package edu.upc.essi.dtim.NextiaCore.queries;

import java.util.*;

public class QuerySolution {

	private Map<String, String> solutionMap; // A mapping of variable names to URI strings

	public QuerySolution() {
		solutionMap = new HashMap<>();
	}

	public void set(String varName, String uri) {
		solutionMap.put(varName, uri);
	}

	public String get(String varName) {
		return solutionMap.get(varName);
	}

	public boolean contains(String varName) {
		return solutionMap.containsKey(varName);
	}

	public Iterable<String> varNames() {
		return solutionMap.keySet();
	}

}