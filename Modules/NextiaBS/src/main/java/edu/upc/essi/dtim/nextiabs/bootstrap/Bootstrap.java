package edu.upc.essi.dtim.nextiabs.bootstrap;

import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.nextiabs.utils.BootstrapResult;

public interface Bootstrap {
    BootstrapResult bootstrapDataset(Dataset dataset);
}
