package edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaBS;

import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.nextiabs.bootstrap.BootstrapODIN;
import edu.upc.essi.dtim.nextiabs.bootstrap.BootstrapFactory;
import edu.upc.essi.dtim.nextiabs.bootstrap.BootstrapResult;
import edu.upc.essi.dtim.odin.exception.InternalServerErrorException;

public class bsModuleImpl implements bsModuleInterface {

    public BootstrapResult bootstrapDataset(Dataset dataset) {
        BootstrapODIN bootstrapODINInterface;
        try {
            bootstrapODINInterface = BootstrapFactory.getInstance(dataset);
            return bootstrapODINInterface.bootstrapDataset(dataset);
        } catch (Exception e) {
            throw new InternalServerErrorException("Error converting dataset to graph", e.getMessage());
        }
    }

}
