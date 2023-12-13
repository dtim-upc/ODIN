package edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaBS;

import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.NextiaCore.graph.Graph;
import edu.upc.essi.dtim.nextiabs.*;
import edu.upc.essi.dtim.nextiabs.utils.BootstrapResult;

public class bsModuleImpl implements bsModuleInterface {

    public BootstrapResult bootstrapDataset(Dataset dataset) {
        NextiaBootstrapInterface bootstrapInterface;
        try {
            bootstrapInterface = BootstrapFactory.getInstance(dataset);
            return bootstrapInterface.bootstrap(dataset);
        } catch (Exception e) {
            throw new RuntimeException("Error converting dataset to graph: " + e.getMessage(), e);
        }
    }

    @Override
    public Graph bootstrapGraph(Dataset dataset) {
        NextiaBootstrapInterface bootstrapInterface;
        try {
            bootstrapInterface = BootstrapFactory.getInstance(dataset);
            return bootstrapInterface.bootstrapGraph(dataset);
        } catch (Exception e) {
            throw new RuntimeException("Error converting dataset to graph: " + e.getMessage(), e);
        }
    }

}
