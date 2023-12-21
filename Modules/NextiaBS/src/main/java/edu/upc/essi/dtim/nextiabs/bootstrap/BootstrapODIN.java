package edu.upc.essi.dtim.nextiabs.bootstrap;

import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;

public interface BootstrapODIN {
    BootstrapResult bootstrapDataset(Dataset dataset);
}
