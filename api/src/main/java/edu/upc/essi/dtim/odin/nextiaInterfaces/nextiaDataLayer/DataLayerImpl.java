package edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaDataLayer;

import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.NextiaDataLayer.materialized.DLMDuckDB;
import edu.upc.essi.dtim.NextiaDataLayer.materialized.DataLayerMaterialized;
import edu.upc.essi.dtim.NextiaDataLayer.utils.DataLoading;
import edu.upc.essi.dtim.odin.config.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.sql.SQLException;

public class DataLayerImpl implements DataLayerInterace{
    private final String dataLayerPath;
    public DataLayerImpl(@Autowired AppConfig appConfig) {
        this.dataLayerPath = appConfig.getDataLayerPath();
    }

    @Override
    public void uploadToDataLayer(Dataset dataset) {
        DataLoading dl = DataLoadingSingleton.getInstance(dataLayerPath);

        dl.uploadToLandingZone(dataset);

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
            dlm.uploadToFormattedZone(dataset, dataset.getDataLayerPath());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
