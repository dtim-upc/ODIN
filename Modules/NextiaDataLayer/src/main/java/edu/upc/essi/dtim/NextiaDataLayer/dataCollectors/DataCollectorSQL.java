package edu.upc.essi.dtim.NextiaDataLayer.dataCollectors;

import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;

public class DataCollectorSQL extends DataCollector {
    public DataCollectorSQL(String dataStorePath) {
        super(dataStorePath);
    }

    // We do not need to do anything as we directly put the data in the landing zone when doing generateBootstrappedDF
    // inside uploadToTemporalLandingZone
    @Override
    public void uploadDataToTemporalFolder(Dataset d) {

    }
}
