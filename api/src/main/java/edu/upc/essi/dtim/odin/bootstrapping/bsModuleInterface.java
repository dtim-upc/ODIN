package edu.upc.essi.dtim.odin.bootstrapping;

import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.NextiaCore.graph.Graph;

public interface bsModuleInterface {
    Graph convertDatasetToGraph(Dataset dataset);
}
