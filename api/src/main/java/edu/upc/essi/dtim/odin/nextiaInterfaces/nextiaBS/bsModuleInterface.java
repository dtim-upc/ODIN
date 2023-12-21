package edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaBS;

import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.nextiabs.bootstrap.BootstrapResult;

public interface bsModuleInterface {
    /**
     * Converts a dataset into a graph and generates the wrapper to access the data from the dataset.
     *
     * @param dataset The dataset to be converted into a graph.
     * @return A BootstrapResult, a class that which contains two variables: graph and wrapper
     */
    BootstrapResult bootstrapDataset(Dataset dataset);
}
