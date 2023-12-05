package edu.upc.essi.dtim.nextiabs.utils;

import edu.upc.essi.dtim.NextiaCore.graph.Graph;

public class BootstrapResult {
    Graph graph;

    String wrapper;

    public BootstrapResult(Graph graph, String wrapper) {
        this.graph = graph;
        this.wrapper = wrapper;
    }

    public BootstrapResult(Graph graph) {
        this.graph = graph;
    }

    public BootstrapResult(String wrapper) {
        this.wrapper = wrapper;
    }

    public BootstrapResult() {
    }

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    public String getWrapper() {
        return wrapper;
    }

    public void setWrapper(String wrapper) {
        this.wrapper = wrapper;
    }
}
