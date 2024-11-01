package edu.upc.essi.dtim.NextiaDataLayer.dataCollectors;

import edu.upc.essi.dtim.NextiaCore.datasets.Dataset;

public abstract class DataCollector {
    String dataStorePath;
    public DataCollector(String dataStorePath) {
        this.dataStorePath = dataStorePath;
    }

    public abstract void uploadDataToTemporalFolder(Dataset d);

}
