package edu.upc.essi.dtim.odin.bootstrapping;

import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.NextiaCore.graph.Graph;

public interface bsModuleInterface {
    /**
     * Converts a dataset represented as a DataResource into a Graph.
     *
     * @param dataset The dataset to be converted into a graph.
     * @return The resulting graph representing the dataset.
     */
    Graph convertDatasetToGraph(Dataset dataset);
}
