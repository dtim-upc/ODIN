package edu.upc.essi.dtim.nextiabs.bootstrap;

import edu.upc.essi.dtim.NextiaCore.graph.Graph;
import edu.upc.essi.dtim.NextiaCore.graph.LocalGraph;

public class BootstrapResult {
    LocalGraph graph;
    String wrapper;

    public BootstrapResult() {}

    public BootstrapResult(LocalGraph graph) {
        this.graph = graph;
    }

    public BootstrapResult(String wrapper) {
        this.wrapper = wrapper;
    }

    public BootstrapResult(LocalGraph graph, String wrapper) {
        this.graph = graph;
        this.wrapper = wrapper;
    }

    public LocalGraph getGraph() {
        return graph;
    }
    public void setGraph(LocalGraph graph) {
        this.graph = graph;
    }

    public String getWrapper() {
        return wrapper;
    }
    public void setWrapper(String wrapper) {
        this.wrapper = wrapper;
    }
}
