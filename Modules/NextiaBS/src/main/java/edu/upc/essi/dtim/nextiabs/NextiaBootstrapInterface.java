package edu.upc.essi.dtim.nextiabs;

import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.NextiaCore.graph.Graph;
import edu.upc.essi.dtim.nextiabs.utils.BootstrapResult;

public interface NextiaBootstrapInterface {
    BootstrapResult bootstrap(Dataset dataset);

    Graph bootstrapGraph(Dataset dataset);
}
