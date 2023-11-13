package edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaBS;

import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.NextiaCore.graph.Graph;
import edu.upc.essi.dtim.nextiabs.utils.BootstrapResult;

public interface bsModuleInterface {
    /**
     * Converts a dataset represented as a DataResource into a Graph.
     *
     * @param dataset The dataset to be converted into a graph.
     * @return The resulting graph representing the dataset.
     */
    BootstrapResult bootstrapDataset(Dataset dataset);

    Graph bootstrapGraph(Dataset dataset);
}
