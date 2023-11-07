package edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaDataLayer;

import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.NextiaDataLayer.materialized.DataLayerMaterialized;
import edu.upc.essi.dtim.NextiaDataLayer.utils.DataLayerFactory;
import edu.upc.essi.dtim.NextiaDataLayer.utils.DataLoading;
import edu.upc.essi.dtim.odin.config.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.sql.SQLException;

public class DataLayerImpl implements DataLayerInterace{
    private final String dataLayerPath;
    private final String technology;

    public DataLayerImpl(@Autowired AppConfig appConfig) {
        this.dataLayerPath = appConfig.getDataLayerPath();
        this.technology = appConfig.getDataLayerTechnology();
    }

    @Override
    public void uploadToDataLayer(Dataset dataset) {
        DataLoading dl = DataLoadingSingleton.getInstance(dataLayerPath);

        dl.uploadToLandingZone(dataset);

        DataLayerMaterialized dlm = null;
        try {
            dlm = DataLayerFactory.getInstance(technology,dataLayerPath);
            dlm.uploadToFormattedZone(dataset, dataset.getDataLayerPath());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
