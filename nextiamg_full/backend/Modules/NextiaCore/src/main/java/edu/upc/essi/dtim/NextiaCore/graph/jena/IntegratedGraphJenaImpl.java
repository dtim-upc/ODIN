package edu.upc.essi.dtim.NextiaCore.graph.jena;

import edu.upc.essi.dtim.NextiaCore.graph.IntegratedGraph;
import org.apache.jena.rdf.model.*;

public class IntegratedGraphJenaImpl extends GraphJenaImpl implements IntegratedGraph {
	GlobalGraphJenaImpl globalGraph;

	public IntegratedGraphJenaImpl() {
		super();
	}

	public IntegratedGraphJenaImpl(String id, String name, Model triples, String graphicalSchema) {
		super();
	}

	public GlobalGraphJenaImpl getGlobalGraph() {
		return globalGraph;
	}
	public void setGlobalGraph(GlobalGraphJenaImpl globalGraph) {
		this.globalGraph = globalGraph;
	}
}