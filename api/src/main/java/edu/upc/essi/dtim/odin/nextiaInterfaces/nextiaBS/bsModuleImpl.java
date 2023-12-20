package edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaBS;

import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.nextiabs.bootstrap.Bootstrap;
import edu.upc.essi.dtim.nextiabs.utils.BootstrapFactory;
import edu.upc.essi.dtim.nextiabs.utils.BootstrapResult;

public class bsModuleImpl implements bsModuleInterface {

    public BootstrapResult bootstrapDataset(Dataset dataset) {
        Bootstrap bootstrapInterface;
        try {
            bootstrapInterface = BootstrapFactory.getInstance(dataset);
            return bootstrapInterface.bootstrapDataset(dataset);
        } catch (Exception e) {
            throw new RuntimeException("Error converting dataset to graph: " + e.getMessage(), e);
        }
    }

}
