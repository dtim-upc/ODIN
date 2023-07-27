package edu.upc.essi.dtim.odin.integration;

import edu.upc.essi.dtim.NextiaCore.graph.Graph;
import edu.upc.essi.dtim.nextiadi.models.Alignment;
import edu.upc.essi.dtim.odin.integration.pojos.JoinAlignment;

import java.util.List;

public interface integrationModuleInterface {
    Graph integrate(Graph graphA, Graph graphB, List<Alignment> alignments);

    List<Alignment> getUnused(Graph graphA, Graph graphB, List<Alignment> alignments);

    Graph globalGraph(Graph graphA, Graph graphB, List<Alignment> alignments);

    Graph joinIntegration(Graph integratedGraph, List<JoinAlignment> joinAlignments);

    Graph generateGlobalGraph(Graph graph);
}
