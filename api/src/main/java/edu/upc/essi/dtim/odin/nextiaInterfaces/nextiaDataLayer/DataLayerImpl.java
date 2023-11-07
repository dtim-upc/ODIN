package edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaDataLayer;

import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.NextiaDataLayer.materialized.DLMDuckDB;
import edu.upc.essi.dtim.NextiaDataLayer.materialized.DataLayerMaterialized;
import edu.upc.essi.dtim.NextiaDataLayer.utils.DataLoading;

import java.io.IOException;
import java.sql.SQLException;

public class DataLayerImpl implements DataLayerInterace{

    @Override
    public void uploadToDataLayer(Dataset dataset) {
        String dataLayerPath = "C:\\temp";
        DataLoading dl = new DataLoading(dataLayerPath);

        dl.uploadToLandingZone(dataset);
        dl.close();

        DataLayerMaterialized dlm = null;
        try {
            dlm = new DLMDuckDB(dataLayerPath);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            dlm.uploadToFormattedZone(dataset, dataset.getDatasetName());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
