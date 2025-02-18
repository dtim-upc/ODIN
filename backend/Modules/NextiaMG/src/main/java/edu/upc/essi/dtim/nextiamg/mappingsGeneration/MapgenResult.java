package edu.upc.essi.dtim.nextiamg.mappingsGeneration;

import edu.upc.essi.dtim.NextiaCore.graph.LocalGraph;
import edu.upc.essi.dtim.NextiaCore.graph.IntegratedGraph;
import edu.upc.essi.dtim.NextiaCore.graph.GlobalGraph;
import edu.upc.essi.dtim.NextiaCore.graph.MappingsGraph;


public class MapgenResult {
    MappingsGraph graph;

    public MapgenResult() {}

    public MapgenResult(MappingsGraph graph) {
        this.graph = graph;
    }

    public MappingsGraph getGraph() {
        return graph;
    }
    public void setGraph(MappingsGraph graph) {
        this.graph = graph;
    }

}
