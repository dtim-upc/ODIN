package edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaDataLayer;

import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;

public interface DataLayerInterace {
    /**
     * @param dataset The dataset to upload to DataLayer
     */
    void uploadToDataLayer(Dataset dataset);
}
