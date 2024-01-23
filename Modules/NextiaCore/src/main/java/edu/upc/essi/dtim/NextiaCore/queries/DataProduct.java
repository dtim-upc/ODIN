package edu.upc.essi.dtim.NextiaCore.queries;

import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;

public class DataProduct extends Dataset {

    public DataProduct() {

    }
    public DataProduct(String UUID, String dataProductName, String dataProductDescription) {
        this.setUUID(UUID);
        this.setDatasetName(dataProductName);
        this.setDatasetDescription(dataProductDescription);
    }
}
